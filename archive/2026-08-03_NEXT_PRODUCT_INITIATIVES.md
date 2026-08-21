# TFT Next Product Initiatives

Status: proposed product roadmap  
Planning snapshot: 2026-08-03  
Scope: final-composition analytics, items, mode identity, mobile support, and lobby invitations

## 1. Purpose

This document defines the next product-level initiatives for Theme Fusion Tactics. It describes the player outcome,
minimum useful scope, important product decisions, and acceptance signals for each initiative. It is intentionally not an
implementation plan and does not prescribe classes, endpoints, database migrations, or component changes.

The initiatives should strengthen the existing game rather than broaden it in every direction at once. The recommended
order favors learning from real matches, making it easier to invite and retain players, and then adding strategic depth.

## 2. Product priorities

| Priority | Initiative | Intended outcome | Recommended first release |
|---|---|---|---|
| 1 | Final-composition analytics | Understand which end boards achieve which placements | One compact final-board snapshot per human run |
| 2 | Lobby invitations | Let another player join without manually exchanging a room id | Shareable join link with a clear join flow |
| 3 | Mobile support | Make invitations and complete matches usable on common touch devices | Tablets and landscape phones first |
| 4 | Item system | Add a new layer of strategic unit customization | Small set of complete items from loot orbs |
| 5 | Distinct mode identities | Make mode selection change meaningful decisions, not only content | One signature rule for each mode |

Planning for items and mode identities can happen alongside the earlier initiatives, but their gameplay releases should
remain separately testable and balanceable.

## 3. Initiative 1 — Final-composition analytics

### Product goal

Answer a deliberately small set of questions:

- What did a player's final deployed board contain?
- What placement and final round did that board achieve?
- How do final boards differ by game mode and released game version?
- Which units appear most often in high and low placements?

The existing run and round analytics remain useful for investigating individual games. This initiative adds a compact
end-of-run view; it does not turn analytics into a complete event stream.

### Minimum useful record

Capture one final-composition snapshot when a human player's placement becomes final. The snapshot should contain:

- the existing match and anonymous run identity;
- mode and backend version/commit;
- final placement, final round, completion status, and whether the player abandoned;
- each unit on the final deployed board, represented by definition id, line id, and star level;
- equipped item ids per unit once the item system exists.

The bench, unit coordinates, shop history, purchases, rerolls, sells, gold history, per-tick state, and combat event data
are not part of this snapshot. Abandoned or interrupted runs may retain their snapshot for diagnosis, but balance views
should exclude them by default.

Unit and item image files should not be copied into analytics. The dashboard resolves the correct icon from the recorded
mode and definition id. Historical records remain associated with the game version that produced them.

### Admin experience

The first useful dashboard experience should provide:

- a composition row of unit portraits with star and item badges;
- placement, mode, version, and final-round context beside the composition;
- date, mode, version, placement, and completed-run filters;
- a simple top-four versus bottom-four unit-presence comparison;
- a drill-down to the already recorded run and round detail.

Exact-board rankings should only be added if real data shows compositions repeat often enough to be meaningful. The
dashboard should always display sample sizes and avoid presenting tiny samples as balance conclusions.

### Explicit non-goals

- No general-purpose player action tracking.
- No per-tick or full-match snapshot retention.
- No player-facing profiles or match history in this initiative.
- No replacement of operational metrics or logs with gameplay analytics.
- No personally identifying data beyond the existing anonymous client id.

### Success signals

- Every completed human run has exactly one final-composition snapshot.
- An administrator can visually identify a final board without reading unit ids.
- Pokemon and One Piece results can be compared without mixing game versions.
- Storage growth stays proportional to completed human runs, not match duration or tick count.

## 4. Initiative 2 — Lobby invitations

### Product goal

Let a host move another person from receiving an invitation to entering the waiting room with minimal explanation. The
first release should preserve the current account-free room model.

### Recommended first experience

The waiting room offers a prominent invite action that can copy a link and use the device's native share sheet where
available. Opening the link presents the room being joined, asks for a display name, and then enters the normal waiting
room. A QR code is useful for transferring an invitation from desktop to phone but is not required for the first slice.

The flow must clearly explain room-not-found, full-room, already-started, expired invitation, and connection-failure
states. It must also provide a normal path back to creating or manually joining another room.

### Privacy and safety boundaries

- An invitation must never contain a player id, STOMP session id, analytics client id, or reconnect token.
- Shareable rooms should use sufficiently unpredictable join codes; a user-selected room name alone is not a private
  access control mechanism.
- Joining from a link still requires a display name and normal backend authorization.
- Public-facing create/join attempts need sensible abuse and rate limits before broad promotion.

### Later expansion

- Host-revocable or expiring private invitation tokens.
- Waiting-room preview with mode and available player slots.
- Host controls for locking a lobby or removing an unwanted participant.
- Party rematch links after a completed game.

### Success signals

- A new player can go from invite link to waiting room without knowing what a room id is.
- The flow works from messaging applications on desktop and mobile.
- Failed joins resolve immediately with a useful next action.
- Reconnect credentials and private session data never appear in an invite URL.

## 5. Initiative 3 — Better mobile support

### Product goal

Make creating, joining, and completing a match comfortable on touch devices without weakening the desktop experience.
The interface should adapt to small screens instead of shrinking the complete desktop layout until it becomes unreadable.

### Target experience

The first gameplay target is tablets and landscape phones. Portrait phones must fully support the landing page, invite
flow, lobby, and waiting room; complete portrait-match play can follow after the landscape interaction model is proven.

The mobile match layout should prioritize the board, bench, current phase, and primary economy actions. Secondary
information such as traits, opponents, damage reports, and detailed tooltips can move into tabs, drawers, or sheets that
do not permanently consume board space.

### Interaction principles

- Every drag action has a tap-select and tap-destination alternative.
- No required information or action depends on hover.
- Important controls meet comfortable touch-target and spacing expectations.
- Scrolling a panel does not accidentally move a unit.
- Tooltips remain readable and dismissible without blocking the next action.
- Safe areas, browser chrome, virtual keyboards, and orientation changes do not hide critical controls.
- Reduced-motion and reduced-effects preferences continue to work during combat.

### Experience coverage

The mobile pass covers the complete player journey, not only the board:

- landing, room creation, manual join, and invitation join;
- waiting room and mode selection;
- shop, bench, board movement, selling, XP, reroll, and shop lock;
- augment selection, loot collection, player scouting, and damage report;
- reconnect, abandonment, end screen, and rematch/invite actions.

### Success signals

- The full primary journey works on a representative tablet and landscape-phone device matrix.
- All authoritative actions are possible without hover or precision dragging.
- The board and unit state remain legible during crowded combat.
- Mobile layout changes do not create a second source of game logic.
- Touch users do not show materially higher early abandonment or connection failure rates.

## 6. Initiative 4 — Item system

### Product goal

Add strategic unit customization and more interesting loot decisions without overwhelming players or multiplying balance
variables before the base system is understood.

The current `GameItem` contract is a useful placeholder, but it does not represent a finished player-facing system.

### Recommended first release

Start with a small roster of roughly 8–12 complete items rather than a component-combination matrix. Items come from
existing loot orbs and are equipped during planning. Each item should have a clear role—offense, defense, sustain,
utility, or support—and should be understandable from one short tooltip.

The first set should use theme-neutral mechanics so all modes can share the same balance rules. Modes may apply their own
names and icon treatment later, provided the underlying item remains recognizable across modes.

The final-composition analytics snapshot should show equipped item icons with their units, but no item acquisition or
movement history is required.

### Product decisions to lock before delivery

- Maximum equipped items per unit; two is the recommended starting point.
- Whether an equipped item is committed until the unit is sold or may be freely moved during planning.
- What happens to equipped items when a unit is sold or combined.
- Whether every item can drop equally or loot tables change by round.
- How duplicate or unwanted items can be converted without creating inventory clutter.

### Balance principles

- Items enhance a unit's role rather than erase its weaknesses completely.
- Offensive, defensive, and support choices remain competitive.
- One item should not be mandatory for a specific unit or entire mode.
- Item power must not depend on paid or persistent progression.
- Tooltips describe the authoritative backend effect without hidden exceptions.

### Later expansion

- Component combinations after the complete-item loop has proven enjoyable.
- Rare or late-game items with carefully bounded power.
- Mode-specific visual skins or a small number of mode-owned items.
- Item reforging or removal rewards.

### Success signals

- New players can understand how to obtain, inspect, and equip an item without external documentation.
- Items create meaningful placement decisions rather than automatic best-in-slot assignments.
- Final-board analytics can reveal item concentration and outliers without new event tracking.
- No single item or item/unit pairing dominates across both modes.

## 7. Initiative 5 — Distinct mode identities

### Product goal

A player should notice a meaningful strategic difference between One Piece and Pokemon even if all names and
art were temporarily hidden. Shared fundamentals—shop, economy, board, stars, phases, and backend authority—should remain
familiar so each mode does not become a separate game.

### Identity framework

Each mode receives one signature rule that affects recurring player decisions. A signature rule must be explainable in
one short lobby description, visible in normal play, and valuable without requiring franchise knowledge.

Candidate identities for design validation are:

- **One Piece — Captain and crew:** designate or develop a captain whose crew relationships shape team bonuses.
- **Pokemon — evolution mastery:** make evolution timing, form identity, or type adaptation a more deliberate decision
  beyond the existing automatic star upgrade.

These are product directions, not locked mechanics. Each requires a short rules proposal and playtest before approval.

### Shared boundaries

- The core engine remains theme-agnostic.
- A mode-owned rule is supplied through a generic extension point rather than franchise conditionals in core gameplay.
- Mode identity should not require separate room, transport, or frontend authority models.
- No mode should receive a lasting power or complexity advantage merely because it was implemented most recently.
- Signature rules should interact predictably with traits, augments, items, bots, ghosts, and final-composition analytics.

### Recommended rollout

Define both identity contracts before releasing the first one. Prototype each direction in turn to validate the generic
product shape, then deliver equivalent signature depth for the other mode in the same release window or behind an
explicit experimental flag.

### Success signals

- Players can explain each mode's strategic identity in one sentence.
- Mode selection changes planning decisions throughout a match.
- Existing shared rules remain learnable and consistent.
- Mode pick rate, completion rate, and placement distributions remain healthy after release.
- Bots can use each signature rule credibly.

## 8. Recommended delivery sequence

### Stage A — Learn and invite

Deliver final-composition analytics and the first shareable invitation flow. These are narrow additions that improve
future balance decisions and make multiplayer testing easier.

### Stage B — Make the invited experience work on mobile

Complete the mobile lobby/invitation journey, then adapt the core match experience for tablets and landscape phones.
Validate the whole match flow before expanding portrait gameplay.

### Stage C — Add strategic depth

Release the small complete-item set. Review its final-board data and player comprehension before approving combinations,
rare items, or mode-specific item variants.

### Stage D — Establish mode identity

Approve one signature-rule contract for every mode, validate both prototypes, and release balanced mode identities
without weakening the theme-agnostic core.

## 9. Cross-initiative guardrails

- Final-composition analytics remains one compact record per human run.
- Icons are resolved by the frontend; analytics never stores image blobs.
- Items and mode mechanics are backend-authoritative and visible in the final composition.
- Mobile layouts do not duplicate game rules.
- Invitation links never carry session or reconnect authority.
- Every initiative is independently releasable and can be evaluated before the next one expands its scope.
- Runtime implementation, balance values, and release notes are intentionally deferred to focused follow-up plans.

## 10. Roadmap completion criteria

This roadmap is complete when:

- final boards and placements are visible as icon-based analytics with bounded storage growth;
- hosts can invite account-free players through a safe shareable link;
- touch users can complete the primary journey on tablets and landscape phones;
- a small, understandable item set creates meaningful unit customization;
- every mode has one approved and balanced signature identity;
- README, architecture context, tests, and the in-app changelog are updated alongside the eventual runtime changes.
