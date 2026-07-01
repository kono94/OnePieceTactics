/**
 * TypeScript DTOs for game state - mirrors backend Java models
 * Auto-synced with: GameState.java, GameUnit.java, UnitDefinition.java
 */

// ============================================================================
// Enums
// ============================================================================

export type GamePhase = 'LOBBY' | 'PLANNING' | 'COMBAT' | 'END_CELEBRATION' | 'END'

export type GameMode = 'onepiece' | 'pokemon'

export type ActionType =
    | 'BUY'
    | 'SELL'
    | 'MOVE'
    | 'REROLL'
    | 'EXP'
    | 'LOCK'
    | 'COLLECT_ORB'
    | 'READY_FOR_COMBAT'
    | 'SELECT_AUGMENT'

export type CombatSide = 'TOP' | 'BOTTOM'

export type AugmentTier = 'SILVER' | 'GOLD' | 'DIAMOND'

export type PlanningPauseReason = 'AUGMENT_SELECTION' | 'SOLO_READY' | null

// ============================================================================
// Core Game Entities
// ============================================================================

export interface AbilityDefinition {
    name: string
    description: string
    type: string // 'DAMAGE' | 'STUN' | 'HEAL' | 'BUFF_ATK' | 'BUFF_SPD' (future)
    pattern: string // 'SINGLE' | 'LINE' | 'SURROUND'
    range: number[]
    values: number[]
    modifiers?: Record<string, unknown>[]
}

export interface UnitFormDefinition {
    starLevel: number
    definitionId: string
    name: string
    traits?: string[]
    range?: number[]
    ability?: AbilityDefinition | null
}

export interface GameItem {
    id: string
    name: string
    description: string
    statBonuses: Record<string, number>
}

export interface GameUnit {
    id: string
    definitionId: string
    lineId: string
    name: string
    cost: number
    maxHealth: number
    currentHealth: number
    shield: number
    mana: number
    maxMana: number
    attackDamage: number
    abilityPower: number
    armor: number
    magicResist: number
    attackSpeed: number
    range: number
    traits: string[]
    items: GameItem[]
    x: number
    y: number
    starLevel: number
    ownerId: string
    ability: AbilityDefinition | null
    activeAbility: string | null
    // Combat status effects
    stunSecondsRemaining: number
    atkBuff: number  // 1.0 = no buff
    spdBuff: number  // 1.0 = no buff
}

export interface UnitDefinition {
    id: string
    lineId?: string
    name: string
    cost: number
    maxHealth: number
    maxMana: number
    attackDamage: number
    abilityPower: number
    armor: number
    magicResist: number
    attackSpeed: number
    range: number
    traits: string[]
    ability: AbilityDefinition | null
    forms?: UnitFormDefinition[]
}

// ============================================================================
// Trait State
// ============================================================================

export interface ActiveTrait {
    id: string
    name: string
    description: string
    count: number // Number of units contributing
    activeLevel: number // Which breakpoint is active
}

export type LootType = 'GOLD' | 'UNIT'

export interface LootOrb {
    id: string
    x: number
    y: number
    type: LootType
    contentId: string
    amount: number
}

// ============================================================================
// Augments
// ============================================================================

export interface AugmentOffer {
    id: string
    name: string
    description: string
    tier: AugmentTier
    effectType: string
    value: number
    image?: string | null
}

export interface SelectedAugment {
    id: string
    name: string
    description: string
    tier: AugmentTier
    effectType: string
    value: number
    selectedRound: number
    image?: string | null
}

// ============================================================================
// Player State
// ============================================================================

export interface PlayerState {
    playerId: string
    name: string
    health: number
    gold: number
    level: number
    xp: number
    nextLevelXp: number
    place: number | null // Final placement (1st, 2nd, etc.) - null if still playing
    combatSide: CombatSide | null
    bench: GameUnit[]
    board: GameUnit[]
    activeTraits: ActiveTrait[]
    shop: UnitDefinition[]
    lootOrbs: LootOrb[]
    augmentChoices: AugmentOffer[]
    selectedAugments: SelectedAugment[]
    isGhost: boolean
    boardUnits?: GameUnit[] // Alternative name for board in some contexts
}

// ============================================================================
// Events
// ============================================================================

export interface CombatEvent {
    timestamp: number
    type: 'DAMAGE' | 'SKILL' | 'DEATH' | 'MOVE' | 'HEAL' | 'SHIELD'
    sourceId: string
    targetId: string
    value: number
    skillName?: string
}

export interface DamageEntry {
    unitName: string
    definitionId: string
    ownerId: string
    damage: number
    healing: number
    shielding: number
}

export interface CombatResultPayload {
    winnerId: string | null
    loserId: string | null
    participantIds: string[]
    damageLog: Record<string, DamageEntry>
}

// ============================================================================
// Full Game State (sent from backend every tick)
// ============================================================================

export interface GameState {
    roomId: string
    hostId: string
    phase: GamePhase
    round: number
    timeRemainingMs: number
    totalPhaseDuration: number
    players: Record<string, PlayerState>
    matchups: Record<string, string> // playerId -> opponentId
    recentEvents: CombatEvent[]
    damageLog: Record<string, DamageEntry>
    gameMode: GameMode
    planningTimerPaused: boolean
    planningReadyPlayerId: string | null
    planningPauseReason: PlanningPauseReason
}

// ============================================================================
// Player Actions (sent to backend)
// ============================================================================

export interface GameAction {
    type: ActionType
    playerId: string
    unitId?: string // For MOVE, SELL
    targetX?: number // For MOVE (0-6)
    targetY?: number // For MOVE (-1 for bench, 0-7 for board)
    shopIndex?: number // For BUY (0-4)
    orbId?: string // For COLLECT_ORB
    augmentId?: string // For SELECT_AUGMENT
}

// ============================================================================
// Game Events (received from backend)
// ============================================================================

export interface GameEvent<T = unknown> {
    type: string
    payload: T
}

export interface CombatResultEvent extends GameEvent<CombatResultPayload> {
    type: 'COMBAT_RESULT'
}

// ============================================================================
// Rendered State (computed for display in GameCanvas/GameInterface)
// ============================================================================

export interface RenderedUnit extends GameUnit {
    visualX: number
    visualY: number
    isMine: boolean
    image: string
}

export interface RenderedOrb extends LootOrb {
    visualX: number
    visualY: number
}

export interface DisplayedUnit extends RenderedUnit {
    isDying?: boolean
}
