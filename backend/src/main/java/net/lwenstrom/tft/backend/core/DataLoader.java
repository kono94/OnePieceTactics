package net.lwenstrom.tft.backend.core;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
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

    private Map<String, UnitDefinition> unitRegistry;
    private List<TraitMetadata> traitMetadata;

    @PostConstruct
    public void loadData() {
        var provider = gameModeRegistry.getActiveProvider();
        loadUnits(provider.getUnitsPath());
        loadTraits(provider.getTraitsPath());
    }

    private void loadUnits(String path) {
        try {
            var is = getClass().getResourceAsStream(path);
            if (is != null) {
                List<UnitDefinition> units = jsonMapper.readValue(is, new TypeReference<>() {});
                unitRegistry = units.stream().collect(Collectors.toMap(UnitDefinition::id, u -> u));
                log.info("Loaded {} units from {}", unitRegistry.size(), path);
            } else {
                log.error("Could not find units at {}", path);
                unitRegistry = Map.of();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load unit data: " + path, e);
        }
    }

    private void loadTraits(String path) {
        try {
            var is = getClass().getResourceAsStream(path);
            if (is != null) {
                traitMetadata = jsonMapper.readValue(is, new TypeReference<>() {});
                log.info("Loaded {} traits from {}", traitMetadata.size(), path);
            } else {
                log.error("Could not find traits at {}", path);
                traitMetadata = List.of();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load trait data: " + path, e);
        }
    }

    public UnitDefinition getUnitDefinition(String id) {
        return unitRegistry.get(id);
    }

    public List<UnitDefinition> getAllUnits() {
        return List.copyOf(unitRegistry.values());
    }

    public GameMode getGameMode() {
        return gameModeRegistry.getActiveMode();
    }

    public List<TraitMetadata> getTraitMetadata() {
        return traitMetadata;
    }
}
