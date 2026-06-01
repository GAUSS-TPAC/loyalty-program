package com.yowyob.loyaulty.program.domain.wallet.service;

import com.yowyob.loyaulty.program.domain.wallet.model.WalletTransaction;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.TransactionType;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class FraudDetectionService {

    private static final int MAX_DEBITS_PER_MINUTE = 5;
    private static final BigDecimal UNUSUAL_AMOUNT_MULTIPLIER = new BigDecimal("10");
    private static final Duration TOPUP_WITHDRAWAL_MIN_DELAY = Duration.ofHours(24);

    public FraudAssessment assess(WalletTransaction incoming, List<WalletTransaction> recentHistory) {
        if (hasTooManyDebitsInLastMinute(recentHistory)) {
            return FraudAssessment.suspicious("TOO_MANY_DEBITS",
                    "More than " + MAX_DEBITS_PER_MINUTE + " debits in the last minute");
        }
        if (isUnusuallyLargeAmount(incoming, recentHistory)) {
            return FraudAssessment.suspicious("UNUSUAL_AMOUNT",
                    "Amount " + incoming.getAmount() + " is unusually large compared to history");
        }
        if (isImmediateWithdrawalAfterTopup(incoming, recentHistory)) {
            return FraudAssessment.suspicious("IMMEDIATE_WITHDRAWAL",
                    "Withdrawal initiated too soon after a top-up");
        }
        return FraudAssessment.clean();
    }

    private boolean hasTooManyDebitsInLastMinute(List<WalletTransaction> history) {
        Instant oneMinuteAgo = Instant.now().minus(Duration.ofMinutes(1));
        long recentDebits = history.stream()
                .filter(tx -> tx.getType() == TransactionType.DEBIT)
                .filter(tx -> tx.getCreatedAt().isAfter(oneMinuteAgo))
                .count();
        return recentDebits >= MAX_DEBITS_PER_MINUTE;
    }

    private boolean isUnusuallyLargeAmount(WalletTransaction incoming,
                                             List<WalletTransaction> history) {
        if (history.isEmpty()) return false;
        BigDecimal avgAmount = history.stream()
                .map(WalletTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(history.size()), 2, java.math.RoundingMode.HALF_UP);
        if (avgAmount.compareTo(BigDecimal.ZERO) == 0) return false;
        return incoming.getAmount().compareTo(avgAmount.multiply(UNUSUAL_AMOUNT_MULTIPLIER)) > 0;
    }

    private boolean isImmediateWithdrawalAfterTopup(WalletTransaction incoming,
                                                      List<WalletTransaction> history) {
        if (incoming.getType() != TransactionType.DEBIT) return false;
        return history.stream()
                .filter(tx -> tx.getType() == TransactionType.CREDIT)
                .filter(tx -> tx.getSource() != null && tx.getSource().name().startsWith("TOPUP"))
                .anyMatch(topup -> Duration.between(topup.getCreatedAt(), Instant.now())
                        .compareTo(TOPUP_WITHDRAWAL_MIN_DELAY) < 0);
    }

    public record FraudAssessment(boolean suspicious, String reason, String code) {
        public static FraudAssessment clean() {
            return new FraudAssessment(false, null, null);
        }
        public static FraudAssessment suspicious(String code, String reason) {
            return new FraudAssessment(true, reason, code);
        }
    }
}
