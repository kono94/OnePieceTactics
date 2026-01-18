package net.lwenstrom.tft.backend.core.engine;

import static net.lwenstrom.tft.backend.test.TestHelpers.createTestClock;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import net.lwenstrom.tft.backend.core.combat.BfsUnitMover;
import net.lwenstrom.tft.backend.core.combat.UnitMover;
import net.lwenstrom.tft.backend.core.model.GameUnit;
import net.lwenstrom.tft.backend.test.TestClock;
import org.junit.jupiter.api.Test;

public class CombatPathingTest {

    private static class MockUnit implements GameUnit {
        private String id;
        private int x, y;
        private int hp = 100;
        private String ownerId;
        private long nextMoveTime = 0;
        private int range = 1;

        public MockUnit(String id, int x, int y, String ownerId) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.ownerId = ownerId;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getDefinitionId() {
            return id;
        }

        @Override
        public String getName() {
            return "MockUnit";
        }

        @Override
        public int getCost() {
            return 1;
        }

        @Override
        public int getMaxHealth() {
            return 100;
        }

        @Override
        public int getCurrentHealth() {
            return hp;
        }

        @Override
        public int getMana() {
            return 0;
        }

        @Override
        public int getMaxMana() {
            return 100;
        }

        @Override
        public int getAttackDamage() {
            return 10;
        }

        @Override
        public int getAbilityPower() {
            return 0;
        }

        @Override
        public int getArmor() {
            return 0;
        }

        @Override
        public int getMagicResist() {
            return 0;
        }

        @Override
        public float getAttackSpeed() {
            return 1.0f;
        }

        @Override
        public int getRange() {
            return range;
        }

        @Override
        public java.util.Set<String> getTraits() {
            return java.util.Set.of();
        }

        @Override
        public java.util.List<net.lwenstrom.tft.backend.core.model.GameItem> getItems() {
            return List.of();
        }

        @Override
        public int getX() {
            return x;
        }

        @Override
        public int getY() {
            return y;
        }

        @Override
        public void setPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void takeDamage(int amount) {
            this.hp -= amount;
        }

        @Override
        public void gainMana(int amount) {}

        @Override
        public int getStarLevel() {
            return 1;
        }

        @Override
        public String getOwnerId() {
            return ownerId;
        }

        @Override
        public void setOwnerId(String ownerId) {
            this.ownerId = ownerId;
        }

        @Override
        public long getNextAttackTime() {
            return 0;
        }

        @Override
        public void setNextAttackTime(long time) {}

        @Override
        public long getNextMoveTime() {
            return nextMoveTime;
        }

        @Override
        public void setNextMoveTime(long time) {
            this.nextMoveTime = time;
        }

        @Override
        public void savePlanningPosition() {}

        @Override
        public void restorePlanningPosition() {}

        @Override
        public net.lwenstrom.tft.backend.core.model.AbilityDefinition getAbility() {
            return null;
        }

        @Override
        public String getActiveAbility() {
            return null;
        }

        @Override
        public void setActiveAbility(String abilityName) {}

        @Override
        public void setMana(int mana) {}

        @Override
        public void setMaxHealth(int maxHealth) {}

        @Override
        public void setCurrentHealth(int currentHealth) {
            this.hp = currentHealth;
        }

        @Override
        public void setAttackSpeed(float attackSpeed) {}
    }

    @Test
    public void testBlockedPath() {
        GameUnit mover = new MockUnit("mover", 0, 0, "P1");
        GameUnit blocker = new MockUnit("blocker", 0, 1, "P1");
        GameUnit target = new MockUnit("target", 0, 3, "P2");

        List<GameUnit> allUnits = new ArrayList<>(List.of(mover, blocker, target));

        TestClock clock = createTestClock();
        UnitMover unitMover = new BfsUnitMover(clock);

        // Step 1: Should move sideways to avoid blocker
        unitMover.moveTowards(mover, target, allUnits);
        assertEquals(1, mover.getX(), "Should move sideways to avoid blocker");
        assertEquals(0, mover.getY(), "Should stay on Y=0 line");

        // Step 2: Advance clock and move again
        clock.advance(1000);
        unitMover.moveTowards(mover, target, allUnits);
        assertEquals(1, mover.getX());
        assertEquals(1, mover.getY());

        // Step 3
        clock.advance(1000);
        unitMover.moveTowards(mover, target, allUnits);
        assertEquals(1, mover.getX());
        assertEquals(2, mover.getY());

        // Step 4: Should reach adjacent to target
        clock.advance(1000);
        unitMover.moveTowards(mover, target, allUnits);

        assertTrue(
                (mover.getX() == 0 && mover.getY() == 2) || (mover.getX() == 1 && mover.getY() == 3),
                "Should reach an adjacent tile to target");
    }
}
