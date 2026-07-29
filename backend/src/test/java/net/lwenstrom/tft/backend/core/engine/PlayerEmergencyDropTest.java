package net.lwenstrom.tft.backend.core.engine;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.lwenstrom.tft.backend.test.TestHelpers;
import org.junit.jupiter.api.Test;

class PlayerEmergencyDropTest {

    @Test
    void onlySurvivingHumanThresholdCrossingTriggersOnce() {
        var human = TestHelpers.createTestPlayer("Human");
        human.setHealth(20);

        assertTrue(human.triggerEmergencyDropIfEligible(21));

        human.setHealth(15);
        assertFalse(human.triggerEmergencyDropIfEligible(21));
    }

    @Test
    void botsGhostsLethalDamageAndAlreadyLowHealthDoNotTrigger() {
        var bot = TestHelpers.createTestPlayer("Bot");
        bot.setBot(true);
        bot.setHealth(20);
        assertFalse(bot.triggerEmergencyDropIfEligible(21));

        var ghost = TestHelpers.createTestPlayer("Ghost");
        ghost.setGhost(true);
        ghost.setHealth(20);
        assertFalse(ghost.triggerEmergencyDropIfEligible(21));

        var eliminated = TestHelpers.createTestPlayer("Eliminated");
        eliminated.setHealth(0);
        assertFalse(eliminated.triggerEmergencyDropIfEligible(21));

        var alreadyLow = TestHelpers.createTestPlayer("Already Low");
        alreadyLow.setHealth(19);
        assertFalse(alreadyLow.triggerEmergencyDropIfEligible(20));
    }
}
