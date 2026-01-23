/**
 * TypeScript DTOs for game state - mirrors backend Java models
 * Auto-synced with: GameState.java, GameUnit.java, UnitDefinition.java
 */

// ============================================================================
// Enums
// ============================================================================

export type GamePhase = 'LOBBY' | 'PLANNING' | 'COMBAT' | 'END'

export type GameMode = 'onepiece' | 'pokemon'

export type ActionType = 'BUY' | 'SELL' | 'MOVE' | 'REROLL' | 'EXP' | 'LOCK' | 'COLLECT_ORB'

export type CombatSide = 'TOP' | 'BOTTOM'

// ============================================================================
// Core Game Entities
// ============================================================================

export interface AbilityDefinition {
    name: string
    description: string
    type: string // 'DAMAGE' | 'STUN' | 'HEAL' | 'BUFF_ATK' | 'BUFF_SPD' (future)
    pattern: string // 'SINGLE' | 'LINE' | 'SURROUND'
    value: number
    range: number
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
    name: string
    cost: number
    maxHealth: number
    currentHealth: number
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
    stunTicksRemaining: number
    atkBuff: number  // 1.0 = no buff
    spdBuff: number  // 1.0 = no buff
}

export interface UnitDefinition {
    id: string
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
}

// ============================================================================
// Events
// ============================================================================

export interface CombatEvent {
    timestamp: number
    type: 'DAMAGE' | 'SKILL' | 'DEATH' | 'MOVE'
    sourceId: string
    targetId: string
    value: number
}

export interface DamageEntry {
    unitName: string
    definitionId: string
    ownerId: string
    damage: number
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
