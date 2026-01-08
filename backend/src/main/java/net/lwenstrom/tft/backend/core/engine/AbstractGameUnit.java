package net.lwenstrom.tft.backend.core.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.lwenstrom.tft.backend.core.model.AbilityDefinition;
import net.lwenstrom.tft.backend.core.model.GameItem;
import net.lwenstrom.tft.backend.core.model.GameUnit;

@Getter
@RequiredArgsConstructor
public abstract class AbstractGameUnit implements GameUnit {
    private final String id = UUID.randomUUID().toString();
    private final String name;
    private final int cost;
    private final AbilityDefinition ability;

    @Setter
    private int maxHealth;

    @Setter
    private int maxMana;

    @Setter
    private int attackDamage;

    @Setter
    private int abilityPower;

    @Setter
    private int armor;

    @Setter
    private int magicResist;

    @Setter
    private float attackSpeed;

    private final int range;

    @NonNull
    private final Set<String> traits;

    @Setter
    private int starLevel = 1;

    @Setter
    private int currentHealth;

    @Setter
    private int mana = 0; // Start with 0 mana usually

    @Setter
    private int x = -1;

    @Setter
    private int y = -1;

    private final List<GameItem> items = new ArrayList<>();

    @Override
    public void takeDamage(int amount) {
        // Simple mitigation formula could go here
        this.currentHealth = Math.max(0, this.currentHealth - amount);
    }

    @Override
    public void gainMana(int amount) {
        this.mana = Math.min(maxMana, this.mana + amount);
    }

    @Override
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    private int planningX = -1;
    private int planningY = -1;

    // Saved stats for restoration after combat
    private int savedMaxHealth;
    private float savedAttackSpeed;

    @Override
    public void savePlanningPosition() {
        this.planningX = x;
        this.planningY = y;

        // Save stats
        this.savedMaxHealth = maxHealth;
        this.savedAttackSpeed = attackSpeed;
    }

    @Override
    public void restorePlanningPosition() {
        if (planningX != -1) {
            this.x = planningX;
            this.y = planningY;
        }

        // Restore stats
        if (savedMaxHealth > 0) {
            this.maxHealth = savedMaxHealth;
            // Fully heal after round
            this.currentHealth = this.maxHealth;
        }
        if (savedAttackSpeed > 0) {
            this.attackSpeed = savedAttackSpeed;
        }
    }

    private long nextMoveTime;

    @Override
    public long getNextMoveTime() {
        return nextMoveTime;
    }

    @Override
    public void setNextMoveTime(long time) {
        this.nextMoveTime = time;
    }

    public AbilityDefinition getAbility() {
        return ability;
    }

    @Setter
    private String activeAbility;

    public String getActiveAbility() {
        return activeAbility;
    }
}
