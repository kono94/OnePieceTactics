Feature Roadmap - One Piece Tactics
Purpose: Top-level implementation plan for new features, sorted by effort (simplest first).
Created: 2026-01-21

Overview
Based on your feedback, here are the approved features sorted by implementation effort. We'll tackle the simple, independent features first to build momentum.

Phase 1: Quick Wins (Frontend-Only, ~1-2 hours each)
These require no backend changes and can be done immediately.

1.1 TypeScript DTO Interfaces
Aspect	Details
Effort	⭐ Very Low
Files	frontend/src/types/ (new)
Dependencies	None
Description	Create proper TypeScript interfaces for GameState, PlayerState, GameUnit, etc. Replace any types throughout the frontend.
1.2 Unit Death Animation
Aspect	Details
Effort	⭐ Very Low
Files	GameCanvas.vue, style.css
Dependencies	None
Description	Add a fade-out + scale-down CSS animation when a unit's HP reaches 0. Currently units just disappear.
Implementation Approach:

Track "dying" units in a reactive set
Apply .dying CSS class with opacity: 0, transform: scale(0.5), transition: 0.3s
Remove from DOM after animation completes
1.3 Star-Up Level Celebration
Aspect	Details
Effort	⭐ Very Low
Files	GameCanvas.vue or new StarUpEffect.vue
Dependencies	None
Description	When a unit upgrades from 1★→2★ or 2★→3★, show a brief particle burst / golden glow effect.
Implementation Approach:

Detect star level changes by comparing previous vs current gameState
Trigger a CSS keyframe animation (radial burst, sparkles)
Auto-dismiss after ~1 second
Phase 2: Backend + Frontend (Medium Effort)
These require changes to both layers but are self-contained.

2.1 Damage Report / Combat Log
Aspect	Details
Effort	⭐⭐ Low-Medium
Backend	CombatSystem.java, new CombatLog record
Frontend	New DamageReport.vue component
Dependencies	None
Description	Track damage dealt by each unit during combat. Display a summary after combat ends.
Implementation Approach:

Backend: Accumulate damage per unit ID in CombatSystem during simulateTick()
Backend: Include damageLog: Map<String, Integer> in combat result event
Frontend: Display a modal/panel after combat showing "Luffy dealt 450 damage", sorted by damage
2.2 Unit Ability Depth (STUN, HEAL, BUFFS)
Aspect	Details
Effort	⭐⭐⭐ Medium
Backend	AbilityDefinition.java, DefaultAbilityCaster.java, AbstractGameUnit.java
Frontend	Visual indicators for stun/buff status
Dependencies	None
Description	Expand the ability system beyond damage-only.
New Ability Types:

Type	Backend Logic	Frontend Visual
STUN	Target skips N ticks (add stunTicksRemaining to unit)	Gray overlay + "STUNNED" text
HEAL	Restore HP to self or allies (new targeting mode)	Green number float (+50)
BUFF_ATK	Increase ATK for all allied units until combat end	Orange glow on buffed units
BUFF_SPD	Decrease attack cooldown for allies	Blue glow on buffed units
Implementation Approach:

Add abilityType enum: DAMAGE, STUN, HEAL, BUFF_ATK, BUFF_SPD
Add stunTicksRemaining, atkBuff, spdBuff fields to AbstractGameUnit
In simulateTick(), skip stunned units; apply buff multipliers to stats
Update units_onepiece.json with appropriate ability types per character
2.3 Loot Orbs
Aspect	Details
Effort	⭐⭐⭐ Medium
Backend	GameRoom.java (spawn logic), new LootOrb model
Frontend	Orb rendering in GameCanvas.vue, click to collect
Dependencies	Item System (for item orbs) — can start with gold-only orbs
Description	Spawn loot orbs every other planning phase. Players click to collect.
Orb Contents (initial version):

🪙 Gold (3-8)
🎁 Random unit from pool
Implementation Approach:

Backend: On even-numbered rounds, generate 1-3 orbs per player with random contents
Backend: Add LootOrb to PlayerState (position, contents, collected flag)
Frontend: Render orbs on the board; on click, emit COLLECT_ORB action
Later: Add item drops once Item System is complete
2.4 Reconnection Handling
Aspect	Details
Effort	⭐⭐⭐ Medium
Backend	GameRoom.java, GameController.java
Frontend	App.vue reconnection logic
Dependencies	None
Description	Allow players to reconnect to an ongoing game if they disconnect.
Implementation Approach:

Backend: Store sessionId → playerId mapping; on reconnect, restore player to same slot
Backend: Add timeout (30s) before marking player as "abandoned" and converting to bot
Frontend: Persist roomId and playerName to localStorage; on page load, attempt rejoin
Phase 3: Major Systems (High Effort)
These are larger features that touch many files and require careful design.

3.1 Item System
Aspect	Details
Effort	⭐⭐⭐⭐⭐ High
Backend	New Item model, ItemLoader, equip logic in Player, effects in CombatSystem
Frontend	Item UI in bench/shop, equip drag-drop, item tooltips
Dependencies	Should be done after Loot Orbs (orbs can drop items)
Description	Full item system: items drop from orbs, can be equipped to units, provide stat bonuses or effects.
Data Model:

public record Item(
    String id,
    String name,
    String iconPath,
    int bonusAttack,
    int bonusHealth,
    int bonusSpeed,
    String specialEffect // e.g., "LIFESTEAL", "CRIT", null
) {}
Implementation Approach:

Create items_onepiece.json with item definitions
Add items: List<Item> to GameUnit
Add item slots to unit display in GameCanvas.vue
Add item inventory panel (or merge with bench)
Implement drag-drop from inventory to unit
Apply item bonuses in TraitManager.applyTraits() or similar
Update Loot Orbs to include item drops
3.2 Opponent Scouting
Aspect	Details
Effort	⭐⭐⭐⭐ Medium-High
Backend	Minimal (already sends all player data)
Frontend	ScoutView.vue, click handlers in PlayerList.vue
Dependencies	None
Description	Click on an opponent in the player list to view their board (read-only).
Implementation Approach:

Frontend: Add scoutingPlayerId ref to GameInterface.vue
Frontend: When set, render GameCanvas with that player's units (no drag-drop)
Frontend: Add "Back to My Board" button
UI: Highlight scouted player in PlayerList
Implementation Order Summary
#	Feature	Effort	Dependencies
1	TypeScript DTOs	⭐	None
2	Death Animation	⭐	None
3	Star-Up Celebration	⭐	None
4	Damage Report	⭐⭐	None
5	Ability Depth (STUN/HEAL/BUFF)	⭐⭐⭐	None
6	Loot Orbs (gold/unit only)	⭐⭐⭐	None
7	Reconnection	⭐⭐⭐	None
8	Item System	⭐⭐⭐⭐⭐	Loot Orbs (for drops)
9	Opponent Scouting	⭐⭐⭐⭐	None
Rejected Features (For Reference)
❌ Augment System
❌ Hexagon Grid
❌ Sound Effects
❌ Minimap
❌ Mobile Support
❌ Frontend Tests
❌ Spectator Mode
❌ Leaderboard
❌ Shield abilities
❌ Summon abilities
❌ Targeting lines (projectiles already show intent)
Ready to proceed with Phase 1 when you approve!