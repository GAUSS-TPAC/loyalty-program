package com.yowyob.loyaulty.program.domain.rule.model;

import com.yowyob.loyaulty.program.domain.rule.model.enums.EffectType;

import java.util.Map;
import java.util.UUID;

public record AppliedEffect(
        UUID ruleId,
        String ruleName,
        EffectType type,
        Map<String, Object> params
) {}
