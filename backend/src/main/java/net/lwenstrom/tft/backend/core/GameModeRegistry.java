package net.lwenstrom.tft.backend.core;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.lwenstrom.tft.backend.core.model.GameMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GameModeRegistry {

    private final Map<GameMode, GameModeProvider> providers;
    private final GameMode activeMode;

    public GameModeRegistry(List<GameModeProvider> providerList, @Value("${game.mode:onepiece}") String gameModeStr) {
        this.providers = providerList.stream().collect(Collectors.toMap(GameModeProvider::getMode, p -> p));
        this.activeMode = GameMode.fromString(gameModeStr);
    }

    public GameModeProvider getActiveProvider() {
        GameModeProvider provider = providers.get(activeMode);
        if (provider == null) {
            throw new IllegalStateException("No provider found for game mode: " + activeMode);
        }
        return provider;
    }

    public GameMode getActiveMode() {
        return activeMode;
    }
}
