package net.lwenstrom.tft.backend.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class GameModeTest {

    @Test
    void parsesSupportedModesCaseInsensitivelyAndTrimsWhitespace() {
        assertEquals(GameMode.ONEPIECE, GameMode.fromString(" onepiece "));
        assertEquals(GameMode.POKEMON, GameMode.fromString("POKEMON"));
    }

    @Test
    void rejectsUnsupportedModesInsteadOfFallingBack() {
        assertThrows(IllegalArgumentException.class, () -> GameMode.fromString("legacy-mode"));
        assertThrows(IllegalArgumentException.class, () -> GameMode.fromString(""));
        assertThrows(IllegalArgumentException.class, () -> GameMode.fromString(null));
    }
}
