package de.sheasepherd.ghostnet.web;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

/**
 * Kleine Hilfsklasse, um Meldungen für die Oberfläche (h:messages) zu erzeugen.
 */
public final class Nachrichten {

    private Nachrichten() {
    }

    public static void info(String text) {
        hinzufuegen(FacesMessage.SEVERITY_INFO, text);
    }

    public static void fehler(String text) {
        hinzufuegen(FacesMessage.SEVERITY_ERROR, text);
    }

    /** Sorgt dafür, dass Meldungen einen Redirect (Post/Redirect/Get) überleben. */
    public static void ueberRedirectBehalten() {
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
    }

    private static void hinzufuegen(FacesMessage.Severity schwere, String text) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(schwere, text, null));
    }
}
