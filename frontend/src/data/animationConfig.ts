// Animation configuration for attack and ability effects
// Pattern = shape (SINGLE, LINE, SURROUND), styling comes from this config

export type AttackType =
    | 'punch'
    | 'slash'
    | 'projectile'
    | 'kick'
    | 'blunt'
    | 'rubberPunch'
    | 'tripleSlash'
    | 'sniperShot'
    | 'magmaFist'
    | 'waterShock'
    | 'lightning'
    | 'fireKick'

export interface AttackAnimationConfig {
    type: AttackType
    color: string
    secondaryColor?: string
    particles?: number
}

export type AbilityEffectStyle =
    | 'DEFAULT'
    | 'BEAM_HEAVY'
    | 'MAGMA_RAIN'
    | 'QUAKE'
    | 'SOUL_SPIRAL'
    | 'DRAGON_ROAR'
    | 'LUFFY_GATLING'
    | 'ZORO_ONIGIRI'
    | 'USOPP_EXPLOSIVE_STAR'
    | 'NAMI_LIGHTNING_TEMPO'
    | 'SANJI_DIABLE_JAMBE'
    | 'AKAINU_MAGMA_RAIN'
    | 'WHITEBEARD_QUAKE'
    | 'FRANKY_RADICAL_BEAM'
    | 'ROBIN_ARM_FIELD'
    | 'BROOK_SOUL_FREEZE'
    | 'JINBEI_WATER_SURGE'
    | 'KIZARU_LIGHT_BEAM'
    | 'MIHAWK_BLACK_BLADE'
    | 'KAIDO_DRAGON_ROAR'
    | 'BIG_MOM_SOUL_STORM'
    | 'ACE_FIRE_FIST'
    | 'MARCO_PHOENIX_FLAME'
    | 'CHOPPER_HEAL'
    | 'KOBY_DETERMINATION'
    | 'PEROSPERO_CANDY_SHOWER'
    | 'IVANKOV_HORMONE_HEAL'
    | 'JOZU_DIAMOND_GUARD'
    | 'HANCOCK_LOVE_ARROW'
    | 'CROCODILE_SANDSTORM'
    | 'DOFLAMINGO_STRING_CAGE'
    | 'SMOKER_WHITE_OUT'
    | 'HINA_BINDING_CAGE'
    | 'KUMA_URSUS_SHOCK'
    | 'BUGGY_CHOP_FESTIVAL'
    | 'MORIA_SHADOW_STEAL'
    | 'QUEEN_PLAGUE_ROUND'
    | 'KAIDO_THUNDER_BAGUA'
    | 'GARP_FIST_METEOR'
    | 'SENGOKU_BUDDHA_SHOCK'
    | 'QUEEN_LASER_VOLLEY'
    | 'KING_FLAME_SLASH'
    | 'KATAKURI_MOCHI_CRUSH'
    | 'ELEMENT_BURST'
    | 'WEAPON_BARRAGE'
    | 'AURA_COMMAND'
    | 'BEAST_RUSH'
    | 'SHADOW_DRAIN'
    | 'POISON_CLOUD'
    | 'CANDY_TRAP'
    | 'LOVE_BURST'
    | 'WIND_STORM'

export interface AbilityAnimationConfig {
    color: string
    effectStyle?: AbilityEffectStyle
    secondaryColor?: string
    signature?: string
    screenShake?: number
    particleScale?: number
}

// Per-unit attack animations (auto-attacks)
export const ATTACK_ANIMATIONS: Record<string, AttackAnimationConfig> = {
    // === STRAW HAT ===
    'luffy_v1': { type: 'rubberPunch', color: '#ef4444', secondaryColor: '#fbbf24', particles: 18 },
    'zoro_v1': { type: 'tripleSlash', color: '#22c55e', secondaryColor: '#dcfce7', particles: 16 },
    'nami_v1': { type: 'lightning', color: '#38bdf8', secondaryColor: '#fef08a', particles: 18 },
    'usopp_v1': { type: 'sniperShot', color: '#f59e0b', secondaryColor: '#fef3c7', particles: 14 },
    'chopper_v1': { type: 'punch', color: '#f472b6' }, // Pink
    'sanji_v1': { type: 'fireKick', color: '#f97316', secondaryColor: '#fde047', particles: 18 },
    'robin_v1': { type: 'punch', color: '#a855f7' }, // Purple
    'franky_v1': { type: 'punch', color: '#0ea5e9' }, // Sky blue
    'brook_v1': { type: 'slash', color: '#22d3ee' }, // Ice cyan
    'jinbei_v1': { type: 'waterShock', color: '#1d4ed8', secondaryColor: '#67e8f9', particles: 18 },

    // === MARINE ===
    'koby_v1': { type: 'punch', color: '#f472b6' },
    'helmeppo_v1': { type: 'slash', color: '#8b5cf6' },
    'tashigi_v1': { type: 'slash', color: '#6366f1' },
    'hina_v1': { type: 'punch', color: '#ec4899' },
    'smoker_v1': { type: 'blunt', color: '#cbd5e1' }, // Smoke gray
    'garp_v1': { type: 'punch', color: '#94a3b8' },
    'sengoku_v1': { type: 'punch', color: '#eab308' }, // Gold
    'kizaru_v1': { type: 'projectile', color: '#facc15' }, // Light yellow
    'akainu_v1': { type: 'magmaFist', color: '#991b1b', secondaryColor: '#fb923c', particles: 20 },

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
    'whitebeard_v1': { type: 'blunt', color: '#f8fafc', secondaryColor: '#93c5fd', particles: 20 },

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
    'luffy_v1': { color: '#ef4444', secondaryColor: '#fbbf24', effectStyle: 'LUFFY_GATLING', signature: 'Gum Gum Gatling', screenShake: 6, particleScale: 1.2 },
    'zoro_v1': { color: '#22c55e', secondaryColor: '#dcfce7', effectStyle: 'ZORO_ONIGIRI', signature: 'Oni Giri', screenShake: 5, particleScale: 1.15 },
    'nami_v1': { color: '#38bdf8', secondaryColor: '#fef08a', effectStyle: 'NAMI_LIGHTNING_TEMPO', signature: 'Lightning Tempo', screenShake: 3, particleScale: 1.05 },
    'usopp_v1': { color: '#f59e0b', secondaryColor: '#fef3c7', effectStyle: 'USOPP_EXPLOSIVE_STAR', signature: 'I Got This!', screenShake: 4, particleScale: 1.1 },
    'chopper_v1': { color: '#22c55e', secondaryColor: '#bbf7d0', effectStyle: 'CHOPPER_HEAL', signature: 'Doctor Heal', screenShake: 0, particleScale: 1.15 },
    'sanji_v1': { color: '#fbbf24', secondaryColor: '#f97316', effectStyle: 'SANJI_DIABLE_JAMBE', signature: 'Diable Jambe', screenShake: 4, particleScale: 1.1 },
    'robin_v1': { color: '#a855f7', secondaryColor: '#f0abfc', effectStyle: 'ROBIN_ARM_FIELD', signature: 'Cien Fleur', screenShake: 2 },
    'franky_v1': { color: '#0ea5e9', secondaryColor: '#bae6fd', effectStyle: 'FRANKY_RADICAL_BEAM', signature: 'Radical Beam', screenShake: 4, particleScale: 1.1 },
    'brook_v1': { color: '#22d3ee', secondaryColor: '#e0f2fe', effectStyle: 'BROOK_SOUL_FREEZE', signature: 'Soul Solid', screenShake: 2 },
    'jinbei_v1': { color: '#1d4ed8', secondaryColor: '#67e8f9', effectStyle: 'JINBEI_WATER_SURGE', signature: 'Ocean Current Throw', screenShake: 4, particleScale: 1.1 },

    // === MARINE ===
    'koby_v1': { color: '#22c55e', secondaryColor: '#dcfce7', effectStyle: 'KOBY_DETERMINATION', signature: 'Determination', screenShake: 0, particleScale: 0.85 },
    'helmeppo_v1': { color: '#8b5cf6', secondaryColor: '#ddd6fe', effectStyle: 'WEAPON_BARRAGE', signature: 'Twin Kukri Rush', screenShake: 2 },
    'tashigi_v1': { color: '#6366f1', secondaryColor: '#c7d2fe', effectStyle: 'WEAPON_BARRAGE', signature: 'Marine Blade Draw', screenShake: 2 },
    'hina_v1': { color: '#ec4899', secondaryColor: '#fbcfe8', effectStyle: 'HINA_BINDING_CAGE', signature: 'Cage Cage', screenShake: 2 },
    'smoker_v1': { color: '#cbd5e1', secondaryColor: '#64748b', effectStyle: 'SMOKER_WHITE_OUT', signature: 'White Out', screenShake: 3 },
    'garp_v1': { color: '#f8fafc', secondaryColor: '#60a5fa', effectStyle: 'GARP_FIST_METEOR', signature: 'Galaxy Fist', screenShake: 8, particleScale: 1.35 },
    'sengoku_v1': { color: '#eab308', secondaryColor: '#fef3c7', effectStyle: 'SENGOKU_BUDDHA_SHOCK', signature: 'Buddha Shockwave', screenShake: 5, particleScale: 1.1 },
    'kizaru_v1': { color: '#facc15', secondaryColor: '#fef9c3', effectStyle: 'KIZARU_LIGHT_BEAM', signature: 'Yasakani Light', screenShake: 4, particleScale: 1.15 },
    'akainu_v1': { color: '#991b1b', secondaryColor: '#fb923c', effectStyle: 'AKAINU_MAGMA_RAIN', signature: 'Great Eruption', screenShake: 6, particleScale: 1.2 },

    // === WARLORD ===
    'buggy_v1': { color: '#ef4444', secondaryColor: '#60a5fa', effectStyle: 'BUGGY_CHOP_FESTIVAL', signature: 'Chop Chop Festival', screenShake: 3 },
    'moria_v1': { color: '#7c3aed', secondaryColor: '#c4b5fd', effectStyle: 'MORIA_SHADOW_STEAL', signature: 'Shadow Steal', screenShake: 4 },
    'crocodile_v1': { color: '#d97706', secondaryColor: '#fde68a', effectStyle: 'CROCODILE_SANDSTORM', signature: 'Desert Spada', screenShake: 4 },
    'kuma_v1': { color: '#3b82f6', secondaryColor: '#bfdbfe', effectStyle: 'KUMA_URSUS_SHOCK', signature: 'Ursus Shock', screenShake: 5, particleScale: 1.1 },
    'doflamingo_v1': { color: '#f472b6', secondaryColor: '#fbcfe8', effectStyle: 'DOFLAMINGO_STRING_CAGE', signature: 'String Cage', screenShake: 4 },
    'mihawk_v1': { color: '#22c55e', secondaryColor: '#f8fafc', effectStyle: 'MIHAWK_BLACK_BLADE', signature: 'Black Blade Wave', screenShake: 5, particleScale: 1.15 },
    'hancock_v1': { color: '#ec4899', secondaryColor: '#f9a8d4', effectStyle: 'HANCOCK_LOVE_ARROW', signature: 'Love Arrow', screenShake: 3 },

    // === BEAST PIRATES ===
    'gifter_v1': { color: '#78350f', secondaryColor: '#f59e0b', effectStyle: 'BEAST_RUSH', signature: 'Beast Charge', screenShake: 3 },
    'headliner_v1': { color: '#450a0a', secondaryColor: '#f87171', effectStyle: 'BEAST_RUSH', signature: 'Headliner Stomp', screenShake: 3 },
    'ulti_v1': { color: '#be185d', secondaryColor: '#f9a8d4', effectStyle: 'BEAST_RUSH', signature: 'Meteor Headbutt', screenShake: 4 },
    'page_one_v1': { color: '#4c1d95', secondaryColor: '#c4b5fd', effectStyle: 'BEAST_RUSH', signature: 'Spinosaurus Rush', screenShake: 4 },
    'sasaki_v1': { color: '#166534', secondaryColor: '#86efac', effectStyle: 'BEAST_RUSH', signature: 'Armored Drill', screenShake: 4 },
    'whos_who_v1': { color: '#b91c1c', secondaryColor: '#fca5a5', effectStyle: 'WEAPON_BARRAGE', signature: 'Fang Pistol', screenShake: 3 },
    'queen_v1': { color: '#a855f7', secondaryColor: '#84cc16', effectStyle: 'QUEEN_PLAGUE_ROUND', signature: 'Plague Bullet', screenShake: 5 },
    'king_v1': { color: '#dc2626', secondaryColor: '#fb923c', effectStyle: 'KING_FLAME_SLASH', signature: 'Imperial Flame', screenShake: 5 },
    'kaido_v1': { color: '#1e3a8a', secondaryColor: '#a78bfa', effectStyle: 'KAIDO_THUNDER_BAGUA', signature: 'Thunder Bagua', screenShake: 9, particleScale: 1.45 },

    // === WHITEBEARD ===
    'thatch_v1': { color: '#0d9488', secondaryColor: '#5eead4', effectStyle: 'WEAPON_BARRAGE', signature: 'Chef Blade Flurry', screenShake: 2 },
    'jozu_v1': { color: '#0ea5e9', secondaryColor: '#e0f2fe', effectStyle: 'JOZU_DIAMOND_GUARD', signature: 'Diamond Defense', screenShake: 0, particleScale: 1.05 },
    'vista_v1': { color: '#7c3aed', secondaryColor: '#f0abfc', effectStyle: 'WEAPON_BARRAGE', signature: 'Rose Rondo', screenShake: 3 },
    'ace_v1': { color: '#ea580c', secondaryColor: '#fef3c7', effectStyle: 'ACE_FIRE_FIST', signature: 'Fire Fist', screenShake: 7, particleScale: 1.35 },
    'marco_v1': { color: '#22d3ee', secondaryColor: '#60a5fa', effectStyle: 'MARCO_PHOENIX_FLAME', signature: 'Phoenix Flames', screenShake: 0, particleScale: 1.2 },
    'whitebeard_v1': { color: '#f8fafc', secondaryColor: '#93c5fd', effectStyle: 'WHITEBEARD_QUAKE', signature: 'Marineford Quake', screenShake: 10, particleScale: 1.5 },

    // === BIG MOM ===
    'chess_soldiers_v1': { color: '#ef4444', secondaryColor: '#fca5a5', effectStyle: 'WEAPON_BARRAGE', signature: 'Chess Formation', screenShake: 2 },
    'prometheus_v1': { color: '#ea580c', secondaryColor: '#fde047', effectStyle: 'ACE_FIRE_FIST', signature: 'Solar Flare', screenShake: 4 },
    'perospero_v1': { color: '#22c55e', secondaryColor: '#f9a8d4', effectStyle: 'PEROSPERO_CANDY_SHOWER', signature: 'Candy Shower', screenShake: 0, particleScale: 1 },
    'daifuku_v1': { color: '#2563eb', secondaryColor: '#bfdbfe', effectStyle: 'WEAPON_BARRAGE', signature: 'Genie Slash', screenShake: 4 },
    'cracker_v1': { color: '#92400e', secondaryColor: '#fbbf24', effectStyle: 'WEAPON_BARRAGE', signature: 'Biscuit Legion', screenShake: 3 },
    'smoothie_v1': { color: '#db2777', secondaryColor: '#f9a8d4', effectStyle: 'SHADOW_DRAIN', signature: 'Juice Drain', screenShake: 3 },
    'katakuri_v1': { color: '#701a75', secondaryColor: '#f0abfc', effectStyle: 'KATAKURI_MOCHI_CRUSH', signature: 'Mochi Crush', screenShake: 5 },
    'big_mom_v1': { color: '#f472b6', secondaryColor: '#fef08a', effectStyle: 'BIG_MOM_SOUL_STORM', signature: 'Soul Storm', screenShake: 7, particleScale: 1.25 },

    // === REVOLUTIONARY ===
    'hack_v1': { color: '#2563eb', secondaryColor: '#67e8f9', effectStyle: 'JINBEI_WATER_SURGE', signature: 'Fishman Palm', screenShake: 3 },
    'koala_v1': { color: '#fb7185', secondaryColor: '#fecdd3', effectStyle: 'BEAST_RUSH', signature: 'Fishman Counter', screenShake: 3 },
    'belo_betty_v1': { color: '#be123c', secondaryColor: '#fda4af', effectStyle: 'AURA_COMMAND', signature: 'Rebel Flag', screenShake: 2 },
    'ivankov_v1': { color: '#22c55e', secondaryColor: '#f0abfc', effectStyle: 'IVANKOV_HORMONE_HEAL', signature: 'Healing Hormone', screenShake: 0, particleScale: 1.05 },
    'sabo_v1': { color: '#f97316', secondaryColor: '#fef3c7', effectStyle: 'ACE_FIRE_FIST', signature: 'Dragon Claw Flame', screenShake: 5 },
    'dragon_v1': { color: '#059669', secondaryColor: '#a7f3d0', effectStyle: 'WIND_STORM', signature: 'Revolution Storm', screenShake: 6, particleScale: 1.15 },

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
