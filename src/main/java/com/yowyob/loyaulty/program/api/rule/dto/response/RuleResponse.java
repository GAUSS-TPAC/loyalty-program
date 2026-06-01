package com.yowyob.loyaulty.program.api.rule.dto.response;

import com.yowyob.loyaulty.program.domain.rule.model.Rule;

import java.time.Instant;
import java.util.UUID;

public record RuleResponse(
        UUID id,
        String name,
        String description,
        int priority,
        String status,
        Instant validFrom,
        Instant validUntil,
        Instant createdAt
) {
    public static RuleResponse from(Rule r) {
        return new RuleResponse(
                r.getId(), r.getName(), r.getDescription(),
                r.getPriority(), r.getStatus().name(),
                r.getValidFrom(), r.getValidUntil(),
                r.getAuditInfo().createdAt()
        );
    }
}
