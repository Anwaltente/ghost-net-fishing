package de.sheasepherd.ghostnet.web;

import de.sheasepherd.ghostnet.model.Geisternetz;
import de.sheasepherd.ghostnet.model.NetzStatus;
import de.sheasepherd.ghostnet.service.GeisternetzService;
import de.sheasepherd.ghostnet.service.GeschaeftsregelException;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.List;

/**
 * Backing Bean der Seite "Zu bergende Netze".
 * <ul>
 *   <li>User Story 3: Liste aller noch zu bergenden Netze</li>
 *   <li>User Story 2: für die Bergung eintragen</li>
 *   <li>User Story 4: als geborgen melden</li>
 *   <li>User Story 7: als verschollen melden</li>
 * </ul>
 */
@Named
@ViewScoped
public class NetzUebersichtBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Filterwerte für die Auswahlliste. */
    public static final String FILTER_ALLE_OFFENEN = "OFFEN";

    @Inject
    private GeisternetzService geisternetzService;

    @Inject
    private SitzungBean sitzung;

    private String filter = FILTER_ALLE_OFFENEN;
    private List<Geisternetz> netze;

    @PostConstruct
    public void init() {
        laden();
    }

    public void laden() {
        if (FILTER_ALLE_OFFENEN.equals(filter)) {
            netze = geisternetzService.findeNochZuBergende();
        } else {
            netze = geisternetzService.findeNachStatus(EnumSet.of(NetzStatus.valueOf(filter)));
        }
    }

    public void uebernehmen(Geisternetz netz) {
        try {
            geisternetzService.uebernehmeBergung(netz.getId(), sitzung.getPersonId());
            Nachrichten.info("Sie haben die Bergung von Netz Nr. " + netz.getId()
                    + " übernommen. Es erscheint jetzt unter \"Meine Bergungen\".");
        } catch (GeschaeftsregelException e) {
            Nachrichten.fehler(e.getMessage());
        }
        laden();
    }

    public void alsGeborgenMelden(Geisternetz netz) {
        try {
            geisternetzService.meldeGeborgen(netz.getId(), sitzung.getPersonId());
            Nachrichten.info("Netz Nr. " + netz.getId() + " wurde als geborgen gemeldet. Vielen Dank!");
        } catch (GeschaeftsregelException e) {
            Nachrichten.fehler(e.getMessage());
        }
        laden();
    }

    public void alsVerschollenMelden(Geisternetz netz) {
        try {
            geisternetzService.meldeVerschollen(netz.getId(), sitzung.getPersonId());
            Nachrichten.info("Netz Nr. " + netz.getId() + " wurde als verschollen gemeldet.");
        } catch (GeschaeftsregelException e) {
            Nachrichten.fehler(e.getMessage());
        }
        laden();
    }

    /** Prüft, ob die angemeldete Person die Bergung dieses Netzes übernommen hat. */
    public boolean istMeineBergung(Geisternetz netz) {
        return sitzung.isAngemeldet() && netz.getBergendePerson() != null
                && netz.getBergendePerson().getId().equals(sitzung.getPersonId());
    }

    public NetzStatus[] getOffeneStatus() {
        return new NetzStatus[]{NetzStatus.GEMELDET, NetzStatus.BERGUNG_BEVORSTEHEND};
    }

    public List<Geisternetz> getNetze() {
        return netze;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }
}
