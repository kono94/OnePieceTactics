package net.lwenstrom.tft.backend.core.grid;

import net.lwenstrom.tft.backend.core.model.GameUnit;
import java.util.Optional;

public class Grid {
    public static final int ROWS = 4; // Standard TFT rows per player
    public static final int COLS = 7; // Standard TFT cols

    // We treat the board as one large grid or two separate grids?
    // Usually one large grid `8x7` for the full battle
    // Rows 0-3 = Player 1, Rows 4-7 = Player 2 (mirrored)

    private final GameUnit[][] board;

    // Hex illusion offset: odd rows are shifted
    // Backend uses square coordinates (r, c)
    // Distance calculation considers the shift

    public Grid() {
        this.board = new GameUnit[ROWS * 2][COLS];
    }

    public void placeUnit(GameUnit unit, int r, int c) {
        if (isValid(r, c) && board[r][c] == null) {
            board[r][c] = unit;
            unit.setPosition(r, c);
        } else {
            throw new IllegalArgumentException("Invalid placement at " + r + "," + c);
        }
    }

    public void removeUnit(GameUnit unit) {
        if (isValid(unit.getX(), unit.getY()) && board[unit.getX()][unit.getY()] == unit) {
            board[unit.getX()][unit.getY()] = null;
        }
    }

    public Optional<GameUnit> getUnitAt(int r, int c) {
        if (isValid(r, c)) {
            return Optional.ofNullable(board[r][c]);
        }
        return Optional.empty();
    }

    public boolean isValid(int r, int c) {
        return r >= 0 && r < ROWS * 2 && c >= 0 && c < COLS;
    }

    // Hex distance calculation
    // Even rows are normal, Odd rows are shifted right by 0.5 visually
    // In backend, we can treat neighbors differently based on row parity
    public int distance(int r1, int c1, int r2, int c2) {
        // Simplified hex distance (Manhattan logic adapted for Hex)
        // Or simplified Euclidean for nearest neighbor logic
        // For standard "neighbor" checks (range 1):
        // Up/Down is always neighbor
        // Left/Right is always neighbor
        // Diagonals depend on shift

        // This is a placeholder for the actual hex distance formula
        int du = r2 - r1;
        int dv = (c2 + r2 / 2) - (c1 + r1 / 2);
        // Using axial coordinates conversion might be cleaner if we wanted full hex
        // logic
        // But for "Square with illusion", Euclidean distance on the visual
        // representation works best for "Search"

        return Math.abs(r1 - r2) + Math.abs(c1 - c2); // Placeholder simple distance
    }
}
