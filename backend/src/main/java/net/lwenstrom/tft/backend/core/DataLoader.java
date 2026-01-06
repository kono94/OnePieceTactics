package net.lwenstrom.tft.backend.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.lwenstrom.tft.backend.core.engine.UnitDefinition;
import org.springframework.stereotype.Service;

@Service
public class DataLoader {

    private final ObjectMapper objectMapper;
    private Map<String, UnitDefinition> unitRegistry;

    public DataLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void loadData() {
        try {
            // Load units
            var is = getClass().getResourceAsStream("/data/units.json");
            if (is != null) {
                List<UnitDefinition> units = objectMapper.readValue(is, new TypeReference<>() {});
                unitRegistry = units.stream().collect(Collectors.toMap(UnitDefinition::id, u -> u));
                System.out.println("Loaded " + unitRegistry.size() + " units.");
            } else {
                System.err.println("Could not find /data/units.json");
                unitRegistry = Map.of();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load game data", e);
        }
    }

    public UnitDefinition getUnitDefinition(String id) {
        return unitRegistry.get(id);
    }

    public List<UnitDefinition> getAllUnits() {
        return List.copyOf(unitRegistry.values());
    }
}
