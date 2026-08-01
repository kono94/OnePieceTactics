package net.lwenstrom.tft.backend.core;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.lwenstrom.tft.backend.core.model.GameMode;
import org.springframework.stereotype.Service;

@Service
public class GameModeRegistry {

    private final Map<GameMode, GameModeProvider> providers;
    private final GameMode defaultMode;

    public GameModeRegistry(List<GameModeProvider> providerList) {
        this.providers = providerList.stream().collect(Collectors.toMap(GameModeProvider::getMode, p -> p));
        this.defaultMode = providers.containsKey(GameMode.ONEPIECE)
                ? GameMode.ONEPIECE
                : providers.keySet().stream()
                        .sorted()
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("At least one game mode provider is required."));
    }

    public GameModeProvider getProvider(GameMode mode) {
        GameModeProvider provider = providers.get(mode);
        if (provider == null) {
            throw new IllegalStateException("No provider found for game mode: " + mode);
        }
        return provider;
    }

    public GameMode getDefaultMode() {
        return defaultMode;
    }

    public List<GameMode> getAvailableModes() {
        return providers.keySet().stream().sorted().toList();
    }
}
