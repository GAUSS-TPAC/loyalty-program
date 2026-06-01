package com.yowyob.loyaulty.program.shared.exception;

/**
 * Exception levée quand le Kernel Core est inaccessible ou retourne une erreur.
 *
 * <p>Hérite de {@link RuntimeException} — cohérent avec les autres exceptions du projet.</p>
 */
public class KernelCoreException extends RuntimeException {

    private final int httpStatusCode;

    /**
     * Construit une exception Kernel Core avec un code HTTP et un message de détail.
     *
     * @param httpStatusCode code HTTP associé (ex. 503, 502, 404).
     * @param detail         message descriptif de l'erreur.
     */
    public KernelCoreException(int httpStatusCode, String detail) {
        super("[KernelCore %d] %s".formatted(httpStatusCode, detail));
        this.httpStatusCode = httpStatusCode;
    }

    /**
     * Construit une exception Kernel Core en encapsulant une cause racine (ex. WebClient error).
     *
     * @param httpStatusCode code HTTP associé.
     * @param detail         message descriptif de l'erreur.
     * @param cause          exception racine (timeout, connexion refusée…).
     */
    public KernelCoreException(int httpStatusCode, String detail, Throwable cause) {
        super("[KernelCore %d] %s".formatted(httpStatusCode, detail), cause);
        this.httpStatusCode = httpStatusCode;
    }

    public int getHttpStatusCode() { return httpStatusCode; }
}
