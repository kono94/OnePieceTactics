# Palworld Tactics 2.0 — Master Implementation Plan

Status: approved implementation blueprint  
Baseline: repository tag `1.8.0`  
Target: `2.0.0`  
Planning snapshot: 2026-07-29  
Mode id: `palworld`

## 1. Purpose

This directory is the implementation contract for adding Palworld as the third lobby-selectable game mode. The work is deliberately split into independently usable handoff documents so an implementation agent can load only the material needed for its phase without inventing game-design decisions.

This planning package creates documentation only. It does not add the mode, modify combat, or add generated images. The implementation itself is commit-worthy and is tracked under the temporary `Version X.X.X` heading in `frontend/src/components/Changelog.vue`; the heading becomes `Version 2.0.0` immediately before the release tag.

## 2. Source of truth and document map

When two documents appear to overlap, use this priority order:

1. [`01_GAME_DESIGN_SPEC.md`](01_GAME_DESIGN_SPEC.md) owns roster, ids, elements, roles, ranges, stats, traits, augments, abilities, and combat semantics.
2. [`03_BACKEND_IMPLEMENTATION_PLAN.md`](03_BACKEND_IMPLEMENTATION_PLAN.md) owns Java/data architecture and migration order.
3. [`05_COMBAT_ANIMATION_PLAN.md`](05_COMBAT_ANIMATION_PLAN.md) owns animation identities, visual signatures, performance, and gallery coverage.
4. [`04_FRONTEND_LOBBY_ASSET_PLAN.md`](04_FRONTEND_LOBBY_ASSET_PLAN.md) owns lobby/meta treatment, icon paths, and the intentionally limited frontend theme scope.
5. [`02_ICON_BATCH_PROMPTS.md`](02_ICON_BATCH_PROMPTS.md) owns the exact image-generation prompts, quadrant order, filenames, and acceptance checks.
6. [`06_BALANCE_QA_RELEASE_PLAN.md`](06_BALANCE_QA_RELEASE_PLAN.md) owns automated validation, simulation gates, manual QA, rollout, and release acceptance.

The documents are specifications, not suggestions. Any implementation-time rebalance must update the canonical values in `01_GAME_DESIGN_SPEC.md` and add an old `=>` new entry to the in-app changelog.

## 3. Locked product decisions

- Add exactly 55 purchasable Pal lines with the same shop-cost distribution as the existing sets: 12/13/11/12/7 at costs 1/2/3/4/5.
- Use 47 pre-1.0 Pals and eight 1.0 additions: Hoodle, Tetroise, Aegidron, Renjishi, Silvance, Dandilord, Shaolong, and Panthalus.
- Use only Palworld's nine canonical elements as traits: Neutral, Fire, Water, Electric, Grass, Ice, Ground, Dark, and Dragon. Do not add class, job, legendary, or partner-skill traits.
- Element traits affect the entire team, matching the established Pokemon mode behavior.
- Derive the offensive element of every basic attack and damaging ability from the Pal's traits using the existing Pokemon-style best-attacker-trait behavior. Traits remain the Pal's defensive elements and team-wide elemental synergies; there is no separate basic-attack or ability element field and no same-type attack bonus.
- Use the Palworld relationship graph with TFT-tuned multipliers: `1.20` for each strong edge and `0.80` for each resisted edge.
- Keep three gameplay roles only: `DAMAGE`, `TANK`, and `SUPPORT`. A unit is considered melee only at range 1; range 2–4 is ranged.
- Give every Pal exactly one root ability. The same ability identity is used at 1/2/3 stars, with its configured values scaling by star. The mode therefore has exactly 55 resolved abilities and 55 attack previews.
- Extend the core with reusable, theme-neutral primitives needed by the roster. Do not hardcode Pal ids in combat logic.
- Add 15 Palworld-flavored augments using the existing augment effect types and established values. No Palworld-only augment engine is required.
- Reskin the public lobby, waiting room, title, and favicon. Do not reskin the board or in-match chrome in 2.0.
- Register explicit attack and ability animation configurations. Reuse low-level effect families, but give most ultimates a distinct composition; all seven 5-cost lines and iconic 4-cost lines require bespoke signatures.
- The user supplies the 14 manually generated 2x2 batches. The implementation workflow cuts, validates, names, and compresses them; it does not silently substitute scraped artwork.

## 4. Scope boundaries

### In scope

- A third `PALWORLD` game-mode provider and its unit, trait, augment, and elemental-affinity data.
- A mode-aware elemental damage resolver that loads the Pokemon and Palworld relationship graphs from data.
- Generic target selection, ordered effect steps, statuses, multi-hit attacks, movement effects, and persistent zones required by the approved abilities.
- Combat event data sufficient to render deterministic keyed effects.
- Theme-aware frontend metadata and asset resolution.
- A Palworld animation gallery and full animation registry.
- Data validation, deterministic unit tests, frontend tests, balance simulations, performance checks, documentation updates, and 2.0 release preparation.

### Explicitly out of scope

- Capturing Pals, Pal Spheres as a battle mechanic, workers, bases, breeding, saddles, mounts, player weapons, hunger, SAN, terrain construction, vertical physics, or open-world systems.
- A player/avatar combat entity. Partner skills can inspire an ability but must be expressed through a Pal unit.
- Per-unit model rigs, skeletal animation, voice lines, or audio.
- Pal fusion/evolution forms. Star progression scales the root ability values while the portrait, Pal identity, and ability identity stay the same.
- Replacing One Piece or Pokemon data, routes, balance, or art.
- Changing REST or STOMP destinations.
- A Palworld board skin, board scenery, shop chrome, trait sidebar chrome, or end screen skin.
- New augment effect types.

## 5. Delivery sequence and hard gates

### Phase 0 — Planning package

Deliver all seven Markdown files in this directory. Cross-check counts, ids, element spellings, root abilities, animation identities, batch quadrants, and acceptance gates.

Exit gate: the consistency checks at the end of this document pass. No runtime behavior changes are included.

### Phase 1 — Generic combat/data foundation

Implement the data contracts and reusable engine primitives before adding Palworld JSON. Route combat through a mode-aware resolver that loads both Pokemon and Palworld relationship graphs without changing Pokemon's existing trait-derived results. Make combat rules room-mode-aware and update them when a host switches modes in the lobby. Isolate delayed effects and zones by combat-pair context.

Exit gate: all existing backend tests pass; Pokemon effectiveness parity tests pass; new generic primitive tests pass; One Piece damage remains untyped/neutral and unchanged.

### Phase 2 — Palworld data and validation

Add the provider plus `units_palworld.json`, `traits_palworld.json`, `augments_palworld.json`, and `affinities_palworld.json`. Encode the canonical roster exactly. Add data-validation tests before balance tuning.

Exit gate: 55 unique line ids, 12/13/11/12/7 cost counts, 23/16/16 role counts, 55 resolved root abilities, trait-derived offensive typing, and 15 augments.

### Phase 3 — Frontend mode and asset pipeline

Replace hardcoded binary mode decisions with a small mode metadata registry. Add `palworld` to frontend types, waiting-room selection, metadata, favicon resolution, trait loading, unit-icon paths, and gallery route. Apply the Palpagos/World Tree palette only to the public lobby and waiting room.

Exit gate: all three modes can be selected, survive room state updates, resolve their own icons and favicon, and leave in-game One Piece/Pokemon visuals unchanged.

### Phase 4 — Portrait production and ingestion

The user generates the 14 grids from `02_ICON_BATCH_PROMPTS.md`. Save untouched source grids under the ignored scratch folder, run the cutter, map quadrants to ids, inspect every 512x512 crop, compress losslessly enough for pixel art, and install 55 unit portraits plus one Pal Sphere favicon.

Exit gate: no placeholder, swapped quadrant, divider, text, watermark, broken path, or portrait ambiguity remains.

### Phase 5 — Combat visuals

Implement the Palworld attack and ultimate registries in `05_COMBAT_ANIMATION_PLAN.md`, along with any shared canvas primitives they require. Drive lookup with `(gameMode, definitionId)` and, where needed, the stable ability identity from the resolved event; never guess from display names. Add the Palworld gallery route and audit normal, crowded, and reduced-motion modes.

Exit gate: all 55 attack previews and all 55 ability previews have explicit configs and the gallery reports no fallback use.

### Phase 6 — Balance, regression, and release

Run focused tests, full backend/frontend suites, million-run balance reports, 100k role reports, manual multiplayer smoke tests, and asset/performance audits. Tune JSON, updating the design spec and changelog for every changed number. Update README and context documents, then finalize the 2.0 changelog heading and tag.

Exit gate: every item in `06_BALANCE_QA_RELEASE_PLAN.md` passes or has an explicitly accepted waiver documented in the release PR.

## 6. Recommended commit slices

Keep migration reviewable and bisectable. A suitable series is:

1. `docs: add Palworld 2.0 implementation blueprint`
2. `refactor: generalize elemental combat rules`
3. `feat: add generic composite ability effects`
4. `feat: add Palworld mode data`
5. `feat: add Palworld lobby theme and asset routing`
6. `assets: add generated Palworld portraits`
7. `feat: add Palworld combat animations`
8. `test: validate and balance Palworld mode`
9. `docs: prepare 2.0.0 release`

Each implementation commit that changes behavior or balance also updates the temporary changelog. Run `mvn spotless:apply` after each backend slice, then run the relevant focused tests before committing.

## 7. Global definition of done

- Creating or joining a room still defaults according to `game.mode`; `GAME_MODE=palworld` is accepted.
- A host can switch among One Piece, Pokemon, and Palworld only in `LOBBY`; all players' shops/owned units/augments reset using the selected provider, and the combat rules object updates too.
- Palworld shops contain only the 55 approved lines and maintain the expected pool/cost behavior.
- All traits, attacks, damage abilities, statuses, targets, movement, zones, and events are backend-authoritative.
- Two simultaneous combat pairs cannot share zones, delayed hits, status targets, or event ids.
- Dual-element defense is deterministic and the same relationship resolver can represent Pokemon and Palworld.
- The frontend never calculates damage, status success, targets, or hit timing.
- Every portrait/fav icon resolves without fallback in production builds.
- Every attack/ability has a stable definition/ability-identity animation configuration and reduced-motion rendering.
- Existing One Piece and Pokemon behavior and assets pass regression tests.
- README, backend/frontend context, changelog, and gallery documentation describe all three modes.
- Production builds complete and the release is tagged `2.0.0` only after all release gates pass.

## 8. Planning-package consistency checklist

Use these checks whenever any plan file changes:

- [ ] Roster contains 55 unique kebab-case ids.
- [ ] Cost counts are 12, 13, 11, 12, 7.
- [ ] Role counts are 23 Damage, 16 Tank, 16 Support.
- [ ] Exactly eight rows carry the `1.0` marker.
- [ ] Every row uses one or two of the nine canonical elements.
- [ ] Every row derives offensive typing from one or two defensive trait elements using the Pokemon-style best-attacker-trait rule.
- [ ] Every row contains exactly one root ability whose values scale across 1/2/3 stars.
- [ ] Total resolved root abilities equal 55 and all are listed in the animation plan.
- [ ] The icon prompt has 14 batches, 56 quadrants, 55 unit ids, and one Pal Sphere favicon.
- [ ] Trait and affinity ids match exactly across backend, frontend, and design docs.
- [ ] No implementation phase requires a Pal-specific conditional in the theme-agnostic engine.

## 9. Research snapshot

Roster recognition and canonical flavor were frozen against these sources on 2026-07-29:

- [Pocketpair global popularity poll results](https://www.pocketpair.jp/en/news/palworld-global-popularity-poll-the-results-are-in/) for memorable fan favorites.
- [Palworld Global Popularity Poll result site](https://palworld-vote.com/en/) for the favorite-Pal ranking.
- [Palworld Wiki element rules](https://palworld.wiki.gg/wiki/Elements) for the nine elements and directional matchup graph.
- [Palworld Wiki status effects](https://palworld.wiki.gg/wiki/Status_Effects) for status flavor and interactions.
- [PalDB](https://paldb.cc/en/Pal) as a versioned secondary reference for Pal elements and active-skill names, especially 1.0 additions.

The 2.0 mode is an auto-battler adaptation, not a numerical copy of Palworld. Canonical names, elements, and visual motifs are preserved; damage, duration, mana, targeting, and trait values are intentionally tuned to this repository's TFT combat model.
