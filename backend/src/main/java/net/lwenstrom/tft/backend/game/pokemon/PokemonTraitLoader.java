package net.lwenstrom.tft.backend.game.pokemon;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import net.lwenstrom.tft.backend.core.engine.GenericTraitApplier;
import net.lwenstrom.tft.backend.core.engine.TraitManager;
import net.lwenstrom.tft.backend.core.model.EffectType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

public class PokemonTraitLoader {
    private static final Logger log = LoggerFactory.getLogger(PokemonTraitLoader.class);

    public static void load(TraitManager traitManager, JsonMapper jsonMapper) {
        try {
            InputStream is = PokemonTraitLoader.class.getResourceAsStream("/data/traits_pokemon.json");
            if (is == null) {
                log.error("Could not find traits_pokemon.json");
                return;
            }

            JsonNode traitsArray = jsonMapper.readTree(is);
            for (JsonNode traitNode : traitsArray) {
                var traitId = traitNode.get("id").asString();
                var effectTypeStr = traitNode.has("effectType")
                        ? traitNode.get("effectType").asString()
                        : "NONE";
                var effectType = EffectType.valueOf(effectTypeStr);

                List<JsonNode> effects = new ArrayList<>();
                if (traitNode.has("effects")) {
                    for (JsonNode effect : traitNode.get("effects")) {
                        effects.add(effect);
                    }
                }

                traitManager.registerEffect(traitId, new GenericTraitApplier(traitId, effectType, effects));
                log.debug("Registered Pokemon trait: {} with effectType: {}", traitId, effectType);
            }

            log.info("Loaded {} traits from traits_pokemon.json", traitsArray.size());
        } catch (Exception e) {
            log.error("Failed to load Pokemon traits from JSON", e);
        }
    }
}
