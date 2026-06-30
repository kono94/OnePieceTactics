package net.lwenstrom.tft.backend.core.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import net.lwenstrom.tft.backend.core.model.AugmentEffectType;
import net.lwenstrom.tft.backend.core.model.AugmentOffer;
import net.lwenstrom.tft.backend.core.model.AugmentTier;
import net.lwenstrom.tft.backend.core.model.SelectedAugment;
import net.lwenstrom.tft.backend.test.TestHelpers;
import org.junit.jupiter.api.Test;

class AugmentManagerTest {

    @Test
    void instantRewardsApplyOnSelection() {
        var player = TestHelpers.createTestPlayer("Player");
        var manager = new AugmentManager(TestHelpers.createDefaultAugments(), TestHelpers.createSeededRandomProvider());

        player.setAugmentChoices(List.of(new AugmentOffer(
                "treasure-cache",
                "Treasure Cache",
                "Gain 8 gold.",
                AugmentTier.SILVER,
                AugmentEffectType.GOLD,
                8,
                null)));
        manager.selectAugment(player, "treasure-cache", 3);

        assertEquals(18, player.getGold());
        assertEquals(1, player.getSelectedAugments().size());
        assertEquals(0, player.getAugmentChoices().size());

        player.setAugmentChoices(List.of(new AugmentOffer(
                "training-arc", "Training Arc", "Gain 8 XP.", AugmentTier.GOLD, AugmentEffectType.XP, 8, null)));
        manager.selectAugment(player, "training-arc", 6);

        assertEquals(3, player.getLevel());
        assertEquals(0, player.getXp());
    }

    @Test
    void roundTierMappingUsesPlannedRounds() {
        assertEquals(AugmentTier.SILVER, AugmentManager.tierForRound(2));
        assertEquals(AugmentTier.GOLD, AugmentManager.tierForRound(5));
        assertEquals(AugmentTier.DIAMOND, AugmentManager.tierForRound(10));
        assertNull(AugmentManager.tierForRound(3));
    }

    @Test
    void offersNeverIncludeAlreadySelectedAugments() {
        var player = TestHelpers.createTestPlayer("Player");
        player.addSelectedAugment(selected("ranged-tempo"));
        player.addSelectedAugment(selected("guarded-formation"));
        player.addSelectedAugment(selected("snowball-strike"));
        player.addSelectedAugment(selected("treasure-cache"));
        var manager = new AugmentManager(TestHelpers.createDefaultAugments(), TestHelpers.createSeededRandomProvider());

        var offers = manager.generateOffers(player, AugmentTier.DIAMOND);

        assertEquals(3, offers.size());
        assertEquals(
                0,
                offers.stream()
                        .filter(offer -> player.getSelectedAugments().stream()
                                .anyMatch(selected -> selected.id().equals(offer.id())))
                        .count());
    }

    private SelectedAugment selected(String id) {
        return new SelectedAugment(id, id, id, AugmentTier.SILVER, AugmentEffectType.GOLD, 0, 3, null);
    }
}
