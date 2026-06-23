package net.lwenstrom.tft.backend.core.engine;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.NonNull;
import net.lwenstrom.tft.backend.core.model.AbilityDefinition;
import net.lwenstrom.tft.backend.core.model.DotEffect;
import net.lwenstrom.tft.backend.core.model.GameItem;
import net.lwenstrom.tft.backend.core.model.GameUnit;

public abstract class AbstractGameUnit implements GameUnit {
    private final String id = UUID.randomUUID().toString();
    private final String definitionId;
    private final String lineId;
    private final String name;
    private final int cost;
    private final AbilityDefinition ability;
    private final int range;
    private final Set<String> traits;

    // Combat stats
    private int maxHealth;
    private int maxMana;
    private int attackDamage;
    private int abilityPower;
    private int armor;
    private int magicResist;
    private float attackSpeed;

    // State
    private int starLevel = 1;
    private int currentHealth;
    private int mana = 0;
    private int x = -1;
    private int y = -1;
    private final List<GameItem> items = new ArrayList<>();
    private final List<DotEffect> dotEffects = new ArrayList<>();

    // Combat buffs (reset after combat)
    private float stunSecondsRemaining = 0;
    private float atkBuff = 1.0f;
    private float spdBuff = 1.0f;
    private int shield = 0;

    // Planning position (saved before combat)
    private int planningX = -1;
    private int planningY = -1;
    private int savedMaxHealth;
    private float savedAttackSpeed;

    // Timing
    private long nextMoveTime;
    private long nextAttackTime;

    // Ownership
    private String ownerId;
    private String activeAbility;

    // Trait specific values
    private float abilityDamageMultiplier = 1.0f;
    private float lifesteal = 0.0f;
    private float manaGainMultiplier = 1.0f;
    private float extraAttackChance = 0.0f;
    private float damagePerCell = 0.0f;
    private float healAmplification = 1.0f;
    private boolean hasRevive = false;
    private boolean reviveUsed = false;
    private int goldBonusMin = 0;
    private int goldBonusMax = 0;
    private float asOnCast = 0.0f;
    private int asOnCastDuration = 0;
    private float lowHpDamageBonus = 0.0f;
    private float lowHpDamageThreshold = 0.0f;
    private float lowHpAsBonus = 0.0f;
    private float lowHpAsThreshold = 0.0f;
    private boolean shieldOnDeath = false;

    // Musician state
    private long buffExpirationTime = 0;
    private float activeMusicianBuff = 0.0f;

    @Override
    public float getSpdBuff() {
        return spdBuff + activeMusicianBuff;
    }

    public void updateBuffs(long currentTime) {
        if (buffExpirationTime > 0 && currentTime >= buffExpirationTime) {
            activeMusicianBuff = 0;
            buffExpirationTime = 0;
        }
    }

    public void applyTemporaryAsBuff(float as, int durationSeconds, long currentTime) {
        this.activeMusicianBuff = as;
        this.buffExpirationTime = currentTime + (durationSeconds * 1000L);
    }

    public AbstractGameUnit(
            String definitionId,
            String lineId,
            String name,
            int cost,
            AbilityDefinition ability,
            int range,
            Set<String> traits) {
        this.definitionId = definitionId;
        this.lineId = lineId == null || lineId.isBlank() ? definitionId : lineId;
        this.name = name;
        this.cost = cost;
        this.ability = ability;
        this.range = range;
        this.traits = traits;
    }

    protected AbstractGameUnit(AbstractGameUnit other) {
        // Immutable fields - reference copy is safe
        this.definitionId = other.definitionId;
        this.lineId = other.lineId;
        this.name = other.name;
        this.cost = other.cost;
        this.ability = other.ability;
        this.range = other.range;
        this.traits = new HashSet<>(other.traits);

        // Combat stats
        this.maxHealth = other.maxHealth;
        this.maxMana = other.maxMana;
        this.attackDamage = other.attackDamage;
        this.abilityPower = other.abilityPower;
        this.armor = other.armor;
        this.magicResist = other.magicResist;
        this.attackSpeed = other.attackSpeed;

        // State
        this.starLevel = other.starLevel;
        this.currentHealth = other.currentHealth;
        this.mana = other.mana;
        this.x = other.x;
        this.y = other.y;
        this.dotEffects.addAll(other.dotEffects);
        // Items are not cloned for now (ghosts don't need them)

        // Combat buffs
        this.stunSecondsRemaining = other.stunSecondsRemaining;
        this.atkBuff = other.atkBuff;
        this.spdBuff = other.spdBuff;

        // Planning position
        this.planningX = other.planningX;
        this.planningY = other.planningY;
        this.savedMaxHealth = other.savedMaxHealth;
        this.savedAttackSpeed = other.savedAttackSpeed;

        // Timing
        this.nextMoveTime = other.nextMoveTime;
        this.nextAttackTime = other.nextAttackTime;

        this.ownerId = other.ownerId;
        this.activeAbility = other.activeAbility;
        this.buffExpirationTime = other.buffExpirationTime;
        this.activeMusicianBuff = other.activeMusicianBuff;
        this.shieldOnDeath = other.shieldOnDeath;
        this.shield = other.shield;
    }

    // ========== GETTERS ==========

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDefinitionId() {
        return definitionId;
    }

    @Override
    public String getLineId() {
        return lineId;
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
    @NonNull
    public Set<String> getTraits() {
        return traits;
    }

    @Override
    public int getStarLevel() {
        return starLevel;
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
    @JsonIgnore
    public List<DotEffect> getDotEffects() {
        return dotEffects;
    }

    @Override
    public void addDotEffect(DotEffect effect) {
        dotEffects.add(effect);
    }

    @Override
    public float getStunSecondsRemaining() {
        return stunSecondsRemaining;
    }

    @Override
    public float getAtkBuff() {
        return atkBuff;
    }

    @Override
    public long getNextMoveTime() {
        return nextMoveTime;
    }

    @Override
    public long getNextAttackTime() {
        return nextAttackTime;
    }

    @Override
    public String getOwnerId() {
        return ownerId;
    }

    @Override
    public String getActiveAbility() {
        return activeAbility;
    }

    // ========== SETTERS ==========

    @Override
    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    @Override
    public void setMaxMana(int maxMana) {
        this.maxMana = maxMana;
    }

    @Override
    public void setAttackDamage(int attackDamage) {
        this.attackDamage = attackDamage;
    }

    @Override
    public void setAbilityPower(int abilityPower) {
        this.abilityPower = abilityPower;
    }

    @Override
    public void setArmor(int armor) {
        this.armor = armor;
    }

    @Override
    public void setMagicResist(int magicResist) {
        this.magicResist = magicResist;
    }

    @Override
    public void setAttackSpeed(float attackSpeed) {
        this.attackSpeed = attackSpeed;
    }

    @Override
    public void setStarLevel(int starLevel) {
        this.starLevel = starLevel;
    }

    @Override
    public void setCurrentHealth(int currentHealth) {
        this.currentHealth = currentHealth;
    }

    @Override
    public void setMana(int mana) {
        this.mana = mana;
    }

    @Override
    public void setStunSecondsRemaining(float seconds) {
        this.stunSecondsRemaining = seconds;
    }

    @Override
    public void setAtkBuff(float buff) {
        this.atkBuff = buff;
    }

    @Override
    public void setSpdBuff(float buff) {
        this.spdBuff = buff;
    }

    @Override
    public void setNextMoveTime(long time) {
        this.nextMoveTime = time;
    }

    @Override
    public void setNextAttackTime(long time) {
        this.nextAttackTime = time;
    }

    @Override
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    @Override
    public void setActiveAbility(String abilityName) {
        this.activeAbility = abilityName;
    }

    // ========== ACTIONS ==========

    @Override
    public void takeDamage(int amount) {
        if (shield > 0) {
            if (amount <= shield) {
                shield -= amount;
                return;
            } else {
                amount -= shield;
                shield = 0;
            }
        }
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
        // Reset combat buffs
        this.stunSecondsRemaining = 0;
        this.atkBuff = 1.0f;
        this.spdBuff = 1.0f;
        this.dotEffects.clear();

        // Reset trait values
        this.abilityDamageMultiplier = 1.0f;
        this.lifesteal = 0.0f;
        this.manaGainMultiplier = 1.0f;
        this.extraAttackChance = 0.0f;
        this.damagePerCell = 0.0f;
        this.healAmplification = 1.0f;
        this.hasRevive = false;
        this.reviveUsed = false;
        this.goldBonusMin = 0;
        this.goldBonusMax = 0;
        this.asOnCast = 0.0f;
        this.asOnCastDuration = 0;
        this.lowHpDamageBonus = 0.0f;
        this.lowHpDamageThreshold = 0.0f;
        this.lowHpAsBonus = 0.0f;
        this.lowHpAsThreshold = 0.0f;
        this.shieldOnDeath = false;
        this.shield = 0;
    }

    @Override
    public float getAbilityDamageMultiplier() {
        return abilityDamageMultiplier;
    }

    @Override
    public void setAbilityDamageMultiplier(float multiplier) {
        this.abilityDamageMultiplier = multiplier;
    }

    @Override
    public float getLifesteal() {
        return lifesteal;
    }

    @Override
    public void setLifesteal(float lifesteal) {
        this.lifesteal = lifesteal;
    }

    @Override
    public float getManaGainMultiplier() {
        return manaGainMultiplier;
    }

    @Override
    public void setManaGainMultiplier(float multiplier) {
        this.manaGainMultiplier = multiplier;
    }

    @Override
    public float getExtraAttackChance() {
        return extraAttackChance;
    }

    @Override
    public void setExtraAttackChance(float chance) {
        this.extraAttackChance = chance;
    }

    @Override
    public float getDamagePerCell() {
        return damagePerCell;
    }

    @Override
    public void setDamagePerCell(float damage) {
        this.damagePerCell = damage;
    }

    @Override
    public float getHealAmplification() {
        return healAmplification;
    }

    @Override
    public void setHealAmplification(float amp) {
        this.healAmplification = amp;
    }

    @Override
    public boolean hasRevive() {
        return hasRevive;
    }

    @Override
    public void setHasRevive(boolean hasRevive) {
        this.hasRevive = hasRevive;
    }

    @Override
    public boolean isReviveUsed() {
        return reviveUsed;
    }

    @Override
    public void setReviveUsed(boolean used) {
        this.reviveUsed = used;
    }

    @Override
    public int getGoldBonusMin() {
        return goldBonusMin;
    }

    @Override
    public void setGoldBonusMin(int min) {
        this.goldBonusMin = min;
    }

    @Override
    public int getGoldBonusMax() {
        return goldBonusMax;
    }

    @Override
    public void setGoldBonusMax(int max) {
        this.goldBonusMax = max;
    }

    @Override
    public float getAsOnCast() {
        return asOnCast;
    }

    @Override
    public void setAsOnCast(float as) {
        this.asOnCast = as;
    }

    @Override
    public int getAsOnCastDuration() {
        return asOnCastDuration;
    }

    @Override
    public void setAsOnCastDuration(int duration) {
        this.asOnCastDuration = duration;
    }

    @Override
    public float getLowHpDamageBonus() {
        return lowHpDamageBonus;
    }

    @Override
    public void setLowHpDamageBonus(float bonus) {
        this.lowHpDamageBonus = bonus;
    }

    @Override
    public float getLowHpDamageThreshold() {
        return lowHpDamageThreshold;
    }

    @Override
    public void setLowHpDamageThreshold(float threshold) {
        this.lowHpDamageThreshold = threshold;
    }

    @Override
    public float getLowHpAsBonus() {
        return lowHpAsBonus;
    }

    @Override
    public void setLowHpAsBonus(float bonus) {
        this.lowHpAsBonus = bonus;
    }

    @Override
    public float getLowHpAsThreshold() {
        return lowHpAsThreshold;
    }

    @Override
    public void setLowHpAsThreshold(float threshold) {
        this.lowHpAsThreshold = threshold;
    }

    @Override
    public boolean hasShieldOnDeath() {
        return shieldOnDeath;
    }

    @Override
    public void setShieldOnDeath(boolean hasShield) {
        this.shieldOnDeath = hasShield;
    }

    @Override
    public int getShield() {
        return shield;
    }

    @Override
    public void setShield(int amount) {
        this.shield = amount;
    }
}
