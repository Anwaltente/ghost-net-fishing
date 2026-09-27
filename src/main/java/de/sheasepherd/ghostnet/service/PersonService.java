package de.sheasepherd.ghostnet.service;

import de.sheasepherd.ghostnet.model.Person;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Datenzugriff und Geschäftslogik rund um Personen.
 * Personen werden über ihre (normalisierte) Telefonnummer wiedererkannt.
 */
@ApplicationScoped
public class PersonService {

    @PersistenceContext(unitName = "ghostnetPU")
    private EntityManager em;

    /**
     * Liefert die Person mit der angegebenen Telefonnummer oder legt sie neu an.
     * Hat sich der Name geändert, wird er aktualisiert.
     */
    @Transactional
    public Person findeOderErstelle(String name, String telefonnummer) {
        String telefon = Person.normalisiereTelefon(telefonnummer);
        String bereinigterName = name == null ? null : name.trim();

        Optional<Person> vorhanden = findeNachTelefon(telefon);
        if (vorhanden.isPresent()) {
            Person person = vorhanden.get();
            if (bereinigterName != null && !bereinigterName.isEmpty()
                    && !bereinigterName.equals(person.getName())) {
                person.setName(bereinigterName);
            }
            return person;
        }

        Person neu = new Person(bereinigterName, telefon);
        neu.setErfasstAm(LocalDateTime.now());
        em.persist(neu);
        return neu;
    }

    public Optional<Person> findeNachTelefon(String telefonnummer) {
        return em.createNamedQuery(Person.FINDE_NACH_TELEFON, Person.class)
                .setParameter("telefon", Person.normalisiereTelefon(telefonnummer))
                .getResultStream()
                .findFirst();
    }

    public Person finde(Long id) {
        return id == null ? null : em.find(Person.class, id);
    }
}
