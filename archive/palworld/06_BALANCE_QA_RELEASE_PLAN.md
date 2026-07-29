# Palworld 2.0 — Balance, QA, and Release Plan

This is the final acceptance contract. “Implemented” means the mode passes this document, not merely that it loads. Run deterministic focused checks before expensive simulations.

## 1. Test layers and ownership

| Layer | Purpose | Blocking? |
|---|---|:---:|
| Static/data validation | Catch missing ids, arrays, traits, root abilities, counts, and schema mistakes | Yes |
| Focused backend unit tests | Prove resolver, targets, statuses, zones, scheduling, context isolation | Yes |
| Backend regression suite | Protect One Piece/Pokemon/game loop/API behavior | Yes |
| Frontend unit/type/lint/build | Protect typed mode flow, asset lookup, events, gallery, accessibility hooks | Yes |
| Deterministic combat smoke matrix | Exercise all 55 root abilities and 55 attacks without exceptions/fallback | Yes |
| Balance simulation | Tune role/cost/unit/trait outcomes statistically | Yes |
| Manual multiplayer/visual QA | Validate lobby, assets, timing, readability, concurrency, responsive/reduced motion | Yes |
| Production container smoke | Validate packaged resources, proxies, metadata, and runtime config | Yes |

No million-run report should be used to diagnose a schema or deterministic mechanics bug. Fix focused tests first.

## 2. Data acceptance matrix

The Palworld validation test must print a compact summary and assert:

| Dimension | Required result |
|---|---|
| Lines | 55 unique ids/line ids |
| Costs | 1:12, 2:13, 3:11, 4:12, 5:7 |
| Roles | Damage:23, Tank:16, Support:16 |
| Range category | Melee:21, Ranged:34 |
| New 1.0 Pals | Exactly the eight canonical ids |
| Elements | Nine valid ids; every unit has 1–2; no duplicate within a unit |
| Basic attacks | 55 definition-id previews; elements are resolved from traits at runtime |
| Standard root abilities | 39 unique ability identities |
| Additional root abilities | 16 unique ability identities with star-scaled values |
| Total abilities | 55 unique resolved root abilities |
| Traits | Nine, TEAM scope, four increasing breakpoints each |
| Affinities | Complete nine-row graph, exact 1.20/0.80 multipliers |
| Augments | 15 using existing effect types, three values/descriptions each |
| Portrait manifest | 55 expected `_v1.png` names, no extras mistaken for units |

Also validate all effect-specific fields, positive durations/intervals, bounded max targets, known status ids, known element ids, nonempty descriptions, and exact three-number lists kept on one JSON line.

## 3. Focused mechanics scenarios

Each scenario uses `TestClock` and `SeededRandomProvider` and asserts events as well as final HP/state.

### Elemental resolver

- Fire→Grass is 1.20; Fire→Water is 0.80; Fire→Neutral is 1.00.
- Ice→Dragon/Fire is `0.96` before DEF due to `1.20 × 0.80`.
- Dark→Dark/Neutral is 1.20; Neutral→Dark/Dragon is 0.80.
- A dual unit's element order cannot change the product.
- Basic and ability offensive elements are derived from the caster's traits using the best-attacker-trait rule; they cannot differ through configuration.
- Burn/Poison ticks use the resolved caster trait element for the effect; healing/shield/recoil do not use affinities.
- One Piece with no affinity config remains 1.00.
- Every Pokemon golden case matches the 1.8.0 implementation exactly.

### Targets and shapes

- Deterministic ties for current/farthest/lowest/cluster.
- Line cells for horizontal, vertical, diagonal, shallow, and reverse directions.
- Cone boundary at 45 degrees and range boundary.
- Radius uses Chebyshev distance and respects target cap/order.
- Chain never hits a unit twice, respects jump distance, and applies falloff exactly once per jump.
- Multi-shot round-robin assignment remains stable when one target dies between hits.

### Statuses

- Burn strongest-wins refresh and exactly three ticks.
- Poison three-source cap, same-source replacement, independent expiry, and kill credit.
- Freeze/stun use the longer hard-control remainder and block attack/move/cast.
- Ivy blocks voluntary/forced movement and is removed after a Fire hit.
- Muddy attack-speed/path cost expires cleanly.
- Soak modifies Fire/Electric only and Electric consumes it after damage.
- Electrified arc uses post-DEF basic damage, has one secondary target, does not recurse, and is consumed.
- Blind follows seeded 35% misses over a deterministic sequence and never affects abilities.
- Combat end and clone/reset paths remove every active status.

### Scheduling/zones/movement

- Delayed effects resolve on the first eligible 100 ms tick in `(dueAt, sequence)` order.
- Caster death does not cancel emitted hits/zones; target death follows the documented reacquisition rule.
- Zone tick/contact/expiry timing and coordinates are correct.
- Two concurrent pair contexts remain isolated even with identical unit ids/keys and overlapping coordinates.
- Dash, dash-line, pull, and knockback respect bounds, blockers, roots, and no-destination behavior.
- Direct-hit mana occurs once per target per cast; DOT/recoil/arc do not grant victim mana.
- Final death/revive/on-kill behavior is identical across basics, abilities, DOT, zones, and Electrified arcs.

### Canonical root-ability cases

Add named regression cases for Pengullet Aqua Cannon knockback, Tocotoco Megaton Egg stun, Grizzbolt chain mana, Bellanoir Nightmare Ray split healing, Paladius team shields, Necromus execute threshold, Panthalus tracking zones, and Xenolord caster-death persistence.

## 4. Backend execution sequence

### 4.1 Fast feedback on every implementation slice

```bash
cd backend
mvn spotless:apply
mvn -Dtest=PalworldDataValidationTest,DamageResolverTest,CompositeAbilityCasterTest test
```

### 4.2 Full deterministic backend gate

```bash
cd backend
mvn test
```

### 4.3 Balance smoke during tuning

Use 10,000 matchups to catch crashes/outliers quickly:

```bash
cd backend
mvn -Dtest=BalanceSimulationReportTest -Dsimulation.mode=palworld -Dsimulation.report=true -Dsimulation.runs=10000 -Dsimulation.threads=8 test
mvn -Dtest=BalanceSimulationReportTest -Dsimulation.mode=palworld -Dsimulation.report=true -Dsimulation.runs=10000 -Dsimulation.threads=8 -Dsimulation.style=random-stars test
mvn -Dtest=BalanceSimulationReportTest -Dsimulation.mode=palworld -Dsimulation.report=true -Dsimulation.runs=10000 -Dsimulation.threads=8 -Dsimulation.style=random-boards test
```

### 4.4 Final statistical gate

Run all three report styles at one million matchups with fixed seed 42, then the existing role gate at 100,000 matchups per enum mode:

```bash
cd backend
mvn -Dtest=BalanceSimulationReportTest -Dsimulation.mode=palworld -Dsimulation.report=true -Dsimulation.runs=1000000 -Dsimulation.threads=8 -Dsimulation.seed=42 test
mvn -Dtest=BalanceSimulationReportTest -Dsimulation.mode=palworld -Dsimulation.report=true -Dsimulation.runs=1000000 -Dsimulation.threads=8 -Dsimulation.seed=42 -Dsimulation.style=random-stars test
mvn -Dtest=BalanceSimulationReportTest -Dsimulation.mode=palworld -Dsimulation.report=true -Dsimulation.runs=1000000 -Dsimulation.threads=8 -Dsimulation.seed=42 -Dsimulation.style=random-boards test
mvn -Dtest=RoleBalanceSimulationTest -Dsimulation.role-report=true -Dsimulation.runs=100000 -Dsimulation.seed=42 test
```

If the machine has fewer cores, lower `simulation.threads`, not run count. Archive the generated Markdown reports as release artifacts or attach their relevant summaries to the release PR; do not commit large generated report directories unless repository policy changes.

## 5. Balance acceptance gates

### 5.1 Role gate — existing hard assertions

Keep the repository's existing target philosophy unchanged:

| Board size | Required outcome |
|---:|---|
| 3 | Damage-only remains viable: at least 35% win rate |
| 4 | Balanced board at least 55% win rate |
| 5 | Balanced board at least 60% win rate |
| 6 | Balanced board at least 65% win rate |
| 7 | Balanced board at least 65% win rate |
| Aggregate 3–7 | Balanced win rate between 60% and 70% |

These assertions run for every `GameMode`, including Palworld after enum expansion.

### 5.2 Palworld unit/trait review gates

For entries with enough appearances to be meaningful:

- Within each `(cost, star, role)` cohort, no unit's win rate may differ from its cohort median by more than 8 percentage points without an explicit documented reason and follow-up matchup test.
- No single Pal may occupy the top or bottom five aggregate unit-impact slots in all three simulation styles.
- No trait at an active breakpoint may differ from the median trait impact by more than 8 percentage points across two or more board sizes without review.
- Draw rate must remain below 2% overall and below 5% for any board-size/star bucket.
- Median combat duration must stay within ±20% of the corresponding Pokemon report for the same board size/star; P95 must remain below the combat phase timeout.
- 5-cost 1★ units must be stronger than the same-role 4-cost cohort median but must not consistently defeat an equal-value balanced board alone.
- 3★ root abilities must feel decisive without producing universal one-cast board wipes against equal-cost/equal-star boards.
- Support healing+shielding and Tank effective durability must appear in reports; a nonfunctional support/tank cannot pass merely because its team wins.

Treat these as investigation thresholds. A waiver must name the Pal/trait, show report evidence, explain the intentional niche, and be approved in review. Never “fix” an outlier by changing role or canonical elements casually; prefer base stat, mana, primary effect value, target cap, duration, or secondary effect tuning.

### 5.3 Tuning order

Tune one axis at a time:

1. Crashes, infinite combat, targeting bugs, event/mana duplication.
2. Cost-tier base stat outliers.
3. Role-wide durability/damage/support output.
4. Ability primary values and mana.
5. Target caps, hit counts, zone durations, and control durations.
6. Trait breakpoint values.
7. Individual attack speed/range only when identity and positioning warrant it.

After each numeric change, update `01_GAME_DESIGN_SPEC.md` and add a changelog balance block with previous value, `=>`, and new value in separate styled spans. Re-run 10k smoke for the affected styles; after the final tuning batch, rerun all million/100k gates.

## 6. Frontend automated gate

```bash
cd frontend
npm test
npm run lint
npm run build
```

Blocking expectations:

- Three typed game modes and deterministic registry order.
- Palworld metadata/title/favicon/unit paths correct.
- 55 definition-id attack previews and 55 root-ability identities exactly match backend JSON.
- No Palworld gallery fallback or missing-animation-config badge.
- Event normalization handles cast/hit/status/zone/move/death lifecycle.
- Existing frontend tests pass.
- TypeScript strict build and ESLint produce no errors.

## 7. Asset QA

Run a manifest script/test that parses `units_palworld.json` and checks the production public directory. Then inspect the contact sheet at 512 px and simulated 48 px.

- 55 expected unit PNGs, exactly named, decodable, 512×512.
- One `pal-sphere.png` favicon source available at public root.
- No raw `_generated_batches` content included in production output if the directory is intended to remain ignored.
- Correct primary background on every Pal and secondary accents on dual types.
- No text, watermark, divider, wrong quadrant, clipped identity feature, or neighboring bleed.
- No transparent edge artifacts after palette compression.
- Target 40–250 KiB per portrait; investigate rather than automatically reject a visually justified exception.
- Network panel shows no 404 and no placeholder across shop, bench, board, tooltip, spectating, end screen, and gallery.

## 8. Manual gameplay matrix

### Lobby/waiting room

- Create room with each configured default (`onepiece`, `pokemon`, `palworld`).
- Host switches One Piece→Pokemon→Palworld→One Piece repeatedly; non-host observes state/theme and cannot switch.
- Confirm shops/owned units/augments reset on mode change and no stale trait response appears.
- Confirm title/favicon/palette follow active room, restore default on leave, and survive reconnect/refresh.
- Test keyboard mode-card focus, Enter flows, 320 px mobile layout, and high zoom.

### Match lifecycle

- Start Palworld with 1 human + bots and with 2+ humans; play through augment rounds, loot, spectating, elimination, end celebration, and room cleanup.
- Validate shops across all cost tiers and combination to 2★/3★ for every line, confirming each Pal keeps the same root ability identity while values scale.
- Validate dual traits and team-wide trait bonuses via visible stats.
- Switch viewed players during simultaneous battles containing zones/delayed hits; no foreign effects may appear.
- Disconnect/reconnect during combat and confirm active statuses/zones recover from authoritative state without permanent stale overlays.

### Combat spot checks

- Melee and ranged basics for every element.
- All eight statuses: application, persistent cue, interaction/consume, expiry, cleanse where applicable.
- Every targeting shape and movement effect at edges, corners, blocked cells, and crowded boards.
- At least one cast of all 55 root abilities through the gallery; in live combat, cover every standard and additional root-ability family.
- Damage report correctly attributes basic, direct, multi-hit, DOT, zone, arc, healing, and shielding after caster death.
- Reduced motion retains readable hit/status timing without shake or travel-heavy motion.

## 9. Performance and soak

- Use the gallery replay-all mode for 10 minutes in normal and crowded settings. Heap/DOM/effect counts must stabilize; no monotonic particle/effect growth.
- Run a bot match through at least 20 rounds with logging at normal production level. No scheduler context remains after each combat.
- Simulate rapid mode switching in lobby for 5 minutes. Cached data remains mode-keyed and memory does not accumulate rooms/resources.
- Profile a crowded board with two 5-cost 3★ multi-hit/zone casts. Target 60 FPS desktop; accept brief dips but maintain 30+ FPS crowded fallback and responsive input.
- Confirm event payload size and event count remain bounded. If a cast creates excessive snapshots, batch visual metadata without removing authoritative damage events.

## 10. Documentation and changelog

Update in the implementation PR:

- `README.md`: three-mode overview/table, `GAME_MODE=palworld` example, generic elemental combat wording, Palworld gallery route.
- `backend/BACKEND_CONTEXT.md`: provider resources, combat rules/damage resolver, composite effects, statuses, pair contexts, enriched events.
- `frontend/FRONTEND_CONTEXT.md`: mode metadata registry, Palworld asset path, lobby-only theme scope, event normalization, animation modules/gallery.
- `frontend/src/components/Changelog.vue`: temporary `Version X.X.X` at top containing the mode, roster, elemental system, abilities/statuses, lobby/assets, animations, and tests.

Balance changelog entries follow the repository rule exactly: one `.balance-block` per Pal, name in its heading, each old value, `=>`, and new value in separate styled elements. Do not list initial design values as buffs/nerfs; only post-baseline tuning changes need arrows.

## 11. Production/container gate

From repository root:

```bash
docker compose build
docker compose up
```

Smoke through the normal public endpoint:

- `/api/config` lists all enabled modes and accepts Palworld as configured default.
- `/api/traits?mode=palworld` returns nine traits.
- Native WebSocket/STOMP connection, room creation, mode change, start, actions, state, and event topic work through the proxy.
- Static Palworld assets and favicon have correct content types/cache behavior.
- Direct reload of lobby and gallery hashes works with the production server fallback.

Stop the stack normally after verification. Do not alter deployment secrets or production state as part of local QA.

## 12. Release procedure

1. Confirm working tree contains only intended changes and no raw/generated scratch grids or simulation artifacts.
2. Run Spotless, full backend tests, frontend test/lint/build, final simulations, asset manifest, manual matrix, and container smoke.
3. Review every deviation between code/data and this planning package; update docs or implementation until one source of truth remains.
4. Replace `Version X.X.X` with `Version 2.0.0` in the changelog without changing its entries/values.
5. Update build/version metadata if the existing release process requires it.
6. Commit the release preparation, tag `2.0.0`, and build the tagged commit.
7. Run a post-tag smoke using `GAME_MODE=palworld` and one default-mode smoke with One Piece.

## 13. Final go/no-go checklist

- [ ] All backend and frontend automated gates pass.
- [ ] Pokemon affinity parity and One Piece regression are proven.
- [ ] 55 portraits, 55 attacks, 55 root abilities, nine traits, and 15 augments have exact coverage.
- [ ] Two-pair zone/delayed-effect isolation passes.
- [ ] Final million-run reports and 100k role report meet gates or contain approved explicit waivers.
- [ ] Lobby-only Palworld skin, title, and favicon work; board chrome remains shared.
- [ ] Gallery has zero missing configs in normal/crowded/reduced-motion modes.
- [ ] Documentation and final 2.0.0 changelog are current.
- [ ] Production container smoke passes from the tagged commit.

Any unchecked blocking item is a no-go for `2.0.0`.
