package net.lwenstrom.tft.backend.core.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.lwenstrom.tft.backend.core.model.AugmentDefinition;
import net.lwenstrom.tft.backend.core.model.AugmentOffer;
import net.lwenstrom.tft.backend.core.model.AugmentTier;
import net.lwenstrom.tft.backend.core.model.GameUnit;
import net.lwenstrom.tft.backend.core.model.SelectedAugment;
import net.lwenstrom.tft.backend.core.random.RandomProvider;

public class AugmentManager {
    private static final int OFFER_COUNT = 3;

    private final List<AugmentDefinition> definitions;
    private final RandomProvider randomProvider;

    public AugmentManager(List<AugmentDefinition> definitions, RandomProvider randomProvider) {
        this.definitions = definitions;
        this.randomProvider = randomProvider;
    }

    public static AugmentTier tierForRound(int round) {
        return switch (round) {
            case 2 -> AugmentTier.SILVER;
            case 5 -> AugmentTier.GOLD;
            case 10 -> AugmentTier.DIAMOND;
            default -> null;
        };
    }

    public List<AugmentOffer> generateOffers(Player player, AugmentTier tier) {
        var selectedIds =
                player.getSelectedAugments().stream().map(SelectedAugment::id).toList();
        var available = definitions.stream()
                .filter(definition -> !selectedIds.contains(definition.id()))
                .toList();

        var candidates = new ArrayList<>(available);
        randomProvider.shuffle(candidates);

        return candidates.stream()
                .limit(OFFER_COUNT)
                .map(definition -> toOffer(definition, tier))
                .toList();
    }

    public boolean selectAugment(Player player, String augmentId, long round) {
        var selectedOffer = player.getAugmentChoices().stream()
                .filter(offer -> offer.id().equals(augmentId))
                .findFirst()
                .orElse(null);
        if (selectedOffer == null) {
            return false;
        }

        var selected = new SelectedAugment(
                selectedOffer.id(),
                selectedOffer.name(),
                selectedOffer.description(),
                selectedOffer.tier(),
                selectedOffer.effectType(),
                selectedOffer.value(),
                round,
                selectedOffer.image());
        player.addSelectedAugment(selected);
        player.clearAugmentChoices();
        applyInstantReward(player, selected);
        return true;
    }

    public void applyCombatEffects(Collection<Player> players) {
        players.forEach(player -> player.getSelectedAugments().forEach(augment -> applyCombatEffect(player, augment)));
    }

    private AugmentOffer toOffer(AugmentDefinition definition, AugmentTier tier) {
        var tierIndex = tier.ordinal();
        return new AugmentOffer(
                definition.id(),
                definition.name(),
                definition.descriptions().get(tierIndex),
                tier,
                definition.effectType(),
                definition.values().get(tierIndex),
                definition.image());
    }

    private void applyInstantReward(Player player, SelectedAugment augment) {
        switch (augment.effectType()) {
            case GOLD -> player.gainGold(augment.value());
            case XP -> player.gainXp(augment.value());
            case GOLD_PER_EMPTY_BENCH_SLOT ->
                player.gainGold(augment.value()
                        * (int) player.getBenchSlots().toList().stream()
                                .filter(unit -> unit == null)
                                .count());
            case TEAM_ATTACK_SPEED_PER_RANGED_UNIT,
                    TEAM_DAMAGE_REDUCTION,
                    TEAM_ATTACK_DAMAGE_ON_KILL,
                    TEAM_MAX_HEALTH,
                    TEAM_ATTACK_DAMAGE,
                    TEAM_ABILITY_POWER,
                    TEAM_ARMOR_AND_MAGIC_RESIST,
                    MELEE_LIFESTEAL,
                    RANGED_ATTACK_DAMAGE,
                    TEAM_MANA_GAIN,
                    TEAM_STARTING_MANA,
                    TEAM_STARTING_SHIELD -> {}
        }
    }

    private void applyCombatEffect(Player player, SelectedAugment augment) {
        switch (augment.effectType()) {
            case TEAM_ATTACK_SPEED_PER_RANGED_UNIT -> applyRangedAttackSpeed(player, augment.value());
            case TEAM_DAMAGE_REDUCTION ->
                player.getBoardUnits().forEach(unit -> unit.setDamageReduction(augment.value()));
            case TEAM_ATTACK_DAMAGE_ON_KILL ->
                player.getBoardUnits().forEach(unit -> unit.setTeamAttackDamageOnKill(augment.value()));
            case TEAM_MAX_HEALTH ->
                player.getBoardUnits().forEach(unit -> {
                    unit.setMaxHealth(unit.getMaxHealth() + augment.value());
                    unit.setCurrentHealth(unit.getCurrentHealth() + augment.value());
                });
            case TEAM_ATTACK_DAMAGE ->
                player.getBoardUnits().forEach(unit -> unit.setAttackDamage(unit.getAttackDamage() + augment.value()));
            case TEAM_ABILITY_POWER ->
                player.getBoardUnits()
                        .forEach(unit -> unit.setAbilityDamageMultiplier(
                                unit.getAbilityDamageMultiplier() * (1.0f + augment.value() / 100.0f)));
            case TEAM_ARMOR_AND_MAGIC_RESIST ->
                player.getBoardUnits().forEach(unit -> {
                    unit.setArmor(unit.getArmor() + augment.value());
                    unit.setMagicResist(unit.getMagicResist() + augment.value());
                });
            case MELEE_LIFESTEAL ->
                player.getBoardUnits().stream()
                        .filter(unit -> unit.getRange() <= 1)
                        .forEach(unit -> unit.setLifesteal(unit.getLifesteal() + augment.value() / 100.0f));
            case RANGED_ATTACK_DAMAGE ->
                player.getBoardUnits().stream()
                        .filter(unit -> unit.getRange() > 1)
                        .forEach(unit -> unit.setAttackDamage(unit.getAttackDamage() + augment.value()));
            case TEAM_MANA_GAIN ->
                player.getBoardUnits()
                        .forEach(unit -> unit.setManaGainMultiplier(
                                unit.getManaGainMultiplier() * (1.0f + augment.value() / 100.0f)));
            case TEAM_STARTING_MANA ->
                player.getBoardUnits()
                        .forEach(unit -> unit.setMana(Math.min(unit.getMaxMana(), unit.getMana() + augment.value())));
            case TEAM_STARTING_SHIELD ->
                player.getBoardUnits().forEach(unit -> unit.setShield(unit.getShield() + augment.value()));
            case GOLD, XP, GOLD_PER_EMPTY_BENCH_SLOT -> {}
        }
    }

    private void applyRangedAttackSpeed(Player player, int percentPerRangedUnit) {
        var rangedUnits = player.getBoardUnits().stream()
                .filter(unit -> unit.getRange() > 1)
                .count();
        if (rangedUnits == 0) {
            return;
        }

        var multiplier = 1.0f + (percentPerRangedUnit * rangedUnits / 100.0f);
        player.getBoardUnits().forEach(unit -> unit.setAttackSpeed(unit.getAttackSpeed() * multiplier));
    }

    public static void applyTeamAttackDamageOnKill(GameUnit source, List<GameUnit> allUnits) {
        var bonus = source.getTeamAttackDamageOnKill();
        if (bonus <= 0) {
            return;
        }

        allUnits.stream()
                .filter(unit -> unit.getCurrentHealth() > 0)
                .filter(unit -> unit.getOwnerId() != null && unit.getOwnerId().equals(source.getOwnerId()))
                .forEach(unit -> unit.setAttackDamage(unit.getAttackDamage() + bonus));
    }
}
