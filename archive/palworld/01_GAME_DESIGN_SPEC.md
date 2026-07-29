# Palworld Tactics — Canonical Game Design Specification

This file owns every game-design value for the Palworld mode. Implementation code and JSON must conform to it. Values are initial 2.0 balance targets calibrated to the 1.8.0 One Piece/Pokemon distributions; simulations may change them only if this file and the changelog are updated together.

## 1. Data conventions

- Mode id: `palworld`.
- Unit and line ids: lowercase kebab-case. Palworld has no star-form identity changes, so `lineId` equals `id` for all 55 units.
- Element ids: `neutral`, `fire`, `water`, `electric`, `grass`, `ice`, `ground`, `dark`, `dragon`.
- Role ids: `DAMAGE`, `TANK`, `SUPPORT`.
- Range 1 is melee. Ranges 2, 3, and 4 are ranged. Range is measured using the engine's current distance rules.
- Every three-number array is `[1-star, 2-star, 3-star]` and must remain on one JSON line.
- All units use `abilityPower: [100, 100, 100]` initially.
- Health and attack-damage star scaling uses the established `1.00 / 1.80 / 3.24` curve. Defense, attack speed, range, and max mana are intentionally flat by star unless explicitly changed later.
- Offensive elements are never stored separately from traits. Basic attacks, damaging abilities, and their damage-over-time effects use the existing Pokemon-style best-attacker-trait behavior against the target's defensive trait elements. A dual-element Pal keeps both traits for defense and team synergy; the resolver selects the strongest attacking trait for each target.
- Each Pal has one root ability at the JSON root. Its identity is stable at 1/2/3 stars and its numeric values are star-scaled arrays.
- Animation lookup uses the stable Pal definition id and, where needed, the stable ability identity. No `basicElement`, ability `element`, `attackAnimationKey`, or JSON `animationKey` field is required.
- A percentage written as `20%` is serialized as `0.20` when a record field is a multiplier and as `20` only when an existing API explicitly uses integer percentage points. New APIs should prefer decimal multipliers.

## 2. Roster goals and counts

The set contains 55 lines: 47 memorable legacy Pals and eight marked 1.0 additions. The distribution is intentionally identical to the existing sets.

| Cost | Count | Design purpose |
|---:|---:|---|
| 1 | 12 | Recognizable early-game mascots and simple combat identities |
| 2 | 13 | Partner-skill personalities and bridge carries/frontliners |
| 3 | 11 | Mid-game build anchors and fan favorites |
| 4 | 12 | Iconic, rare, raid, and 1.0 additions |
| 5 | 7 | Legendary/end-game centerpieces |

Role totals are 23 Damage, 16 Tank, and 16 Support. Exactly 21 units are melee and 34 are ranged.

## 3. Canonical unit roster and stat blocks

Table abbreviations: `Elems` is canonical defensive/team-trait order; `HP`, `Mana`, `ATK`, and `DEF` are exact three-star arrays; `AS` and `R` are also arrays even when constant. `New` marks a Pal introduced in 1.0. Offensive elements are derived from `Elems` at runtime.

### Cost 1 — 12 lines

| Id / name | New | Elems | Role | HP | Mana | ATK | DEF | AS | R |
|---|:---:|---|---|---|---|---|---|---|---|
| `lamball` / Lamball | — | Neutral | TANK | `[900, 1620, 2916]` | `[60, 60, 60]` | `[31, 56, 100]` | `[42, 42, 42]` | `[0.58, 0.58, 0.58]` | `[1, 1, 1]` |
| `cattiva` / Cattiva | — | Neutral | DAMAGE | `[460, 828, 1490]` | `[70, 70, 70]` | `[54, 97, 175]` | `[14, 14, 14]` | `[0.68, 0.68, 0.68]` | `[1, 1, 1]` |
| `chikipi` / Chikipi | — | Neutral | SUPPORT | `[520, 936, 1685]` | `[55, 55, 55]` | `[30, 54, 97]` | `[18, 18, 18]` | `[0.65, 0.65, 0.65]` | `[1, 1, 1]` |
| `foxparks` / Foxparks | — | Fire | DAMAGE | `[400, 720, 1296]` | `[70, 70, 70]` | `[60, 108, 194]` | `[12, 12, 12]` | `[0.72, 0.72, 0.72]` | `[2, 2, 2]` |
| `lifmunk` / Lifmunk | — | Grass | DAMAGE | `[420, 756, 1361]` | `[60, 60, 60]` | `[58, 104, 188]` | `[13, 13, 13]` | `[0.70, 0.70, 0.70]` | `[4, 4, 4]` |
| `pengullet` / Pengullet | — | Water, Ice | DAMAGE | `[390, 702, 1264]` | `[65, 65, 65]` | `[62, 112, 201]` | `[12, 12, 12]` | `[0.72, 0.72, 0.72]` | `[4, 4, 4]` |
| `daedream` / Daedream | — | Dark | DAMAGE | `[410, 738, 1328]` | `[75, 75, 75]` | `[60, 108, 194]` | `[12, 12, 12]` | `[0.68, 0.68, 0.68]` | `[3, 3, 3]` |
| `depresso` / Depresso | — | Dark | SUPPORT | `[560, 1008, 1814]` | `[70, 70, 70]` | `[34, 61, 110]` | `[22, 22, 22]` | `[0.58, 0.58, 0.58]` | `[3, 3, 3]` |
| `gumoss` / Gumoss | — | Grass, Ground | TANK | `[980, 1764, 3175]` | `[80, 80, 80]` | `[27, 49, 87]` | `[50, 50, 50]` | `[0.55, 0.55, 0.55]` | `[1, 1, 1]` |
| `vixy` / Vixy | — | Neutral | SUPPORT | `[500, 900, 1620]` | `[65, 65, 65]` | `[32, 58, 104]` | `[20, 20, 20]` | `[0.66, 0.66, 0.66]` | `[3, 3, 3]` |
| `sparkit` / Sparkit | — | Electric | SUPPORT | `[505, 909, 1636]` | `[60, 60, 60]` | `[33, 59, 107]` | `[18, 18, 18]` | `[0.72, 0.72, 0.72]` | `[3, 3, 3]` |
| `tanzee` / Tanzee | — | Grass | DAMAGE | `[430, 774, 1393]` | `[65, 65, 65]` | `[56, 101, 181]` | `[14, 14, 14]` | `[0.72, 0.72, 0.72]` | `[4, 4, 4]` |

### Cost 2 — 13 lines

| Id / name | New | Elems | Role | HP | Mana | ATK | DEF | AS | R |
|---|:---:|---|---|---|---|---|---|---|---|
| `fuack` / Fuack | — | Water | SUPPORT | `[600, 1080, 1944]` | `[65, 65, 65]` | `[36, 65, 117]` | `[25, 25, 25]` | `[0.67, 0.67, 0.67]` | `[2, 2, 2]` |
| `tocotoco` / Tocotoco | — | Neutral | DAMAGE | `[500, 900, 1620]` | `[75, 75, 75]` | `[70, 126, 227]` | `[16, 16, 16]` | `[0.66, 0.66, 0.66]` | `[4, 4, 4]` |
| `direhowl` / Direhowl | — | Neutral | DAMAGE | `[610, 1098, 1976]` | `[65, 65, 65]` | `[65, 117, 211]` | `[20, 20, 20]` | `[0.76, 0.76, 0.76]` | `[1, 1, 1]` |
| `celaray` / Celaray | — | Water | SUPPORT | `[640, 1152, 2074]` | `[75, 75, 75]` | `[34, 61, 110]` | `[27, 27, 27]` | `[0.66, 0.66, 0.66]` | `[3, 3, 3]` |
| `dumud` / Dumud | — | Ground | TANK | `[1150, 2070, 3726]` | `[75, 75, 75]` | `[35, 63, 113]` | `[62, 62, 62]` | `[0.55, 0.55, 0.55]` | `[1, 1, 1]` |
| `dazzi` / Dazzi | — | Electric | SUPPORT | `[570, 1026, 1847]` | `[65, 65, 65]` | `[38, 68, 123]` | `[23, 23, 23]` | `[0.72, 0.72, 0.72]` | `[3, 3, 3]` |
| `flambelle` / Flambelle | — | Fire | SUPPORT | `[550, 990, 1782]` | `[60, 60, 60]` | `[42, 76, 136]` | `[22, 22, 22]` | `[0.68, 0.68, 0.68]` | `[2, 2, 2]` |
| `mimog` / Mimog | — | Neutral | TANK | `[1000, 1800, 3240]` | `[80, 80, 80]` | `[42, 76, 136]` | `[66, 66, 66]` | `[0.55, 0.55, 0.55]` | `[1, 1, 1]` |
| `cremis` / Cremis | — | Neutral | SUPPORT | `[680, 1224, 2203]` | `[80, 80, 80]` | `[32, 58, 104]` | `[30, 30, 30]` | `[0.60, 0.60, 0.60]` | `[2, 2, 2]` |
| `melpaca` / Melpaca | — | Neutral | TANK | `[1120, 2016, 3629]` | `[70, 70, 70]` | `[38, 68, 123]` | `[54, 54, 54]` | `[0.62, 0.62, 0.62]` | `[1, 1, 1]` |
| `galeclaw` / Galeclaw | — | Neutral | DAMAGE | `[540, 972, 1750]` | `[60, 60, 60]` | `[68, 122, 220]` | `[17, 17, 17]` | `[0.80, 0.80, 0.80]` | `[1, 1, 1]` |
| `lovander` / Lovander | — | Neutral | SUPPORT | `[670, 1206, 2171]` | `[70, 70, 70]` | `[36, 65, 117]` | `[28, 28, 28]` | `[0.65, 0.65, 0.65]` | `[1, 1, 1]` |
| `hoodle` / Hoodle | 1.0 | Dark | DAMAGE | `[510, 918, 1652]` | `[65, 65, 65]` | `[72, 130, 233]` | `[16, 16, 16]` | `[0.75, 0.75, 0.75]` | `[3, 3, 3]` |

### Cost 3 — 11 lines

| Id / name | New | Elems | Role | HP | Mana | ATK | DEF | AS | R |
|---|:---:|---|---|---|---|---|---|---|---|
| `chillet` / Chillet | — | Ice, Dragon | TANK | `[1250, 2250, 4050]` | `[70, 70, 70]` | `[52, 94, 168]` | `[60, 60, 60]` | `[0.68, 0.68, 0.68]` | `[1, 1, 1]` |
| `penking` / Penking | — | Water, Ice | TANK | `[1420, 2556, 4601]` | `[85, 85, 85]` | `[46, 83, 149]` | `[70, 70, 70]` | `[0.56, 0.56, 0.56]` | `[1, 1, 1]` |
| `katress` / Katress | — | Dark | DAMAGE | `[620, 1116, 2009]` | `[75, 75, 75]` | `[90, 162, 292]` | `[22, 22, 22]` | `[0.72, 0.72, 0.72]` | `[3, 3, 3]` |
| `lunaris` / Lunaris | — | Neutral | SUPPORT | `[750, 1350, 2430]` | `[65, 65, 65]` | `[48, 86, 156]` | `[30, 30, 30]` | `[0.70, 0.70, 0.70]` | `[3, 3, 3]` |
| `quivern` / Quivern | — | Dragon | TANK | `[1300, 2340, 4212]` | `[80, 80, 80]` | `[48, 86, 156]` | `[62, 62, 62]` | `[0.64, 0.64, 0.64]` | `[1, 1, 1]` |
| `petallia` / Petallia | — | Grass | SUPPORT | `[820, 1476, 2657]` | `[80, 80, 80]` | `[42, 76, 136]` | `[36, 36, 36]` | `[0.62, 0.62, 0.62]` | `[3, 3, 3]` |
| `mossanda` / Mossanda | — | Grass | TANK | `[1500, 2700, 4860]` | `[90, 90, 90]` | `[44, 79, 143]` | `[66, 66, 66]` | `[0.60, 0.60, 0.60]` | `[4, 4, 4]` |
| `grizzbolt` / Grizzbolt | — | Electric | DAMAGE | `[720, 1296, 2333]` | `[80, 80, 80]` | `[82, 148, 266]` | `[28, 28, 28]` | `[0.76, 0.76, 0.76]` | `[4, 4, 4]` |
| `tarantriss` / Tarantriss | — | Dark | DAMAGE | `[690, 1242, 2236]` | `[70, 70, 70]` | `[86, 155, 279]` | `[26, 26, 26]` | `[0.72, 0.72, 0.72]` | `[1, 1, 1]` |
| `relaxaurus` / Relaxaurus | — | Dragon, Water | DAMAGE | `[760, 1368, 2462]` | `[85, 85, 85]` | `[78, 140, 253]` | `[30, 30, 30]` | `[0.62, 0.62, 0.62]` | `[4, 4, 4]` |
| `tetroise` / Tetroise | 1.0 | Ground | TANK | `[1550, 2790, 5022]` | `[90, 90, 90]` | `[42, 76, 136]` | `[78, 78, 78]` | `[0.50, 0.50, 0.50]` | `[1, 1, 1]` |

### Cost 4 — 12 lines

| Id / name | New | Elems | Role | HP | Mana | ATK | DEF | AS | R |
|---|:---:|---|---|---|---|---|---|---|---|
| `anubis` / Anubis | — | Ground | DAMAGE | `[820, 1476, 2657]` | `[70, 70, 70]` | `[112, 202, 363]` | `[30, 30, 30]` | `[0.82, 0.82, 0.82]` | `[1, 1, 1]` |
| `shadowbeak` / Shadowbeak | — | Dark | DAMAGE | `[780, 1404, 2527]` | `[90, 90, 90]` | `[116, 209, 376]` | `[26, 26, 26]` | `[0.78, 0.78, 0.78]` | `[4, 4, 4]` |
| `lyleen` / Lyleen | — | Grass | SUPPORT | `[1020, 1836, 3305]` | `[80, 80, 80]` | `[58, 104, 188]` | `[44, 44, 44]` | `[0.68, 0.68, 0.68]` | `[3, 3, 3]` |
| `orserk` / Orserk | — | Dragon, Electric | DAMAGE | `[850, 1530, 2754]` | `[85, 85, 85]` | `[108, 194, 350]` | `[32, 32, 32]` | `[0.75, 0.75, 0.75]` | `[1, 1, 1]` |
| `selyne` / Selyne | — | Dark, Neutral | DAMAGE | `[760, 1368, 2462]` | `[80, 80, 80]` | `[110, 198, 356]` | `[28, 28, 28]` | `[0.80, 0.80, 0.80]` | `[4, 4, 4]` |
| `jormuntide-ignis` / Jormuntide Ignis | — | Dragon, Fire | TANK | `[1650, 2970, 5346]` | `[90, 90, 90]` | `[64, 115, 207]` | `[74, 74, 74]` | `[0.62, 0.62, 0.62]` | `[3, 3, 3]` |
| `bellanoir` / Bellanoir | — | Dark | SUPPORT | `[940, 1692, 3046]` | `[85, 85, 85]` | `[64, 115, 207]` | `[38, 38, 38]` | `[0.72, 0.72, 0.72]` | `[4, 4, 4]` |
| `aegidron` / Aegidron | 1.0 | Dragon, Ground | TANK | `[1750, 3150, 5670]` | `[95, 95, 95]` | `[58, 104, 188]` | `[82, 82, 82]` | `[0.60, 0.60, 0.60]` | `[1, 1, 1]` |
| `renjishi` / Renjishi | 1.0 | Fire | DAMAGE | `[900, 1620, 2916]` | `[75, 75, 75]` | `[104, 187, 337]` | `[34, 34, 34]` | `[0.75, 0.75, 0.75]` | `[1, 1, 1]` |
| `silvance` / Silvance | 1.0 | Grass | SUPPORT | `[1100, 1980, 3564]` | `[80, 80, 80]` | `[54, 97, 175]` | `[48, 48, 48]` | `[0.66, 0.66, 0.66]` | `[3, 3, 3]` |
| `dandilord` / Dandilord | 1.0 | Grass, Dark | TANK | `[1600, 2880, 5184]` | `[90, 90, 90]` | `[62, 112, 201]` | `[76, 76, 76]` | `[0.64, 0.64, 0.64]` | `[3, 3, 3]` |
| `shaolong` / Shaolong | 1.0 | Dragon, Water | DAMAGE | `[880, 1584, 2851]` | `[90, 90, 90]` | `[106, 191, 343]` | `[30, 30, 30]` | `[0.74, 0.74, 0.74]` | `[3, 3, 3]` |

### Cost 5 — 7 lines

| Id / name | New | Elems | Role | HP | Mana | ATK | DEF | AS | R |
|---|:---:|---|---|---|---|---|---|---|---|
| `jetragon` / Jetragon | — | Dragon | DAMAGE | `[980, 1764, 3175]` | `[90, 90, 90]` | `[140, 252, 454]` | `[34, 34, 34]` | `[0.86, 0.86, 0.86]` | `[4, 4, 4]` |
| `frostallion` / Frostallion | — | Ice | SUPPORT | `[1450, 2610, 4698]` | `[100, 100, 100]` | `[90, 162, 292]` | `[62, 62, 62]` | `[0.82, 0.82, 0.82]` | `[3, 3, 3]` |
| `paladius` / Paladius | — | Neutral | TANK | `[2100, 3780, 6804]` | `[95, 95, 95]` | `[92, 166, 298]` | `[90, 90, 90]` | `[0.68, 0.68, 0.68]` | `[1, 1, 1]` |
| `necromus` / Necromus | — | Dark | DAMAGE | `[1150, 2070, 3726]` | `[85, 85, 85]` | `[126, 227, 408]` | `[44, 44, 44]` | `[0.80, 0.80, 0.80]` | `[1, 1, 1]` |
| `neptilius` / Neptilius | — | Water | TANK | `[1950, 3510, 6318]` | `[90, 90, 90]` | `[96, 173, 311]` | `[82, 82, 82]` | `[0.72, 0.72, 0.72]` | `[3, 3, 3]` |
| `xenolord` / Xenolord | — | Dragon, Dark | DAMAGE | `[1050, 1890, 3402]` | `[100, 100, 100]` | `[136, 245, 441]` | `[38, 38, 38]` | `[0.78, 0.78, 0.78]` | `[4, 4, 4]` |
| `panthalus` / Panthalus | 1.0 | Water | TANK | `[2400, 4320, 7776]` | `[110, 110, 110]` | `[82, 148, 266]` | `[100, 100, 100]` | `[0.60, 0.60, 0.60]` | `[3, 3, 3]` |

## 4. Elemental combat rules

### 4.1 Relationship graph

The graph is directional. Only the following edges exist; all unlisted pairings, including an element attacking itself, are neutral.

| Attacking element | Strong against (`1.20`) | Resisted by (`0.80`) |
|---|---|---|
| Neutral | — | Dark |
| Fire | Grass, Ice | Water |
| Water | Fire | Electric |
| Electric | Water | Ground |
| Grass | Ground | Fire |
| Ice | Dragon | Fire |
| Ground | Electric | Grass |
| Dark | Neutral | Dragon |
| Dragon | Dark | Ice |

For each defender element, apply at most one relationship factor. A dual-element defender multiplies both applicable factors in declared trait order. Examples:

- Fire into Grass/Dark: `base × 1.20`.
- Ice into Dragon/Fire: `base × 1.20 × 0.80 = base × 0.96`.
- Dark into Dark/Neutral: `base × 1.00 × 1.20 = base × 1.20`.
- Neutral into Dark/Dragon: `base × 0.80 × 1.00 = base × 0.80`.

Apply the affinity multiplier after ability/trait/augment outgoing multipliers and before DEF mitigation, rounding once at the final integer damage boundary. For every damaging basic, ability, or damage-over-time step, evaluate the caster's trait elements against the target's defensive trait elements and use the best attacking-trait result, exactly as Pokemon does today. Healing, shielding, recoil, and non-damaging effects never use affinities. There is no STAB, critical hit, immunity, or random variance in 2.0.

### 4.2 Element traits

All nine traits use `type: "element"` and `targetScope: "TEAM"`. Counts use unique Pal lines on the board, exactly like existing traits. Dual-element Pals count once toward both elements. Trait bonuses apply to every allied unit, not just Pals sharing that element.

| Id | Color | Generic effect | 1 / 2 / 3 / 4 unique lines | Exact JSON values |
|---|---|---|---|---|
| `neutral` | `#A8A29E` | `ATK_BUFF` | +2% / +7% / +14% / +22% ATK | `atkBuff: 0.02 / 0.07 / 0.14 / 0.22` |
| `fire` | `#F97316` | `ABILITY_DAMAGE` | +4% / +14% / +22% / +32% ability damage | `abilityDamage: 0.04 / 0.14 / 0.22 / 0.32` |
| `water` | `#3B82F6` | `MANA_GAIN` | +15% / +40% / +65% / +90% mana from attacks | `manaGain: 0.15 / 0.40 / 0.65 / 0.90` |
| `electric` | `#FACC15` | `AS` | +4% / +12% / +20% / +30% attack speed | `as: 0.04 / 0.12 / 0.20 / 0.30` |
| `grass` | `#22C55E` | `HP` | +100 / +275 / +500 / +800 Health | `hp: 100 / 275 / 500 / 800` |
| `ice` | `#67E8F9` | `DAMAGE_REDUCTION` | 3% / 8% / 13% / 20% reduced damage | `damageReduction: 0.03 / 0.08 / 0.13 / 0.20` |
| `ground` | `#A16207` | `DEFENSE` | +5 / +12 / +20 / +30 DEF | `defense: 5 / 12 / 20 / 30` |
| `dark` | `#7C3AED` | `LIFESTEAL` | 3% / 8% / 14% / 22% lifesteal | `lifesteal: 0.03 / 0.08 / 0.14 / 0.22` |
| `dragon` | `#6366F1` | `START_MANA_PERCENT` | 4% / 12% / 22% / 35% max mana at combat start | `manaPercent: 0.04 / 0.12 / 0.22 / 0.35` |

Trait display descriptions should say “Pals” rather than “Pokemon,” but the core effect implementation remains generic. The four styles in order are `bronze`, `silver`, `gold`, and `prismatic`.

## 5. Reusable ability vocabulary

The ability executor processes an ordered list of effect steps. The tables below use this compact notation; the backend plan defines the records that encode it.

### 5.1 Target and shape notation

| Notation | Exact meaning |
|---|---|
| `CURRENT` | Current valid enemy target; reacquire nearest enemy if the old target died before resolution |
| `FARTHEST` | Living enemy with greatest grid distance; deterministic tie-break by x, y, owner id, runtime id |
| `LOWEST_ALLY` | Living ally with lowest health percentage; deterministic tie-break by current HP then runtime id |
| `CLUSTER` | Living enemy whose radius contains the most other living enemies; normal deterministic tie-break |
| `SELF` | Caster only |
| `ALL_ALLIES` | Every living allied unit including caster |
| `SINGLE` | Selected target only |
| `LINE(n)` | Aimed line from caster through selected target up to n cells |
| `CONE(n)` | Cells up to n away within a 90-degree cone toward target; closest targets resolve first |
| `RADIUS(n)` | Chebyshev distance n around the selected center; `RADIUS_SELF(n)` centers on caster |
| `CHAIN(n,f)` | Initial target plus up to n-1 nearest unhit enemies; each jump uses factor f from the previous hit |

Unless a row says otherwise, target caps are `[3, 4, 5]` for line/cone/radius effects and unlimited for `ALL_ALLIES`. A multi-hit step resolves its target set once, snapshots the caster's damage modifiers, and schedules later hits against surviving members of that set. A zone snapshots owner, element, base value, AP, star, and outgoing multipliers at cast time; its incoming target modifiers and DEF are evaluated per tick.

### 5.2 Status definitions

Statuses are generic engine data keyed by id. “Strongest wins” means compare magnitude first, then remaining duration; reapplying the same status refreshes to the stronger/longer state instead of adding duration.

| Id | Default duration | Exact rules |
|---|---:|---|
| `burn` | 3.0 s | One Fire DOT tick each second at the ability-provided value. Strongest wins; no stacking. Uses Fire affinity and DEF. |
| `poison` | 4.0 s | One Dark DOT tick each second at the ability-provided value. Up to three stacks; each caster owns one stack, and that caster's reapplication refreshes/replaces its stack. Uses Dark affinity and DEF. |
| `freeze` | ability value | Hard control: cannot move, attack, or cast. Strongest wins. Does not alter damage and does not stack with `stun`; use the longer remaining hard-control time. |
| `ivy` | 3.0 s | Root: cannot move or be moved, but can attack and cast. Strongest wins. Fire damage removes Ivy after that hit resolves. |
| `muddy` | 4.0 s | `-20%` attack speed and movement is evaluated as one extra pathfinding cell of cost. Strongest wins. |
| `soak` | 4.0 s | Incoming Electric damage gains a `1.15` multiplier; incoming Fire damage gains a `0.85` multiplier. Apply after affinity, then remove Soak on the first Electric hit; Fire does not consume it. |
| `electrified` | 3.0 s | `-20%` attack speed. The next direct basic attack against this unit arcs Neutral damage equal to `25%` of that post-DEF hit to the nearest other enemy within 2 cells, then consumes Electrified. |
| `blind` | 4.0 s | Basic attacks have a deterministic RNG-driven `35%` miss chance. Abilities never miss. Strongest wins. |

Status DOT cannot grant mana to its victim and cannot trigger on-hit effects. Direct ability hits still grant the existing direct-hit mana once per target per cast, not once per multi-hit projectile. A dead caster's already-created zones and scheduled hits continue; they remain credited to that caster in the damage report.

### 5.3 Movement and durability rules

- `DASH_TO` moves the caster along the shortest legal path to the closest open cell adjacent to the target, up to the listed maximum cells, then resolves damage. If no legal destination exists, resolve damage from the current cell.
- `DASH_LINE` advances up to the listed cells along the aimed line and damages valid enemies crossed. Stop at the last open cell; never swap through occupied cells.
- `KNOCKBACK(n)` attempts n cells directly away from the caster, stopping before bounds, occupied cells, or an Ivy root. Damage still applies if movement fails.
- `PULL(n)` moves targets n cells toward the effect center using the same collision rules.
- Movement caused by an ability does not consume or reset the normal attack timer.
- Timed shields expire after the listed duration; taking damage consumes shield before HP using current semantics. Reapplication adds shield amount but refreshes expiry to the later time. The initial maximum shield cap is 100% of the recipient's max HP.
- Timed stat modifiers with the same `(source, stat)` replace themselves; different sources stack multiplicatively for percentages and additively for flat DEF.

## 6. Standard scaling abilities — 39 lines

Every row below is one ability definition used at all three stars. Arrays are star values. Damage/heal/shield values are base values before AP/outgoing modifiers. Unless specified, delayed-hit interval is 120 ms, zone tick interval is 1 second, and timed buffs last 5 seconds.

| Unit | Ability identity / name | Target and ordered effect steps |
|---|---|---|
| Lamball | `pw-lamball-fluffy-shield` / Fluffy Shield | `SELF`; shield caster and the two nearest allies within 2 for `[140, 250, 450]`, 5 s. |
| Cattiva | `pw-cattiva-cat-punch` / Cat Punch | `CURRENT SINGLE`; damage `[135, 245, 440]`. |
| Chikipi | `pw-chikipi-egg-drop` / Egg Drop | `LOWEST_ALLY`; heal the `[2, 2, 3]` lowest-health allies for `[100, 180, 325]`. |
| Foxparks | `pw-foxparks-huggy-fire` / Huggy Fire | `CURRENT CONE(2)`; damage `[150, 270, 485]`, then Burn for 3 s with tick `[12, 22, 40]`. |
| Daedream | `pw-daedream-dark-ball` / Dark Ball | `FARTHEST SINGLE`; damage `[145, 260, 470]`, then Blind for `[3.0, 3.5, 4.0]` s. |
| Depresso | `pw-depresso-caffeine-slap` / Caffeine Slap | `ALL_ALLIES`; +`[12%, 20%, 32%]` attack speed for 5 s, then grant `[5, 8, 12]` mana. |
| Gumoss | `pw-gumoss-sand-blast` / Sand Blast | `RADIUS_SELF(1)`; damage `[110, 200, 360]`, then Muddy for 4 s. |
| Vixy | `pw-vixy-dig-here` / Dig Here! | Target `[2, 2, 3]` lowest-health allies; heal `[75, 135, 245]`, then shield the same amount for 5 s. |
| Sparkit | `pw-sparkit-static-electricity` / Static Electricity | `ALL_ALLIES`; +`[10%, 16%, 24%]` attack speed for 5 s. |
| Fuack | `pw-fuack-surfing-slam` / Surfing Slam | `CURRENT`; `DASH_TO(3)`, then `RADIUS(1)` damage `[155, 280, 505]` and Soak for 4 s. |
| Direhowl | `pw-direhowl-fierce-fang` / Fierce Fang | `CURRENT`; `DASH_TO(4)`, `SINGLE` damage `[165, 300, 540]`, then caster gains `20%` attack speed for 5 s. |
| Celaray | `pw-celaray-zephyr-glider` / Zephyr Glider | `ALL_ALLIES`; shield `[100, 180, 325]` for 5 s and cleanse Muddy. |
| Dumud | `pw-dumud-earth-impact` / Earth Impact | `RADIUS_SELF(1)`; damage `[140, 250, 450]`, then Muddy for 4 s. |
| Dazzi | `pw-dazzi-lady-of-lightning` / Lady of Lightning | `CURRENT CHAIN([3,4,5], 0.80)`; first-hit damage `[125, 225, 405]`, then Electrified for 3 s on every hit target. |
| Flambelle | `pw-flambelle-magma-tears` / Magma Tears | `CLUSTER RADIUS(1)` zone, 4 ticks; damage `[45, 80, 145]` per tick, first tick applies Burn with tick `[10, 18, 32]`. |
| Mimog | `pw-mimog-surprise-box` / Surprise Box | `CURRENT`; shield `SELF` `[220, 400, 720]` for 5 s, then stun target for `[1.25, 1.50, 1.75]` s. |
| Cremis | `pw-cremis-fluffy-wool` / Fluffy Wool | `ALL_ALLIES`; heal `[90, 160, 290]`, then grant `[5, 8, 12]` DEF for 5 s. |
| Melpaca | `pw-melpaca-fluffy-tackle` / Fluffy Tackle | `CURRENT LINE(3)`; damage `[140, 250, 450]`, `KNOCKBACK(1)`, then shield self `[100, 180, 325]` for 5 s. |
| Galeclaw | `pw-galeclaw-gale-claw` / Gale Claw | `FARTHEST`; `DASH_TO(5)`, `SINGLE` damage `[180, 325, 585]`, then caster gains `25%` attack speed for 4 s. |
| Lovander | `pw-lovander-heart-drain` / Heart Drain | `CURRENT SINGLE`; damage `[130, 235, 420]`; heal caster and lowest-health other ally for `50%` of actual damage each. |
| Hoodle | `pw-hoodle-dark-whisp` / Dark Whisp | `FARTHEST SINGLE`; damage `[175, 315, 570]`, then Blind for 4 s. |
| Penking | `pw-penking-emperor-slide` / Emperor Slide | `CURRENT`; `DASH_TO(3)`, `RADIUS(1)` damage `[180, 325, 585]` and Soak 4 s; shield self `[180, 325, 585]` for 5 s. |
| Katress | `pw-katress-nightmare-ball` / Nightmare Ball | `CURRENT RADIUS(1)`; damage `[230, 415, 745]`, then Blind for 4 s. |
| Lunaris | `pw-lunaris-antigravity` / Antigravity | `CLUSTER RADIUS(1)`; stun `[1.25, 1.50, 1.75]` s and shred `[8, 12, 18]` DEF for 5 s. No damage. |
| Quivern | `pw-quivern-dragon-meteor` / Dragon Meteor | `CLUSTER RADIUS(1)`; damage `[190, 340, 610]`, then Burn 3 s with tick `[15, 25, 40]`. |
| Petallia | `pw-petallia-flower-spirit` / Blessing of the Flower Spirit | `ALL_ALLIES`; heal `[140, 250, 450]`, then +`[10%, 15%, 20%]` attack speed for 5 s. |
| Mossanda | `pw-mossanda-grenade-launcher` / Grenadier Panda | Three projectiles at `CLUSTER`, 180 ms apart; each `RADIUS(1)` damage `[85, 155, 280]`; final hit applies Ivy for 2.5 s. |
| Tarantriss | `pw-tarantriss-web-shooter` / Web Shooter | `CURRENT CONE(3)`; damage `[200, 360, 650]`, then Ivy for 3 s. |
| Relaxaurus | `pw-relaxaurus-hungry-missile` / Hungry Missile | Three missiles distributed round-robin over up to three nearest enemies; each `RADIUS(1)` damage `[110, 200, 360]`. |
| Tetroise | `pw-tetroise-cube-press` / Cube Press | `CURRENT`; `DASH_TO(2)`, `RADIUS(1)` damage `[220, 395, 710]` and Muddy 4 s; caster gains `[12, 20, 30]` DEF for 5 s. |
| Lyleen | `pw-lyleen-harvest-goddess` / Harvest Goddess | Heal the three lowest-health allies `[180, 325, 585]`; `ALL_ALLIES` gain +`[10%, 15%, 25%]` ability damage for 5 s. |
| Orserk | `pw-orserk-kerauno` / Kerauno | `CURRENT`; `DASH_TO(4)`, `SINGLE` damage `[260, 470, 845]`, then chain to two nearest unhit enemies at `45%`; all hit targets become Electrified for 3 s. |
| Selyne | `pw-selyne-seigetsu-blade` / Seigetsu Blade | `CURRENT LINE(4)`; damage `[250, 450, 810]` and shred `[8, 12, 18]` DEF for 5 s. |
| Jormuntide Ignis | `pw-jormuntide-ignis-stormbringer-lava` / Stormbringer Lava | `CURRENT CONE(3)`; damage `[230, 415, 745]`, then Burn 3 s with tick `[20, 35, 60]`. |
| Aegidron | `pw-aegidron-explosive-missile` / Explosive Missile | Four missiles round-robin over up to four nearest enemies, each `RADIUS(1)` damage `[90, 160, 290]`; shield self `[250, 450, 810]` for 5 s. |
| Renjishi | `pw-renjishi-volcanic-rain` / Volcanic Rain | `CLUSTER RADIUS(2)` zone, 5 ticks at 500 ms; damage `[65, 115, 210]` per tick; first tick applies Burn with tick `[15, 25, 45]`. |
| Silvance | `pw-silvance-spore-burst` / Spore Burst | `CLUSTER RADIUS(2)` damage `[210, 380, 680]` and Ivy 3 s; shield the three lowest-health allies `[100, 180, 325]` for 5 s. |
| Dandilord | `pw-dandilord-toxic-dance` / Toxic Dance | `RADIUS_SELF(2)` zone, 4 ticks; damage `[65, 115, 205]` per tick and add/refresh Poison with tick `[18, 32, 55]`; shield self `[180, 325, 585]` for 5 s. |
| Shaolong | `pw-shaolong-azure-dracoflare` / Azure Dracoflare | `CURRENT CONE(4)`; damage `[280, 505, 910]`, then Burn 3 s with tick `[20, 35, 60]`. |

## 7. Additional root abilities — 16 lines

The 39 abilities above and the 16 abilities below make one resolved root ability for each of the 55 Pals. Every ability is stored once at the unit root and is used at 1/2/3 stars; arrays are the star-scaled values. The first column is a stable ability identity for tooling and animation lookup, not a required JSON animation-key field. Offensive typing is derived from the caster's traits for every damaging step.

| Unit | Ability identity / name | Target and ordered effect steps |
|---|---|---|
| Lifmunk | `pw-lifmunk-seed-burst` / Seed Burst | `CURRENT LINE(4)`; damage `[150, 270, 485]`, then Ivy for `[2.0, 2.5, 3.0]` s. |
| Pengullet | `pw-pengullet-aqua-cannon` / Aqua Cannon | `CURRENT LINE(5)`; damage `[155, 330, 650]`, `KNOCKBACK([1, 1, 2])`, then Soak for 4 s. |
| Tanzee | `pw-tanzee-rocket-launcher` / Rocket Launcher | `CLUSTER RADIUS(2)`; damage `[150, 300, 600]`, `KNOCKBACK([0, 1, 1])`, then Burn for 3 s with tick `[20, 30, 45]`. |
| Tocotoco | `pw-tocotoco-megaton-egg` / Megaton Egg | `CLUSTER RADIUS(2)`; damage `[190, 390, 650]`, `KNOCKBACK(1)`, then stun `[1.0, 1.25, 1.5]` s. |
| Chillet | `pw-chillet-cryst-breath` / Cryst Breath | `CURRENT CONE(3)`; damage `[230, 420, 750]`, then Freeze `[1.0, 1.25, 1.5]` s. |
| Grizzbolt | `pw-grizzbolt-lightning-claw` / Lightning Claw | `CURRENT`; `DASH_TO(4)`, damage `[300, 500, 750]`, then chain to two nearest unhit enemies at `45%`; Electrified for 3 s on all hit targets. |
| Anubis | `pw-anubis-spinning-roundhouse` / Spinning Roundhouse | `RADIUS_SELF(2)`; damage `[320, 550, 850]`, `KNOCKBACK(1)`, then stun `[1.0, 1.25, 1.5]` s. |
| Shadowbeak | `pw-shadowbeak-divine-disaster` / Divine Disaster | `CURRENT RADIUS(1)`; damage `[340, 550, 850]`, then Blind for `[3.0, 3.5, 4.0]` s. |
| Bellanoir | `pw-bellanoir-nightmare-ray` / Nightmare Ray | `CURRENT LINE(5)`; damage `[260, 500, 780]`; heal caster and lowest-health other ally for `[25%, 30%, 40%]` of actual damage each. |
| Jetragon | `pw-jetragon-dragonic-meteor` / Dragonic Meteor | Five meteors over 800 ms at `CLUSTER`; each `RADIUS(1)` damage `[120, 165, 210]`, then final hit applies Burn for 3 s with tick `[35, 50, 60]`. |
| Frostallion | `pw-frostallion-blizzard-spike` / Blizzard Spike | `CLUSTER RADIUS(2)`; damage `[320, 520, 750]`, then Freeze `[1.25, 1.5, 1.75]` s; shield `ALL_ALLIES` for `[200, 325, 450]` for 5 s. |
| Paladius | `pw-paladius-holy-burst` / Holy Burst | `CLUSTER RADIUS(2)`; damage `[300, 550, 800]`; shield `ALL_ALLIES` for `[250, 375, 500]` for 6 s and cleanse Blind. |
| Necromus | `pw-necromus-nightmare-spear` / Nightmare Spear | `CURRENT LINE(4)`; damage `[380, 600, 850]`; add `[20%, 28%, 35%]` damage when each target is below 20% max HP. |
| Neptilius | `pw-neptilius-ocean-sovereign` / Ocean Sovereign | `CLUSTER RADIUS(2)`; four spear hits of `[150, 190, 230]`, 150 ms apart, then Soak for 4 s; shield self for `[350, 500, 700]` for 6 s. |
| Xenolord | `pw-xenolord-mystic-whirlwind` / Mystic Whirlwind | `CURRENT`; `DASH_TO(5)`, then `RADIUS(2)` damage `[400, 700, 1000]`, `KNOCKBACK(2)`, and Burn tick `[45, 55, 65]` for 3 s. |
| Panthalus | `pw-panthalus-aqua-tornado` / Aqua Tornado | Four tracking zones assigned round-robin to up to four enemies; each resolves `RADIUS(1)` damage `[70, 150, 250]`, `PULL(1)`, and Soak for 4 s. |

## 8. Ability resolution rules

Use this order for each direct damaging effect:

1. Resolve living targets deterministically.
2. Snapshot the base value, caster ability-damage multiplier, conditional/execute modifier, and caster trait elements.
3. Apply outgoing ability multipliers.
4. Apply elemental affinity against the target's current defensive elements.
5. Apply target damage reduction and DEF using existing formulas.
6. Consume shields, then HP.
7. Grant direct-hit mana once per target per cast.
8. Apply status and forced movement in the order written in the ability table.
9. Attribute actual HP/shield damage and any resulting kill to the caster.

Each cast consumes all current mana and schedules all steps atomically. A cast must not begin if the caster is dead or hard-controlled. Once emitted, later steps survive caster death. If no valid enemy exists for an enemy-targeted ability, cancel without consuming mana; if the selected target dies during a pre-impact delay, reacquire according to the declared selector. Ally-only skills still cast while at least one living ally exists.

Composite healing uses the existing heal amplification. Lifesteal and “heal for actual damage” count HP and shield damage actually dealt after mitigation, never overkill. Shields do not receive heal amplification. Buff percentages use multiplicative stacking and expire cleanly at combat end.

## 9. Palworld augments — 15 definitions

These are a naming/description skin over the existing augment effect types and established values. All three tiers are present in each definition in Bronze/Silver/Gold order. `image` remains `null` for 2.0 and uses the existing placeholder.

| Id | Name | Existing effect type | Values | Exact descriptions by tier |
|---|---|---|---|---|
| `swift-passive` | Swift Passive | `TEAM_ATTACK_SPEED_PER_RANGED_UNIT` | `[3, 5, 8]` | “Your team gains 3/5/8% Attack Speed for each ranged Pal on your board.” |
| `burly-body` | Burly Body | `TEAM_DAMAGE_REDUCTION` | `[5, 10, 15]` | “Your team takes 5/10/15% reduced damage.” |
| `musclehead-momentum` | Musclehead Momentum | `TEAM_ATTACK_DAMAGE_ON_KILL` | `[6, 10, 15]` | “When one of your Pals gets a kill, your team gains 6/10/15 Attack Damage for the rest of combat.” |
| `pal-sphere-reserves` | Pal Sphere Reserves | `GOLD` | `[10, 16, 24]` | “Gain 10/16/24 gold.” |
| `training-manual` | Training Manual | `XP` | `[8, 16, 24]` | “Gain 8/16/24 XP.” |
| `workforce-wellness` | Workforce Wellness | `TEAM_MAX_HEALTH` | `[120, 220, 360]` | “Your team gains 120/220/360 Health.” |
| `ferocious-passive` | Ferocious Passive | `TEAM_ATTACK_DAMAGE` | `[6, 10, 15]` | “Your team gains 6/10/15 Attack Damage.” |
| `condenser-calibration` | Condenser Calibration | `TEAM_ABILITY_POWER` | `[10, 18, 30]` | “Your team's damaging abilities deal 10/18/30% more damage.” |
| `plasteel-armor` | Plasteel Armor | `TEAM_DEFENSE` | `[8, 14, 22]` | “Your team gains 8/14/22 DEF.” |
| `vanguard-partner` | Vanguard Partner | `MELEE_LIFESTEAL` | `[12, 20, 30]` | “Melee Pals gain 12/20/30% Lifesteal.” |
| `marksman-manual` | Marksman Manual | `RANGED_ATTACK_DAMAGE` | `[7, 11, 16]` | “Ranged Pals gain 7/11/16 Attack Damage.” |
| `prankster-mana` | Prankster Mana | `TEAM_MANA_GAIN` | `[15, 25, 40]` | “Your team gains 15/25/40% more mana from attacks.” |
| `burden-of-the-future` | Burden of the Future | `TEAM_STARTING_MANA` | `[10, 15, 20]` | “Your team starts combat with 10/15/20 additional mana.” |
| `organized-palbox` | Organized Palbox | `GOLD_PER_EMPTY_BENCH_SLOT` | `[1, 2, 3]` | “Gain 1/2/3 gold for each empty Palbox slot.” The engine still counts empty bench slots. |
| `shield-module` | Shield Module | `TEAM_STARTING_SHIELD` | `[125, 225, 375]` | “Your team starts combat with a 125/225/375 shield.” |

In JSON, write three separate description strings with the selected numeric value; do not serialize slash notation. Augment offers, tier odds, selection rounds, stacking, and UI behavior remain unchanged.

## 10. Tooltip copy rules

- Display canonical element names in the declared order and use the trait colors.
- Display the unit's role and “Melee” for range 1 or “Ranged (N)” for range N.
- Display the single root ability name and the current star's formatted numbers. Do not show inactive star abilities because none exist.
- Status names are title case and their rules are available from a shared glossary tooltip. Avoid promising Palworld's original percentages or cooldowns.
- Ability descriptions must state target shape, damage/heal/shield amount, status duration, hit count, and special movement/recoil. Do not expose implementation terms such as `CLUSTER` or `RADIUS(2)`; translate them to natural language.
- Damage numbers inherit the trait element selected by the affinity resolver for that target. Neutral is gray, never untyped white.

## 11. Canonical validation assertions

Automated data validation must assert all of the following:

- 55 unique `id` and `lineId` values; `id == lineId` for every row.
- Costs `[1:12, 2:13, 3:11, 4:12, 5:7]`.
- Roles `[DAMAGE:23, TANK:16, SUPPORT:16]`.
- Melee/ranged `[21, 34]` using `range == 1`.
- Exactly eight 1.0 roster ids: `hoodle`, `tetroise`, `aegidron`, `renjishi`, `silvance`, `dandilord`, `shaolong`, `panthalus`.
- Every stat list has exactly three non-negative values; HP, mana, and ATK are positive; range is 1–4; attack speed is positive.
- Every unit has one or two unique valid trait elements and no separate basic-attack element.
- Every unit has one non-null root ability with exactly three-star values; no unit has star-specific ability overrides.
- The 55 root ability identities are unique and stable for tooltip/gallery lookup; no animation key is required in JSON.
- Nine element traits exist with four strictly increasing breakpoints `[1, 2, 3, 4]`, `TEAM` target scope, a supported generic effect, and valid style/color metadata.
- The affinity graph contains only the nine strong edges implied by the table (Fire has two; seven other attacking elements have one; Neutral has none), all strong multipliers are `1.20`, all resisted multipliers are `0.80`, and Neutral has no strength.
- Exactly 15 augments exist; all effect types are already supported by `AugmentManager`; each has three values and three descriptions.

## 12. Canonical-source notes

Canonical Pal elements and recognizable skill names were checked against the [Palworld Wiki element documentation](https://palworld.wiki.gg/wiki/Elements) and versioned [PalDB Pal reference](https://paldb.cc/en/Pal). The roster emphasizes official poll favorites such as Chillet, Jetragon, and Anubis, while reserving eight slots for 1.0 Pals. `Astralym` is intentionally excluded because it is not a normal purchasable/catchable Pal reference and did not provide a stable canonical element for this data freeze.

Palworld's source-game power, cooldown, partner-skill, and status numbers are not copied. The abilities above translate recognizable actions into deterministic, bounded 9x6 auto-battler mechanics.
