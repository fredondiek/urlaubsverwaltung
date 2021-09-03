package org.synyx.urlaubsverwaltung.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.application.dao.ApplicationRepository;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.WAITING;


/**
 * Unit test for {@link ApplicationServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class ApplicationServiceImplTest {

    private ApplicationServiceImpl sut;

    @Mock
    private ApplicationRepository applicationRepository;

    @BeforeEach
    void setUp() {
        sut = new ApplicationServiceImpl(applicationRepository);
    }

    // Get application by ID -------------------------------------------------------------------------------------------
    @Test
    void ensureGetApplicationByIdCallsCorrectDaoMethod() {
        sut.getApplicationById(1234);
        verify(applicationRepository).findById(1234);
    }

    @Test
    void ensureGetApplicationByIdReturnsAbsentOptionalIfNoOneExists() {
        final Optional<Application> optional = sut.getApplicationById(1234);
        assertThat(optional).isEmpty();
    }

    // Save application ------------------------------------------------------------------------------------------------
    @Test
    void ensureSaveCallsCorrectDaoMethod() {

        final Application application = new Application();
        sut.save(application);
        verify(applicationRepository).save(application);
    }

    // Get total overtime reduction ------------------------------------------------------------------------------------
    @Test
    void ensureReturnsZeroIfPersonHasNoApplicationsForLeaveYet() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        when(applicationRepository.calculateTotalOvertimeReductionOfPerson(person)).thenReturn(null);

        final Duration totalHours = sut.getTotalOvertimeReductionOfPerson(person);

        verify(applicationRepository).calculateTotalOvertimeReductionOfPerson(person);

        assertThat(totalHours).isEqualTo(Duration.ZERO);
    }

    @Test
    void getForStates() {

        final Application application = new Application();
        final List<Application> applications = List.of(application);

        when(applicationRepository.findByStatusInAndEndDateGreaterThanEqual(List.of(WAITING), LocalDate.of(2020, 10, 3))).thenReturn(applications);

        final List<Application> result = sut.getForStatesSince(List.of(WAITING), LocalDate.of(2020, 10, 3));
        assertThat(result).isEqualTo(applications);
    }

    @Test
    void getForStatesAndPerson() {

        final Application application = new Application();
        final List<Application> applications = List.of(application);

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        when(applicationRepository.findByStatusInAndPersonIn(List.of(WAITING), List.of(person))).thenReturn(applications);

        final List<Application> result = sut.getForStatesAndPerson(List.of(WAITING), List.of(person));
        assertThat(result).isEqualTo(applications);
    }


    @Test
    void ensureReturnsCorrectTotalOvertimeReductionForPerson() {

        final Person person = new Person("muster", "Muster", "Marlene", "muster@example.org");

        when(applicationRepository.calculateTotalOvertimeReductionOfPerson(person)).thenReturn(BigDecimal.ONE);

        final Duration totalHours = sut.getTotalOvertimeReductionOfPerson(person);

        verify(applicationRepository).calculateTotalOvertimeReductionOfPerson(person);

        assertThat(totalHours).isEqualTo(Duration.ofHours(1));
    }

    @Test
    void getForHolidayReplacement() {

        final Person holidayReplacement = new Person();
        final LocalDate localDate = LocalDate.of(2020, 10, 1);

        final Application application = new Application();
        final List<ApplicationStatus> statuses = List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED);

        when(applicationRepository.findByHolidayReplacements_PersonAndEndDateIsGreaterThanEqualAndStatusIn(holidayReplacement, localDate, statuses))
            .thenReturn(List.of(application));

        final List<Application> holidayReplacementApplications = sut.getForHolidayReplacement(holidayReplacement, localDate);
        assertThat(holidayReplacementApplications).hasSize(1).contains(application);
    }

    @Test
    void getApplicationsWithStartDateAndState() {
        final LocalDate startDate = LocalDate.of(2020, 10, 1);

        final Application application = new Application();
        final List<ApplicationStatus> statuses = List.of(TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED);
        when(applicationRepository.findByStatusInAndStartDate(statuses, startDate)).thenReturn(List.of(application));

        final List<Application> holidayReplacementApplications = sut.getApplicationsWithStartDateAndState(startDate, statuses);
        assertThat(holidayReplacementApplications).hasSize(1).contains(application);
    }

    @Test
    void getApplicationsWithStartDateAndStateAndHolidayReplacementIsNotEmpty() {
        final LocalDate startDate = LocalDate.of(2020, 10, 1);

        final Application application = new Application();
        final List<ApplicationStatus> statuses = List.of(TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED);
        when(applicationRepository.findByStatusInAndStartDateAndHolidayReplacementsIsNotEmpty(statuses, startDate)).thenReturn(List.of(application));

        final List<Application> holidayReplacementApplications = sut.getApplicationsWithStartDateAndStateAndHolidayReplacementIsNotEmpty(startDate, statuses);
        assertThat(holidayReplacementApplications).hasSize(1).contains(application);
    }
}
