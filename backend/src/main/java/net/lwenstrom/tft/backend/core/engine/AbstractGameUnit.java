package net.lwenstrom.tft.backend.core.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.lwenstrom.tft.backend.core.model.GameItem;
import net.lwenstrom.tft.backend.core.model.GameUnit;

@Getter
@RequiredArgsConstructor
public abstract class AbstractGameUnit implements GameUnit {
    private final String id = UUID.randomUUID().toString();
    private final String name;
    private final int cost;
    private final int maxHealth;
    private final int maxMana;
    private final int attackDamage;
    private final int abilityPower;
    private final int armor;
    private final int magicResist;
    private final float attackSpeed;
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
}
