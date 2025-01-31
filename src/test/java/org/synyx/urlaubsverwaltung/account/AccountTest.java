package org.synyx.urlaubsverwaltung.account;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.LocalDate;
import java.util.List;

import static java.math.BigDecimal.TEN;
import static java.time.Month.APRIL;
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.synyx.urlaubsverwaltung.person.MailNotification.NOTIFICATION_USER;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

class AccountTest {

    @Test
    void toStringTest() {
        final Person person = new Person("Theo", "Theo", "Theo", "Theo");
        person.setId(10);
        person.setPassword("Theo");
        person.setPermissions(List.of(USER));
        person.setNotifications(List.of(NOTIFICATION_USER));

        final LocalDate validFrom = LocalDate.of(2014, JANUARY, 1);
        final LocalDate validTo = LocalDate.of(2014, DECEMBER, 31);
        final LocalDate expiryDate = LocalDate.of(2014, APRIL, 1);
        final Account account = new Account(person, validFrom, validTo, expiryDate, TEN, TEN, TEN, "Comment");

        final String accountToString = account.toString();
        assertThat(accountToString).isEqualTo("Account{person=Person{id='10'}, validFrom=2014-01-01, validTo=2014-12-31, " +
            "expiryDate=2014-04-01, annualVacationDays=10, actualVacationDays=null, " +
            "remainingVacationDays=10, remainingVacationDaysNotExpiring=10}");
    }

    @Test
    void equals() {
        final Account accountOne = new Account();
        accountOne.setId(1);

        final Account accountOneOne = new Account();
        accountOneOne.setId(1);

        final Account accountTwo = new Account();
        accountTwo.setId(2);

        assertThat(accountOne)
            .isEqualTo(accountOne)
            .isEqualTo(accountOneOne)
            .isNotEqualTo(accountTwo)
            .isNotEqualTo(new Object())
            .isNotEqualTo(null);
    }

    @Test
    void hashCodeTest() {
        final Account accountOne = new Account();
        accountOne.setId(1);

        assertThat(accountOne.hashCode()).isEqualTo(32);
    }
}
