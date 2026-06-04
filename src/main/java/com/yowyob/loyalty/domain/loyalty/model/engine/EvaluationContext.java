package com.yowyob.loyalty.domain.loyalty.model.engine;

import com.yowyob.loyalty.domain.loyalty.model.counter.Counter;
import com.yowyob.loyalty.domain.loyalty.model.event.IncomingEvent;
import com.yowyob.loyalty.domain.loyalty.model.points.PointsAccount;
import com.yowyob.loyalty.domain.loyalty.model.tier.MemberTier;
import com.yowyob.loyalty.domain.loyalty.model.tier.TierPolicy;

import java.util.Map;
import java.util.Optional;

public record EvaluationContext(
        IncomingEvent event,
        PointsAccount pointsAccount,
        MemberTier memberTier,
        Map<String, Counter> counters,
        TierPolicy tierPolicy
) {
    public Optional<Counter> getCounter(String key) {
        if (counters == null) return Optional.empty();
        return Optional.ofNullable(counters.get(key));
    }

    public long getCounterValue(String key) {
        return getCounter(key).map(Counter::value).orElse(0L);
    }
}
