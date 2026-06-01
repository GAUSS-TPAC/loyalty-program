package com.yowyob.loyaulty.program.domain.member;

import com.yowyob.loyaulty.program.domain.member.model.Member;
import com.yowyob.loyaulty.program.domain.member.model.MemberTier;
import com.yowyob.loyaulty.program.domain.member.model.PointsAccount;
import com.yowyob.loyaulty.program.domain.member.model.PointsTransaction;
import com.yowyob.loyaulty.program.domain.member.model.enums.MemberStatus;
import com.yowyob.loyaulty.program.domain.member.model.enums.PointsTransactionType;
import com.yowyob.loyaulty.program.domain.member.model.enums.TierLevel;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class MemberDomainTest {

    private final TenantId tenantId = TenantId.of(UUID.randomUUID().toString());

    // ── Member ──────────────────────────────────────────────────────────────

    @Test
    void member_enroll_setsActiveStatus() {
        Member member = Member.enroll(tenantId, "ext-001", "jean@test.cm", "+237600000001", "Jean");
        assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        assertThat(member.getExternalId()).isEqualTo("ext-001");
        assertThat(member.isActive()).isTrue();
    }

    @Test
    void member_enroll_requiresExternalId() {
        assertThatThrownBy(() -> Member.enroll(tenantId, "", "e@test.cm", null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("externalId");
    }

    @Test
    void member_block_changesStatus() {
        Member member = Member.enroll(tenantId, "ext-002", null, null, null);
        member.block();
        assertThat(member.isActive()).isFalse();
        assertThat(member.getStatus()).isEqualTo(MemberStatus.BLOCKED);
    }

    // ── MemberTier ──────────────────────────────────────────────────────────

    @Test
    void memberTier_startsAtBronze() {
        MemberTier tier = MemberTier.create(tenantId, "member-1");
        assertThat(tier.getLevel()).isEqualTo(TierLevel.BRONZE);
        assertThat(tier.getMultiplier()).isEqualTo(1.0);
    }

    @Test
    void memberTier_upgradeToSilverAt1000Points() {
        MemberTier tier = MemberTier.create(tenantId, "member-1");
        boolean changed = tier.addPoints(1000);
        assertThat(changed).isTrue();
        assertThat(tier.getLevel()).isEqualTo(TierLevel.SILVER);
        assertThat(tier.getMultiplier()).isEqualTo(1.5);
    }

    @Test
    void memberTier_upgradeToGoldAt5000Points() {
        MemberTier tier = MemberTier.create(tenantId, "member-1");
        tier.addPoints(4999);
        assertThat(tier.getLevel()).isEqualTo(TierLevel.SILVER);
        tier.addPoints(1);
        assertThat(tier.getLevel()).isEqualTo(TierLevel.GOLD);
    }

    @Test
    void memberTier_upgradeToPlatinumAt20000Points() {
        MemberTier tier = MemberTier.create(tenantId, "member-1");
        tier.addPoints(20_000);
        assertThat(tier.getLevel()).isEqualTo(TierLevel.PLATINUM);
        assertThat(tier.getMultiplier()).isEqualTo(3.0);
        assertThat(tier.pointsToNextTier()).isEqualTo(0);
    }

    @Test
    void memberTier_noChangeWhenPointsInsideSameTier() {
        MemberTier tier = MemberTier.create(tenantId, "member-1");
        boolean changed = tier.addPoints(500);
        assertThat(changed).isFalse();
        assertThat(tier.getLevel()).isEqualTo(TierLevel.BRONZE);
    }

    // ── PointsAccount ────────────────────────────────────────────────────────

    @Test
    void pointsAccount_earn_creditsBalance() {
        PointsAccount account = PointsAccount.create(tenantId, "member-1");
        PointsTransaction tx = account.earn(500, "Achat", "ref-1");

        assertThat(account.getAvailablePoints()).isEqualTo(500);
        assertThat(account.getLifetimeEarned()).isEqualTo(500);
        assertThat(tx.getType()).isEqualTo(PointsTransactionType.EARN);
        assertThat(tx.getAmount()).isEqualTo(500);
        assertThat(tx.getBalanceBefore()).isEqualTo(0);
        assertThat(tx.getBalanceAfter()).isEqualTo(500);
    }

    @Test
    void pointsAccount_spend_debitsBalance() {
        PointsAccount account = PointsAccount.create(tenantId, "member-1");
        account.earn(1000, "Achat", "ref-1");
        PointsTransaction tx = account.spend(400, "Rédemption", "reward-1");

        assertThat(account.getAvailablePoints()).isEqualTo(600);
        assertThat(account.getLifetimeSpent()).isEqualTo(400);
        assertThat(tx.getType()).isEqualTo(PointsTransactionType.SPEND);
    }

    @Test
    void pointsAccount_spend_throwsWhenInsufficientBalance() {
        PointsAccount account = PointsAccount.create(tenantId, "member-1");
        account.earn(100, "Achat", "ref-1");

        assertThatThrownBy(() -> account.spend(200, "Rédemption", "reward-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient points");
    }

    @Test
    void pointsAccount_canSpend_returnsCorrectly() {
        PointsAccount account = PointsAccount.create(tenantId, "member-1");
        account.earn(500, "Achat", "ref-1");

        assertThat(account.canSpend(500)).isTrue();
        assertThat(account.canSpend(501)).isFalse();
    }

    @Test
    void pointsAccount_expire_reducesBalance() {
        PointsAccount account = PointsAccount.create(tenantId, "member-1");
        account.earn(300, "Achat", "ref-1");
        PointsTransaction tx = account.expire(100);

        assertThat(account.getAvailablePoints()).isEqualTo(200);
        assertThat(tx.getType()).isEqualTo(PointsTransactionType.EXPIRE);
    }

    @Test
    void pointsAccount_multiplierApplied_viaEarnPointsHandler() {
        // Le multiplicateur est appliqué dans EarnPointsHandler, pas dans PointsAccount directement.
        // Ce test vérifie que PointsAccount.earn() accepte n'importe quelle valeur positive.
        PointsAccount account = PointsAccount.create(tenantId, "member-1");
        long effectivePoints = Math.round(100 * 1.5); // Silver multiplier
        account.earn(effectivePoints, "Achat Silver", "ref-1");

        assertThat(account.getAvailablePoints()).isEqualTo(150);
    }
}
