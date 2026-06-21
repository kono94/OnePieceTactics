package net.lwenstrom.tft.backend.core;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lwenstrom.tft.backend.core.engine.UnitDefinition;
import net.lwenstrom.tft.backend.core.model.GameMode;
import net.lwenstrom.tft.backend.core.model.TraitMetadata;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataLoader {

    private final GameModeRegistry gameModeRegistry;
    private final JsonMapper jsonMapper;

    private record ModeData(Map<String, UnitDefinition> unitRegistry, List<TraitMetadata> traitMetadata) {}

    private final Map<GameMode, ModeData> modeDataCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void loadData() {
        // Preload default mode for faster startup, other modes are lazy-loaded.
        getModeData(gameModeRegistry.getDefaultMode());
    }

    private ModeData getModeData(GameMode mode) {
        return modeDataCache.computeIfAbsent(mode, this::loadModeData);
    }

    private ModeData loadModeData(GameMode mode) {
        var provider = gameModeRegistry.getProvider(mode);
        var units = loadUnits(provider.getUnitsPath());
        var traits = loadTraits(provider.getTraitsPath());
        return new ModeData(units, traits);
    }

    private Map<String, UnitDefinition> loadUnits(String path) {
        try {
            var is = getClass().getResourceAsStream(path);
            if (is != null) {
                List<UnitDefinition> units = jsonMapper.readValue(is, new TypeReference<>() {});
                var registry = units.stream().collect(Collectors.toMap(UnitDefinition::id, u -> u));
                log.info("Loaded {} units from {}", registry.size(), path);
                return registry;
            } else {
                log.error("Could not find units at {}", path);
                return Map.of();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load unit data: " + path, e);
        }
    }

    private List<TraitMetadata> loadTraits(String path) {
        try {
            var is = getClass().getResourceAsStream(path);
            if (is != null) {
                var metadata = jsonMapper.readValue(is, new TypeReference< List<TraitMetadata>>() {});
                log.info("Loaded {} traits from {}", metadata.size(), path);
                return metadata;
            } else {
                log.error("Could not find traits at {}", path);
                return List.of();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load trait data: " + path, e);
        }
    }

    public UnitDefinition getUnitDefinition(GameMode mode, String id) {
        return getModeData(mode).unitRegistry().get(id);
    }

    public List<UnitDefinition> getAllUnits(GameMode mode) {
        return List.copyOf(getModeData(mode).unitRegistry().values());
    }

    public List<TraitMetadata> getTraitMetadata(GameMode mode) {
        return getModeData(mode).traitMetadata();
    }
}
