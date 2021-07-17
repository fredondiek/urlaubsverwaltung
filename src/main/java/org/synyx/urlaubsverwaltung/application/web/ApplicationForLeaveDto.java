package org.synyx.urlaubsverwaltung.application.web;

import org.synyx.urlaubsverwaltung.period.DayLength;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

public class ApplicationForLeaveDto {

    private final int id;
    private final Person person;
    private final VacationType vacationType;
    private final String duration;
    private final DayLength dayLength;
    private final BigDecimal workDays;
    private final LocalDate startDate;
    private final ZonedDateTime startDateWithTime;
    private final LocalDate endDate;
    private final ZonedDateTime endDateWithTime;
    private final boolean statusWaiting;
    private final boolean editAllowed;
    private final boolean approveAllowed;
    private final boolean rejectAllowed;

    @SuppressWarnings("java:S107") // "Methods should not have too many parameters" - Builder is used for construction
    private ApplicationForLeaveDto(int id, Person person, VacationType vacationType, String duration,
                                   DayLength dayLength, BigDecimal workDays, LocalDate startDate,
                                   ZonedDateTime startDateWithTime, LocalDate endDate, ZonedDateTime endDateWithTime,
                                   boolean statusWaiting, boolean editAllowed, boolean approveAllowed,
                                   boolean rejectAllowed) {
        this.id = id;
        this.person = person;
        this.vacationType = vacationType;
        this.duration = duration;
        this.dayLength = dayLength;
        this.workDays = workDays;
        this.startDate = startDate;
        this.startDateWithTime = startDateWithTime;
        this.endDate = endDate;
        this.endDateWithTime = endDateWithTime;
        this.statusWaiting = statusWaiting;
        this.editAllowed = editAllowed;
        this.approveAllowed = approveAllowed;
        this.rejectAllowed = rejectAllowed;
    }

    public int getId() {
        return id;
    }

    public Person getPerson() {
        return person;
    }

    public VacationType getVacationType() {
        return vacationType;
    }

    public String getDuration() {
        return duration;
    }

    public DayLength getDayLength() {
        return dayLength;
    }

    public BigDecimal getWorkDays() {
        return workDays;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public ZonedDateTime getStartDateWithTime() {
        return startDateWithTime;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public ZonedDateTime getEndDateWithTime() {
        return endDateWithTime;
    }

    public boolean isStatusWaiting() {
        return statusWaiting;
    }

    public boolean isEditAllowed() {
        return editAllowed;
    }

    public boolean isApproveAllowed() {
        return approveAllowed;
    }

    public boolean isRejectAllowed() {
        return rejectAllowed;
    }

    static Builder builder() {
        return new Builder();
    }

    static class Builder {
        private int id;
        private Person person;
        private VacationType vacationType;
        private String duration;
        private DayLength dayLength;
        private BigDecimal workDays;
        private LocalDate startDate;
        private ZonedDateTime startDateWithTime;
        private LocalDate endDate;
        private ZonedDateTime endDateWithTime;
        private boolean statusWaiting;
        private boolean editAllowed;
        private boolean approveAllowed;
        private boolean rejectAllowed;

        Builder id(int id) {
            this.id = id;
            return this;
        }

        Builder person(Person person) {
            this.person = person;
            return this;
        }

        Builder vacationType(VacationType vacationType) {
            this.vacationType = vacationType;
            return this;
        }

        Builder duration(String duration) {
            this.duration = duration;
            return this;
        }

        Builder dayLength(DayLength dayLength) {
            this.dayLength = dayLength;
            return this;
        }

        Builder workDays(BigDecimal workDays) {
            this.workDays = workDays;
            return this;
        }

        Builder startDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        Builder startDateWithTime(ZonedDateTime startDateWithTime) {
            this.startDateWithTime = startDateWithTime;
            return this;
        }

        Builder endDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        Builder endDateWithTime(ZonedDateTime endDateWithTime) {
            this.endDateWithTime = endDateWithTime;
            return this;
        }

        Builder statusWaiting(boolean statusWaiting) {
            this.statusWaiting = statusWaiting;
            return this;
        }

        Builder editAllowed(boolean editAllowed) {
            this.editAllowed = editAllowed;
            return this;
        }

        Builder approveAllowed(boolean approveAllowed) {
            this.approveAllowed = approveAllowed;
            return this;
        }

        Builder rejectAllowed(boolean rejectAllowed) {
            this.rejectAllowed = rejectAllowed;
            return this;
        }

        ApplicationForLeaveDto build() {
            return new ApplicationForLeaveDto(
                id,
                person,
                vacationType,
                duration,
                dayLength,
                workDays,
                startDate,
                startDateWithTime,
                endDate,
                endDateWithTime,
                statusWaiting,
                editAllowed,
                approveAllowed,
                rejectAllowed
            );
        }
    }

    public static class Person {
        private final String name;
        private final String avatarUrl;

        public Person(String name, String avatarUrl) {
            this.name = name;
            this.avatarUrl = avatarUrl;
        }

        public String getName() {
            return name;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }
    }

    public static class VacationType {
        private final String category;
        private final String messageKey;

        public VacationType(String category, String messageKey) {
            this.category = category;
            this.messageKey = messageKey;
        }

        public String getCategory() {
            return category;
        }

        public String getMessageKey() {
            return messageKey;
        }
    }
}