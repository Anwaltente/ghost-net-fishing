package de.sheasepherd.ghostnet.web;

import de.sheasepherd.ghostnet.model.Geisternetz;
import de.sheasepherd.ghostnet.model.Person;
import de.sheasepherd.ghostnet.service.GeisternetzService;
import de.sheasepherd.ghostnet.service.GeschaeftsregelException;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;

/**
 * Backing Bean der Seite "Netz melden" (User Story 1).
 * ViewScoped, damit der Zustand der Checkbox "anonym melden" bei
 * Ajax Anfragen erhalten bleibt.
 */
@Named
@ViewScoped
public class MeldungBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private GeisternetzService geisternetzService;

    @Inject
    private SitzungBean sitzung;

    private Geisternetz netz;
    private Person melder;
    private boolean anonym = true;

    @PostConstruct
    public void init() {
        netz = new Geisternetz();
        melder = new Person();
        if (sitzung.isAngemeldet()) {
            anonym = false;
            melder.setName(sitzung.getAngemeldetePerson().getName());
            melder.setTelefonnummer(sitzung.getAngemeldetePerson().getTelefonnummer());
        }
    }

    public String melden() {
        try {
            Geisternetz gespeichert = geisternetzService.melden(netz, anonym ? null : melder);
            Nachrichten.info("Vielen Dank! Das Geisternetz wurde unter der Nr. " + gespeichert.getId()
                    + (anonym ? " anonym" : "") + " gemeldet.");
            Nachrichten.ueberRedirectBehalten();
            return "netze?faces-redirect=true";
        } catch (GeschaeftsregelException e) {
            Nachrichten.fehler(e.getMessage());
            return null;
        }
    }

    public Geisternetz getNetz() {
        return netz;
    }

    public Person getMelder() {
        return melder;
    }

    public boolean isAnonym() {
        return anonym;
    }

    public void setAnonym(boolean anonym) {
        this.anonym = anonym;
    }
}
