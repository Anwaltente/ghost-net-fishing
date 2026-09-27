package de.sheasepherd.ghostnet.web;

import de.sheasepherd.ghostnet.model.NetzStatus;
import de.sheasepherd.ghostnet.service.GeisternetzService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.Map;

/**
 * Liefert die Kennzahlen (Anzahl Netze je Status) für die Startseite.
 */
@Named
@RequestScoped
public class StartseiteBean {

    @Inject
    private GeisternetzService geisternetzService;

    private Map<NetzStatus, Long> anzahlJeStatus;

    @PostConstruct
    public void init() {
        anzahlJeStatus = geisternetzService.zaehleNachStatus();
    }

    public long anzahl(String status) {
        return anzahlJeStatus.getOrDefault(NetzStatus.valueOf(status), 0L);
    }

    public long getNochZuBergen() {
        return anzahlJeStatus.get(NetzStatus.GEMELDET) + anzahlJeStatus.get(NetzStatus.BERGUNG_BEVORSTEHEND);
    }
}
