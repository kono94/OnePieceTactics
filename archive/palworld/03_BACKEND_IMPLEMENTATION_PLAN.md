# Palworld 2.0 — Backend and Data Implementation Plan

This plan turns the canonical design in `01_GAME_DESIGN_SPEC.md` into a theme-agnostic Java implementation. Complete the phases in order. Do not begin by special-casing Pal ids inside `CombatSystem` or `DefaultAbilityCaster`.

## 1. Architectural outcome

The finished backend has three independent mode providers and a reusable combat rules layer:

```text
GameRoom(gameMode)
  -> GameModeRegistry / GameModeProvider
      -> units, traits, augments, affinities
  -> CombatSystem(mode CombatRules)
      -> DamageResolver
      -> CompositeAbilityCaster
      -> CombatContextStore (pair-isolated delayed hits/zones)
```

One Piece continues to have neutral/default damage. Pokemon and Palworld both use data-loaded affinities. The core knows elements as string ids and relationships as data; it never imports a Pokemon or Palworld package.

## 2. Public contract changes

### 2.1 Game mode and provider

Add `PALWORLD("palworld")` to `core/model/GameMode.java`.

Extend `GameModeProvider` with a default affinity resource method so One Piece remains source-compatible:

```java
default Optional<String> getAffinitiesPath() {
    return Optional.empty();
}
```

Pokemon and Palworld providers return `/data/affinities_pokemon.json` and `/data/affinities_palworld.json`. Avoid a method named specifically for types or either franchise.

Create:

- `game/palworld/PalworldGameModeProvider.java`
- `game/palworld/PalworldTraitLoader.java`

The provider returns the three normal resource paths and the affinity path. Trait registration delegates only to `GenericTraitApplier`; no Palworld-specific trait effect is required.

### 2.2 Unit definition

Keep `UnitDefinition` focused on existing unit stats, traits, root ability, and definition id. Do not add `basicElement` or `attackAnimationKey` fields. Keep compatibility constructors or use a Jackson-compatible canonical constructor update so existing tests compile. Runtime combat derives offensive typing from the existing trait list, and frontend animation lookup can use the definition id.

- Palworld validation requires one or two valid trait elements and a root ability for every line.
- Pokemon and Palworld use the same best-attacker-trait resolver; neither mode needs explicit attack or ability element fields.
- One Piece keeps its existing neutral/default behavior when no affinity config is present.
- Animation registries use `(gameMode, definitionId)` and may additionally use a stable resolved ability identity; JSON animation keys are not required.

`UnitFormDefinition` does not need element, portrait, or ability overrides for Palworld. Existing form support remains available to Pokemon and other modes.

### 2.3 Ability definition compatibility strategy

Preserve all existing fields on `AbilityDefinition`. Append these optional fields:

```java
String key;
TargetingDefinition targeting;
List<AbilityEffectDefinition> effects;
```

If `effects` is empty, execute the existing `type/pattern/range/values/modifiers/targetLimit/stunDurationSeconds` path exactly. If `effects` is non-empty, use the composite executor and treat the legacy fields only as tooltip compatibility metadata. Palworld abilities all use the composite path. This avoids a high-risk rewrite of existing unit JSON in the same release.

Add theme-neutral records/enums under `core/model/ability/`:

- `TargetSelectorType`: `CURRENT`, `FARTHEST`, `LOWEST_HEALTH_ALLY`, `CLUSTER`, `SELF`, `ALL_ALLIES`, `NEAREST_ENEMIES`.
- `TargetShape`: `SINGLE`, `LINE`, `CONE`, `RADIUS`, `SELF_RADIUS`, `CHAIN`.
- `AbilityEffectType`: `DAMAGE`, `HEAL`, `SHIELD`, `BUFF_ATTACK_SPEED`, `BUFF_ABILITY_DAMAGE`, `BUFF_DEFENSE`, `DEBUFF_DEFENSE`, `GRANT_MANA`, `APPLY_STATUS`, `CLEANSE_STATUS`, `DASH_TO`, `DASH_LINE`, `KNOCKBACK`, `PULL`, `SELF_DAMAGE`, `ZONE`.
- `EffectRecipient`: `SELECTED_ENEMIES`, `SELECTED_ALLIES`, `CASTER`, `ALL_ALLIES`, `LOWEST_HEALTH_ALLIES`, `ACTUAL_DAMAGE_CASTER_AND_LOWEST_ALLY`.
- `TargetingDefinition(selector, shape, range, maxTargets, includeCaster, chainFalloff)`.
- `AbilityEffectDefinition(type, recipient, values, delayMs, hitCount, intervalMs, durationMs, statusId, statusMagnitude, distance, percent, maxTargets, childEffects)`.

Use lists for star values to match the current schema. Nullable fields are acceptable in the data record, but validation must require exactly the fields used by each effect type. Do not encode an open-ended `Map<String,Object>`.

`ZONE` owns ordered `childEffects` executed per tick/contact. The Bubble Rain contact-or-timeout case needs `trigger: FIRST_CONTACT_OR_EXPIRY`; model that as a small `ZoneTrigger` enum rather than a Panthalus boolean.

### 2.4 Status model

Replace the collection of one-off runtime status fields only as far as needed; do not remove `stunSecondsRemaining` until compatibility tests are migrated. Add:

```java
record ActiveStatus(
        String id,
        String sourceId,
        String sourceOwnerId,
        String resolvedElement,
        double magnitude,
        long appliedAtMs,
        long expiresAtMs,
        long nextTickAtMs,
        int remainingTicks) {}
```

Store active statuses by status id and, for stackable statuses such as Poison, by source id beneath the status id. Expose a read-only flattened `List<ActiveStatusView>` in `GameUnit` state for tooltips/VFX. Never serialize internal scheduler callbacks.

Create `StatusDefinition` and a registry loaded from a generic `statuses_palworld.json`, or encode the eight definitions in `affinities_palworld.json` under a `statuses` section. Prefer the separate file only if `GameModeProvider` gains a generic `getStatusDefinitionsPath`; do not hardcode status semantics by mode. Exact rules come from section 5.2 of the design spec.

Keep `stunSecondsRemaining` as the effective hard-control compatibility projection: `max(stun, freeze)`. At each tick, derive whether move/attack/cast is blocked from active statuses. Combat end clears all statuses.

### 2.5 Elemental rules and damage resolution

Remove production use of `PokemonTypeEffectiveness`. Replace it with:

- `ElementalAffinityConfig`: data record for element ids, default multiplier, strong/resisted multipliers, and relationships.
- `ElementalAffinityLoader`: validates and loads a provider resource.
- `DamageContext`: damage kind, source trait elements, source, target, ability/basic/DOT flags, and existing outgoing multipliers.
- `DamageResolver`: one entry point that calculates outgoing modifiers, affinity, target reductions, DEF, integer rounding, shield/HP application, and an auditable breakdown for tests/logging.
- `CombatRules`: immutable mode-owned affinity config plus mode feature data.

Use a sparse relationship map. For every defender element, resolve one of strong, resisted, or neutral and multiply it. The Palworld file uses `1.20/0.80`; the Pokemon file must reproduce all current `PokemonTypeEffectivenessTest` results exactly. An absent affinity config returns `1.0`.

Migrate every direct damage call—basic, legacy ability, composite ability, and DOT—through `DamageResolver`. Preserve the current order of non-elemental multipliers for legacy modes. Add an optional debug breakdown but do not put it in every `GameState` snapshot.

Load both mode graphs through `ElementalAffinityLoader` and route all callers through `DamageResolver`. Keep `PokemonTypeEffectiveness.java` only as a compatibility facade if existing callers require it; it must delegate to the loaded Pokemon config rather than contain a second hardcoded graph.

### 2.6 Combat event contract

Promote `GameState.CombatEvent` from a name-guessed visual hint to a stable identity record. Keep old fields and append:

```java
long eventId;
String combatContextId;
String castId;
String animationId;
String resolvedElement;
Integer hitIndex;
Integer hitCount;
Integer x;
Integer y;
Long durationMs;
String statusId;
```

Use event types `ATTACK`, `CAST`, `DAMAGE`, `HEAL`, `SHIELD`, `MOVE`, `DEATH`, `STATUS_APPLY`, `STATUS_REMOVE`, `ZONE_START`, `ZONE_END`. A cast emits one `CAST` with the stable ability identity when available; impacts emit `DAMAGE` with the same `castId`, hit index, and target-resolved element. A basic attack emits `ATTACK` plus its definition-id-resolved `DAMAGE`. The frontend may temporarily accept old `SKILL` events during migration, but new Palworld events never rely on `skillName` for lookup.

Generate monotonic event ids per `CombatSystem`, not timestamps alone. Keep timestamps for animation scheduling. Coordinates are grid coordinates at event creation; they make zone and death-target effects render correctly even if the unit moves or disappears before the snapshot reaches the client.

### 2.7 Pair-isolated combat contexts

`GameRoom` currently drives multiple combat pairs through one `CombatSystem`. Persistent zones and delayed hits must never leak across pairs. Add an explicit stable `combatContextId`, preferably a record constructed from round plus sorted participant ids, and pass it to:

- `startCombat(contextId, participants)`
- `simulateTick(contextId, participants)`
- `endCombat(contextId, participants)`

Maintain delayed effects, zones, cast sequence, and pair-local temporary lookup inside `Map<CombatContextId, CombatContextState>`. Damage reporting can remain room-aggregated because runtime unit ids and owner ids are unique, but every queued effect must carry its context. `endCombat` and phase abort remove the context. Add a regression test with two pairs using the same Pal/ability simultaneously.

### 2.8 Lobby mode changes

`GameRoom.setGameMode` currently refreshes trait/augment/player data but the combat service retains its original rules. Centralize mode-owned reconstruction:

```text
configureMode(newMode)
  clear/register trait effects
  create augment manager
  load immutable CombatRules
  rebuild/reset CombatSystem or atomically replace its rules
  reset players for mode
```

Prefer rebuilding an immutable `CombatSystem` while the room is in `LOBBY`; make the field replaceable and preserve injected `Clock`, `RandomProvider`, target selector, mover, and caster/factory. Do not allow a mid-combat rules mutation. Add a test that creates a Pokemon-default room, switches to Palworld, and confirms a Fire→Grass Palworld hit uses 1.20 rather than Pokemon data.

### 2.9 Bot profiles

Replace `if (gameMode == GameMode.POKEMON)` with provider data or a mode-neutral roster profile selector. Palworld should initially use the Pokemon bot roster profile because both sets have 55 lines and star-scaled root abilities. One Piece retains its current profile. Suitable options:

- Add `BotRosterProfileId` to the provider (`DEFAULT`, `CREATURE_SET`), or
- Move the per-round profile method onto a generic provider-owned configuration record.

Do not add `if PALWORLD` alongside the Pokemon branch. Add enum-coverage tests so future modes cannot silently receive an unintended profile.

## 3. Data files

### 3.1 `units_palworld.json`

Create `backend/src/main/resources/data/units_palworld.json` directly from the canonical tables. Requirements:

- Preserve the cost-group order and unit order from the design spec.
- Use exact ids, display names, roles, stat arrays, and trait order. Do not add explicit basic-attack, ability-element, or animation-key fields.
- `lineId` is explicit even though it equals `id`.
- Standard units store their ability at the root.
- Every unit stores exactly one root ability. Its values are three-entry star arrays; there are no ability overrides in `forms`.
- Ability identity is stable across stars and may be used by the animation registry, but no JSON animation key is required.
- Populate legacy `name`, `description`, `type`, `pattern`, `range`, and `values` with a representative primary effect for existing tooltip compatibility, while `effects` remains authoritative.
- Keep every three-number array on one line as required by repository guidelines.

### 3.2 `traits_palworld.json`

Create exactly nine trait objects from section 4.2. Use `type: "element"`, `targetScope: "TEAM"`, four breakpoint entries, canonical colors, and existing `EffectType` values. Descriptions should be concise and theme-correct.

### 3.3 `augments_palworld.json`

Create exactly 15 entries from section 9. Keep `image: null`. Write full tier-specific descriptions rather than slash notation. No new `AugmentEffectType` is needed.

### 3.4 `affinities_palworld.json`

Use this shape:

```json
{
  "defaultMultiplier": 1.0,
  "strongMultiplier": 1.2,
  "resistedMultiplier": 0.8,
  "elements": ["neutral", "fire", "water", "electric", "grass", "ice", "ground", "dark", "dragon"],
  "relationships": [
    { "attacking": "neutral", "strongAgainst": [], "resistedBy": ["dark"] },
    { "attacking": "fire", "strongAgainst": ["grass", "ice"], "resistedBy": ["water"] }
  ]
}
```

Complete the remaining seven rows exactly from the design spec. Validate lowercase unique ids, complete attacking-element coverage, valid referenced elements, positive finite multipliers, and no defender appearing in both lists for one attacker.

### 3.5 Pokemon affinity data

Create `affinities_pokemon.json` by transcribing the existing relationship graph exactly. Preserve Pokemon's existing trait-derived best-attacker behavior for basics, abilities, and damage-over-time effects; do not add explicit basic-element or ability-element fields to Pokemon lines/forms. The migration must be behavior-preserving: `PokemonAffinityParityTest` and the existing Pokemon effectiveness tests are the regression boundary. Do not add Palworld elements to Pokemon traits or change Pokemon's source multipliers as a side effect.

## 4. Composite execution detail

### 4.1 Determinism

- All selector ties sort by distance, x, y, owner id, then runtime unit id.
- Random-looking missile distribution is round-robin over a deterministic ordered target list unless a design row explicitly says random. The approved rows do not require RNG target selection.
- Blind is the only new RNG combat behavior. Use the injected `RandomProvider`; never `Math.random()` or a new `Random`.
- Use the injected `Clock`; never `System.currentTimeMillis()` inside the new executor or scheduler.
- Give each cast a deterministic id based on context and a per-context incrementing sequence.

### 4.2 Scheduling

Do not create background threads or timers. Store `ScheduledEffect(dueAtMs, castId, sequence, ...)` in the pair context and drain due effects at the start of `simulateTick`. Sort by due time then sequence. Zones are state records with next trigger and expiry and are also advanced from the combat tick.

At a 100 ms tick, a due step resolves on the first tick whose time is greater than or equal to `dueAtMs`. Visual events use the actual resolution timestamp. Clear all schedules at combat end.

### 4.3 Shapes and grid behavior

- Reuse the current aimed-line logic but move it to a tested generic shape resolver.
- Implement a 90-degree cone using the dot product from source→target and source→candidate; include candidates at an angle of 45 degrees or less and within Chebyshev range.
- Radius uses Chebyshev distance, consistent with the square-grid surroundings currently used by abilities.
- Cluster selection counts living enemy candidates within the ability radius, then applies deterministic ties.
- Chain chooses the closest unhit enemy within 3 cells of the previous target; end when none exists.
- Forced movement always goes through `Grid` occupancy/bounds helpers and emits a `MOVE` event on success.

### 4.4 Death, revive, mana, and kill credit

Extract the duplicated “final death versus revive” logic so every damage path uses it. Delayed hits retain source attribution even if source died. Team-on-kill augment effects trigger once on the final damage that kills a non-reviving target. DOT/zone kills count for the source.

Direct-hit mana is granted once per target per cast, regardless of hit count, and never for DOT ticks, recoil, or status arcs. The Electrified arc is credited to the unit that dealt the consuming basic attack and can kill, but does not recursively trigger Electrified or on-hit DOT.

## 5. File-by-file implementation checklist

### Core/model

- [ ] Add `PALWORLD` to `GameMode`.
- [ ] Keep `UnitDefinition`, runtime `GameUnit`, `AbstractGameUnit`, `StandardGameUnit`, and frontend-facing serialization free of required basic-element/animation-key fields; expose trait-derived resolved element only where events need it.
- [ ] Extend `AbilityDefinition` compatibly and add typed targeting/effect records.
- [ ] Add affinity, status-view, damage-context, and enriched combat-event records.
- [ ] Preserve record constructors used by tests or update all call sites in the same commit.

### Core services

- [ ] Extend `GameModeProvider`; cache affinities in `DataLoader` or a dedicated loader.
- [ ] Add `DamageResolver` and migrate all damage entry points.
- [ ] Add composite targeting/effect execution while retaining legacy ability execution.
- [ ] Add status update/clear behavior to runtime units.
- [ ] Add pair-local scheduled effects/zones and event ids.
- [ ] Rebuild mode-owned combat rules on lobby switch.
- [ ] Generalize bot roster profile selection.

### Palworld package/resources

- [ ] Add provider and generic trait loader wiring.
- [ ] Add four JSON resources: units, traits, augments, affinities (plus generic statuses file only if chosen consistently).
- [ ] Add Palworld data validation and mode-registry context tests.

### Existing modes

- [ ] Add Pokemon affinity data and prove trait-derived best-attacker parity.
- [ ] Prove Pokemon parity and remove any duplicate hardcoded relationship table, retaining only a compatibility facade if needed.
- [ ] Prove One Piece attacks/abilities produce the same numbers without an affinity config.

### Formatting and documentation

- [ ] Run `mvn spotless:apply` from `backend` after Java changes.
- [ ] Update `README.md` game-mode table and backend context architecture/contracts.
- [ ] Add a `Version X.X.X` changelog entry describing Palworld mode and core mechanics; each later numeric tune uses old `=>` new presentation.

## 6. Backend tests to add

Create focused tests rather than relying only on simulations:

- `PalworldDataValidationTest`: all assertions from design section 11 plus exact cost/role/range/root-ability counts.
- `ElementalAffinityLoaderTest`: malformed/duplicate/unknown elements and multiplier validation.
- `DamageResolverTest`: all nine Palworld rows, neutral cases, dual-element best-attacker selection, One Piece no-config behavior, and trait-derived DOT/ability damage.
- `PokemonAffinityParityTest`: golden results captured from current `PokemonTypeEffectivenessTest` for single and dual types and both basics/abilities.
- `CompositeAbilityCasterTest`: each effect kind, ordering, target cap, line/cone/radius/chain shapes, cluster ties, heal/shield/buff recipients.
- `StatusSystemTest`: refresh/strongest behavior, Poison per-source stacking cap, Burn/Poison ticks, Freeze/stun maximum, Ivy+Fire removal, Soak consumption, Electrified one-hop/no recursion, Blind seeded RNG.
- `AbilityMovementTest`: bounds, blocked cells, roots, dash without destination, pull/knockback, emitted positions.
- `ScheduledEffectTest`: tick quantization, caster death, target reacquisition, cast attribution, combat-end cleanup.
- `CombatContextIsolationTest`: two concurrent pairs cannot share zone hits, delayed hits, cast ids, or statuses.
- `GameRoomPalworldModeTest`: provider discovery, default mode, lobby switching, shop reset, augment reset, combat-rules replacement, illegal in-match switch.
- `PalworldAbilityResolutionTest`: parameterized smoke case for all 55 resolved root abilities; each cast completes without exception and emits its stable ability identity.
- Extend `SimulationTest`/data iteration so enum expansion automatically includes Palworld and failures identify the mode.

Use `TestClock` and `SeededRandomProvider`. Avoid wall-clock waits.

## 7. Backend completion gates

Run in this order:

```bash
cd backend
mvn spotless:apply
mvn -Dtest=ElementalAffinityLoaderTest,DamageResolverTest,PokemonAffinityParityTest test
mvn -Dtest=CompositeAbilityCasterTest,StatusSystemTest,AbilityMovementTest,ScheduledEffectTest,CombatContextIsolationTest test
mvn -Dtest=PalworldDataValidationTest,GameRoomPalworldModeTest,PalworldAbilityResolutionTest test
mvn test
```

Completion requires zero fallback Palworld ability executions, zero mode-specific combat checks in `core`, preserved Pokemon golden outputs, preserved One Piece focused tests, and no scheduled context remaining after combat cleanup.

## 8. Implementation risks and controls

| Risk | Control |
|---|---|
| Appending record fields breaks many constructors | Add compatibility constructors first; compile before behavior changes. |
| Generic resolver silently rebalances Pokemon | Golden parity tests created before deleting old logic. |
| A lobby switch keeps stale combat rules | Rebuild immutable mode services and test Pokemon→Palworld switch. |
| Zones from one matchup hit another | Explicit context id in every scheduled/zone record plus two-pair regression test. |
| Multi-hit grants too much mana | Track direct-hit recipient ids per cast. |
| Death logic differs between basic, ability, DOT, and zone | One shared damage/death resolver and parameterized kill-credit tests. |
| Composite JSON becomes an unvalidated mini-language | Typed records/enums plus per-effect validation; no raw maps. |
| Event volume overwhelms snapshots | One cast event, bounded hit events, per-tick caps, and performance tests from the QA plan. |
