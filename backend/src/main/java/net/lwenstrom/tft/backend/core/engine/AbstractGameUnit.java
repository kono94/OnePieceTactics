package net.lwenstrom.tft.backend.core.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.NonNull;
import net.lwenstrom.tft.backend.core.model.AbilityDefinition;
import net.lwenstrom.tft.backend.core.model.GameItem;
import net.lwenstrom.tft.backend.core.model.GameUnit;

public abstract class AbstractGameUnit implements GameUnit {
    private final String id = UUID.randomUUID().toString();
    private final String definitionId;
    private final String name;
    private final int cost;
    private final AbilityDefinition ability;

    private int maxHealth;
    private int maxMana;
    private int attackDamage;
    private int abilityPower;
    private int armor;
    private int magicResist;
    private float attackSpeed;
    private final int range;
    private final Set<String> traits;

    public AbstractGameUnit(
            String definitionId, String name, int cost, AbilityDefinition ability, int range, Set<String> traits) {
        this.definitionId = definitionId;
        this.name = name;
        this.cost = cost;
        this.ability = ability;
        this.range = range;
        this.traits = traits;
    }

    private int starLevel = 1;
    private int currentHealth;
    private int mana = 0;
    private int x = -1;
    private int y = -1;
    private final List<GameItem> items = new ArrayList<>();

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDefinitionId() {
        return definitionId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getCost() {
        return cost;
    }

    @Override
    public AbilityDefinition getAbility() {
        return ability;
    }

    @Override
    public int getMaxHealth() {
        return maxHealth;
    }

    @Override
    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    @Override
    public int getMaxMana() {
        return maxMana;
    }

    @Override
    public void setMaxMana(int maxMana) {
        this.maxMana = maxMana;
    }

    @Override
    public int getAttackDamage() {
        return attackDamage;
    }

    @Override
    public void setAttackDamage(int attackDamage) {
        this.attackDamage = attackDamage;
    }

    @Override
    public int getAbilityPower() {
        return abilityPower;
    }

    @Override
    public void setAbilityPower(int abilityPower) {
        this.abilityPower = abilityPower;
    }

    @Override
    public int getArmor() {
        return armor;
    }

    @Override
    public void setArmor(int armor) {
        this.armor = armor;
    }

    @Override
    public int getMagicResist() {
        return magicResist;
    }

    @Override
    public void setMagicResist(int magicResist) {
        this.magicResist = magicResist;
    }

    @Override
    public float getAttackSpeed() {
        return attackSpeed;
    }

    @Override
    public void setAttackSpeed(float attackSpeed) {
        this.attackSpeed = attackSpeed;
    }

    @Override
    public int getRange() {
        return range;
    }

    @Override
    @NonNull
    public Set<String> getTraits() {
        return traits;
    }

    @Override
    public int getStarLevel() {
        return starLevel;
    }

    @Override
    public void setStarLevel(int starLevel) {
        this.starLevel = starLevel;
    }

    @Override
    public int getCurrentHealth() {
        return currentHealth;
    }

    @Override
    public void setCurrentHealth(int currentHealth) {
        this.currentHealth = currentHealth;
    }

    @Override
    public int getMana() {
        return mana;
    }

    @Override
    public void setMana(int mana) {
        this.mana = mana;
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
    public List<GameItem> getItems() {
        return items;
    }

    @Override
    public void takeDamage(int amount) {
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
    private int savedMaxHealth;
    private float savedAttackSpeed;

    private int stunTicksRemaining = 0;
    private float atkBuff = 1.0f;
    private float spdBuff = 1.0f;

    @Override
    public int getStunTicksRemaining() {
        return stunTicksRemaining;
    }

    @Override
    public void setStunTicksRemaining(int ticks) {
        this.stunTicksRemaining = ticks;
    }

    @Override
    public float getAtkBuff() {
        return atkBuff;
    }

    @Override
    public void setAtkBuff(float buff) {
        this.atkBuff = buff;
    }

    @Override
    public float getSpdBuff() {
        return spdBuff;
    }

    @Override
    public void setSpdBuff(float buff) {
        this.spdBuff = buff;
    }

    @Override
    public void savePlanningPosition() {
        this.planningX = x;
        this.planningY = y;
        this.savedMaxHealth = maxHealth;
        this.savedAttackSpeed = attackSpeed;
    }

    @Override
    public void restorePlanningPosition() {
        if (planningX != -1) {
            this.x = planningX;
            this.y = planningY;
        }
        if (savedMaxHealth > 0) {
            this.maxHealth = savedMaxHealth;
            this.currentHealth = this.maxHealth;
        }
        if (savedAttackSpeed > 0) {
            this.attackSpeed = savedAttackSpeed;
        }
        this.stunTicksRemaining = 0;
        this.atkBuff = 1.0f;
        this.spdBuff = 1.0f;
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

    private long nextAttackTime;

    @Override
    public long getNextAttackTime() {
        return nextAttackTime;
    }

    @Override
    public void setNextAttackTime(long time) {
        this.nextAttackTime = time;
    }

    private String ownerId;

    @Override
    public String getOwnerId() {
        return ownerId;
    }

    @Override
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    private String activeAbility;

    @Override
    public String getActiveAbility() {
        return activeAbility;
    }

    @Override
    public void setActiveAbility(String abilityName) {
        this.activeAbility = abilityName;
    }
}
