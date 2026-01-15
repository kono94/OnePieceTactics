package net.lwenstrom.tft.backend.api;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import net.lwenstrom.tft.backend.core.DataLoader;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Allow frontend to call
@RequiredArgsConstructor
public class InfoController {

    private final DataLoader dataLoader;

    @GetMapping("/config")
    public Map<String, String> getConfig() {
        return Map.of("gameMode", dataLoader.getGameMode());
    }
}
