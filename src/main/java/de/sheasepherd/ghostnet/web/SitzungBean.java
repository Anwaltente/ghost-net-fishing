package de.sheasepherd.ghostnet.web;

import de.sheasepherd.ghostnet.model.Person;
import de.sheasepherd.ghostnet.service.PersonService;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.Set;

/**
 * Hält die in der aktuellen Sitzung angemeldete Person.
 * <p>
 * Für den Prototyp genügt eine einfache Identifikation über Name und Telefonnummer
 * (kein Passwort). Die Telefonnummer ist laut Fachkonzept ohnehin für Rückfragen nötig.
 * Eine Anmeldung ist erforderlich für: Bergung übernehmen, als geborgen melden
 * und als verschollen melden (nicht anonym möglich).
 */
@Named("sitzung")
@SessionScoped
public class SitzungBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Erlaubte Zielseiten nach der Anmeldung (Schutz vor offenen Weiterleitungen). */
    private static final Set<String> ERLAUBTE_ZIELE = Set.of("index", "netze", "melden", "meine-bergungen");

    @Inject
    private PersonService personService;

    private Person eingabe = new Person();
    private Person angemeldetePerson;
    private String ziel;

    public String anmelden() {
        angemeldetePerson = personService.findeOderErstelle(eingabe.getName(), eingabe.getTelefonnummer());
        eingabe = new Person();
        Nachrichten.info("Hallo " + angemeldetePerson.getName() + ", Sie sind jetzt angemeldet.");
        Nachrichten.ueberRedirectBehalten();
        String zielSeite = ziel != null && ERLAUBTE_ZIELE.contains(ziel) ? ziel : "netze";
        ziel = null;
        return zielSeite + "?faces-redirect=true";
    }

    public String abmelden() {
        angemeldetePerson = null;
        Nachrichten.info("Sie wurden abgemeldet.");
        Nachrichten.ueberRedirectBehalten();
        return "index?faces-redirect=true";
    }

    public boolean isAngemeldet() {
        return angemeldetePerson != null;
    }

    public Long getPersonId() {
        return angemeldetePerson == null ? null : angemeldetePerson.getId();
    }

    public Person getAngemeldetePerson() {
        return angemeldetePerson;
    }

    public Person getEingabe() {
        return eingabe;
    }

    public String getZiel() {
        return ziel;
    }

    public void setZiel(String ziel) {
        this.ziel = ziel;
    }
}
