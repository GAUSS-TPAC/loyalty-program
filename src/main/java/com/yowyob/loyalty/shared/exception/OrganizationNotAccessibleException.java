package com.yowyob.loyalty.shared.exception;

/**
 * Levée quand un {@code organizationId} explicitement demandé à la connexion
 * ne fait pas partie des organisations KernelCore accessibles à l'acteur authentifié.
 */
public class OrganizationNotAccessibleException extends AppException {
    public OrganizationNotAccessibleException(String detail) {
        super(ErrorCode.ORGANIZATION_NOT_ACCESSIBLE, detail);
    }
}
