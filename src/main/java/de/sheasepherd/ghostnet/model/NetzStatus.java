package de.sheasepherd.ghostnet.model;

/**
 * Mögliche Zustände eines Geisternetzes laut Fachkonzept.
 */
public enum NetzStatus {

    /** Eine meldende Person hat das Geisternetz im System erfasst. */
    GEMELDET("Gemeldet"),

    /** Eine bergende Person hat die Bergung angekündigt. */
    BERGUNG_BEVORSTEHEND("Bergung bevorstehend"),

    /** Eine bergende Person hat das Geisternetz erfolgreich geborgen. */
    GEBORGEN("Geborgen"),

    /** Das Geisternetz war am gemeldeten Standort nicht auffindbar. */
    VERSCHOLLEN("Verschollen");

    private final String bezeichnung;

    NetzStatus(String bezeichnung) {
        this.bezeichnung = bezeichnung;
    }

    public String getBezeichnung() {
        return bezeichnung;
    }

    /** Ein Netz ist "offen", solange es noch geborgen werden muss. */
    public boolean isOffen() {
        return this == GEMELDET || this == BERGUNG_BEVORSTEHEND;
    }
}
