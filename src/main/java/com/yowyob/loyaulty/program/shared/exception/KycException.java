package com.yowyob.loyaulty.program.shared.exception;

import java.util.UUID;

/**
 * Exception levée quand une opération est refusée pour cause de KYC insuffisant.
 *
 * <p>Typiquement levée par la logique métier avant d'autoriser un retrait :
 * si le membre n'est pas KYC-vérifié, l'opération est bloquée.</p>
 */
public class KycException extends RuntimeException {

    private final UUID memberId;
    private final int httpStatusCode;

    /**
     * Construit une exception KYC.
     *
     * @param memberId       identifiant du membre dont le KYC est insuffisant.
     * @param httpStatusCode code HTTP associé (ex. 403 pour non autorisé, 422 pour échec vérif).
     * @param detail         message descriptif.
     */
    public KycException(UUID memberId, int httpStatusCode, String detail) {
        super("[KYC %d] memberId=%s — %s".formatted(httpStatusCode, memberId, detail));
        this.memberId = memberId;
        this.httpStatusCode = httpStatusCode;
    }

    public UUID getMemberId() { return memberId; }
    public int getHttpStatusCode() { return httpStatusCode; }
}
