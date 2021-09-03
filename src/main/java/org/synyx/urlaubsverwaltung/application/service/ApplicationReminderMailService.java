package org.synyx.urlaubsverwaltung.application.service;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.synyx.urlaubsverwaltung.application.ApplicationSettings;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.settings.SettingsService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.WAITING;

@Component
class ApplicationReminderMailService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final ApplicationService applicationService;
    private final SettingsService settingsService;
    private final ApplicationMailService applicationMailService;
    private final Clock clock;

    @Autowired
    ApplicationReminderMailService(ApplicationService applicationService, SettingsService settingsService, ApplicationMailService applicationMailService, Clock clock) {
        this.applicationService = applicationService;
        this.settingsService = settingsService;
        this.applicationMailService = applicationMailService;
        this.clock = clock;
    }

    void sendWaitingApplicationsReminderNotification() {

        boolean isRemindForWaitingApplicationsActive =
            settingsService.getSettings().getApplicationSettings().isRemindForWaitingApplications();

        if (isRemindForWaitingApplicationsActive) {
            final List<Application> longWaitingApplications = applicationService.getForStates(List.of(WAITING)).stream()
                .filter(isLongWaitingApplications())
                .collect(toList());

            if (!longWaitingApplications.isEmpty()) {
                LOG.info("{} long waiting applications found. Sending Notification...", longWaitingApplications.size());

                applicationMailService.sendRemindForWaitingApplicationsReminderNotification(longWaitingApplications);

                for (Application longWaitingApplication : longWaitingApplications) {
                    longWaitingApplication.setRemindDate(LocalDate.now(clock));
                    applicationService.save(longWaitingApplication);
                }

                LOG.info("Sending Notification for waiting applications finished.");
            } else {
                LOG.info("No long waiting application found.");
            }
        }
    }

    void sendUpcomingApplicationsReminderNotification() {

        final ApplicationSettings applicationSettings = settingsService.getSettings().getApplicationSettings();
        if (applicationSettings.isRemindForUpcomingApplications()) {
            final LocalDate dateForUpcomingApplications = LocalDate.now(clock).plusDays(applicationSettings.getDaysBeforeRemindForUpcomingApplications());
            final List<ApplicationStatus> allowedStatuses = List.of(ALLOWED, ALLOWED_CANCELLATION_REQUESTED, TEMPORARY_ALLOWED);
            final List<Application> upcomingApplications = applicationService.getApplicationsWithStartDateAndState(dateForUpcomingApplications, allowedStatuses);

            applicationMailService.sendRemindForUpcomingApplicationsReminderNotification(upcomingApplications, applicationSettings.getDaysBeforeRemindForUpcomingApplications());
        }
    }

    void sendUpcomingHolidayReplacementReminderNotification() {

        final ApplicationSettings applicationSettings = settingsService.getSettings().getApplicationSettings();
        if (applicationSettings.isRemindForUpcomingHolidayReplacement()) {
            final LocalDate dateForUpcomingHolidayReplacement = LocalDate.now(clock).plusDays(applicationSettings.getDaysBeforeRemindForUpcomingHolidayReplacement());
            final List<ApplicationStatus> allowedStatuses = List.of(ALLOWED, ALLOWED_CANCELLATION_REQUESTED, TEMPORARY_ALLOWED);
            final List<Application> upcomingApplicationsForHolidayReplacement = applicationService.getApplicationsWithStartDateAndStateAndHolidayReplacementIsNotEmpty(dateForUpcomingHolidayReplacement, allowedStatuses);

            applicationMailService.sendRemindForUpcomingHolidayReplacement(upcomingApplicationsForHolidayReplacement, applicationSettings.getDaysBeforeRemindForUpcomingHolidayReplacement());
        }
    }

    private Predicate<Application> isLongWaitingApplications() {
        return application -> {

            LocalDate remindDate = application.getRemindDate();
            if (remindDate == null) {
                Integer daysBeforeRemindForWaitingApplications =
                    settingsService.getSettings().getApplicationSettings().getDaysBeforeRemindForWaitingApplications();

                // never reminded before
                LocalDate minDateForNotification = application.getApplicationDate()
                    .plusDays(daysBeforeRemindForWaitingApplications);

                // true -> remind!
                // false -> to early for notification
                return minDateForNotification.isBefore(LocalDate.now(clock));
            } else {
                // true -> not reminded today
                // false -> already reminded today
                return !remindDate.isEqual(LocalDate.now(clock));
            }
        };
    }
}
