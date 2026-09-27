package de.sheasepherd.ghostnet.service;

/**
 * Wird ausgelöst, wenn eine fachliche Regel verletzt wird, z. B. wenn ein Netz
 * bereits einer anderen bergenden Person zugeordnet ist. Als RuntimeException
 * führt sie in einer mit {@code @Transactional} markierten Methode zum Rollback.
 */
public class GeschaeftsregelException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public GeschaeftsregelException(String nachricht) {
        super(nachricht);
    }
}
