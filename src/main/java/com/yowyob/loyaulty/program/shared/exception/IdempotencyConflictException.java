package com.yowyob.loyaulty.program.shared.exception;

/**
 * Levée quand une clé d'idempotence est soumise en doublon pour une opération déjà traitée.
 *
 * <p>Le handler qui attrape cette exception doit retourner le résultat original
 * (payload JSON stocké dans Redis) au lieu de re-traiter la requête.</p>
 */
public class IdempotencyConflictException extends RuntimeException {

    private final String idempotencyKey;
    private final String cachedResult;

    public IdempotencyConflictException(String idempotencyKey, String cachedResult) {
        super("Opération déjà traitée pour la clé d'idempotence : " + idempotencyKey);
        this.idempotencyKey = idempotencyKey;
        this.cachedResult   = cachedResult;
    }

    public String getIdempotencyKey() { return idempotencyKey; }

    /**
     * Retourne le payload JSON du résultat original.
     * À utiliser pour la réponse HTTP 200 (rejouer sans re-traitement).
     */
    public String getCachedResult()   { return cachedResult;   }
}
