package net.lwenstrom.tft.backend.core.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.lwenstrom.tft.backend.test.TestHelpers;
import org.junit.jupiter.api.Test;

class GameRoomStateRefreshTest {

    @Test
    void refreshStateReflectsRemovedBoardUnits() {
        var unitDef = TestHelpers.createUnitDef("unit", "Unit", 1, 100, 10);
        var dataLoader = TestHelpers.createMockDataLoader(List.of(unitDef));
        var room = TestHelpers.createTestGameRoom(dataLoader);
        var player = room.addPlayer("Player");
        player.setLevel(2);
        player.addUnitToBoard(unitDef, 0, 0);
        room.refreshState();

        var unitId = player.getBoardUnits().getFirst().getId();
        assertEquals(1, room.getState().players().get(player.getId()).board().size());

        player.sellUnit(unitId, true);
        room.refreshState();

        assertTrue(room.getState().players().get(player.getId()).board().isEmpty());
    }
}
