package net.lwenstrom.tft.backend.core;

import java.util.Optional;
import net.lwenstrom.tft.backend.core.engine.TraitManager;
import net.lwenstrom.tft.backend.core.model.GameMode;

/**
 * Interface for game mode-specific configurations and logic.
 */
public interface GameModeProvider {
    /**
     * @return The GameMode this provider handles.
     */
    GameMode getMode();

    /**
     * @return Resource path to the units JSON for this mode.
     */
    String getUnitsPath();

    /**
     * @return Resource path to the traits JSON for this mode.
     */
    String getTraitsPath();

    default String getAugmentsPath() {
        return "/data/augments_" + getMode().getValue() + ".json";
    }

    default Optional<String> getAffinitiesPath() {
        return Optional.empty();
    }

    /**
     * Registers mode-specific trait effects into the TraitManager.
     *
     * @param traitManager The manager to register effects into.
     */
    void registerTraitEffects(TraitManager traitManager);
}
