package com.yowyob.loyalty.shared.exception;

import java.util.Map;

/**
 * Levée quand l'acteur qui se connecte appartient à plusieurs organisations
 * KernelCore et qu'aucun {@code organizationId} n'a été précisé dans la requête
 * de connexion pour lever l'ambiguïté.
 */
public class OrganizationSelectionRequiredException extends AppException {
    public OrganizationSelectionRequiredException(String detail, Map<String, Object> properties) {
        super(ErrorCode.ORGANIZATION_SELECTION_REQUIRED, detail, properties);
    }
}
