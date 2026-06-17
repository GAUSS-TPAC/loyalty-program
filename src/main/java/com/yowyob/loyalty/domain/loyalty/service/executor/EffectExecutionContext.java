package com.yowyob.loyalty.domain.loyalty.service.executor;

import com.yowyob.loyalty.domain.loyalty.model.tier.TierLevel;
import com.yowyob.loyalty.domain.shared.model.UserId;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EffectExecutionContext {

    private final List<PointsOperation> pendingPointsOperations = new ArrayList<>();
    private final List<WalletOperation> pendingWalletOperations = new ArrayList<>();
    private final List<RewardOperation> pendingRewardOperations = new ArrayList<>();
    private final List<CounterOperation> pendingCounterOperations = new ArrayList<>();
    private final List<NotificationOperation> pendingNotifications = new ArrayList<>();
    private final List<TierOperation> pendingTierOperations = new ArrayList<>();

    public record PointsOperation(UserId memberId, long amount, String type, UUID ruleId) {}
    public record WalletOperation(UserId memberId, BigDecimal amount, String source) {}
    public record RewardOperation(UserId memberId, String rewardId, UUID ruleId, double amount) {}
    public record CounterOperation(String counterKey, String operationType, long delta) {}
    public record NotificationOperation(UserId memberId, String template, Map<String, Object> params) {}
    public record TierOperation(UserId memberId, TierLevel newLevel) {}

    public void addPointsCredit(UserId memberId, long amount, String type, UUID ruleId) {
        pendingPointsOperations.add(new PointsOperation(memberId, amount, type, ruleId));
    }

    public void addWalletCredit(UserId memberId, BigDecimal amount, String source) {
        pendingWalletOperations.add(new WalletOperation(memberId, amount, source));
    }

    public void addRewardGrant(UserId memberId, String rewardId, UUID ruleId, double amount) {
        pendingRewardOperations.add(new RewardOperation(memberId, rewardId, ruleId, amount));
    }

    public void addCounterIncrement(String counterKey, long delta) {
        pendingCounterOperations.add(new CounterOperation(counterKey, "INCREMENT", delta));
    }

    public void addCounterReset(String counterKey) {
        pendingCounterOperations.add(new CounterOperation(counterKey, "RESET", 0));
    }

    public void addNotification(UserId memberId, String template, Map<String, Object> params) {
        pendingNotifications.add(new NotificationOperation(memberId, template, params));
    }

    public void addTierUpdate(UserId memberId, TierLevel newLevel) {
        pendingTierOperations.add(new TierOperation(memberId, newLevel));
    }

    public List<PointsOperation> getPendingPointsOperations() {
        return pendingPointsOperations;
    }

    public List<WalletOperation> getPendingWalletOperations() {
        return pendingWalletOperations;
    }

    public List<RewardOperation> getPendingRewardOperations() {
        return pendingRewardOperations;
    }

    public List<CounterOperation> getPendingCounterOperations() {
        return pendingCounterOperations;
    }

    public List<NotificationOperation> getPendingNotifications() {
        return pendingNotifications;
    }

    public List<TierOperation> getPendingTierOperations() {
        return pendingTierOperations;
    }
}
