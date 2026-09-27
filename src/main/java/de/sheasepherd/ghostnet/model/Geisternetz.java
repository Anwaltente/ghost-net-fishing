package de.sheasepherd.ghostnet.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;

/**
 * Herrenloses Fischernetz, das im Meer treibt.
 * <p>
 * Eigenschaften laut Fachkonzept: Standort (GPS Koordinaten), geschätzte Größe und Status.
 * Ein Netz ist höchstens einer bergenden Person zugeordnet (ManyToOne, optional).
 */
@Entity
@Table(name = "geisternetz")
@NamedQueries({
        @NamedQuery(name = Geisternetz.FINDE_NACH_STATUS,
                query = "SELECT n FROM Geisternetz n LEFT JOIN FETCH n.bergendePerson "
                        + "WHERE n.status IN :status ORDER BY n.gemeldetAm ASC"),
        @NamedQuery(name = Geisternetz.FINDE_NACH_BERGENDER_PERSON,
                query = "SELECT n FROM Geisternetz n WHERE n.bergendePerson = :person "
                        + "ORDER BY n.statusGeaendertAm DESC"),
        @NamedQuery(name = Geisternetz.ZAEHLE_NACH_STATUS,
                query = "SELECT n.status, COUNT(n) FROM Geisternetz n GROUP BY n.status")
})
public class Geisternetz implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String FINDE_NACH_STATUS = "Geisternetz.findeNachStatus";
    public static final String FINDE_NACH_BERGENDER_PERSON = "Geisternetz.findeNachBergenderPerson";
    public static final String ZAEHLE_NACH_STATUS = "Geisternetz.zaehleNachStatus";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Bitte den Breitengrad angeben.")
    @DecimalMin(value = "-90.0", message = "Der Breitengrad muss zwischen -90 und 90 liegen.")
    @DecimalMax(value = "90.0", message = "Der Breitengrad muss zwischen -90 und 90 liegen.")
    @Column(name = "breitengrad", nullable = false)
    private Double breitengrad;

    @NotNull(message = "Bitte den Längengrad angeben.")
    @DecimalMin(value = "-180.0", message = "Der Längengrad muss zwischen -180 und 180 liegen.")
    @DecimalMax(value = "180.0", message = "Der Längengrad muss zwischen -180 und 180 liegen.")
    @Column(name = "laengengrad", nullable = false)
    private Double laengengrad;

    /** Geschätzte Größe in Quadratmetern. */
    @NotNull(message = "Bitte die geschätzte Größe angeben.")
    @Min(value = 1, message = "Die geschätzte Größe muss mindestens 1 m² betragen.")
    @Max(value = 1_000_000, message = "Die geschätzte Größe darf höchstens 1.000.000 m² betragen.")
    @Column(name = "geschaetzte_groesse_qm", nullable = false)
    private Integer geschaetzteGroesse;

    @Size(max = 500, message = "Die Bemerkung darf höchstens 500 Zeichen lang sein.")
    @Column(name = "bemerkung", length = 500)
    private String bemerkung;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private NetzStatus status = NetzStatus.GEMELDET;

    @Column(name = "gemeldet_am", nullable = false)
    private LocalDateTime gemeldetAm;

    @Column(name = "status_geaendert_am", nullable = false)
    private LocalDateTime statusGeaendertAm;

    /** Meldende Person; bleibt leer (null), wenn anonym gemeldet wurde. */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "meldende_person_id", foreignKey = @ForeignKey(name = "fk_netz_meldende_person"))
    private Person meldendePerson;

    /** Bergende Person; maximal eine pro Netz. */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "bergende_person_id", foreignKey = @ForeignKey(name = "fk_netz_bergende_person"))
    private Person bergendePerson;

    /** Person, die das Netz als verschollen gemeldet hat (nie anonym). */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "verschollen_gemeldet_von_id",
            foreignKey = @ForeignKey(name = "fk_netz_verschollen_person"))
    private Person verschollenGemeldetVon;

    /** Versionsspalte für optimistisches Sperren bei gleichzeitigen Änderungen. */
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public Geisternetz() {
    }

    // ------------------------------------------------------------------
    // Abgeleitete Anzeigeeigenschaften
    // ------------------------------------------------------------------

    /** Standort als lesbarer Text, z. B. "54,3233° N, 10,1228° O". */
    public String getStandortText() {
        if (breitengrad == null || laengengrad == null) {
            return "";
        }
        return String.format(Locale.GERMANY, "%.4f° %s, %.4f° %s",
                Math.abs(breitengrad), breitengrad >= 0 ? "N" : "S",
                Math.abs(laengengrad), laengengrad >= 0 ? "O" : "W");
    }

    /** Link auf OpenStreetMap mit Markierung am Standort. */
    public String getKartenLink() {
        return String.format(Locale.ROOT, "https://www.openstreetmap.org/?mlat=%.5f&mlon=%.5f#map=9/%.5f/%.5f",
                breitengrad, laengengrad, breitengrad, laengengrad);
    }

    public boolean isAnonymGemeldet() {
        return meldendePerson == null;
    }

    // ------------------------------------------------------------------
    // Getter und Setter
    // ------------------------------------------------------------------

    public Long getId() {
        return id;
    }

    public Double getBreitengrad() {
        return breitengrad;
    }

    public void setBreitengrad(Double breitengrad) {
        this.breitengrad = breitengrad;
    }

    public Double getLaengengrad() {
        return laengengrad;
    }

    public void setLaengengrad(Double laengengrad) {
        this.laengengrad = laengengrad;
    }

    public Integer getGeschaetzteGroesse() {
        return geschaetzteGroesse;
    }

    public void setGeschaetzteGroesse(Integer geschaetzteGroesse) {
        this.geschaetzteGroesse = geschaetzteGroesse;
    }

    public String getBemerkung() {
        return bemerkung;
    }

    public void setBemerkung(String bemerkung) {
        this.bemerkung = bemerkung;
    }

    public NetzStatus getStatus() {
        return status;
    }

    public void setStatus(NetzStatus status) {
        this.status = status;
    }

    public LocalDateTime getGemeldetAm() {
        return gemeldetAm;
    }

    public void setGemeldetAm(LocalDateTime gemeldetAm) {
        this.gemeldetAm = gemeldetAm;
    }

    public LocalDateTime getStatusGeaendertAm() {
        return statusGeaendertAm;
    }

    public void setStatusGeaendertAm(LocalDateTime statusGeaendertAm) {
        this.statusGeaendertAm = statusGeaendertAm;
    }

    public Person getMeldendePerson() {
        return meldendePerson;
    }

    public void setMeldendePerson(Person meldendePerson) {
        this.meldendePerson = meldendePerson;
    }

    public Person getBergendePerson() {
        return bergendePerson;
    }

    public void setBergendePerson(Person bergendePerson) {
        this.bergendePerson = bergendePerson;
    }

    public Person getVerschollenGemeldetVon() {
        return verschollenGemeldetVon;
    }

    public void setVerschollenGemeldetVon(Person verschollenGemeldetVon) {
        this.verschollenGemeldetVon = verschollenGemeldetVon;
    }

    public Long getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        // instanceof und getId() statt Feldzugriff, da Hibernate lazy geladene
        // Beziehungen als Proxy Unterklasse liefert, deren Felder nicht befüllt sind.
        if (!(o instanceof Geisternetz other)) {
            return false;
        }
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
