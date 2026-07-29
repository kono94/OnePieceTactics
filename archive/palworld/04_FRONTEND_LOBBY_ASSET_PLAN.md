# Palworld 2.0 — Frontend Lobby, Metadata, and Asset Plan

This phase adds Palworld as a first-class frontend mode while deliberately limiting the visual reskin to the public lobby, waiting room, document metadata, favicon, unit assets, and animation gallery. The live board and in-match chrome remain shared.

## 1. Product behavior

- `/api/config` may return `onepiece`, `pokemon`, and `palworld`; the frontend renders them in that order regardless of backend enum sort order.
- Before a room exists, the public lobby uses the configured default mode's metadata/theme. Once room state exists, `gameState.gameMode` is authoritative.
- The host sees three mode cards in the waiting room and can select Palworld under the same host/LOBBY restrictions already used for Pokemon.
- Non-hosts see the selected mode but cannot change it.
- Switching mode updates the waiting-room palette, title, favicon, unit icon base path, trait request, and gallery metadata without reconnecting STOMP.
- Entering the game removes lobby-only decoration. The board, shop, trait sidebar, overlays, and end screen use existing shared styling.
- One Piece and Pokemon retain their current title/favicon/assets and visual behavior.

## 2. Replace binary mode conditionals with metadata

Create `frontend/src/data/gameModeMetadata.ts`:

```ts
export interface GameModeMetadata {
    id: GameMode
    label: string
    shortLabel: string
    documentTitle: string
    favicon: string
    unitAssetFolder: string
    themeClass: string
    galleryPath: string
    order: number
}
```

Register exact entries:

| id | label | document title | favicon | unit folder | class | gallery | order |
|---|---|---|---|---|---|---|---:|
| `onepiece` | One Piece | Theme Fusion Tactics — One Piece | `/favicon.svg` | `onepiece` | `theme-onepiece` | `#/ultimate-gallery/onepiece` | 1 |
| `pokemon` | Pokemon | Pokemon Tactics | `/pokeball.png` | `pokemon` | `theme-pokemon` | `#/ultimate-gallery/pokemon` | 2 |
| `palworld` | Palworld | Palworld Tactics | `/pal-sphere.png` | `palworld` | `theme-palworld` | `#/ultimate-gallery/palworld` | 3 |

Export `getGameModeMetadata(mode)` and `sortGameModes(modes)`. Unknown runtime strings should be logged and normalized to the backend-provided default or One Piece at the bootstrap boundary; helpers should not silently map every unknown mode to One Piece.

Update the shared `GameMode` type to `'onepiece' | 'pokemon' | 'palworld'`. Search for all literal unions and mode comparisons in `src`, tests, and comments. Use metadata for labels, icon folders, title, favicon, theme class, and gallery navigation. Retain explicit mode branches only where a truly different data source/behavior exists.

## 3. `App.vue` changes

- Default fallback mode list becomes `['onepiece', 'pokemon', 'palworld']` and is sorted through metadata.
- `activeVisualMode` is computed as `gameState?.gameMode ?? defaultMode`.
- Replace `applyThemeMeta` branches with metadata lookup. Update or create one `<link rel="icon">`; do not accumulate link tags on repeated switching.
- Support all three gallery hashes via a parser that validates the final path segment as a `GameMode`, rather than checking whether the string contains `/pokemon`.
- Pass `activeVisualMode` or its `themeClass` into `Lobby` and `WaitingRoom`. Do not pass it through `GameInterface` solely for restyling.
- Trait fetch remains `/api/traits?mode={mode}`. Cancel or disregard stale responses when users switch rapidly so an earlier Pokemon response cannot overwrite Palworld trait metadata.
- On leaving a room, restore the configured default mode's metadata and public-lobby palette.
- Keep STOMP destinations and payloads unchanged.

Add a lightweight request-generation token around trait fetching: increment before a request and apply the response only if its token and mode still equal the latest active mode.

## 4. Lobby and waiting-room visual language

### 4.1 Exact palette

Define lobby-scoped CSS variables on `.theme-palworld`:

```css
--pw-sky: #5bc9e8;
--pw-sky-deep: #1888a6;
--pw-teal: #187c6b;
--pw-leaf: #48a868;
--pw-sand: #f0d59a;
--pw-coral: #ff7f6e;
--pw-gold: #d9a441;
--pw-ink: #173443;
--pw-cloud: #f5feff;
```

The intended read is Palpagos coastline under the World Tree: bright sky cyan, lush teal/green, warm sand, Pal Sphere coral, and restrained gold. Gold is a highlight for focus/host status, not the dominant surface color.

### 4.2 Public lobby

For Palworld only:

- Background: layered CSS gradients—sky cyan top, pale cloud band, teal island silhouette, warm sand at the bottom. Use pseudo-elements/CSS shapes; do not introduce downloaded scenic art.
- Main card: translucent cloud-white surface with dark teal ink, 18–24 px radius, restrained cyan shadow, and accessible opaque fallback.
- Primary action: teal with cloud-white text; hover shifts toward leaf green. Coral is used for the small sphere/button accent and focused input ring.
- Heading: “Palworld Tactics” from metadata. Supporting copy remains mode-neutral (“Create or join a tactics room”) unless product copy is intentionally revised for all modes.
- Preserve current responsive geometry, Enter-key behavior, connection status, admin route separation, and VersionDisplay.

### 4.3 Waiting room

- Render mode choices from sorted metadata, not hardcoded buttons.
- Each card shows label, compact motif, selection state, and disabled/non-host state. The Palworld motif is a CSS Pal Sphere: two colored semicircles, dark center band, circular button. It is decorative and `aria-hidden`.
- Selected Palworld card uses teal border, coral center accent, sand highlight, and a small gold host-only focus glow.
- The room panel and player list use the same cloud/ink surfaces as the lobby only while `theme-palworld` is active.
- Mode changes continue to emit only the `GameMode` id.
- Add `aria-pressed`, visible keyboard focus, and a clear text label. Color is not the only selected-state cue.

### 4.4 Scope guard

Do not apply `.theme-palworld` selectors to `GameInterface`, `GameCanvas`, shop cards, bench, damage report, augment overlay, trait sidebar, phase announcement, or end screen. Shared unit portrait backgrounds and trait colors naturally differ via data; that is not a chrome reskin.

## 5. Unit and favicon asset resolution

Refactor `frontend/src/utils/iconUtils.ts` to resolve `unitAssetFolder` from metadata and preserve each mode's filename convention:

```text
/assets/units/{metadata.unitAssetFolder}/{definitionId}.png
Palworld: /assets/units/palworld/{definitionId}_v1.png
```

Palworld always uses the Pal id as definition id at all stars. Keep Pokemon evolution/form definition ids working. Define one explicit placeholder path for missing assets and log the missing `(mode, definitionId)` in development. Do not fall through to a One Piece character image.

Add:

- `frontend/public/assets/units/palworld/{55 ids}_v1.png` after manual generation/cutting.
- `frontend/public/pal-sphere.png` from batch 14 q4.
- `frontend/public/assets/units/palworld/ICON_GENERATION_PROMPTS.md` only if a local pointer is useful; prefer linking to `docs/palworld/02_ICON_BATCH_PROMPTS.md` to avoid prompt duplication.

Update `scripts/compress_images.py` to use `argparse` with a required directory positional argument, validate that the resolved path is a directory, process only `.png` files, and report before/after totals. Add `--recursive` only if needed; default to one directory. Never retain the hardcoded `/Users/.../pokemon/` path.

## 6. Frontend data types

Mirror the backend's additive contract in `frontend/src/types/game.ts`:

- Keep unit and ability transport types free of required `basicElement`, ability `element`, `attackAnimationKey`, or JSON `animationKey` fields.
- A resolved combat event may expose the definition id, stable ability identity, and target-resolved element for rendering.
- `AbilityDefinition.key?: string`, `targeting?`, and `effects?` remain optional compatibility fields; `key` is an ability identity, not an animation-key requirement.
- `ActiveStatusView` and `GameUnit.activeStatuses`.
- Enriched `CombatEvent` optional fields during migration, required for newly created Palworld fixtures.
- Expand event-type union for `ATTACK`, `CAST`, status, and zone events while temporarily accepting `SKILL`.

Define `ElementId` as `string` in the theme-agnostic transport type rather than a Palworld-only union. The static Palworld animation/trait files may narrow to the nine ids.

The frontend displays data only. It must not reproduce status timing, element multipliers, target choice, zone collision, or damage calculation.

## 7. Trait presentation

`traitData.ts` already hydrates from the backend. Ensure it does not assume Pokemon type names or only two modes. Palworld traits use their backend-supplied name, description, colors, breakpoint style, and values. Add a generic element glyph fallback—small colored diamond/circle—rather than nine franchise-specific image assets.

The tooltip should list both elements for dual-element Pals in order and show the active trait contributions normally. Do not add a Legendary badge; cost styling already communicates rarity.

## 8. Ultimate gallery routing and roster

Create `frontend/src/data/palworldUltimateGalleryRoster.ts` using the same JSON-derived approach as Pokemon:

- Import `units_palworld.json`.
- Emit one entry for every Pal line and one attack preview for every definition id.
- Emit one ability entry per Pal root ability. Reuse that entry at 1/2/3 stars with star-scaled values; do not create duplicate star variants.
- Include `gameMode`, `definitionId`, `starLevel`, optional stable ability identity, the target-resolved element when previewing damage, ability type, pattern/shape, and cost in the gallery entry type.

Refactor gallery selection into a registry:

```ts
const GALLERY_ROSTERS: Record<GameMode, UltimateGalleryUnit[]> = { ... }
```

Support `#/ultimate-gallery/palworld`. The visible gallery count for Palworld is 55 ability previews and 55 attack previews. Add filters for cost, star, and defensive trait if the current single grid becomes unwieldy, but keep the route dependency-free.

The gallery must show a visible “missing animation config” badge in development instead of hiding a fallback. The release gate requires zero such badges.

## 9. Accessibility and responsive behavior

- Maintain WCAG AA contrast for text/buttons on all new surfaces. Cloud white on teal and ink on cloud white should pass; test coral text before using it.
- Respect `prefers-reduced-motion` for lobby cloud drift, card hover, sphere spin, and gallery effects.
- Mode cards must be usable at 320 px width, wrap rather than overflow, and preserve 44 px minimum interactive height.
- The favicon/title updates are decorative; announce successful mode selection through the existing selected label/state, not a noisy live region.
- Images have meaningful alt text (`"Lamball"`); CSS motifs are hidden from screen readers.

## 10. Frontend tests

Add or update tests for:

- Metadata registry returns exact labels, paths, classes, order, and favicon for all three modes.
- `/api/config` fallback and sorting include Palworld.
- `App.vue` applies Palworld title/favicon and restores the default on leave.
- Stale trait responses are ignored after rapid mode switching.
- `WaitingRoom.vue` renders three choices, emits `palworld` only for host, and exposes accessible selected/disabled state.
- `Lobby.vue`/waiting room receive `theme-palworld`; `GameInterface` does not receive lobby skin styling.
- `iconUtils` resolves Palworld, Pokemon forms, One Piece, and a true placeholder for unknown ids.
- Palworld JSON-derived gallery produces 55 unique root abilities and 55 attack previews.
- Hash parsing accepts all three exact gallery routes and rejects unknown segments.
- `UnitTooltip` renders dual elements, role, range, active star ability, and statuses without performing calculations.

Suggested component fixtures should use Lamball (melee tank), Pengullet 2★ (dual ranged Pal), Selyne (dual ranged damage), and Panthalus 3★ (5-cost tank).

## 11. Frontend completion commands

```bash
cd frontend
npm run test -- --run
npm run lint
npm run build
```

If the package scripts differ, use the exact equivalents in `package.json`; do not skip type-checking. Also load the production build and manually test all three gallery hashes and repeated room-mode switching.

## 12. Acceptance checklist

- [ ] No binary `mode === 'pokemon' ? ... : ...` remains where a registry lookup is appropriate.
- [ ] Palworld is a typed mode everywhere and config fallback includes it.
- [ ] Title/favicon update immediately and never leave duplicate favicon link elements.
- [ ] Host/non-host waiting-room behavior is unchanged except for the third option.
- [ ] Palworld lobby/waiting palette uses the exact variables and remains readable/responsive.
- [ ] In-game chrome has no Palworld-specific restyle.
- [ ] All 55 portrait URLs and the Pal Sphere favicon return 200 in the production build.
- [ ] Gallery data contains 55 attacks and 55 unique root abilities with no duplicate star variants.
- [ ] Existing One Piece/Pokemon frontend tests and visuals remain stable.
- [ ] README and `FRONTEND_CONTEXT.md` document the third mode, metadata registry, event additions, and gallery route.
