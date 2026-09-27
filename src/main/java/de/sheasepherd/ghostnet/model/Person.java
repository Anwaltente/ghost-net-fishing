package de.sheasepherd.ghostnet.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Natürliche Person mit Name und Telefonnummer für Rückfragen.
 * <p>
 * Dieselbe Person kann Netze melden, bergen oder als verschollen melden.
 * Die Rolle ergibt sich aus der jeweiligen Beziehung zum {@link Geisternetz}.
 * Anonyme Meldungen erzeugen keinen Personendatensatz.
 */
@Entity
@Table(name = "person",
        uniqueConstraints = @UniqueConstraint(name = "uk_person_telefonnummer", columnNames = "telefonnummer"))
@NamedQuery(name = Person.FINDE_NACH_TELEFON,
        query = "SELECT p FROM Person p WHERE p.telefonnummer = :telefon")
public class Person implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String FINDE_NACH_TELEFON = "Person.findeNachTelefon";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Bitte einen Namen angeben.")
    @Size(max = 100, message = "Der Name darf höchstens 100 Zeichen lang sein.")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Bitte eine Telefonnummer angeben.")
    // Leerer Wert passt zum Muster, damit bei fehlender Eingabe nur die @NotBlank Meldung erscheint
    @Pattern(regexp = "|\\+?[0-9 /()-]{6,25}",
            message = "Bitte eine gültige Telefonnummer angeben, z. B. +49 171 2345678.")
    @Column(name = "telefonnummer", nullable = false, length = 30)
    private String telefonnummer;

    @Column(name = "erfasst_am", nullable = false)
    private LocalDateTime erfasstAm;

    public Person() {
    }

    public Person(String name, String telefonnummer) {
        this.name = name;
        this.telefonnummer = telefonnummer;
    }

    /** Entfernt Leerzeichen, Schrägstriche, Klammern und Bindestriche aus einer Telefonnummer. */
    public static String normalisiereTelefon(String telefon) {
        if (telefon == null) {
            return null;
        }
        return telefon.replaceAll("[\\s/()\\-]", "");
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTelefonnummer() {
        return telefonnummer;
    }

    public void setTelefonnummer(String telefonnummer) {
        this.telefonnummer = telefonnummer;
    }

    public LocalDateTime getErfasstAm() {
        return erfasstAm;
    }

    public void setErfasstAm(LocalDateTime erfasstAm) {
        this.erfasstAm = erfasstAm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        // instanceof und getId() statt Feldzugriff, da Hibernate lazy geladene
        // Beziehungen als Proxy Unterklasse liefert, deren Felder nicht befüllt sind.
        if (!(o instanceof Person other)) {
            return false;
        }
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
