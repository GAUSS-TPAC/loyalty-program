package com.yowyob.loyaulty.program.domain.rule.model.enums;

public enum ConditionType {
    CUMULATIVE_COUNT,    // nombre d'événements cumulés
    CUMULATIVE_AMOUNT,   // montant cumulé
    POINTS_BALANCE,      // solde de points actuel
    TIER_IS,             // palier du membre
    TIME_WINDOW          // fenêtre temporelle (heure, jour de semaine)
}
