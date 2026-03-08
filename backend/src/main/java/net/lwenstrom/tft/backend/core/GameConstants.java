package net.lwenstrom.tft.backend.core;

public final class GameConstants {

    // Combat
    public static final int MANA_PER_HIT = 10;
    public static final long ABILITY_COOLDOWN_MS = 1000L;
    public static final long COMBAT_PHASE_MS = 25000L;

    // Economy
    public static final int XP_PER_PHASE = 2;
    public static final int XP_BUY_COST = 4;
    public static final int XP_BUY_AMOUNT = 4;
    public static final int REROLL_COST = 2;
    public static final int STARTING_GOLD = 10;
    public static final int BASE_INCOME = 5;
    public static final int MAX_INTEREST = 5;

    // Grid & Units
    public static final int MAX_BENCH_SIZE = 9;
    public static final int SHOP_SIZE = 5;
    public static final int GRID_COLS = 9;
    public static final int PLAYER_ROWS = 3;
    public static final int COMBAT_ROWS = 6;

    // Damage
    public static final int BASE_COMBAT_DAMAGE = 2;

    // Timing
    public static final int TICK_RATE_MS = 100;
    public static final long BASE_PLANNING_DURATION_MS = 15000L;
    public static final long PLANNING_DURATION_INCREMENT_MS = 250L;

    // Bot
    public static final int BOT_STARTING_LEVEL = 2;
    public static final int BOT_MAX_LEVEL = 9;
    public static final int BOT_MAX_UNITS_PER_ROW = 9;

    // Loot Orbs
    public static final int MIN_ORB_COUNT = 2;
    public static final int MAX_ORB_COUNT = 4;
    public static final int ORB_GOLD_CHANCE_PERCENT = 60;
    public static final int MIN_ORB_GOLD = 3;
    public static final int MAX_ORB_GOLD = 8;

    private GameConstants() {}
}
