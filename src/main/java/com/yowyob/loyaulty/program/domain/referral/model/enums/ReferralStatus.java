package com.yowyob.loyaulty.program.domain.referral.model.enums;

public enum ReferralStatus {
    PENDING,    // filleul inscrit, pas encore converti
    CONVERTED,  // filleul a réalisé l'action qualifiante
    REWARDED,   // récompenses attribuées aux deux parties
    EXPIRED     // délai de conversion dépassé
}
