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
import java.util.List;

/**
 * Backing Bean der Seite "Meine Bergungen": zeigt die von der angemeldeten
 * Person übernommenen Netze und erlaubt, sie als geborgen zu melden (User Story 4).
 */
@Named
@ViewScoped
public class MeineBergungenBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private GeisternetzService geisternetzService;

    @Inject
    private SitzungBean sitzung;

    private List<Geisternetz> bevorstehend = List.of();
    private List<Geisternetz> abgeschlossen = List.of();

    @PostConstruct
    public void init() {
        laden();
    }

    public void laden() {
        if (!sitzung.isAngemeldet()) {
            return;
        }
        List<Geisternetz> alle = geisternetzService.findeBergungenVon(sitzung.getPersonId());
        bevorstehend = alle.stream().filter(n -> n.getStatus() == NetzStatus.BERGUNG_BEVORSTEHEND).toList();
        abgeschlossen = alle.stream().filter(n -> n.getStatus() != NetzStatus.BERGUNG_BEVORSTEHEND).toList();
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

    public List<Geisternetz> getBevorstehend() {
        return bevorstehend;
    }

    public List<Geisternetz> getAbgeschlossen() {
        return abgeschlossen;
    }
}
