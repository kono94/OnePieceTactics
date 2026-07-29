package net.lwenstrom.tft.backend.game.palworld;

import java.util.ArrayList;
import java.util.List;
import net.lwenstrom.tft.backend.core.engine.GenericTraitApplier;
import net.lwenstrom.tft.backend.core.engine.TraitManager;
import net.lwenstrom.tft.backend.core.model.EffectType;
import net.lwenstrom.tft.backend.core.model.TraitTargetScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

public final class PalworldTraitLoader {
    private static final Logger log = LoggerFactory.getLogger(PalworldTraitLoader.class);

    private PalworldTraitLoader() {}

    public static void load(TraitManager traitManager, JsonMapper jsonMapper) {
        try (var inputStream = PalworldTraitLoader.class.getResourceAsStream("/data/traits_palworld.json")) {
            if (inputStream == null) {
                log.error("Could not find traits_palworld.json");
                return;
            }

            var traits = jsonMapper.readTree(inputStream);
            for (var trait : traits) {
                var traitId = trait.get("id").asString();
                var effectType = EffectType.valueOf(trait.get("effectType").asString());
                var targetScope =
                        TraitTargetScope.valueOf(trait.get("targetScope").asString());
                List<JsonNode> effects = new ArrayList<>();
                if (trait.has("effects")) {
                    trait.get("effects").forEach(effects::add);
                }
                traitManager.registerEffect(
                        traitId, new GenericTraitApplier(traitId, effectType, targetScope, effects));
            }
            log.info("Loaded {} traits from traits_palworld.json", traits.size());
        } catch (Exception e) {
            log.error("Failed to load Palworld traits from JSON", e);
        }
    }
}
