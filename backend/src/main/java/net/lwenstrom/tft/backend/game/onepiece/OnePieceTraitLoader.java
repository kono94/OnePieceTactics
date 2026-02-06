package net.lwenstrom.tft.backend.game.onepiece;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import net.lwenstrom.tft.backend.core.engine.GenericTraitApplier;
import net.lwenstrom.tft.backend.core.engine.TraitManager;
import net.lwenstrom.tft.backend.core.model.EffectType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnePieceTraitLoader {
    private static final Logger log = LoggerFactory.getLogger(OnePieceTraitLoader.class);

    /**
     * Loads all One Piece traits from JSON and registers them with the
     * TraitManager.
     * The traits_onepiece.json file is the single source of truth.
     */
    public static void load(TraitManager traitManager) {
        try {
            var objectMapper = new ObjectMapper();
            InputStream is = OnePieceTraitLoader.class.getResourceAsStream("/data/traits_onepiece.json");
            if (is == null) {
                log.error("Could not find traits_onepiece.json");
                return;
            }

            JsonNode traitsArray = objectMapper.readTree(is);
            for (JsonNode traitNode : traitsArray) {
                var traitId = traitNode.get("id").asText();
                var effectTypeStr = traitNode.has("effectType")
                        ? traitNode.get("effectType").asText()
                        : "NONE";
                var effectType = EffectType.valueOf(effectTypeStr);

                List<JsonNode> effects = new ArrayList<>();
                if (traitNode.has("effects")) {
                    for (JsonNode effect : traitNode.get("effects")) {
                        effects.add(effect);
                    }
                }

                var applier = new GenericTraitApplier(traitId, effectType, effects);
                traitManager.registerEffect(traitId, applier);
                log.debug("Registered trait: {} with effectType: {}", traitId, effectType);
            }

            log.info("Loaded {} traits from traits_onepiece.json", traitsArray.size());
        } catch (Exception e) {
            log.error("Failed to load traits from JSON", e);
        }
    }
}
