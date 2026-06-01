package com.yowyob.loyaulty.program.domain.rule.service;

import com.yowyob.loyaulty.program.domain.rule.model.*;
import com.yowyob.loyaulty.program.domain.rule.model.enums.EffectType;

import java.time.Instant;
import java.util.*;

/**
 * Moteur d'évaluation des règles — Java pur, testable sans Spring.
 *
 * Algorithme :
 *  1. Filtrer les règles actives et valides à l'instant courant
 *  2. Trier par priorité décroissante
 *  3. Pour chaque règle dont le trigger correspond à l'event :
 *     a. Incrémenter son compteur (toujours, même si les conditions ne sont pas remplies)
 *     b. Évaluer les conditions avec la valeur POST-incrément
 *     c. Si toutes les conditions sont remplies → appliquer les effets
 */
public class RuleEngine {

    public EvaluationResult evaluate(List<Rule> rules,
                                      EvaluationContext ctx,
                                      Map<UUID, Long> updatedCounters) {
        Instant now = ctx.event().occurredAt();
        String memberId  = ctx.event().memberId();
        UUID   eventId   = ctx.event().eventId();

        List<AppliedEffect> appliedEffects    = new ArrayList<>();
        List<UUID>          incrementedRules  = new ArrayList<>();
        List<String>        notifications     = new ArrayList<>();

        rules.stream()
             .filter(r -> r.isActiveAt(now))
             .sorted(Comparator.comparingInt(Rule::getPriority).reversed())
             .forEach(rule -> {
                 if (!rule.triggerMatches(ctx.event().eventType(), ctx.event().payload())) {
                     return;
                 }

                 // Valeur du compteur déjà incrémentée par le handler avant l'appel
                 incrementedRules.add(rule.getId());

                 if (rule.conditionsMet(updatedCounters)) {
                     for (Effect effect : rule.getEffects()) {
                         appliedEffects.add(new AppliedEffect(
                                 rule.getId(), rule.getName(),
                                 effect.type(), effect.params()
                         ));
                         if (effect.type() == EffectType.SEND_NOTIFICATION) {
                             String msg = effect.paramAsString("message");
                             if (msg != null) notifications.add(msg);
                         }
                     }
                 }
             });

        return new EvaluationResult(eventId, memberId, appliedEffects, incrementedRules, notifications);
    }
}
