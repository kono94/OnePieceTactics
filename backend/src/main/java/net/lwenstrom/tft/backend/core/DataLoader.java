package net.lwenstrom.tft.backend.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import net.lwenstrom.tft.backend.core.engine.UnitDefinition;
import net.lwenstrom.tft.backend.core.model.GameMode;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DataLoader {

    private final GameModeRegistry gameModeRegistry;
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    private Map<String, UnitDefinition> unitRegistry;
    private List<Object> traitMetadata;

    @PostConstruct
    public void loadData() {
        GameModeProvider provider = gameModeRegistry.getActiveProvider();
        loadUnits(provider.getUnitsPath());
        loadTraits(provider.getTraitsPath());
    }

    private void loadUnits(String path) {
        try {
            var is = getClass().getResourceAsStream(path);
            if (is != null) {
                List<UnitDefinition> units = jsonMapper.readValue(is, new TypeReference<>() {});
                unitRegistry = units.stream().collect(Collectors.toMap(UnitDefinition::id, u -> u));
                System.out.println("Loaded " + unitRegistry.size() + " units from " + path);
            } else {
                System.err.println("Could not find units at " + path);
                unitRegistry = Map.of();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load unit data: " + path, e);
        }
    }

    private void loadTraits(String path) {
        try {
            var is = getClass().getResourceAsStream(path);
            if (is != null) {
                traitMetadata = jsonMapper.readValue(is, new TypeReference<>() {});
                System.out.println("Loaded trait metadata from " + path);
            } else {
                System.err.println("Could not find traits at " + path);
                traitMetadata = List.of();
            }
        } catch (IOException e) {
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

    public List<Object> getTraitMetadata() {
        return traitMetadata;
    }
}
