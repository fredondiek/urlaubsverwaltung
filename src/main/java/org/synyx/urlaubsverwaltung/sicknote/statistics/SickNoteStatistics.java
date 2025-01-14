package org.synyx.urlaubsverwaltung.sicknote.statistics;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.Assert;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import static java.math.BigDecimal.ZERO;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static org.synyx.urlaubsverwaltung.util.DateAndTimeFormat.DD_MM_YYYY;

/**
 * A statistic containing information about sick notes of a year.
 */
public class SickNoteStatistics {

    @DateTimeFormat(pattern = DD_MM_YYYY)
    private final LocalDate created;
    private final int year;
    private final int totalNumberOfSickNotes;
    private final BigDecimal totalNumberOfSickDays;
    private final Long numberOfPersonsWithMinimumOneSickNote;

    SickNoteStatistics(Clock clock, List<SickNote> sickNotes, WorkDaysCountService workDaysCountService) {

        year = Year.now(clock).getValue();
        numberOfPersonsWithMinimumOneSickNote = sickNotes.stream().map(SickNote::getPerson).distinct().count();
        created = LocalDate.now(clock);

        totalNumberOfSickNotes = sickNotes.size();
        totalNumberOfSickDays = calculateTotalNumberOfSickDays(workDaysCountService, sickNotes);
    }

    public int getTotalNumberOfSickNotes() {
        return totalNumberOfSickNotes;
    }

    public BigDecimal getTotalNumberOfSickDays() {
        return totalNumberOfSickDays;
    }

    public LocalDate getCreated() {
        return created;
    }

    public int getYear() {
        return year;
    }

    public Long getNumberOfPersonsWithMinimumOneSickNote() {
        return numberOfPersonsWithMinimumOneSickNote;
    }

    public BigDecimal getAverageDurationOfDiseasePerPerson() {
        final Long numberOfPersons = numberOfPersonsWithMinimumOneSickNote;
        if (numberOfPersons == 0) {
            return ZERO;
        }

        double averageDuration = totalNumberOfSickDays.doubleValue() / numberOfPersons;
        return BigDecimal.valueOf(averageDuration);
    }

    private BigDecimal calculateTotalNumberOfSickDays(WorkDaysCountService workDaysCountService, List<SickNote> sickNotes) {

        BigDecimal numberOfSickDays = ZERO;
        for (final SickNote sickNote : sickNotes) {
            final LocalDate sickNoteStartDate = sickNote.getStartDate();
            final LocalDate sickNoteEndDate = sickNote.getEndDate();

            Assert.isTrue(sickNoteStartDate.getYear() == year || sickNoteEndDate.getYear() == year,
                "Start date OR end date of the sick note must be in the year " + year);

            final LocalDate startDate = sickNoteStartDate.getYear() == year ? sickNoteStartDate : sickNoteEndDate.with(firstDayOfYear());
            final LocalDate endDate = sickNoteEndDate.getYear() == year ? sickNoteEndDate : sickNoteStartDate.with(lastDayOfYear());

            final BigDecimal workDays = workDaysCountService.getWorkDaysCount(sickNote.getDayLength(), startDate, endDate, sickNote.getPerson());
            numberOfSickDays = numberOfSickDays.add(workDays);
        }

        return numberOfSickDays;
    }

    @Override
    public String toString() {
        return "SickNoteStatistics{" +
            "created=" + created +
            ", year=" + year +
            ", totalNumberOfSickNotes=" + totalNumberOfSickNotes +
            ", totalNumberOfSickDays=" + totalNumberOfSickDays +
            ", numberOfPersonsWithMinimumOneSickNote=" + numberOfPersonsWithMinimumOneSickNote +
            '}';
    }
}
