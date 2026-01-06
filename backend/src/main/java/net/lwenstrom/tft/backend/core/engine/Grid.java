package net.lwenstrom.tft.backend.core.engine;

import java.util.Optional;
import net.lwenstrom.tft.backend.core.model.GameUnit;

public class Grid {
    public static final int ROWS = 4;
    public static final int COLS = 7;

    private final GameUnit[][] board;

    public Grid() {
        this.board = new GameUnit[ROWS][COLS];
    }

    public void placeUnit(GameUnit unit, int x, int y) {
        if (isValid(x, y) && board[y][x] == null) {
            board[y][x] = unit;
            unit.setPosition(x, y);
        } else {
            throw new IllegalArgumentException("Invalid placement at " + x + "," + y);
        }
    }

    public void removeUnit(GameUnit unit) {
        if (isValid(unit.getX(), unit.getY()) && board[unit.getY()][unit.getX()] == unit) {
            board[unit.getY()][unit.getX()] = null;
        }
    }

    public Optional<GameUnit> getUnitAt(int x, int y) {
        if (isValid(x, y)) {
            return Optional.ofNullable(board[y][x]);
        }
        return Optional.empty();
    }

    public boolean isValid(int x, int y) {
        return y >= 0 && y < ROWS && x >= 0 && x < COLS;
    }

    public boolean isEmpty(int x, int y) {
        return isValid(x, y) && board[y][x] == null;
    }

    public int distance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }
}
