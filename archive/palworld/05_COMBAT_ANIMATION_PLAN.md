# Palworld 2.0 — Combat Animation Specification

This file maps every Palworld basic attack and root ability identity to an explicit visual treatment. Low-level drawing primitives are reusable; registry entries are not optional. The release must never use `_default` for a Palworld attack or cast.

## 1. Architecture and lookup contract

The current `animationConfig.ts` is already large. Split it without changing One Piece/Pokemon visuals:

```text
frontend/src/animations/
  types.ts
  palettes.ts
  registry.ts
  attackFamilies.ts
  abilityFamilies.ts
  statusFamilies.ts
  modes/onepiece.ts
  modes/pokemon.ts
  modes/palworld.ts
```

Keep a compatibility re-export from `src/data/animationConfig.ts` while imports migrate. Public lookup becomes:

```ts
getAttackConfig(gameMode, definitionId)
getAbilityConfig(gameMode, definitionId, abilityIdentity?)
```

Definition ids and stable ability identities come from backend events/data, not from display names or a guessed element. Development returns a loud magenta diagnostic config and logs a missing-registry-entry error; production may use the neutral fallback to avoid a crash, but the gallery/test gate must prove Palworld never reaches it.

Extend `AbilityAnimationConfig` with data, not additional unit-specific draw functions:

```ts
interface AbilityAnimationConfig {
    effectStyle: AbilityEffectStyle
    signature: string
    color: string
    secondaryColor: string
    accentColor?: string
    screenShake: number
    particleScale: number
    durationScale?: number
    projectileCount?: number
    ringCount?: number
    trailWidth?: number
    glyph?: 'leaf' | 'ember' | 'drop' | 'bolt' | 'snow' | 'rock' | 'void' | 'dragon' | 'star' | 'heart' | 'bubble' | 'spore'
}
```

Add reusable effect styles only when their geometry differs materially. Unit distinction should normally come from composition parameters, timing, palette, glyph, and signature. Suggested generic style families:

- `PAL_PROJECTILE`, `PAL_MELEE_BURST`, `PAL_LINE_CUT`, `PAL_CONE_BREATH`, `PAL_DASH`, `PAL_CHAIN`, `PAL_MULTI_SHOT`.
- `PAL_RADIUS_BURST`, `PAL_AURA_BUFF`, `PAL_HEAL_BLOOM`, `PAL_SHIELD_FIELD`, `PAL_CONTROL_FIELD`.
- `PAL_METEOR_RAIN`, `PAL_PERSISTENT_ZONE`, `PAL_BEAM`, `PAL_WHIRLWIND`, `PAL_BUBBLE_FIELD`.

The canvas renderer receives backend cast ids, hit indexes, coordinates, duration, the target-resolved element, and status. It may interpolate visuals but must not decide hit timing or targets.

## 2. Element palette

| Element | Primary | Secondary | Default glyph |
|---|---|---|---|
| Neutral | `#A8A29E` | `#FAFAF9` | star |
| Fire | `#F97316` | `#FDE68A` | ember |
| Water | `#2563EB` | `#67E8F9` | drop |
| Electric | `#FACC15` | `#FFF7AE` | bolt |
| Grass | `#22C55E` | `#BBF7D0` | leaf |
| Ice | `#67E8F9` | `#F0F9FF` | snow |
| Ground | `#A16207` | `#FDE68A` | rock |
| Dark | `#6D28D9` | `#C4B5FD` | void |
| Dragon | `#6366F1` | `#C7D2FE` | dragon |

Ability rows may add a third accent: coral `#FF7F6E`, deep ink `#173443`, or restrained gold `#D9A441`. Element colors still communicate damage typing.

## 3. Basic attacks — 55 definition-id configs

Particles are upper targets before crowded-board scaling. Existing attack primitives may be reused where named; add `claw`, `bite`, `peck`, `horn`, `spear`, `wing`, and `bubble` only if current primitives cannot preserve the silhouette. Every listed definition id has one stable attack preview. The runtime colors the preview from the target-resolved attacking trait; no attack element or animation key is stored in Palworld JSON.

### Costs 1–2

| Definition id | Primitive | Particles | Distinguishing motion |
|---|---|---:|---|
| `lamball` | `blunt` | 10 | short wool-puff body bump |
| `cattiva` | `claw` | 10 | two quick diagonal paw scratches |
| `chikipi` | `peck` | 9 | tiny forward beak jab with feather specks |
| `foxparks` | `flameBurst` | 12 | compact orange fire pellet |
| `lifmunk` | `sniperShot` | 12 | needle-fast green seed tracer |
| `pengullet` | `aquaJet` | 12 | blue droplet with icy white tail |
| `daedream` | `shadowOrb` | 12 | slow violet dream orb |
| `depresso` | `shadowOrb` | 10 | low, drooping arc with dim impact |
| `gumoss` | `blunt` | 10 | muddy head bump and two pebble flecks |
| `vixy` | `projectile` | 10 | small pale sparkle shot |
| `sparkit` | `thunderJolt` | 12 | narrow zig-zag spark |
| `tanzee` | `sniperShot` | 12 | fast leaf pellet |
| `fuack` | `aquaJet` | 14 | wobbling water glob |
| `tocotoco` | `projectile` | 14 | spinning seed/egg pellet, coral accent |
| `direhowl` | `bite` | 12 | fast lunge and paired fang flash |
| `celaray` | `aquaJet` | 13 | flat crescent water wave |
| `dumud` | `blunt` | 12 | mud splash at target feet |
| `dazzi` | `thunderJolt` | 15 | forked gold bolt from above |
| `flambelle` | `flameBurst` | 13 | tiny molten teardrop projectile |
| `mimog` | `bite` | 12 | chest-lid snap and coin flash |
| `cremis` | `projectile` | 11 | soft wool mote with faint ring |
| `melpaca` | `blunt` | 12 | long-neck headbutt trail |
| `galeclaw` | `wing` | 14 | narrow gray wind feather slash |
| `lovander` | `claw` | 12 | pink-coral heart-shaped swipe |
| `hoodle` | `shadowOrb` | 15 | hood-shaped void mote and smoky trail |

### Costs 3–5

| Definition id | Primitive | Particles | Distinguishing motion |
|---|---|---:|---|
| `chillet` | `bite` | 15 | frosted fang nip and tiny snow puff |
| `penking` | `blunt` | 15 | regal flipper strike with water crescent |
| `katress` | `shadowOrb` | 17 | witch-orb with gold spark orbit |
| `lunaris` | `projectile` | 16 | pale crescent pulse |
| `quivern` | `dragonSpark` | 16 | soft violet breath mote |
| `petallia` | `leafCut` | 16 | paired petal blades |
| `mossanda` | `sniperShot` | 17 | heavy green grenade tracer, no explosion scale |
| `grizzbolt` | `thunderJolt` | 18 | thick black-gold arc |
| `tarantriss` | `bite` | 17 | paired crimson fang streaks |
| `relaxaurus` | `dragonSpark` | 17 | slow chunky violet missile |
| `tetroise` | `stoneToss` | 16 | tumbling square rock |
| `anubis` | `forcePalm` | 19 | sand-gold palm shock at contact |
| `shadowbeak` | `shadowOrb` | 20 | black-violet feather bolt |
| `lyleen` | `leafCut` | 18 | elegant flower-petal dart |
| `orserk` | `thunderJolt` | 21 | dragon-claw lightning fork |
| `selyne` | `slash` | 19 | luminous crescent-moon blade |
| `jormuntide-ignis` | `flameBurst` | 21 | serpentine flame bolt with violet core |
| `bellanoir` | `shadowOrb` | 21 | dark bloom projectile with pale center |
| `aegidron` | `horn` | 19 | armored horn/rock impact |
| `renjishi` | `claw` | 20 | twin kabuki flame slashes |
| `silvance` | `leafCut` | 19 | moth-wing spore blade |
| `dandilord` | `shadowOrb` | 20 | poisonous violet flower orb, green accent |
| `shaolong` | `dragonSpark` | 21 | curling azure dragon flame |
| `jetragon` | `dragonSpark` | 24 | cyan-violet micro beam with jet trail |
| `frostallion` | `iceShard` | 23 | long crystalline lance |
| `paladius` | `spear` | 22 | gold-white spear thrust trail |
| `necromus` | `spear` | 22 | paired violet spear streaks |
| `neptilius` | `spear` | 23 | blue water-lance projectile |
| `xenolord` | `shadowOrb` | 24 | black cosmic shard with violet corona |
| `panthalus` | `bubble` | 24 | large slow bubble with ripple impact |

## 4. Standard abilities — 39 root-ability configs

`Shake` is a CSS-pixel maximum before crowded/reduced-motion damping. `Scale` multiplies the base particle count. Color comes from the trait element selected by the affinity resolver for the current target unless the note names an accent.

| Ability identity | Effect style | Shake | Scale | Signature composition |
|---|---|---:|---:|---|
| `pw-lamball-fluffy-shield` | `PAL_SHIELD_FIELD` | 0 | 0.85 | three soft wool rings pop outward, white-neutral hex shimmer |
| `pw-cattiva-cat-punch` | `PAL_MELEE_BURST` | 2 | 0.85 | oversized crossed paw scratches and tiny dust star |
| `pw-chikipi-egg-drop` | `PAL_HEAL_BLOOM` | 0 | 0.85 | egg-shaped white pulse cracks into healing feather motes |
| `pw-foxparks-huggy-fire` | `PAL_CONE_BREATH` | 3 | 0.95 | short fox-fire cone with curling ember tail and Burn ring |
| `pw-daedream-dark-ball` | `PAL_PROJECTILE` | 2 | 0.95 | slow dark sphere with lavender dream orbit and Blind veil |
| `pw-depresso-caffeine-slap` | `PAL_AURA_BUFF` | 0 | 0.90 | initially drooping dark ripple snaps into fast cyan tick marks |
| `pw-gumoss-sand-blast` | `PAL_RADIUS_BURST` | 3 | 0.95 | squat mud eruption, leaf fragments, Muddy footprint ring |
| `pw-vixy-dig-here` | `PAL_HEAL_BLOOM` | 0 | 0.95 | sand mound pops into gold sparkles, heal pulse then shield shell |
| `pw-sparkit-static-electricity` | `PAL_AURA_BUFF` | 1 | 0.95 | yellow circular current links allies with brief angular sparks |
| `pw-fuack-surfing-slam` | `PAL_DASH` | 3 | 1.00 | small blue surf wake, comic wobble, round splash and Soak droplets |
| `pw-direhowl-fierce-fang` | `PAL_DASH` | 3 | 0.95 | gray speed streak into paired white fangs, haste lines on caster |
| `pw-celaray-zephyr-glider` | `PAL_SHIELD_FIELD` | 0 | 1.00 | broad manta-shaped blue wave sweeps allies and leaves bubble shields |
| `pw-dumud-earth-impact` | `PAL_RADIUS_BURST` | 4 | 1.00 | circular mud slap with chunky tan droplets and Muddy ring |
| `pw-dazzi-lady-of-lightning` | `PAL_CHAIN` | 3 | 1.05 | gold bolts hop target-to-target with a small cloud at the origin |
| `pw-flambelle-magma-tears` | `PAL_PERSISTENT_ZONE` | 2 | 1.00 | molten teardrops fall into four pulsing orange puddle rings |
| `pw-mimog-surprise-box` | `PAL_SHIELD_FIELD` | 2 | 1.00 | chest snaps open, gold coin guard closes around caster, target gets a star stun |
| `pw-cremis-fluffy-wool` | `PAL_HEAL_BLOOM` | 0 | 1.00 | expanding cream wool cloud, mint healing crosses, neutral DEF facets |
| `pw-melpaca-fluffy-tackle` | `PAL_DASH` | 3 | 1.00 | long wool streak through line, soft body impact, rearward knock arrow |
| `pw-galeclaw-gale-claw` | `PAL_DASH` | 4 | 1.05 | red-gray wing silhouette cuts across board with three white wind blades |
| `pw-lovander-heart-drain` | `PAL_PROJECTILE` | 1 | 1.00 | coral heart tether drains from target then splits into two healing trails |
| `pw-hoodle-dark-whisp` | `PAL_PROJECTILE` | 3 | 1.05 | hood-shaped void flame pursues far target, collapses into Blind shroud |
| `pw-penking-emperor-slide` | `PAL_DASH` | 4 | 1.10 | crowned blue ice-water slide, broad splash, regal shield crest |
| `pw-katress-nightmare-ball` | `PAL_RADIUS_BURST` | 4 | 1.10 | witch glyph folds into violet sphere, blooms at target with Blind veil |
| `pw-lunaris-antigravity` | `PAL_CONTROL_FIELD` | 2 | 1.05 | pale crescent field lifts targets, concentric gravity rings, DEF shards fall away |
| `pw-quivern-dragon-meteor` | `PAL_METEOR_RAIN` | 4 | 1.10 | soft violet meteor with white wing trail, compact dragon explosion |
| `pw-petallia-flower-spirit` | `PAL_HEAL_BLOOM` | 0 | 1.10 | pink-white flower opens across allies, green healing pollen and haste petals |
| `pw-mossanda-grenade-launcher` | `PAL_MULTI_SHOT` | 5 | 1.15 | three chunky green grenades arc separately; last impact sprouts binding vines |
| `pw-tarantriss-web-shooter` | `PAL_CONE_BREATH` | 3 | 1.10 | dark cone of silver-crimson web lines tightens into Ivy/root knots |
| `pw-relaxaurus-hungry-missile` | `PAL_MULTI_SHOT` | 5 | 1.15 | three oversized violet cartoon missiles wobble toward separate targets |
| `pw-tetroise-cube-press` | `PAL_DASH` | 6 | 1.20 | square shadow grows, stone-shell body drops, cubic shockwave and DEF plates |
| `pw-lyleen-harvest-goddess` | `PAL_HEAL_BLOOM` | 0 | 1.15 | tall green flower sigil, three targeted healing vines, gold AP pollen on team |
| `pw-orserk-kerauno` | `PAL_DASH` | 6 | 1.20 | black-yellow dragon silhouette dives, impact erupts into two chain bolts |
| `pw-selyne-seigetsu-blade` | `PAL_LINE_CUT` | 5 | 1.15 | enormous pale crescent blade travels down line, dark moon dust and cracked DEF facets |
| `pw-jormuntide-ignis-stormbringer-lava` | `PAL_CONE_BREATH` | 6 | 1.25 | serpentine red-black flame cone rolls in three waves, molten Burn edge |
| `pw-aegidron-explosive-missile` | `PAL_MULTI_SHOT` | 6 | 1.25 | four ground-gold wing missiles with signal-light trails, dome shield closes |
| `pw-renjishi-volcanic-rain` | `PAL_METEOR_RAIN` | 6 | 1.25 | kabuki fan flourish, five volcanic shells rain into a persistent fire circle |
| `pw-silvance-spore-burst` | `PAL_RADIUS_BURST` | 4 | 1.20 | huge green spore homes in, bursts into moth-wing pattern and ally spore shields |
| `pw-dandilord-toxic-dance` | `PAL_PERSISTENT_ZONE` | 4 | 1.20 | rotating petal dancer silhouette emits green-violet mist rings and poison bubbles |
| `pw-shaolong-azure-dracoflare` | `PAL_CONE_BREATH` | 6 | 1.25 | sweeping azure flame follows curling dragon path, water-blue edge and Burn sparks |

## 5. Additional root abilities — 16 configs

These are the remaining root abilities in the 55-Pal roster. Each identity is used at every star; the registry may increase scale, duration, or intensity from the star level, but it never selects a different ability or animation identity.

| Ability identity | Effect style | Shake | Scale | Signature composition |
|---|---|---:|---:|---|
| `pw-lifmunk-seed-burst` | `PAL_LINE_CUT` | 3 | 1.00 | Green seed crescent travels the line and closes with a two-vine Ivy snap. |
| `pw-pengullet-aqua-cannon` | `PAL_BEAM` | 5 | 1.15 | Pressurized blue-white line blast leaves a Soak droplet halo and stronger star-scaled knockback cue. |
| `pw-tanzee-rocket-launcher` | `PAL_PROJECTILE` | 6 | 1.15 | Coral-tipped rocket leaves smoke squares, a compact fire canopy, and a knockback arrow. |
| `pw-tocotoco-megaton-egg` | `PAL_RADIUS_BURST` | 7 | 1.20 | Patterned egg shadow detonates into a white-coral ring, feathers, and star-shaped stun cues. |
| `pw-chillet-cryst-breath` | `PAL_CONE_BREATH` | 6 | 1.15 | Layered ice mist cone grows snow crystals and a readable Freeze rim. |
| `pw-grizzbolt-lightning-claw` | `PAL_DASH` | 8 | 1.25 | Black-yellow claw tears to the target, then forks into two bounded chain arcs. |
| `pw-anubis-spinning-roundhouse` | `PAL_RADIUS_BURST` | 8 | 1.25 | Circular gold sand kick uses rotating afterimages, knockback arrows, and stun stars. |
| `pw-shadowbeak-divine-disaster` | `PAL_RADIUS_BURST` | 7 | 1.20 | Dense black-violet orb blooms with a feather corona and Blind shroud. |
| `pw-bellanoir-nightmare-ray` | `PAL_BEAM` | 8 | 1.25 | Pale-core ray expands to a black-violet line; returned energy splits into two healing streams. |
| `pw-jetragon-dragonic-meteor` | `PAL_METEOR_RAIN` | 9 | 1.35 | Five cyan-violet meteors descend around the backend cluster center; the final hit forms a dragon-wing shock. |
| `pw-frostallion-blizzard-spike` | `PAL_METEOR_RAIN` | 8 | 1.30 | Central ice spear falls through a snowfield, then radial frost and team crystal halos appear. |
| `pw-paladius-holy-burst` | `PAL_RADIUS_BURST` | 8 | 1.30 | White-gold sun disk expands with spear rays, cleanse motes, and fortress shields. |
| `pw-necromus-nightmare-spear` | `PAL_LINE_CUT` | 8 | 1.30 | Colossal black-violet spear pierces the line; low-health execute marks close on impact. |
| `pw-neptilius-ocean-sovereign` | `PAL_MULTI_SHOT` | 8 | 1.30 | Four water lances strike from compass points and collapse into a deep-blue shield dome. |
| `pw-xenolord-mystic-whirlwind` | `PAL_WHIRLWIND` | 9 | 1.40 | Black-violet comet dives into a bounded cosmic vortex and detonates in a wing-shaped shockwave. |
| `pw-panthalus-aqua-tornado` | `PAL_WHIRLWIND` | 9 | 1.40 | Four tracking water funnels follow backend-assigned paths and erupt in synchronized pillars. |

## 6. Status and movement visuals

Status visuals are keyed by `statusId`, not ability. Apply/remove events start transitions; the serialized active-status list repairs visuals after a missed event or reconnect.

| Status | Persistent visual | Apply/consume/remove cue |
|---|---|---|
| Burn | sparse orange embers rising from feet, maximum 8 particles per unit | apply: small flame ring; remove: embers shrink over 180 ms |
| Poison | two green-violet bubbles orbit low around unit per stack, maximum 6 | stack: one bubble pop; expiry: bubbles desaturate; cleanse: outward white ring |
| Freeze | cyan frost rim and one translucent crystal facet over unit corners | apply: six-point snow flash; break: facets fall outward |
| Ivy | two green vine arcs around the unit's cell border | apply: roots snap inward; Fire removal: vines burn from green to orange |
| Muddy | brown footprint/splash decal on ground layer | apply: mud splat; expiry: decal fades without particles |
| Soak | blue droplets orbit and a faint wet sheen | Electric consume: droplets flash yellow and vaporize; normal expiry: fall downward |
| Electrified | intermittent small yellow arcs around outline, no continuous screen flash | consume: one arc leaves toward backend-specified secondary target |
| Blind | dark-violet veil across upper unit and two dim eye motes | apply: iris closes; expiry/cleanse: veil opens and dissolves |

Hard-control cues for stun remain, but Freeze uses snow geometry so they are distinguishable. Do not obscure HP/mana bars or portrait identity.

`MOVE` events caused by abilities include start/end coordinates and cast id. The canvas renders trails while the DOM unit position interpolates to authoritative coordinates. Knockback/pull arrows are decorative and use event direction; frontend collision is never simulated.

## 7. Event-to-effect lifecycle

1. `ATTACK`: create one attack trail using the source `definitionId`; pair it with the matching damage impact through event/cast id.
2. `CAST`: create the signature wind-up and persistent composition using the stable ability identity when present, event center, duration, hit count, and star intensity.
3. `DAMAGE`: add only the keyed impact/hit-index layer; do not replay the whole cast for every projectile.
4. `HEAL`/`SHIELD`: retain generic recipient feedback, tinted by the cast config when a cast id is present.
5. `ZONE_START`: create a ground-layer instance whose lifetime is the backend duration. Tick events pulse it without restarting.
6. `STATUS_APPLY`/`STATUS_REMOVE`: update the lightweight over-unit effect.
7. `MOVE`: interpolate authoritative unit motion and render a cast-linked trail.
8. `DEATH`: retain current death treatment; allow delayed cast visuals to finish without requiring the source DOM unit.
9. `ZONE_END`: fade the matching zone by cast/event id even if its owner is dead.

Deduplicate by backend `eventId`. Never use timestamp as the sole identity. Batch events sharing a cast id so one cast with six hits consumes one ultimate slot plus bounded impact slots.

## 8. Canvas implementation notes

- Add draw helpers for cone masks, grid-aligned zones, targetable line segments, tracking spirals, bubble wobble, shield facets, glyph particles, and simple afterimage silhouettes.
- An “afterimage silhouette” is abstract geometry in the unit's palette, not a copied sprite or full portrait redraw.
- Seed decorative particle placement from a small hash of `(eventId, particleIndex)` for repeatable gallery snapshots. Do not use random visual output in screenshot tests.
- Render in existing order: `ground`, `trail`, `impact`, `over-unit`. Status ground decals stay below units; Blind/Freeze overlays stay above.
- Clip all drawing to board bounds. Large 5-cost effects can touch the board edge but cannot cover lobby/shop controls outside the canvas.
- A cast signature label remains the backend ability name. Keep it readable and do not show internal key strings in normal play.
- Star/cost intensity is encoded by the root ability config and its star-scaled values. Avoid multiplying it a second time merely because a unit is 3★/5-cost; at most use a small shared brightness factor.

## 9. Performance budgets

Maintain 60 FPS on a typical desktop and a usable 30+ FPS crowded fallback. Budgets are hard caps, not targets:

| Resource | Normal | Crowded (12+ visible units or 5+ casts/500 ms) | Reduced motion |
|---|---:|---:|---:|
| Active effect instances | 48 | 32 | 20 |
| Total particles | 700 | 420 | 120 |
| One auto attack | 24 | 10 | 0–4 |
| One normal ability | 180 | 90 | 12 |
| One 5-cost 3★ cast | 420 | 180 | 24 |
| Persistent particles per statused unit | 8 | 3 | 0 |

When over budget, preserve in this order: impact timing/shape, target direction, element color, status cue, signature geometry, particles, screen shake. Drop particles first. Never drop a backend hit or show the wrong target.

Screen shake is clamped to 10, damped by 50% in crowded mode, and disabled under reduced motion. Reduced motion replaces travel with a 120–180 ms source/target pulse, keeps zone outlines static, and removes continuous status particles while preserving icons/rings.

Stop `requestAnimationFrame` when not in combat/gallery and no transient effect remains. Pool particles/temporary arrays. Avoid per-frame gradients for unchanged persistent zones by caching small offscreen patterns if profiling shows a bottleneck.

## 10. Ultimate gallery requirements

Route: `#/ultimate-gallery/palworld`.

The gallery reads Palworld unit JSON and registry data; it does not maintain a third manually duplicated roster. It must offer:

- Attack preview for all 55 Pal ids.
- Ability preview for all 55 root abilities, with unit name, star-scaled values, cost, resolved element, and ability name.
- Filters: Attack/Ability, cost 1–5, star 1–3, defensive trait, and effect family.
- Toggles: normal/crowded, 1×/0.75× playback speed, reduced motion, light/dark board background, and loop.
- A missing-animation-config counter fixed at the top; release requires `0`.
- A deterministic “replay all” mode useful for automated screenshots and memory profiling.

Use representative dummy geometry for line/cone/radius/zone previews and emit synthetic normalized visual events only inside the gallery. No production combat logic is imported or recreated.

## 11. Tests and visual review

### Automated

- Registry test: the 55 Palworld definition ids equal the 55 Palworld attack registry entries.
- Registry test: the 55 resolved root ability identities equal the 55 Palworld ability registry entries.
- Duplicate identity test across each mode namespace.
- Lookup test uses `(gameMode, definitionId, abilityIdentity?)` and cannot return a Pokemon/One Piece config for a Palworld identity.
- Event normalization test covers cast id, hit index/count, missing/dying targets, coordinates, resolved elements, zones, and statuses.
- Reduced-motion test produces no shake and respects reduced particle/effect caps.
- Gallery roster test preserves one root-ability entry with 1★/2★/3★ preview states for every Pal.
- Deterministic renderer/helper tests use seeded event hashes for stable geometry.

### Manual matrix

Review every gallery entry at desktop and mobile canvas sizes, then explicitly review:

- Low-cost firearm progressions: Lifmunk, Tanzee, Grizzbolt.
- Recoil and knockback: Pengullet 3★, Tocotoco 3★, Anubis 2★/3★.
- Support clarity: Lamball, Vixy, Celaray, Petallia, Lyleen, Bellanoir, Frostallion, Paladius.
- Persistent zones: Flambelle, Renjishi, Dandilord, Bellanoir 2★, Panthalus 1★.
- Multi-pair concurrency: simultaneous zones/casts from two matchups while spectating between them.
- All seven 5-costs at all three stars in normal, crowded, and reduced-motion modes using the same root ability identity.
- Dual-color readability for Pengullet, Chillet, Orserk, Selyne, Jormuntide Ignis, Aegidron, Dandilord, Shaolong, and Xenolord.

Reject a visual if the resolved element is ambiguous, hit/target timing contradicts the backend, a support cast looks like enemy damage, or a status hides health information.

## 12. Completion checklist

- [ ] Animation code is split into maintainable mode registries and reusable families.
- [ ] Backend definition ids and stable ability identities drive all Palworld lookups; display names are never parsed.
- [ ] Exactly 55 attack and 55 ability registry entries exist.
- [ ] All 55 root abilities have star-scaled visual intensity without duplicate star identities.
- [ ] All statuses, forced movement, delayed hits, and zones have clear bounded visuals.
- [ ] Gallery missing counter is zero in normal and production builds.
- [ ] Crowded/reduced-motion budgets are enforced and visually audited.
- [ ] Existing One Piece and Pokemon gallery screenshots/config tests remain unchanged except intentional registry plumbing.
