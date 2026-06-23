package net.lwenstrom.tft.backend.api;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import net.lwenstrom.tft.backend.core.GameModeRegistry;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Allow frontend to call
@RequiredArgsConstructor
public class InfoController {

    private final GameModeRegistry gameModeRegistry;

    @GetMapping("/config")
    public Map<String, Object> getConfig() {
        return Map.of(
                "defaultGameMode",
                gameModeRegistry.getDefaultMode().getValue(),
                "availableModes",
                gameModeRegistry.getAvailableModes().stream()
                        .map(m -> m.getValue())
                        .toList());
    }
}
