package com.yowyob.loyaulty.program.shared.exception;

/**
 * Exception levée quand une opération de paiement échoue.
 *
 * <p>Couvre les cas : provider indisponible, initiation échouée,
 * montant invalide, etc. Le Kernel Core Payment API est la source.</p>
 */
public class PaymentException extends RuntimeException {

    private final int httpStatusCode;

    /**
     * Construit une exception de paiement.
     *
     * @param httpStatusCode code HTTP associé (ex. 422, 502, 503).
     * @param detail         message descriptif (ex. "Provider MTN indisponible").
     */
    public PaymentException(int httpStatusCode, String detail) {
        super("[Payment %d] %s".formatted(httpStatusCode, detail));
        this.httpStatusCode = httpStatusCode;
    }

    /**
     * Construit une exception de paiement en encapsulant une cause racine.
     *
     * @param httpStatusCode code HTTP associé.
     * @param detail         message descriptif.
     * @param cause          exception racine.
     */
    public PaymentException(int httpStatusCode, String detail, Throwable cause) {
        super("[Payment %d] %s".formatted(httpStatusCode, detail), cause);
        this.httpStatusCode = httpStatusCode;
    }

    public int getHttpStatusCode() { return httpStatusCode; }
}
