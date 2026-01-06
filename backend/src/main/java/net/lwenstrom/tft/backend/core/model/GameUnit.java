package net.lwenstrom.tft.backend.core.model;

import java.util.List;
import java.util.Set;

public interface GameUnit {
    String getId();

    String getName();

    int getCost();

    int getMaxHealth();

    int getCurrentHealth();

    int getMana();

    int getMaxMana();

    int getAttackDamage();

    int getAbilityPower();

    int getArmor();

    int getMagicResist();

    float getAttackSpeed();

    int getRange();

    Set<String> getTraits();

    List<GameItem> getItems();

    // Position on grid (x, y)
    int getX();

    int getY();

    void setPosition(int x, int y);

    void takeDamage(int amount);

    void gainMana(int amount);

    int getStarLevel();

    String getOwnerId();

    void setOwnerId(String ownerId);

    long getNextAttackTime();

    void setNextAttackTime(long time);
}
