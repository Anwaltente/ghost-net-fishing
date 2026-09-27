package de.sheasepherd.ghostnet.service;

import de.sheasepherd.ghostnet.model.Geisternetz;
import de.sheasepherd.ghostnet.model.NetzStatus;
import de.sheasepherd.ghostnet.model.Person;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/**
 * Geschäftslogik für Geisternetze: Melden, Bergung übernehmen, als geborgen
 * oder verschollen melden sowie Abfragen für die Übersichten.
 * <p>
 * Alle ändernden Methoden laufen in einer JTA Transaktion ({@code @Transactional}).
 * Das betroffene Netz wird dabei mit einer pessimistischen Schreibsperre
 * (SELECT ... FOR UPDATE) geladen. So kann ein Netz auch bei gleichzeitigen
 * Anfragen nie zwei bergenden Personen zugeordnet werden.
 */
@ApplicationScoped
public class GeisternetzService {

    @PersistenceContext(unitName = "ghostnetPU")
    private EntityManager em;

    @Inject
    private PersonService personService;

    // ------------------------------------------------------------------
    // User Story 1: Geisternetz (anonym) erfassen
    // ------------------------------------------------------------------

    /**
     * Speichert ein neu gemeldetes Geisternetz.
     *
     * @param netz    das zu meldende Netz (Standort, Größe, optional Bemerkung)
     * @param melder  meldende Person mit Name und Telefonnummer oder {@code null} bei anonymer Meldung
     */
    @Transactional
    public Geisternetz melden(Geisternetz netz, Person melder) {
        if (netz.getId() != null) {
            throw new GeschaeftsregelException("Dieses Netz wurde bereits gemeldet.");
        }
        LocalDateTime jetzt = LocalDateTime.now();
        netz.setStatus(NetzStatus.GEMELDET);
        netz.setGemeldetAm(jetzt);
        netz.setStatusGeaendertAm(jetzt);
        netz.setBergendePerson(null);
        netz.setVerschollenGemeldetVon(null);

        if (melder != null) {
            netz.setMeldendePerson(personService.findeOderErstelle(melder.getName(), melder.getTelefonnummer()));
        } else {
            netz.setMeldendePerson(null);
        }
        em.persist(netz);
        return netz;
    }

    // ------------------------------------------------------------------
    // User Story 2: Für die Bergung eintragen
    // ------------------------------------------------------------------

    @Transactional
    public Geisternetz uebernehmeBergung(Long netzId, Long personId) {
        Geisternetz netz = ladeMitSperre(netzId);
        Person person = ladePerson(personId);

        if (netz.getStatus() != NetzStatus.GEMELDET || netz.getBergendePerson() != null) {
            if (person.equals(netz.getBergendePerson())) {
                throw new GeschaeftsregelException(
                        "Sie haben die Bergung von Netz Nr. " + netzId + " bereits übernommen.");
            }
            throw new GeschaeftsregelException("Netz Nr. " + netzId + " kann nicht übernommen werden, "
                    + "da es den Status \"" + netz.getStatus().getBezeichnung() + "\" hat.");
        }
        netz.setBergendePerson(person);
        aendereStatus(netz, NetzStatus.BERGUNG_BEVORSTEHEND);
        return netz;
    }

    // ------------------------------------------------------------------
    // User Story 4: Als geborgen melden
    // ------------------------------------------------------------------

    @Transactional
    public Geisternetz meldeGeborgen(Long netzId, Long personId) {
        Geisternetz netz = ladeMitSperre(netzId);
        Person person = ladePerson(personId);

        if (netz.getStatus() != NetzStatus.BERGUNG_BEVORSTEHEND) {
            throw new GeschaeftsregelException("Netz Nr. " + netzId + " kann nur als geborgen gemeldet werden, "
                    + "wenn die Bergung angekündigt wurde.");
        }
        if (!person.equals(netz.getBergendePerson())) {
            throw new GeschaeftsregelException("Nur die eingetragene bergende Person kann Netz Nr. "
                    + netzId + " als geborgen melden.");
        }
        aendereStatus(netz, NetzStatus.GEBORGEN);
        return netz;
    }

    // ------------------------------------------------------------------
    // User Story 7: Als verschollen melden (nicht anonym)
    // ------------------------------------------------------------------

    @Transactional
    public Geisternetz meldeVerschollen(Long netzId, Long personId) {
        if (personId == null) {
            throw new GeschaeftsregelException("Netze können nicht anonym als verschollen gemeldet werden.");
        }
        Geisternetz netz = ladeMitSperre(netzId);
        Person person = ladePerson(personId);

        if (!netz.getStatus().isOffen()) {
            throw new GeschaeftsregelException("Netz Nr. " + netzId + " hat bereits den Status \""
                    + netz.getStatus().getBezeichnung() + "\".");
        }
        netz.setVerschollenGemeldetVon(person);
        aendereStatus(netz, NetzStatus.VERSCHOLLEN);
        return netz;
    }

    // ------------------------------------------------------------------
    // Abfragen (User Story 3 und Übersichten)
    // ------------------------------------------------------------------

    /** Alle Netze, die noch zu bergen sind (Status Gemeldet oder Bergung bevorstehend). */
    public List<Geisternetz> findeNochZuBergende() {
        return findeNachStatus(EnumSet.of(NetzStatus.GEMELDET, NetzStatus.BERGUNG_BEVORSTEHEND));
    }

    public List<Geisternetz> findeNachStatus(Collection<NetzStatus> status) {
        return em.createNamedQuery(Geisternetz.FINDE_NACH_STATUS, Geisternetz.class)
                .setParameter("status", status)
                .getResultList();
    }

    /** Alle Netze, deren Bergung die angegebene Person übernommen hat. */
    public List<Geisternetz> findeBergungenVon(Long personId) {
        Person person = ladePerson(personId);
        return em.createNamedQuery(Geisternetz.FINDE_NACH_BERGENDER_PERSON, Geisternetz.class)
                .setParameter("person", person)
                .getResultList();
    }

    public Map<NetzStatus, Long> zaehleNachStatus() {
        Map<NetzStatus, Long> ergebnis = new EnumMap<>(NetzStatus.class);
        for (NetzStatus status : NetzStatus.values()) {
            ergebnis.put(status, 0L);
        }
        List<Object[]> zeilen = em.createNamedQuery(Geisternetz.ZAEHLE_NACH_STATUS, Object[].class)
                .getResultList();
        for (Object[] zeile : zeilen) {
            ergebnis.put((NetzStatus) zeile[0], (Long) zeile[1]);
        }
        return ergebnis;
    }

    public long anzahl() {
        return em.createQuery("SELECT COUNT(n) FROM Geisternetz n", Long.class).getSingleResult();
    }

    // ------------------------------------------------------------------
    // Hilfsmethoden
    // ------------------------------------------------------------------

    private Geisternetz ladeMitSperre(Long netzId) {
        Geisternetz netz = netzId == null ? null
                : em.find(Geisternetz.class, netzId, LockModeType.PESSIMISTIC_WRITE);
        if (netz == null) {
            throw new GeschaeftsregelException("Das Netz Nr. " + netzId + " existiert nicht.");
        }
        return netz;
    }

    private Person ladePerson(Long personId) {
        Person person = personService.finde(personId);
        if (person == null) {
            throw new GeschaeftsregelException("Bitte melden Sie sich zuerst mit Name und Telefonnummer an.");
        }
        return person;
    }

    private void aendereStatus(Geisternetz netz, NetzStatus neuerStatus) {
        netz.setStatus(neuerStatus);
        netz.setStatusGeaendertAm(LocalDateTime.now());
    }
}
