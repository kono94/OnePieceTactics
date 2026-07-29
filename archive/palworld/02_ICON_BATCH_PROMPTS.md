# Palworld Unit Icon Production — Exact 2×2 Batch Prompts

This document is the complete manual image-generation handoff for 55 Pal portraits and one Pal Sphere favicon. Generate one 1024×1024 PNG per prompt, preserving the stated quadrant order. Do not edit a prompt's characters, order, colors, or composition unless the corresponding roster decision changes in `01_GAME_DESIGN_SPEC.md`.

## 1. Production contract

- Attach the approved Pokemon unit-icon style reference image to every generation request. The reference controls the modern pixel-art rendering quality; this file controls content and layout.
- Generate a single square 1024×1024 image with four equal 512×512 quadrants and no divider lines.
- Keep each character recognizable at small shop-icon size: head plus upper body, centered, with distinguishing ears, horns, wings, accessories, markings, and silhouette visible.
- Use the exact flat pastel background color for the Pal's primary element. For dual-element Pals, retain the primary background and add only subtle accents in the secondary element color.
- No words, letters, numbers, logos, signatures, watermarks, UI frames, borders, weapons held by a human, trainers, extra Pals, duplicated limbs, or scenery crossing quadrant boundaries.
- Do not crop ears, horns, crown points, wing tips, or other identity-critical features. Do not zoom out to a full-body scene.
- Preserve thick dark navy/black pixel outlines, a chunky pixel grid, saturated cel shading, crisp edges, and readable facial features.
- Save untouched grids as `palworld_batch_01.png` through `palworld_batch_14.png` in `frontend/public/assets/units/palworld/_generated_batches/` during ingestion. This scratch directory should remain ignored.

## 2. Element background palette

| Element | Background | Secondary accent language |
|---|---|---|
| Neutral | `#E9E6DE` | soft dust, wool, or light pixels |
| Fire | `#FFD6B8` | embers and warm flame sparks |
| Water | `#BFEFFF` | bubbles, droplets, or ripple pixels |
| Electric | `#FFF4A8` | tiny angular lightning pixels |
| Grass | `#C9F3C2` | leaves, pollen, seeds, or vine pixels |
| Ice | `#D7F7FF` | snow crystals and ice-mist pixels |
| Ground | `#E8D0A5` | pebbles, sand, and dust pixels |
| Dark | `#D8C4F0` | violet shadow wisps and starless motes |
| Dragon | `#CFC8FF` | violet draconic energy motes |

## 3. Exact generation prompts

### Batch 01 — `palworld_batch_01.png`

Quadrants: TL `lamball`, TR `cattiva`, BL `chikipi`, BR `foxparks`.

```text
Create a single 1024x1024 image split into four equal quadrants for auto-battler unit icons. I will provide a reference image of the exact modern pixel-art style to follow; keep each Pal slightly more visible than a face-only portrait, with the head and upper body clearly shown.
Top-left: Lamball, unmistakable round white wool, small dark face, curled brown horns and gentle expression, on a flat soft pastel Neutral-element background #E9E6DE with subtle wool-puff pixel accents.
Top-right: Cattiva, unmistakable pink cat-like Pal with pointed ears, cream muzzle, striped forehead and mischievous determined expression, on a flat soft pastel Neutral-element background #E9E6DE with subtle paw and dust pixel accents.
Bottom-left: Chikipi, unmistakable plump cream chicken-like Pal with red comb, orange beak and wide alert eyes, on a flat soft pastel Neutral-element background #E9E6DE with subtle feather and eggshell pixel accents.
Bottom-right: Foxparks, unmistakable small orange fox-like Fire Pal with flame-shaped tail and bright cream facial markings, on a flat soft pastel Fire-element background #FFD6B8 with subtle ember pixel accents.
Style: close-cropped Pal portrait, head and upper body visible, modern high-quality pixel art, thick dark navy/black pixel outline, chunky pixel grid, saturated cel-shaded colors, crisp edges, centered and filling most of each quadrant. Preserve each Pal's canonical silhouette and color markings. Exactly one Pal per quadrant. No text, letters, numbers, logo, watermark, UI frame, decorative border, black grid lines, or visible divider lines between quadrants. Keep all art contained within its own quadrant.
```

### Batch 02 — `palworld_batch_02.png`

Quadrants: TL `lifmunk`, TR `pengullet`, BL `daedream`, BR `depresso`.

```text
Create a single 1024x1024 image split into four equal quadrants for auto-battler unit icons. I will provide a reference image of the exact modern pixel-art style to follow; keep each Pal slightly more visible than a face-only portrait, with the head and upper body clearly shown.
Top-left: Lifmunk, unmistakable small green squirrel-like Pal with leafy tail, cream face and cheerful focused eyes, on a flat soft pastel Grass-element background #C9F3C2 with subtle leaf and seed pixel accents.
Top-right: Pengullet, unmistakable small blue penguin-like Pal with white face and belly, orange beak and icy tuft, on a flat soft pastel Water-element background #BFEFFF with subtle secondary Ice accents #D7F7FF, bubbles and ice-mist pixels.
Bottom-left: Daedream, unmistakable floating dark dream-like Pal with lavender hair, crescent details, glowing eyes and shadowy body, on a flat soft pastel Dark-element background #D8C4F0 with subtle violet shadow-wisp pixels.
Bottom-right: Depresso, unmistakable small blue-gray cat-like Pal with drooping ears, heavy-lidded eyes and deeply unimpressed expression, on a flat soft pastel Dark-element background #D8C4F0 with subtle gloomy shadow pixels.
Style: close-cropped Pal portrait, head and upper body visible, modern high-quality pixel art, thick dark navy/black pixel outline, chunky pixel grid, saturated cel-shaded colors, crisp edges, centered and filling most of each quadrant. Preserve each Pal's canonical silhouette and color markings. Exactly one Pal per quadrant. No text, letters, numbers, logo, watermark, UI frame, decorative border, black grid lines, or visible divider lines between quadrants. Keep all art contained within its own quadrant.
```

### Batch 03 — `palworld_batch_03.png`

Quadrants: TL `gumoss`, TR `vixy`, BL `sparkit`, BR `tanzee`.

```text
Create a single 1024x1024 image split into four equal quadrants for auto-battler unit icons. I will provide a reference image of the exact modern pixel-art style to follow; keep each Pal slightly more visible than a face-only portrait, with the head and upper body clearly shown.
Top-left: Gumoss, unmistakable squat green moss-covered Pal with sprouting leaves, simple dot eyes and earthy underside, on a flat soft pastel Grass-element background #C9F3C2 with subtle secondary Ground accents #E8D0A5, leaf and soil pixels.
Top-right: Vixy, unmistakable small sandy fox-like Pal with enormous pointed ears, fluffy cream ruff and bright innocent eyes, on a flat soft pastel Neutral-element background #E9E6DE with subtle sparkle and dust pixel accents.
Bottom-left: Sparkit, unmistakable tiny yellow electric cat-like Pal with black-tipped ears, lightning-bolt tail and eager face, on a flat soft pastel Electric-element background #FFF4A8 with subtle angular lightning pixels.
Bottom-right: Tanzee, unmistakable small green monkey-like Pal with leaf-like ears, cream muzzle and energetic expression, on a flat soft pastel Grass-element background #C9F3C2 with subtle leaf and pollen pixel accents.
Style: close-cropped Pal portrait, head and upper body visible, modern high-quality pixel art, thick dark navy/black pixel outline, chunky pixel grid, saturated cel-shaded colors, crisp edges, centered and filling most of each quadrant. Preserve each Pal's canonical silhouette and color markings. Exactly one Pal per quadrant. No text, letters, numbers, logo, watermark, UI frame, decorative border, black grid lines, or visible divider lines between quadrants. Keep all art contained within its own quadrant.
```

### Batch 04 — `palworld_batch_04.png`

Quadrants: TL `fuack`, TR `tocotoco`, BL `direhowl`, BR `celaray`.

```text
Create a single 1024x1024 image split into four equal quadrants for auto-battler unit icons. I will provide a reference image of the exact modern pixel-art style to follow; keep each Pal slightly more visible than a face-only portrait, with the head and upper body clearly shown.
Top-left: Fuack, unmistakable round blue duck-like Pal with yellow bill, tiny body and worried wide-eyed expression, on a flat soft pastel Water-element background #BFEFFF with subtle bubble and droplet pixel accents.
Top-right: Tocotoco, unmistakable colorful toucan-like Pal with enormous patterned beak, bright crest and manic eyes, on a flat soft pastel Neutral-element background #E9E6DE with subtle feather and explosive egg pixel accents.
Bottom-left: Direhowl, unmistakable dark gray wolf-like Pal with sharp cream facial markings, swept ears and fierce eyes, on a flat soft pastel Neutral-element background #E9E6DE with subtle speed-line and dust pixel accents.
Bottom-right: Celaray, unmistakable pale blue manta-ray Pal with broad wing fins, white underside and friendly face, on a flat soft pastel Water-element background #BFEFFF with subtle bubbles and flowing water pixels.
Style: close-cropped Pal portrait, head and upper body visible, modern high-quality pixel art, thick dark navy/black pixel outline, chunky pixel grid, saturated cel-shaded colors, crisp edges, centered and filling most of each quadrant. Preserve each Pal's canonical silhouette and color markings. Exactly one Pal per quadrant. No text, letters, numbers, logo, watermark, UI frame, decorative border, black grid lines, or visible divider lines between quadrants. Keep all art contained within its own quadrant.
```

### Batch 05 — `palworld_batch_05.png`

Quadrants: TL `dumud`, TR `dazzi`, BL `flambelle`, BR `mimog`.

```text
Create a single 1024x1024 image split into four equal quadrants for auto-battler unit icons. I will provide a reference image of the exact modern pixel-art style to follow; keep each Pal slightly more visible than a face-only portrait, with the head and upper body clearly shown.
Top-left: Dumud, unmistakable round brown mud-fish Pal with broad smile, small fin-like ears and muddy body, on a flat soft pastel Ground-element background #E8D0A5 with subtle mud-splash and pebble pixels.
Top-right: Dazzi, unmistakable small floating golden cloud-spirit Pal with dark body, long yellow hair and electric ornaments, on a flat soft pastel Electric-element background #FFF4A8 with subtle lightning pixel accents.
Bottom-left: Flambelle, unmistakable tiny white candle-flame Pal with orange fire hair, teardrop-shaped body and timid face, on a flat soft pastel Fire-element background #FFD6B8 with subtle ember and molten droplet pixels.
Bottom-right: Mimog, unmistakable mimic Pal disguised as a small weathered treasure chest with bright eyes, tongue and toothy surprise, on a flat soft pastel Neutral-element background #E9E6DE with subtle coin and dust pixel accents.
Style: close-cropped Pal portrait, head and upper body visible, modern high-quality pixel art, thick dark navy/black pixel outline, chunky pixel grid, saturated cel-shaded colors, crisp edges, centered and filling most of each quadrant. Preserve each Pal's canonical silhouette and color markings. Exactly one Pal per quadrant. No text, letters, numbers, logo, watermark, UI frame, decorative border, black grid lines, or visible divider lines between quadrants. Keep all art contained within its own quadrant.
```

### Batch 06 — `palworld_batch_06.png`

Quadrants: TL `cremis`, TR `melpaca`, BL `galeclaw`, BR `lovander`.

```text
Create a single 1024x1024 image split into four equal quadrants for auto-battler unit icons. I will provide a reference image of the exact modern pixel-art style to follow; keep each Pal slightly more visible than a face-only portrait, with the head and upper body clearly shown.
Top-left: Cremis, unmistakable tiny cream fox-like Pal almost hidden in an enormous fluffy mane, with long ears and gentle face, on a flat soft pastel Neutral-element background #E9E6DE with subtle soft wool and sparkle pixels.
Top-right: Melpaca, unmistakable cream-and-tan alpaca Pal with very long neck, woolly chest, blue eyes and proud expression, on a flat soft pastel Neutral-element background #E9E6DE with subtle wool and breeze pixels.
Bottom-left: Galeclaw, unmistakable red, cream and dark-gray eagle-like Pal with broad angular wings, hooked beak and intense eyes, on a flat soft pastel Neutral-element background #E9E6DE with subtle wind-slice pixel accents.
Bottom-right: Lovander, unmistakable tall pink heart-themed lizard-like Pal with heart-shaped chest marking, long ears and confident playful expression, on a flat soft pastel Neutral-element background #E9E6DE with subtle heart and sparkle pixels.
Style: close-cropped Pal portrait, head and upper body visible, modern high-quality pixel art, thick dark navy/black pixel outline, chunky pixel grid, saturated cel-shaded colors, crisp edges, centered and filling most of each quadrant. Preserve each Pal's canonical silhouette and color markings. Exactly one Pal per quadrant. No text, letters, numbers, logo, watermark, UI frame, decorative border, black grid lines, or visible divider lines between quadrants. Keep all art contained within its own quadrant.
```

### Batch 07 — `palworld_batch_07.png`

Quadrants: TL `hoodle`, TR `chillet`, BL `penking`, BR `katress`.

```text
Create a single 1024x1024 image split into four equal quadrants for auto-battler unit icons. I will provide a reference image of the exact modern pixel-art style to follow; keep each Pal slightly more visible than a face-only portrait, with the head and upper body clearly shown.
Top-left: Hoodle, unmistakable tiny hooded black ghost-like Pal with a hollow floating cloak silhouette, glowing eyes and pointed hood, on a flat soft pastel Dark-element background #D8C4F0 with subtle void-wisp pixel accents.
Top-right: Chillet, unmistakable long pale-blue ferret-dragon Pal with tiny legs, fin-like ears and adorably cheerful face, curved into the portrait, on a flat soft pastel Ice-element background #D7F7FF with subtle secondary Dragon accents #CFC8FF, ice crystals and violet energy motes.
Bottom-left: Penking, unmistakable regal blue penguin Pal with white beard-like chest, red-and-gold crown crest and stern emperor expression, on a flat soft pastel Water-element background #BFEFFF with subtle secondary Ice accents #D7F7FF, bubbles and frost pixels.
Bottom-right: Katress, unmistakable purple cat-like witch Pal with large pointed hat-like ears, gold accents and mysterious yellow eyes, on a flat soft pastel Dark-element background #D8C4F0 with subtle shadow magic pixels.
Style: close-cropped Pal portrait, head and upper body visible, modern high-quality pixel art, thick dark navy/black pixel outline, chunky pixel grid, saturated cel-shaded colors, crisp edges, centered and filling most of each quadrant. Preserve each Pal's canonical silhouette and color markings. Exactly one Pal per quadrant. No text, letters, numbers, logo, watermark, UI frame, decorative border, black grid lines, or visible divider lines between quadrants. Keep all art contained within its own quadrant.
```

### Batch 08 — `palworld_batch_08.png`

Quadrants: TL `lunaris`, TR `quivern`, BL `petallia`, BR `mossanda`.

```text
Create a single 1024x1024 image split into four equal quadrants for auto-battler unit icons. I will provide a reference image of the exact modern pixel-art style to follow; keep each Pal slightly more visible than a face-only portrait, with the head and upper body clearly shown.
Top-left: Lunaris, unmistakable elegant pale alien-like Pal with crescent-moon horns, long ears, teal eyes and flowing body markings, on a flat soft pastel Neutral-element background #E9E6DE with subtle moonlight and antigravity pixels.
Top-right: Quivern, unmistakable large fluffy white dragon Pal with pale-blue wings, curved horns and warm friendly face, on a flat soft pastel Dragon-element background #CFC8FF with subtle draconic wind motes.
Bottom-left: Petallia, unmistakable small green flower-spirit Pal with white petal dress, pink flower crown and gentle face, on a flat soft pastel Grass-element background #C9F3C2 with subtle petals and pollen pixels.
Bottom-right: Mossanda, unmistakable massive black-and-green panda Pal with leafy markings, round ears and determined face, on a flat soft pastel Grass-element background #C9F3C2 with subtle leaf and grenade-smoke pixel accents, no text.
Style: close-cropped Pal portrait, head and upper body visible, modern high-quality pixel art, thick dark navy/black pixel outline, chunky pixel grid, saturated cel-shaded colors, crisp edges, centered and filling most of each quadrant. Preserve each Pal's canonical silhouette and color markings. Exactly one Pal per quadrant. No text, letters, numbers, logo, watermark, UI frame, decorative border, black grid lines, or visible divider lines between quadrants. Keep all art contained within its own quadrant.
```

### Batch 09 — `palworld_batch_09.png`

Quadrants: TL `grizzbolt`, TR `tarantriss`, BL `relaxaurus`, BR `tetroise`.

```text
Create a single 1024x1024 image split into four equal quadrants for auto-battler unit icons. I will provide a reference image of the exact modern pixel-art style to follow; keep each Pal slightly more visible than a face-only portrait, with the head and upper body clearly shown.
Top-left: Grizzbolt, unmistakable bulky black bear-like Pal with bright yellow lightning markings, pointed ears, heavy arms and fierce grin, on a flat soft pastel Electric-element background #FFF4A8 with subtle lightning pixel accents.
Top-right: Tarantriss, unmistakable dark spider-like Pal with sharp crimson markings, multiple angular legs framing the upper body and predatory eyes, on a flat soft pastel Dark-element background #D8C4F0 with subtle web and shadow pixels.
Bottom-left: Relaxaurus, unmistakable large round blue dinosaur-dragon Pal with tiny arms, cream belly and comically vacant happy face, on a flat soft pastel Dragon-element background #CFC8FF with subtle secondary Water accents #BFEFFF, bubbles and violet energy motes.
Bottom-right: Tetroise, unmistakable massive ancient tortoise Pal with blocky tan stone slabs stacked across its shell, sturdy face and heavy silhouette, on a flat soft pastel Ground-element background #E8D0A5 with subtle rock and sand pixels.
Style: close-cropped Pal portrait, head and upper body visible, modern high-quality pixel art, thick dark navy/black pixel outline, chunky pixel grid, saturated cel-shaded colors, crisp edges, centered and filling most of each quadrant. Preserve each Pal's canonical silhouette and color markings. Exactly one Pal per quadrant. No text, letters, numbers, logo, watermark, UI frame, decorative border, black grid lines, or visible divider lines between quadrants. Keep all art contained within its own quadrant.
```

### Batch 10 — `palworld_batch_10.png`

Quadrants: TL `anubis`, TR `shadowbeak`, BL `lyleen`, BR `orserk`.

```text
Create a single 1024x1024 image split into four equal quadrants for auto-battler unit icons. I will provide a reference image of the exact modern pixel-art style to follow; keep each Pal slightly more visible than a face-only portrait, with the head and upper body clearly shown.
Top-left: Anubis, unmistakable black-and-gold jackal warrior Pal with tall ears, blue cloth accents and composed martial expression, on a flat soft pastel Ground-element background #E8D0A5 with subtle sand and stone-energy pixels.
Top-right: Shadowbeak, unmistakable black avian griffin-like Pal with enormous swept violet crest, sharp pale beak and glowing sinister eyes, on a flat soft pastel Dark-element background #D8C4F0 with subtle nightmare and shadow pixels.
Bottom-left: Lyleen, unmistakable elegant green-and-white flower queen Pal with leafy crown, pink blossom accents and serene face, on a flat soft pastel Grass-element background #C9F3C2 with subtle petals and healing pollen pixels.
Bottom-right: Orserk, unmistakable black dragon Pal with vivid yellow lightning armor markings, long horns and fierce angular face, on a flat soft pastel Dragon-element background #CFC8FF with subtle secondary Electric accents #FFF4A8 and sharp lightning pixels.
Style: close-cropped Pal portrait, head and upper body visible, modern high-quality pixel art, thick dark navy/black pixel outline, chunky pixel grid, saturated cel-shaded colors, crisp edges, centered and filling most of each quadrant. Preserve each Pal's canonical silhouette and color markings. Exactly one Pal per quadrant. No text, letters, numbers, logo, watermark, UI frame, decorative border, black grid lines, or visible divider lines between quadrants. Keep all art contained within its own quadrant.
```

### Batch 11 — `palworld_batch_11.png`

Quadrants: TL `selyne`, TR `jormuntide-ignis`, BL `bellanoir`, BR `aegidron`.

```text
Create a single 1024x1024 image split into four equal quadrants for auto-battler unit icons. I will provide a reference image of the exact modern pixel-art style to follow; keep each Pal slightly more visible than a face-only portrait, with the head and upper body clearly shown.
Top-left: Selyne, unmistakable celestial dark-and-pale moon Pal with crescent ornaments, elegant wing-like shapes and luminous face, on a flat soft pastel Dark-element background #D8C4F0 with subtle secondary Neutral accents #E9E6DE and moonlight pixels.
Top-right: Jormuntide Ignis, unmistakable enormous serpentine red-and-black dragon Pal with flaming mane, curled horns and powerful face, on a flat soft pastel Dragon-element background #CFC8FF with subtle secondary Fire accents #FFD6B8 and ember pixels.
Bottom-left: Bellanoir, unmistakable tall spectral purple raid Pal with crown-like horns, black flowing body, pale mask-like face and haunting eyes, on a flat soft pastel Dark-element background #D8C4F0 with subtle nightmare bloom pixels.
Bottom-right: Aegidron, unmistakable huge armored dragon Pal with dome-like plated body, luminous inner wing panels and heavy guardian silhouette, on a flat soft pastel Dragon-element background #CFC8FF with subtle secondary Ground accents #E8D0A5, rock and signal-light pixels.
Style: close-cropped Pal portrait, head and upper body visible, modern high-quality pixel art, thick dark navy/black pixel outline, chunky pixel grid, saturated cel-shaded colors, crisp edges, centered and filling most of each quadrant. Preserve each Pal's canonical silhouette and color markings. Exactly one Pal per quadrant. No text, letters, numbers, logo, watermark, UI frame, decorative border, black grid lines, or visible divider lines between quadrants. Keep all art contained within its own quadrant.
```

### Batch 12 — `palworld_batch_12.png`

Quadrants: TL `renjishi`, TR `silvance`, BL `dandilord`, BR `shaolong`.

```text
Create a single 1024x1024 image split into four equal quadrants for auto-battler unit icons. I will provide a reference image of the exact modern pixel-art style to follow; keep each Pal slightly more visible than a face-only portrait, with the head and upper body clearly shown.
Top-left: Renjishi, unmistakable fiery kabuki lion-warrior Pal with theatrical red mane, bold face markings and commanding stare, on a flat soft pastel Fire-element background #FFD6B8 with subtle ember and volcanic pixel accents.
Top-right: Silvance, unmistakable tall green moth-like World Tree guardian Pal with broad patterned wings, elegant antennae and luminous spore details, on a flat soft pastel Grass-element background #C9F3C2 with subtle explosive spore and leaf pixels.
Bottom-left: Dandilord, unmistakable regal humanoid flower guardian Pal with layered green petals, dark mist details and princely silhouette, on a flat soft pastel Grass-element background #C9F3C2 with subtle secondary Dark accents #D8C4F0, poisonous mist and petal pixels.
Bottom-right: Shaolong, unmistakable majestic azure eastern dragon Pal with long whiskers, elegant horns, water-blue scales and ring ornament, on a flat soft pastel Dragon-element background #CFC8FF with subtle secondary Water accents #BFEFFF, flowing water and azure flame pixels.
Style: close-cropped Pal portrait, head and upper body visible, modern high-quality pixel art, thick dark navy/black pixel outline, chunky pixel grid, saturated cel-shaded colors, crisp edges, centered and filling most of each quadrant. Preserve each Pal's canonical silhouette and color markings. Exactly one Pal per quadrant. No text, letters, numbers, logo, watermark, UI frame, decorative border, black grid lines, or visible divider lines between quadrants. Keep all art contained within its own quadrant.
```

### Batch 13 — `palworld_batch_13.png`

Quadrants: TL `jetragon`, TR `frostallion`, BL `paladius`, BR `necromus`.

```text
Create a single 1024x1024 image split into four equal quadrants for auto-battler unit icons. I will provide a reference image of the exact modern pixel-art style to follow; keep each Pal slightly more visible than a face-only portrait, with the head and upper body clearly shown.
Top-left: Jetragon, unmistakable sleek legendary jet dragon Pal with black body, bright cyan energy channels, swept wings and aerodynamic face, on a flat soft pastel Dragon-element background #CFC8FF with subtle beam and comet pixels.
Top-right: Frostallion, unmistakable legendary icy white horse Pal with crystalline blue mane, long ice horn and elegant armored face, on a flat soft pastel Ice-element background #D7F7FF with subtle snowflake and crystal pixels.
Bottom-left: Paladius, unmistakable legendary white centaur-knight Pal with gold armor, blue plume, long radiant spear and noble face, on a flat soft pastel Neutral-element background #E9E6DE with subtle holy light pixels; keep the spear inside the quadrant.
Bottom-right: Necromus, unmistakable legendary black centaur-knight Pal with violet armor, dark plume, twin ominous spears and fierce face, on a flat soft pastel Dark-element background #D8C4F0 with subtle nightmare shadow pixels; keep both spears inside the quadrant.
Style: close-cropped Pal portrait, head and upper body visible, modern high-quality pixel art, thick dark navy/black pixel outline, chunky pixel grid, saturated cel-shaded colors, crisp edges, centered and filling most of each quadrant. Preserve each Pal's canonical silhouette and color markings. Exactly one Pal per quadrant. No text, letters, numbers, logo, watermark, UI frame, decorative border, black grid lines, or visible divider lines between quadrants. Keep all art contained within its own quadrant.
```

### Batch 14 — `palworld_batch_14.png`

Quadrants: TL `neptilius`, TR `xenolord`, BL `panthalus`, BR Pal Sphere favicon.

```text
Create a single 1024x1024 image split into four equal quadrants for three auto-battler unit icons and one favicon source. I will provide a reference image of the exact modern pixel-art style to follow; keep each Pal slightly more visible than a face-only portrait, with the head and upper body clearly shown.
Top-left: Neptilius, unmistakable legendary dark-blue orca sea-warrior Pal with water spear, pale aquatic markings and powerful armored face, on a flat soft pastel Water-element background #BFEFFF with subtle spear, bubble and wave pixels; keep the spear inside the quadrant.
Top-right: Xenolord, unmistakable colossal alien dragon Pal with black crystalline armor, violet cosmic energy, sharp crown-like horns and menacing face, on a flat soft pastel Dragon-element background #CFC8FF with subtle secondary Dark accents #D8C4F0, cosmic meteor and void pixels.
Bottom-left: Panthalus, unmistakable legendary colossal whale-like ocean guardian Pal with immense rounded head, pale water markings and ancient gentle expression, on a flat soft pastel Water-element background #BFEFFF with subtle bubble, maelstrom and wave pixels.
Bottom-right: one centered iconic Pal Sphere, the familiar blue-and-white spherical capture device with its dark band and bright central button, shown as a clean three-quarter pixel-art object with no Pal and no hands, on a flat bright Palpagos sky-cyan background #BFEFFF with a few restrained coral #FF7F6E spark pixels and a subtle warm-gold #D9A441 glint. Keep generous safe padding so it remains legible when cropped to a circular browser favicon.
Style: close-cropped Pal portrait for the first three quadrants and a centered object icon for the fourth, modern high-quality pixel art, thick dark navy/black pixel outline, chunky pixel grid, saturated cel-shaded colors, crisp edges, centered and filling most of each quadrant. Preserve each Pal's canonical silhouette and color markings. Exactly one subject per quadrant. No text, letters, numbers, logo, watermark, UI frame, decorative border, black grid lines, or visible divider lines between quadrants. Keep all art contained within its own quadrant.
```

## 4. Deterministic cut-and-name manifest

The cutter numbers quadrants TL/TR/BL/BR as `q1/q2/q3/q4`. Install crops under `frontend/public/assets/units/palworld/`.

| Batch | q1 | q2 | q3 | q4 |
|---:|---|---|---|---|
| 01 | `lamball_v1.png` | `cattiva_v1.png` | `chikipi_v1.png` | `foxparks_v1.png` |
| 02 | `lifmunk_v1.png` | `pengullet_v1.png` | `daedream_v1.png` | `depresso_v1.png` |
| 03 | `gumoss_v1.png` | `vixy_v1.png` | `sparkit_v1.png` | `tanzee_v1.png` |
| 04 | `fuack_v1.png` | `tocotoco_v1.png` | `direhowl_v1.png` | `celaray_v1.png` |
| 05 | `dumud_v1.png` | `dazzi_v1.png` | `flambelle_v1.png` | `mimog_v1.png` |
| 06 | `cremis_v1.png` | `melpaca_v1.png` | `galeclaw_v1.png` | `lovander_v1.png` |
| 07 | `hoodle_v1.png` | `chillet_v1.png` | `penking_v1.png` | `katress_v1.png` |
| 08 | `lunaris_v1.png` | `quivern_v1.png` | `petallia_v1.png` | `mossanda_v1.png` |
| 09 | `grizzbolt_v1.png` | `tarantriss_v1.png` | `relaxaurus_v1.png` | `tetroise_v1.png` |
| 10 | `anubis_v1.png` | `shadowbeak_v1.png` | `lyleen_v1.png` | `orserk_v1.png` |
| 11 | `selyne_v1.png` | `jormuntide-ignis_v1.png` | `bellanoir_v1.png` | `aegidron_v1.png` |
| 12 | `renjishi_v1.png` | `silvance_v1.png` | `dandilord_v1.png` | `shaolong_v1.png` |
| 13 | `jetragon_v1.png` | `frostallion_v1.png` | `paladius_v1.png` | `necromus_v1.png` |
| 14 | `neptilius_v1.png` | `xenolord_v1.png` | `panthalus_v1.png` | `pal-sphere-favicon-source.png` |

Use the existing cutter after each batch:

```bash
python3 scripts/quadrant_cutter.py frontend/public/assets/units/palworld/_generated_batches/palworld_batch_01.png /tmp/palworld_batch_01
```

Rename and copy only after visually confirming all four positions. Preserve each raw grid so a bad crop can be reproduced.

## 5. Favicon derivation

The q4 crop from batch 14 is the source, not the final browser asset. Produce `frontend/public/pal-sphere.png` as a square PNG with transparency or the cyan background intact. Generate at least 512×512 and let the browser scale down. Verify at 16, 32, 48, and 180 CSS pixels. The mode metadata must select it only while Palworld is the active lobby/waiting-room mode; One Piece and Pokemon favicon behavior remains intact.

## 6. Compression and acceptance

Before ingestion, change `scripts/compress_images.py` to accept a directory argument instead of its hardcoded workstation path. Compression must not resize the 512×512 crops. Palette conversion is acceptable only when a before/after pixel-art inspection shows no banding or lost transparent edges.

For every crop, verify:

- [ ] Exactly 512×512 PNG; correct id and `_v1.png` suffix.
- [ ] Correct Pal in the correct quadrant; no duplicate or swapped unit.
- [ ] Correct primary background and restrained dual-element accent.
- [ ] Face and defining silhouette readable at 48×48.
- [ ] No clipped identity feature, adjacent-quadrant bleed, seam, line, text, or watermark.
- [ ] No human trainer, extra Pal, accidental weapon, or malformed anatomy.
- [ ] File decodes in a production frontend build and has a reasonable compressed size target of 40–250 KiB.

The full batch is accepted only when all 55 ids resolve through the same theme-aware icon helper and the favicon passes the four-size check. Regenerate an unacceptable quadrant as part of a new four-image batch; do not paint over identity errors by hand unless the user explicitly approves that workflow.
