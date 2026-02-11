// Animation configuration for attack and ability effects
// Pattern = shape (SINGLE, LINE, SURROUND), styling comes from this config

export type AttackType = 'punch' | 'slash' | 'projectile' | 'kick' | 'blunt'

export interface AttackAnimationConfig {
    type: AttackType
    color: string
}

export type AbilityEffectStyle = 'DEFAULT' | 'BEAM_HEAVY' | 'MAGMA_RAIN' | 'QUAKE' | 'SOUL_SPIRAL' | 'DRAGON_ROAR'

export interface AbilityAnimationConfig {
    color: string
    effectStyle?: AbilityEffectStyle
}

// Per-unit attack animations (auto-attacks)
export const ATTACK_ANIMATIONS: Record<string, AttackAnimationConfig> = {
    // === STRAW HAT ===
    'luffy_v1': { type: 'punch', color: '#ef4444' }, // Red
    'zoro_v1': { type: 'slash', color: '#22c55e' }, // Green
    'nami_v1': { type: 'projectile', color: '#38bdf8' }, // Thunder blue
    'usopp_v1': { type: 'projectile', color: '#f59e0b' }, // Amber
    'chopper_v1': { type: 'punch', color: '#f472b6' }, // Pink
    'sanji_v1': { type: 'kick', color: '#f97316' }, // Orange
    'robin_v1': { type: 'punch', color: '#a855f7' }, // Purple
    'franky_v1': { type: 'punch', color: '#0ea5e9' }, // Sky blue
    'brook_v1': { type: 'slash', color: '#22d3ee' }, // Ice cyan
    'jinbei_v1': { type: 'blunt', color: '#1d4ed8' }, // Sea blue

    // === MARINE ===
    'koby_v1': { type: 'punch', color: '#f472b6' },
    'helmeppo_v1': { type: 'slash', color: '#8b5cf6' },
    'tashigi_v1': { type: 'slash', color: '#6366f1' },
    'hina_v1': { type: 'punch', color: '#ec4899' },
    'smoker_v1': { type: 'blunt', color: '#cbd5e1' }, // Smoke gray
    'garp_v1': { type: 'punch', color: '#94a3b8' },
    'sengoku_v1': { type: 'punch', color: '#eab308' }, // Gold
    'kizaru_v1': { type: 'projectile', color: '#facc15' }, // Light yellow
    'akainu_v1': { type: 'punch', color: '#991b1b' }, // Magma red

    // === WARLORD ===
    'buggy_v1': { type: 'projectile', color: '#ef4444' },
    'moria_v1': { type: 'punch', color: '#7c3aed' },
    'crocodile_v1': { type: 'blunt', color: '#d97706' }, // Sand
    'kuma_v1': { type: 'punch', color: '#3b82f6' },
    'doflamingo_v1': { type: 'slash', color: '#f472b6' },
    'mihawk_v1': { type: 'slash', color: '#4ade80' }, // Lime slash
    'hancock_v1': { type: 'projectile', color: '#ec4899' },

    // === BEAST PIRATES ===
    'gifter_v1': { type: 'blunt', color: '#78350f' },
    'headliner_v1': { type: 'blunt', color: '#450a0a' },
    'ulti_v1': { type: 'punch', color: '#be185d' },
    'page_one_v1': { type: 'kick', color: '#4c1d95' },
    'sasaki_v1': { type: 'blunt', color: '#166534' },
    'whos_who_v1': { type: 'slash', color: '#b91c1c' },
    'queen_v1': { type: 'projectile', color: '#a855f7' },
    'king_v1': { type: 'slash', color: '#dc2626' },
    'kaido_v1': { type: 'blunt', color: '#1e3a8a' },

    // === BIG MOM PIRATES ===
    'chess_soldiers_v1': { type: 'slash', color: '#ef4444' },
    'prometheus_v1': { type: 'projectile', color: '#ea580c' },
    'perospero_v1': { type: 'projectile', color: '#f472b6' },
    'daifuku_v1': { type: 'punch', color: '#2563eb' },
    'cracker_v1': { type: 'slash', color: '#92400e' },
    'smoothie_v1': { type: 'slash', color: '#db2777' },
    'katakuri_v1': { type: 'blunt', color: '#701a75' },
    'big_mom_v1': { type: 'slash', color: '#f472b6' },

    // === REVOLUTIONARY ===
    'hack_v1': { type: 'punch', color: '#2563eb' },
    'koala_v1': { type: 'punch', color: '#fb7185' },
    'belo_betty_v1': { type: 'projectile', color: '#be123c' },
    'ivankov_v1': { type: 'punch', color: '#a855f7' },
    'sabo_v1': { type: 'punch', color: '#f97316' },
    'dragon_v1': { type: 'punch', color: '#059669' },

    // === WHITEBEARD PIRATES ===
    'thatch_v1': { type: 'slash', color: '#0d9488' },
    'jozu_v1': { type: 'punch', color: '#0ea5e9' },
    'vista_v1': { type: 'slash', color: '#7c3aed' },
    'ace_v1': { type: 'projectile', color: '#ea580c' },
    'marco_v1': { type: 'projectile', color: '#22d3ee' },
    'whitebeard_v1': { type: 'blunt', color: '#f8fafc' },

    // Pokemon Fallbacks (kept for compatibility)
    'charmander': { type: 'punch', color: '#f97316' },
    'squirtle': { type: 'projectile', color: '#3b82f6' },
    'bulbasaur': { type: 'slash', color: '#22c55e' },

    // Fallback for unknown units
    '_default': { type: 'punch', color: '#94a3b8' }
}

// Per-unit ability styling
export const ABILITY_ANIMATIONS: Record<string, AbilityAnimationConfig> = {
    // === STRAW HAT ===
    'luffy_v1': { color: '#ef4444' },
    'zoro_v1': { color: '#22c55e' },
    'nami_v1': { color: '#38bdf8' },
    'usopp_v1': { color: '#f59e0b' },
    'chopper_v1': { color: '#f472b6' },
    'sanji_v1': { color: '#fbbf24' },
    'robin_v1': { color: '#a855f7' },
    'franky_v1': { color: '#0ea5e9', effectStyle: 'BEAM_HEAVY' },
    'brook_v1': { color: '#22d3ee' },
    'jinbei_v1': { color: '#1d4ed8' },

    // === MARINE ===
    'kizaru_v1': { color: '#facc15', effectStyle: 'BEAM_HEAVY' },
    'akainu_v1': { color: '#991b1b', effectStyle: 'MAGMA_RAIN' },
    'sengoku_v1': { color: '#eab308' },
    'garp_v1': { color: '#f8fafc' },

    // === WARLORD ===
    'hancock_v1': { color: '#ec4899' },
    'mihawk_v1': { color: '#22c55e' },
    'moria_v1': { color: '#7c3aed', effectStyle: 'SOUL_SPIRAL' },

    // === BEAST PIRATES ===
    'kaido_v1': { color: '#1e3a8a', effectStyle: 'DRAGON_ROAR' },
    'queen_v1': { color: '#a855f7' },
    'king_v1': { color: '#dc2626' },

    // === WHITEBEARD ===
    'whitebeard_v1': { color: '#f8fafc', effectStyle: 'QUAKE' },
    'ace_v1': { color: '#ea580c' },
    'marco_v1': { color: '#22d3ee' },

    // === BIG MOM ===
    'big_mom_v1': { color: '#f472b6', effectStyle: 'SOUL_SPIRAL' },
    'katakuri_v1': { color: '#701a75' },

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
