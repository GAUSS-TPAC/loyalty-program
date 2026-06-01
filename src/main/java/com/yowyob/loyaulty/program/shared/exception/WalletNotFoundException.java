package com.yowyob.loyaulty.program.shared.exception;

import java.util.UUID;

/**
 * Levée quand un wallet n'est pas trouvé pour un membre et un tenant donnés.
 */
public class WalletNotFoundException extends RuntimeException {

    private final UUID walletId;
    private final UUID tenantId;

    public WalletNotFoundException(UUID walletId, UUID tenantId) {
        super("Wallet introuvable : id=%s, tenant=%s".formatted(walletId, tenantId));
        this.walletId = walletId;
        this.tenantId = tenantId;
    }

    public WalletNotFoundException(UUID memberId, UUID tenantId, boolean byMember) {
        super("Aucun wallet trouvé pour le membre : memberId=%s, tenant=%s".formatted(memberId, tenantId));
        this.walletId = memberId;
        this.tenantId = tenantId;
    }

    public UUID getWalletId()  { return walletId; }
    public UUID getTenantId()  { return tenantId; }
}
