package net.lwenstrom.tft.backend.core;

import java.util.Optional;
import net.lwenstrom.tft.backend.core.engine.BotRosterProfile;
import net.lwenstrom.tft.backend.core.engine.TraitManager;
import net.lwenstrom.tft.backend.core.model.GameMode;

public interface GameModeProvider {
    GameMode getMode();

    String getUnitsPath();

    String getTraitsPath();

    default String getAugmentsPath() {
        return "/data/augments_" + getMode().getValue() + ".json";
    }

    default Optional<String> getAffinitiesPath() {
        return Optional.empty();
    }

    default BotRosterProfile getBotRosterProfile(int round) {
        if (round <= 2) {
            return new BotRosterProfile(GameConstants.BOT_MAX_UNITS_PER_ROW, 0, 0, 0, 5, 1, 5, 0, 5);
        }
        if (round == 3) {
            return new BotRosterProfile(GameConstants.BOT_MAX_UNITS_PER_ROW, 1, 0, 0, 30, 2, 30, 2, 10);
        }
        if (round <= 6) {
            return new BotRosterProfile(GameConstants.BOT_MAX_UNITS_PER_ROW, 1, 0, 0, 40, 4, 35, 3, 18);
        }
        if (round <= 9) {
            return new BotRosterProfile(7, 1, 0, 0, 50, 12, 42, 8, 25);
        }
        if (round <= 14) {
            return new BotRosterProfile(7, 1, 2, 0, 25, 55, 50, 18, 35);
        }
        return new BotRosterProfile(7, 0, 2, 2, 15, 75, 35, 45, 45);
    }

    void registerTraitEffects(TraitManager traitManager);
}
