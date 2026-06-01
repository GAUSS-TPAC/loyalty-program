package com.yowyob.loyaulty.program.domain.rule.model.enums;

public enum EffectType {
    CREDIT_POINTS,       // créditer des points sur le wallet
    CREDIT_WALLET,       // créditer de la monnaie virtuelle
    GRANT_REWARD,        // attribuer une récompense du catalogue
    RESET_COUNTER,       // remettre à zéro un compteur
    UPDATE_TIER,         // changer le palier du membre
    SEND_NOTIFICATION    // envoyer une notification
}
