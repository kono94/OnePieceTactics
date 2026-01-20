// Animation configuration for attack and ability effects
// Pattern = shape (SINGLE, LINE, SURROUND), styling comes from this config

export type AttackType = 'punch' | 'slash' | 'projectile'

export interface AttackAnimationConfig {
    type: AttackType
    color: string
}

export interface AbilityAnimationConfig {
    color: string
}

// Per-unit attack animations (auto-attacks)
export const ATTACK_ANIMATIONS: Record<string, AttackAnimationConfig> = {
    // One Piece
    'luffy_v1': { type: 'punch', color: '#f59e0b' },
    'zoro_v1': { type: 'slash', color: '#22c55e' },
    'nami_v1': { type: 'projectile', color: '#38bdf8' },
    // Pokemon
    'charmander': { type: 'punch', color: '#f97316' },
    'squirtle': { type: 'projectile', color: '#3b82f6' },
    'bulbasaur': { type: 'slash', color: '#22c55e' },
    // Fallback for unknown units
    '_default': { type: 'punch', color: '#94a3b8' }
}

// Per-unit ability styling
export const ABILITY_ANIMATIONS: Record<string, AbilityAnimationConfig> = {
    // One Piece
    'luffy_v1': { color: '#ef4444' },
    'zoro_v1': { color: '#22c55e' },
    'nami_v1': { color: '#38bdf8' },
    // Pokemon
    'charmander': { color: '#f97316' },
    'squirtle': { color: '#3b82f6' },
    'bulbasaur': { color: '#22c55e' },
    // Fallback
    '_default': { color: '#fbbf24' }
}

// Helper to get attack animation config for a unit
export function getAttackConfig(definitionId: string): AttackAnimationConfig {
    return ATTACK_ANIMATIONS[definitionId] ?? ATTACK_ANIMATIONS['_default']
}

// Helper to get ability animation config for a unit
export function getAbilityConfig(definitionId: string): AbilityAnimationConfig {
    return ABILITY_ANIMATIONS[definitionId] ?? ABILITY_ANIMATIONS['_default']
}
