package org.synyx.urlaubsverwaltung.web.application;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;

import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.synyx.urlaubsverwaltung.core.account.domain.Account;
import org.synyx.urlaubsverwaltung.core.account.service.AccountService;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationInteractionService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.person.Role;
import org.synyx.urlaubsverwaltung.security.SessionService;
import org.synyx.urlaubsverwaltung.web.ControllerConstants;
import org.synyx.urlaubsverwaltung.web.DateMidnightPropertyEditor;
import org.synyx.urlaubsverwaltung.web.DecimalNumberPropertyEditor;
import org.synyx.urlaubsverwaltung.web.PersonPropertyEditor;
import org.synyx.urlaubsverwaltung.web.person.PersonConstants;
import org.synyx.urlaubsverwaltung.web.person.UnknownPersonException;

import java.math.BigDecimal;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Controller to apply for leave.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@RequestMapping("/application")
@Controller
public class ApplyForLeaveController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private PersonService personService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private ApplicationInteractionService applicationInteractionService;

    @Autowired
    private ApplicationValidator applicationValidator;

    @InitBinder
    public void initBinder(DataBinder binder, Locale locale) {

        binder.registerCustomEditor(DateMidnight.class, new DateMidnightPropertyEditor());
        binder.registerCustomEditor(BigDecimal.class, new DecimalNumberPropertyEditor(locale));
        binder.registerCustomEditor(Person.class, new PersonPropertyEditor(personService));
    }


    /**
     * Show form to apply for leave.
     *
     * @param  personId  of the person that applies for leave
     * @param  applyingOnBehalfOfSomeOne  defines if applying for leave on behalf for somebody
     * @param  model  to be filled
     *
     * @return  form to apply for leave
     */
    @RequestMapping(value = "/new", method = RequestMethod.GET)
    public String newApplicationForm(@RequestParam(value = "personId", required = false) Integer personId,
        @RequestParam(value = "appliesOnOnesBehalf", required = false) Boolean applyingOnBehalfOfSomeOne, Model model)
        throws UnknownPersonException, AccessDeniedException {

        Person person;
        Person applier;

        Person signedInUser = sessionService.getSignedInUser();

        if (personId == null) {
            person = signedInUser;
            applier = person;
        } else {
            person = personService.getPersonByID(personId).orElseThrow(() -> new UnknownPersonException(personId));
            applier = signedInUser;
        }

        boolean isApplyingForOneSelf = person.equals(applier);

        // only office may apply for leave on behalf of other users
        if ((!isApplyingForOneSelf && !signedInUser.hasRole(Role.OFFICE))
                || (applyingOnBehalfOfSomeOne != null && !signedInUser.hasRole(Role.OFFICE))) {
            throw new AccessDeniedException("User " + signedInUser.getLoginName()
                + " has not the correct permissions to apply for leave for user " + person.getLoginName());
        }

        Optional<Account> holidaysAccount = accountService.getHolidaysAccount(DateMidnight.now().getYear(), person);

        if (holidaysAccount.isPresent()) {
            prepareApplicationForLeaveForm(person, new ApplicationForLeaveForm(), model);
        }

        model.addAttribute("noHolidaysAccount", !holidaysAccount.isPresent());
        model.addAttribute("appliesOnOnesBehalf", applyingOnBehalfOfSomeOne);

        return "application/app_form";
    }


    private void prepareApplicationForLeaveForm(Person person, ApplicationForLeaveForm appForm, Model model) {

        List<Person> persons = personService.getActivePersons()
            .stream()
            .sorted(personComparator())
            .collect(Collectors.toList());

        model.addAttribute(PersonConstants.PERSON_ATTRIBUTE, person);
        model.addAttribute(PersonConstants.PERSONS_ATTRIBUTE, persons);
        model.addAttribute("application", appForm);
        model.addAttribute("vacationTypes", VacationType.values());
    }


    private Comparator<Person> personComparator() {

        return (p1, p2) -> p1.getNiceName().toLowerCase().compareTo(p2.getNiceName().toLowerCase());
    }


    @RequestMapping(value = "/new", method = RequestMethod.POST)
    public String newApplication(@RequestParam(value = "personId", required = false) Integer personId,
        @ModelAttribute("application") ApplicationForLeaveForm appForm, RedirectAttributes redirectAttributes,
        Errors errors, Model model) throws UnknownPersonException {

        Person applier = sessionService.getSignedInUser();

        Person personToApplyForLeave;

        if (personId == null) {
            personToApplyForLeave = applier;
        } else {
            personToApplyForLeave = personService.getPersonByID(personId).orElseThrow(() ->
                        new UnknownPersonException(personId));
        }

        applicationValidator.validate(appForm, errors);

        if (errors.hasErrors()) {
            prepareApplicationForLeaveForm(personToApplyForLeave, appForm, model);

            if (errors.hasGlobalErrors()) {
                model.addAttribute(ControllerConstants.ERRORS_ATTRIBUTE, errors);
            }

            return "application/app_form";
        }

        Application application = appForm.generateApplicationForLeave();

        Application savedApplicationForLeave = applicationInteractionService.apply(application, applier,
                Optional.ofNullable(appForm.getComment()));

        redirectAttributes.addFlashAttribute("applySuccess", true);

        return "redirect:/web/application/" + savedApplicationForLeave.getId();
    }
}
