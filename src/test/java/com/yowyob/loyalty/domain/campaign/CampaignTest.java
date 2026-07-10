package com.yowyob.loyalty.domain.campaign;

import com.yowyob.loyalty.domain.campaign.exception.CampaignDomainException;
import com.yowyob.loyalty.domain.campaign.model.Campaign;
import com.yowyob.loyalty.domain.campaign.model.CampaignStatus;
import com.yowyob.loyalty.domain.campaign.model.CampaignType;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CampaignTest {

    private static TenantId tenantId() {
        return new TenantId(UUID.randomUUID());
    }

    private static Campaign bonusMultiplierCampaign(BigDecimal multiplier, Instant start, Instant end) {
        return Campaign.create(UUID.randomUUID(), tenantId(), "Double points", "desc",
                CampaignType.BONUS_MULTIPLIER, "PURCHASE", multiplier, 0, start, end);
    }

    private static Campaign flatBonusCampaign(long bonusPoints) {
        return Campaign.create(UUID.randomUUID(), tenantId(), "Flat bonus", "desc",
                CampaignType.FLAT_BONUS, "PURCHASE", null, bonusPoints, Instant.now(), null);
    }

    @Test
    void create_blankName_throws() {
        assertThrows(CampaignDomainException.class, () -> Campaign.create(
                UUID.randomUUID(), tenantId(), "  ", "desc", CampaignType.FLAT_BONUS,
                "PURCHASE", null, 100, Instant.now(), null));
    }

    @Test
    void create_nullStartDate_throws() {
        assertThrows(CampaignDomainException.class, () -> Campaign.create(
                UUID.randomUUID(), tenantId(), "Name", "desc", CampaignType.FLAT_BONUS,
                "PURCHASE", null, 100, null, null));
    }

    @Test
    void create_endBeforeStart_throws() {
        Instant start = Instant.now();
        Instant end = start.minus(1, ChronoUnit.DAYS);
        assertThrows(CampaignDomainException.class, () -> Campaign.create(
                UUID.randomUUID(), tenantId(), "Name", "desc", CampaignType.FLAT_BONUS,
                "PURCHASE", null, 100, start, end));
    }

    @Test
    void create_bonusMultiplierTypeWithMultiplierNotGreaterThanOne_throws() {
        assertThrows(CampaignDomainException.class,
                () -> bonusMultiplierCampaign(BigDecimal.ONE, Instant.now(), null));
        assertThrows(CampaignDomainException.class,
                () -> bonusMultiplierCampaign(new BigDecimal("0.5"), Instant.now(), null));
        assertThrows(CampaignDomainException.class,
                () -> bonusMultiplierCampaign(null, Instant.now(), null));
    }

    @Test
    void create_flatBonusTypeWithNonPositivePoints_throws() {
        assertThrows(CampaignDomainException.class, () -> flatBonusCampaign(0));
        assertThrows(CampaignDomainException.class, () -> flatBonusCampaign(-10));
    }

    @Test
    void create_validBonusMultiplierCampaign_startsInDraft() {
        Campaign campaign = bonusMultiplierCampaign(new BigDecimal("2.0"), Instant.now(), null);
        assertEquals(CampaignStatus.DRAFT, campaign.status());
        assertEquals(new BigDecimal("2.0"), campaign.bonusMultiplier());
    }

    @Test
    void activate_fromDraft_succeeds() {
        Campaign campaign = flatBonusCampaign(100);
        Campaign activated = campaign.activate();
        assertEquals(CampaignStatus.ACTIVE, activated.status());
    }

    @Test
    void activate_terminalCampaign_throws() {
        Campaign completed = flatBonusCampaign(100).activate().complete();
        assertThrows(CampaignDomainException.class, completed::activate);

        Campaign cancelled = flatBonusCampaign(100).cancel();
        assertThrows(CampaignDomainException.class, cancelled::activate);
    }

    @Test
    void pause_fromNonActive_throws() {
        Campaign draft = flatBonusCampaign(100);
        assertThrows(CampaignDomainException.class, draft::pause);
    }

    @Test
    void pause_fromActive_succeeds() {
        Campaign active = flatBonusCampaign(100).activate();
        assertEquals(CampaignStatus.PAUSED, active.pause().status());
    }

    @Test
    void cancel_alreadyTerminal_throws() {
        Campaign completed = flatBonusCampaign(100).activate().complete();
        assertThrows(CampaignDomainException.class, completed::cancel);
    }

    @Test
    void cancel_fromDraftOrActive_succeeds() {
        assertEquals(CampaignStatus.CANCELLED, flatBonusCampaign(100).cancel().status());
        assertEquals(CampaignStatus.CANCELLED, flatBonusCampaign(100).activate().cancel().status());
    }

    @Test
    void complete_hasNoPreconditionAndIsIdempotentInEffect() {
        Campaign draft = flatBonusCampaign(100);
        assertEquals(CampaignStatus.COMPLETED, draft.complete().status());
    }

    @Test
    void isDueForActivation_trueOnlyWhenDraftAndWithinWindow() {
        Instant now = Instant.now();
        Campaign campaign = flatBonusCampaign(100);
        // start date defaults to "now" at creation time in helper -> should already be due
        assertTrue(campaign.isDueForActivation(now.plusSeconds(1)));
        assertFalse(campaign.isDueForActivation(now.minusSeconds(10)));

        Campaign active = campaign.activate();
        assertFalse(active.isDueForActivation(now.plusSeconds(1)), "already-active campaign is never due for (re)activation");
    }

    @Test
    void isDueForActivation_falseWhenPastEndDate() {
        Instant start = Instant.now().minusSeconds(10);
        Instant end = Instant.now().minusSeconds(1);
        Campaign campaign = Campaign.create(UUID.randomUUID(), tenantId(), "Name", "desc",
                CampaignType.FLAT_BONUS, "PURCHASE", null, 10, start, end);
        assertFalse(campaign.isDueForActivation(Instant.now()));
    }

    @Test
    void isDueForCompletion_trueOnlyWhenActiveAndPastEndDate() {
        Instant start = Instant.now().minusSeconds(10);
        Instant end = Instant.now().minusSeconds(1);
        Campaign campaign = Campaign.create(UUID.randomUUID(), tenantId(), "Name", "desc",
                CampaignType.FLAT_BONUS, "PURCHASE", null, 10, start, end);

        assertFalse(campaign.isDueForCompletion(Instant.now()), "draft campaign is never due for completion");
        Campaign active = campaign.activate();
        assertTrue(active.isDueForCompletion(Instant.now()));
    }

    @Test
    void isDueForCompletion_falseWhenNoEndDate() {
        Campaign active = flatBonusCampaign(100).activate();
        assertFalse(active.isDueForCompletion(Instant.now().plus(365, ChronoUnit.DAYS)));
    }

    @Test
    void appliesToEvent_matchesExactTypeOrWildcard() {
        Campaign targeted = flatBonusCampaign(100);
        assertTrue(targeted.appliesToEvent("PURCHASE"));
        assertFalse(targeted.appliesToEvent("REFUND"));

        Campaign wildcard = Campaign.create(UUID.randomUUID(), tenantId(), "Name", "desc",
                CampaignType.FLAT_BONUS, null, null, 10, Instant.now(), null);
        assertTrue(wildcard.appliesToEvent("ANYTHING"));

        Campaign blankTarget = Campaign.create(UUID.randomUUID(), tenantId(), "Name", "desc",
                CampaignType.FLAT_BONUS, "  ", null, 10, Instant.now(), null);
        assertTrue(blankTarget.appliesToEvent("ANYTHING"));
    }

    @Test
    void calculateExtraPoints_bonusMultiplier() {
        Campaign campaign = bonusMultiplierCampaign(new BigDecimal("1.5"), Instant.now(), null);
        assertEquals(50, campaign.calculateExtraPoints(100));
    }

    @Test
    void calculateExtraPoints_flatBonus_ignoresPointsEarned() {
        Campaign campaign = flatBonusCampaign(250);
        assertEquals(250, campaign.calculateExtraPoints(0));
        assertEquals(250, campaign.calculateExtraPoints(999999));
    }

    @Test
    void reconstruct_bypassesValidation() {
        Instant now = Instant.now();
        Campaign reconstructed = Campaign.reconstruct(
                UUID.randomUUID(), tenantId(), "", "desc", CampaignType.BONUS_MULTIPLIER,
                "PURCHASE", null, -5, now, now.minusSeconds(1), CampaignStatus.ACTIVE, now, now);
        assertEquals(CampaignStatus.ACTIVE, reconstructed.status());
    }
}
