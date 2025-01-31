package org.synyx.urlaubsverwaltung.person;

import java.util.Objects;

public class PersonId {

    private final Integer value;

    public PersonId(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "PersonId{" +
            "value=" + value +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersonId personId = (PersonId) o;
        return Objects.equals(value, personId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
