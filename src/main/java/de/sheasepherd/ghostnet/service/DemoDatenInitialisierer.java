package de.sheasepherd.ghostnet.service;

import de.sheasepherd.ghostnet.model.Geisternetz;
import de.sheasepherd.ghostnet.model.NetzStatus;
import de.sheasepherd.ghostnet.model.Person;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.ServletContext;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.logging.Logger;

/**
 * Legt beim ersten Start einige Beispieldaten an, damit der Prototyp direkt
 * vorgeführt werden kann. Es werden nur Daten angelegt, wenn die Datenbank leer ist.
 * Abschaltbar über den Kontextparameter {@code ghostnet.demodaten} in der web.xml.
 */
@ApplicationScoped
public class DemoDatenInitialisierer {

    private static final Logger LOG = Logger.getLogger(DemoDatenInitialisierer.class.getName());

    @PersistenceContext(unitName = "ghostnetPU")
    private EntityManager em;

    @Transactional
    public void beimStart(@Observes @Initialized(ApplicationScoped.class) Object kontext) {
        if (kontext instanceof ServletContext servletContext
                && "false".equalsIgnoreCase(servletContext.getInitParameter("ghostnet.demodaten"))) {
            return;
        }
        Long anzahl = em.createQuery("SELECT COUNT(n) FROM Geisternetz n", Long.class).getSingleResult();
        if (anzahl > 0) {
            return;
        }
        LocalDateTime jetzt = LocalDateTime.now().withSecond(0).withNano(0);

        Person hansen = person("Maria Hansen", "+491601234567", jetzt.minusDays(20));
        Person petersen = person("Jens Petersen", "+491712345678", jetzt.minusDays(18));
        Person okafor = person("Ada Okafor", "+4940987654", jetzt.minusDays(9));

        netz(54.1823, 7.8856, 120, "Treibt nordwestlich von Helgoland, grünes Schleppnetz.",
                hansen, null, NetzStatus.GEMELDET, jetzt.minusDays(6));
        netz(54.5512, 11.2140, 45, "Im Fehmarnbelt an einer Boje verhakt.",
                null, null, NetzStatus.GEMELDET, jetzt.minusDays(4));
        netz(54.4480, 10.2530, 30, null,
                null, petersen, NetzStatus.BERGUNG_BEVORSTEHEND, jetzt.minusDays(3));
        netz(55.0210, 2.9170, 800, "Sehr großes Netz auf der Doggerbank, Seevögel verfangen.",
                okafor, null, NetzStatus.GEMELDET, jetzt.minusDays(2));
        netz(39.5696, 2.6502, 60, "Vor der Bucht von Palma.",
                null, null, NetzStatus.GEMELDET, jetzt.minusDays(1));
        netz(53.9870, 8.5120, 25, "Elbmündung, bei Niedrigwasser sichtbar.",
                hansen, petersen, NetzStatus.GEBORGEN, jetzt.minusDays(15));

        Geisternetz verschollen = netz(54.9000, 13.4000, 70, "Nördlich von Rügen gesichtet.",
                null, null, NetzStatus.VERSCHOLLEN, jetzt.minusDays(12));
        verschollen.setVerschollenGemeldetVon(okafor);

        LOG.info("Ghost Net Fishing: Beispieldaten angelegt.");
    }

    private Person person(String name, String telefon, LocalDateTime erfasst) {
        Person person = new Person(name, telefon);
        person.setErfasstAm(erfasst);
        em.persist(person);
        return person;
    }

    private Geisternetz netz(double breite, double laenge, int groesse, String bemerkung,
                             Person melder, Person berger, NetzStatus status, LocalDateTime gemeldet) {
        Geisternetz netz = new Geisternetz();
        netz.setBreitengrad(breite);
        netz.setLaengengrad(laenge);
        netz.setGeschaetzteGroesse(groesse);
        netz.setBemerkung(bemerkung);
        netz.setMeldendePerson(melder);
        netz.setBergendePerson(berger);
        netz.setStatus(status);
        netz.setGemeldetAm(gemeldet);
        netz.setStatusGeaendertAm(status == NetzStatus.GEMELDET ? gemeldet : gemeldet.plusDays(1));
        em.persist(netz);
        return netz;
    }
}
