package com.yowyob.loyalty.domain.referral.exception;

import java.util.UUID;

public class ReferralProgramNotFoundException extends ReferralDomainException {
    public ReferralProgramNotFoundException() {
        super("Aucun programme de parrainage configuré pour ce tenant");
    }

    public ReferralProgramNotFoundException(UUID programId) {
        super(programId != null
            ? "Programme de parrainage introuvable: " + programId
            : "Aucun programme de parrainage configuré pour ce tenant");
    }
}
