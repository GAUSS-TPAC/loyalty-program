package com.yowyob.loyalty.shared.multitenancy;

public class TenantContextMissingException extends RuntimeException {
    public TenantContextMissingException() {
        super("TenantContext absent du contexte Reactor — cette opération doit être exécutée dans le contexte d'une requête HTTP");
    }
}
