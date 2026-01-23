package net.lwenstrom.tft.backend.test;

import java.util.List;
import java.util.Set;
import net.lwenstrom.tft.backend.core.model.AbilityDefinition;
import net.lwenstrom.tft.backend.core.model.GameItem;
import net.lwenstrom.tft.backend.core.model.GameUnit;

public class MockUnit implements GameUnit {
    private String id;
    private String definitionId;
    private String name;
    private int cost = 1;
    private int maxHealth = 100;
    private int currentHealth = 100;
    private int mana = 0;
    private int maxMana = 100;
    private int attackDamage = 10;
    private int abilityPower = 0;
    private int armor = 0;
    private int magicResist = 0;
    private float attackSpeed = 1.0f;
    private int range = 1;
    private Set<String> traits = Set.of();
    private int starLevel = 1;
    private String ownerId;
    private int x = 0;
    private int y = 0;
    private int savedX = 0;
    private int savedY = 0;
    private long nextAttackTime = 0;
    private long nextMoveTime = 0;
    private AbilityDefinition ability;
    private String activeAbility;

    public MockUnit(String id, String ownerId) {
        this.id = id;
        this.definitionId = id;
        this.name = "MockUnit";
        this.ownerId = ownerId;
    }

    public static MockUnit create(String id, String ownerId) {
        return new MockUnit(id, ownerId);
    }

    public MockUnit withPosition(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public MockUnit withHealth(int current, int max) {
        this.currentHealth = current;
        this.maxHealth = max;
        return this;
    }

    public MockUnit withAttackDamage(int damage) {
        this.attackDamage = damage;
        return this;
    }

    public MockUnit withRange(int range) {
        this.range = range;
        return this;
    }

    public MockUnit withMana(int current, int max) {
        this.mana = current;
        this.maxMana = max;
        return this;
    }

    public MockUnit withAbility(AbilityDefinition ability) {
        this.ability = ability;
        return this;
    }

    public MockUnit withName(String name) {
        this.name = name;
        return this;
    }

    public MockUnit withAttackSpeed(float speed) {
        this.attackSpeed = speed;
        return this;
    }

    public MockUnit withTraits(Set<String> traits) {
        this.traits = traits;
        return this;
    }

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
    public int getMaxHealth() {
        return maxHealth;
    }

    @Override
    public int getCurrentHealth() {
        return currentHealth;
    }

    @Override
    public int getMana() {
        return mana;
    }

    @Override
    public int getMaxMana() {
        return maxMana;
    }

    @Override
    public int getAttackDamage() {
        return attackDamage;
    }

    @Override
    public int getAbilityPower() {
        return abilityPower;
    }

    @Override
    public int getArmor() {
        return armor;
    }

    @Override
    public int getMagicResist() {
        return magicResist;
    }

    @Override
    public float getAttackSpeed() {
        return attackSpeed;
    }

    @Override
    public int getRange() {
        return range;
    }

    @Override
    public Set<String> getTraits() {
        return traits;
    }

    @Override
    public List<GameItem> getItems() {
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
        this.currentHealth = Math.max(0, this.currentHealth - amount);
    }

    @Override
    public void gainMana(int amount) {
        this.mana = Math.min(this.maxMana, this.mana + amount);
    }

    @Override
    public int getStarLevel() {
        return starLevel;
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
        return nextAttackTime;
    }

    @Override
    public void setNextAttackTime(long time) {
        this.nextAttackTime = time;
    }

    @Override
    public long getNextMoveTime() {
        return nextMoveTime;
    }

    @Override
    public void setNextMoveTime(long time) {
        this.nextMoveTime = time;
    }

    @Override
    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    @Override
    public void setCurrentHealth(int currentHealth) {
        this.currentHealth = currentHealth;
    }

    @Override
    public void setAttackSpeed(float attackSpeed) {
        this.attackSpeed = attackSpeed;
    }

    @Override
    public void savePlanningPosition() {
        this.savedX = this.x;
        this.savedY = this.y;
    }

    @Override
    public void restorePlanningPosition() {
        this.x = this.savedX;
        this.y = this.savedY;
    }

    @Override
    public AbilityDefinition getAbility() {
        return ability;
    }

    @Override
    public String getActiveAbility() {
        return activeAbility;
    }

    @Override
    public void setActiveAbility(String abilityName) {
        this.activeAbility = abilityName;
    }

    @Override
    public void setMana(int mana) {
        this.mana = mana;
    }

    // Stun/buff fields for combat effects
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
}
