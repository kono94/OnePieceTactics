package net.lwenstrom.tft.backend.core.combat;

import java.util.List;
import net.lwenstrom.tft.backend.core.model.GameUnit;

public interface UnitMover {

    void moveTowards(GameUnit mover, GameUnit target, List<GameUnit> allUnits);
}
