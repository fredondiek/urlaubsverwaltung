package org.synyx.urlaubsverwaltung.person;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.account.AccountInteractionService;
import org.synyx.urlaubsverwaltung.search.PageableSearchQuery;
import org.synyx.urlaubsverwaltung.workingtime.WorkingTimeWriteService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;

/**
 * Implementation for {@link PersonService}.
 */
@Service("personService")
class PersonServiceImpl implements PersonService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final PersonRepository personRepository;
    private final AccountInteractionService accountInteractionService;
    private final WorkingTimeWriteService workingTimeWriteService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    PersonServiceImpl(PersonRepository personRepository, AccountInteractionService accountInteractionService,
                      WorkingTimeWriteService workingTimeWriteService, ApplicationEventPublisher applicationEventPublisher) {

        this.personRepository = personRepository;
        this.accountInteractionService = accountInteractionService;
        this.workingTimeWriteService = workingTimeWriteService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public Person create(String username, String lastName, String firstName, String email,
                         List<MailNotification> notifications, List<Role> permissions) {

        final Person person = new Person(username, lastName, firstName, email);
        person.setNotifications(notifications);
        person.setPermissions(permissions);

        return create(person);
    }

    @Override
    public Person create(Person person) {

        final Person createdPerson = personRepository.save(person);
        LOG.info("Created person: {}", person);

        accountInteractionService.createDefaultAccount(person);
        workingTimeWriteService.createDefaultWorkingTime(person);

        applicationEventPublisher.publishEvent(toPersonCreatedEvent(createdPerson));

        return createdPerson;
    }

    @Override
    public Person update(Integer id, String username, String lastName, String firstName, String email,
                         List<MailNotification> notifications, List<Role> permissions) {

        final Person person = getPersonByID(id)
            .orElseThrow(() -> new IllegalArgumentException("Can not find a person for ID = " + id));

        person.setUsername(username);
        person.setLastName(lastName);
        person.setFirstName(firstName);
        person.setEmail(email);
        person.setNotifications(notifications);
        person.setPermissions(permissions);

        return update(person);
    }

    @Override
    public Person update(Person person) {

        if (person.getId() == null) {
            throw new IllegalArgumentException("Can not update a person that is not persisted yet");
        }

        final Person updatedPerson = personRepository.save(person);
        LOG.info("Updated person: {}", updatedPerson);

        if (updatedPerson.isInactive()) {
            applicationEventPublisher.publishEvent(toPersonDisabledEvent(updatedPerson));
        }

        applicationEventPublisher.publishEvent(toPersonUpdateEvent(updatedPerson));

        return updatedPerson;
    }

    @Override
    public Optional<Person> getPersonByID(Integer id) {
        return personRepository.findById(id);
    }

    @Override
    public Optional<Person> getPersonByUsername(String username) {
        return personRepository.findByUsername(username);
    }

    @Override
    public Optional<Person> getPersonByMailAddress(String mailAddress) {
        return personRepository.findByEmail(mailAddress);
    }

    @Override
    public List<Person> getActivePersons() {
        return personRepository.findByPermissionsNotContainingOrderByFirstNameAscLastNameAsc(INACTIVE);
    }

    @Override
    public Page<Person> getActivePersons(PageableSearchQuery personPageableSearchQuery) {
        final Pageable pageable = personPageableSearchQuery.getPageable();
        final Sort implicitSort = mapToImplicitPersonSort(pageable.getSort());
        final String query = personPageableSearchQuery.getQuery();
        final PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), implicitSort);
        return personRepository.findByPermissionsNotContainingAndByNiceNameContainingIgnoreCase(INACTIVE, query, pageRequest);
    }

    @Override
    public List<Person> getActivePersonsByRole(final Role role) {
        return personRepository.findByPermissionsContainingAndPermissionsNotContainingOrderByFirstNameAscLastNameAsc(role, INACTIVE);
    }

    @Override
    public List<Person> getActivePersonsWithNotificationType(final MailNotification notification) {
        return personRepository.findByPermissionsNotContainingAndNotificationsContainingOrderByFirstNameAscLastNameAsc(INACTIVE, notification);
    }

    @Override
    public List<Person> getInactivePersons() {
        return personRepository.findByPermissionsContainingOrderByFirstNameAscLastNameAsc(INACTIVE);
    }

    @Override
    public Page<Person> getInactivePersons(PageableSearchQuery personPageableSearchQuery) {
        final Pageable pageable = personPageableSearchQuery.getPageable();
        final Sort implicitSort = mapToImplicitPersonSort(pageable.getSort());
        final PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), implicitSort);
        return personRepository.findByPermissionsContainingAndNiceNameContainingIgnoreCase(INACTIVE, personPageableSearchQuery.getQuery(), pageRequest);
    }

    @Override
    public Person getSignedInUser() {

        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("No authentication found in context.");
        }

        final String username = authentication.getName();
        final Optional<Person> person = getPersonByUsername(username);
        if (person.isEmpty()) {
            throw new IllegalStateException("Can not get the person for the signed in user with username = " + username);
        }

        return person.get();
    }

    /**
     * Adds {@link Role#OFFICE} to the roles of the given person if no
     * other active user with a office role is defined.
     *
     * @param person that maybe gets the role {@link Role#OFFICE}
     * @return saved {@link Person} with {@link Role#OFFICE} rights
     * if no other active person with {@link Role#OFFICE} is available.
     */
    @Override
    public Person appointAsOfficeUserIfNoOfficeUserPresent(Person person) {

        boolean activeOfficeUserAvailable = !getActivePersonsByRole(OFFICE).isEmpty();
        if (activeOfficeUserAvailable) {
            return person;
        }

        final List<Role> permissions = new ArrayList<>(person.getPermissions());
        permissions.add(OFFICE);
        person.setPermissions(permissions);

        final Person savedPerson = personRepository.save(person);

        LOG.info("Add 'OFFICE' role to person: {}", person);

        return savedPerson;
    }

    @Override
    public int numberOfActivePersons() {
        return personRepository.countByPermissionsNotContaining(INACTIVE);
    }

    private static Sort mapToImplicitPersonSort(Sort requestedSort) {
        final Sort.Order firstNameOrder = requestedSort.getOrderFor("firstName");
        final Sort.Order lastNameOrder = requestedSort.getOrderFor("lastName");

        // e.g. if content should be sorted by firstName, use lastName as second sort criteria
        final Sort implicitSort;

        if (firstNameOrder != null) {
            implicitSort = requestedSort.and(Sort.by(firstNameOrder.getDirection(), "lastName"));
        } else if (lastNameOrder != null) {
            implicitSort = requestedSort.and(Sort.by(lastNameOrder.getDirection(), "firstName"));
        } else {
            implicitSort = requestedSort;
        }

        return implicitSort;
    }

    private PersonCreatedEvent toPersonCreatedEvent(Person person) {
        return new PersonCreatedEvent(this, person.getId(), person.getNiceName(), person.getUsername(), person.getEmail(), person.isActive());
    }

    private PersonUpdatedEvent toPersonUpdateEvent(Person person) {
        return new PersonUpdatedEvent(this, person.getId(), person.getNiceName(), person.getUsername(), person.getEmail(), person.isActive());
    }

    private PersonDisabledEvent toPersonDisabledEvent(Person person) {
        return new PersonDisabledEvent(this, person.getId(), person.getNiceName(), person.getUsername(), person.getEmail());
    }
}
