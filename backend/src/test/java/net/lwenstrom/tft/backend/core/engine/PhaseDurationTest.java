package net.lwenstrom.tft.backend.core.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import net.lwenstrom.tft.backend.core.DataLoader;
import net.lwenstrom.tft.backend.core.GameModeProvider;
import net.lwenstrom.tft.backend.core.GameModeRegistry;
import net.lwenstrom.tft.backend.core.model.GameMode;
import net.lwenstrom.tft.backend.core.model.GamePhase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PhaseDurationTest {

    private GameRoom room;

    @BeforeEach
    public void setup() {
        // Mock Dependencies
        GameModeProvider provider = new GameModeProvider() {
            @Override
            public GameMode getMode() {
                return GameMode.ONEPIECE;
            }

            @Override
            public String getUnitsPath() {
                return "";
            }

            @Override
            public String getTraitsPath() {
                return "";
            }

            @Override
            public void registerTraitEffects(TraitManager traitManager) {}
        };

        GameModeRegistry registry = new GameModeRegistry(List.of(provider), "onepiece");
        DataLoader dataLoader = new DataLoader(registry) {
            @Override
            public List<UnitDefinition> getAllUnits() {
                return List.of(new UnitDefinition("u1", "Unit", 1, 100, 10, 10, 1, 1, 1, 1f, 1, List.of(), null));
            }

            @Override
            public UnitDefinition getUnitDefinition(String id) {
                return getAllUnits().get(0);
            }

            @Override
            public GameMode getGameMode() {
                return GameMode.ONEPIECE;
            }
        };

        room = new GameRoom("test-room", dataLoader, registry);
        room.addPlayer("P1");
    }

    @Test
    public void testPhaseDurationScaling() throws Exception {
        // Start Match -> Round 1 Planning
        room.startMatch();
        assertEquals(1, room.getState().round());
        assertEquals(GamePhase.PLANNING, room.getState().phase());
        assertEquals(10000, room.getState().totalPhaseDuration(), "Round 1 Planning should be 10s");

        // Fast forward to Combat
        fastForward();
        room.tick();

        assertEquals(GamePhase.COMBAT, room.getState().phase());
        assertEquals(10000, room.getState().totalPhaseDuration(), "Round 1 Combat should be 10s");

        // Fast forward to Round 2 Planning
        fastForward();
        room.tick();

        assertEquals(2, room.getState().round());
        assertEquals(GamePhase.PLANNING, room.getState().phase());
        assertEquals(12000, room.getState().totalPhaseDuration(), "Round 2 Planning should be 12s");

        // Fast forward to Round 2 Combat
        fastForward();
        room.tick();

        assertEquals(12000, room.getState().totalPhaseDuration(), "Round 2 Combat should be 12s");
    }

    @Test
    public void testLateGameScaling() {
        // Start Match -> Round 1
        room.startMatch();

        // Advance to Round 10
        for (int i = 1; i < 10; i++) {
            // Planning -> Combat
            try {
                fastForward();
            } catch (Exception e) {
            }
            room.tick();
            // Combat -> Planning (Next Round)
            try {
                fastForward();
            } catch (Exception e) {
            }
            room.tick();
        }

        assertEquals(10, room.getState().round());
        assertEquals(GamePhase.PLANNING, room.getState().phase());
        // 10s + (10 - 1) * 2s = 10 + 18 = 28s
        assertEquals(28000, room.getState().totalPhaseDuration(), "Round 10 Planning should be 28s");
    }

    private void fastForward() throws NoSuchFieldException, IllegalAccessException {
        // Reflection to set phaseEndTime to now
        var field = GameRoom.class.getDeclaredField("phaseEndTime");
        field.setAccessible(true);
        field.set(room, System.currentTimeMillis() - 1);
    }
}
