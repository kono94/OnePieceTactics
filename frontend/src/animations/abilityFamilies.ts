import { elementPalette } from './palettes'
import type {
    AbilityFamilyDefinition,
    PalworldAbilityAnimationConfig,
    PalworldAbilityEffectStyle
} from './types'

interface PalworldAbilityEntry {
    id: string
    signature: string
    effectStyle: PalworldAbilityEffectStyle
    screenShake: number
    particleScale: number
    accentColor?: string
}

export const PALWORLD_ABILITY_FAMILIES: Record<PalworldAbilityEffectStyle, AbilityFamilyDefinition> = {
    PAL_DIAGNOSTIC: { effectStyle: 'PAL_DIAGNOSTIC', durationScale: 0.5 },
    PAL_PROJECTILE: { effectStyle: 'PAL_PROJECTILE', durationScale: 0.9, trailWidth: 8, projectileCount: 1 },
    PAL_MELEE_BURST: { effectStyle: 'PAL_MELEE_BURST', durationScale: 0.72, ringCount: 1, trailWidth: 14 },
    PAL_LINE_CUT: { effectStyle: 'PAL_LINE_CUT', durationScale: 0.82, trailWidth: 18 },
    PAL_CONE_BREATH: { effectStyle: 'PAL_CONE_BREATH', durationScale: 1.05, trailWidth: 22 },
    PAL_DASH: { effectStyle: 'PAL_DASH', durationScale: 0.9, trailWidth: 16 },
    PAL_CHAIN: { effectStyle: 'PAL_CHAIN', durationScale: 1.1, projectileCount: 3, trailWidth: 10 },
    PAL_MULTI_SHOT: { effectStyle: 'PAL_MULTI_SHOT', durationScale: 1.15, projectileCount: 3, trailWidth: 10 },
    PAL_RADIUS_BURST: { effectStyle: 'PAL_RADIUS_BURST', durationScale: 0.95, ringCount: 2, trailWidth: 12 },
    PAL_AURA_BUFF: { effectStyle: 'PAL_AURA_BUFF', durationScale: 1.1, ringCount: 2 },
    PAL_HEAL_BLOOM: { effectStyle: 'PAL_HEAL_BLOOM', durationScale: 1.15, ringCount: 3 },
    PAL_SHIELD_FIELD: { effectStyle: 'PAL_SHIELD_FIELD', durationScale: 1.25, ringCount: 3 },
    PAL_CONTROL_FIELD: { effectStyle: 'PAL_CONTROL_FIELD', durationScale: 1.2, ringCount: 3 },
    PAL_METEOR_RAIN: { effectStyle: 'PAL_METEOR_RAIN', durationScale: 1.25, projectileCount: 5, ringCount: 2 },
    PAL_PERSISTENT_ZONE: { effectStyle: 'PAL_PERSISTENT_ZONE', durationScale: 1.5, ringCount: 4 },
    PAL_BEAM: { effectStyle: 'PAL_BEAM', durationScale: 1.25, trailWidth: 28 },
    PAL_WHIRLWIND: { effectStyle: 'PAL_WHIRLWIND', durationScale: 1.45, projectileCount: 4, ringCount: 3 },
    PAL_BUBBLE_FIELD: { effectStyle: 'PAL_BUBBLE_FIELD', durationScale: 1.35, projectileCount: 6, ringCount: 2 }
}

// Every Pal owns one visual identity. Star level changes intensity in the renderer,
// but never changes the ability key or its geometry family.
const abilityEntries: PalworldAbilityEntry[] = [
    { id: 'lamball', signature: 'Fluffy Shield', effectStyle: 'PAL_SHIELD_FIELD', screenShake: 0, particleScale: 0.85 },
    { id: 'cattiva', signature: 'Cat Punch', effectStyle: 'PAL_MELEE_BURST', screenShake: 2, particleScale: 0.85 },
    { id: 'chikipi', signature: 'Egg Drop', effectStyle: 'PAL_HEAL_BLOOM', screenShake: 0, particleScale: 0.85 },
    { id: 'foxparks', signature: 'Huggy Fire', effectStyle: 'PAL_CONE_BREATH', screenShake: 3, particleScale: 0.95 },
    { id: 'lifmunk', signature: 'Wind Cutter', effectStyle: 'PAL_LINE_CUT', screenShake: 2, particleScale: 0.9 },
    { id: 'pengullet', signature: 'Aqua Gun', effectStyle: 'PAL_PROJECTILE', screenShake: 2, particleScale: 0.9 },
    { id: 'daedream', signature: 'Dark Ball', effectStyle: 'PAL_PROJECTILE', screenShake: 2, particleScale: 0.95 },
    { id: 'depresso', signature: 'Caffeine Slap', effectStyle: 'PAL_AURA_BUFF', screenShake: 0, particleScale: 0.9, accentColor: '#67E8F9' },
    { id: 'gumoss', signature: 'Sand Blast', effectStyle: 'PAL_RADIUS_BURST', screenShake: 3, particleScale: 0.95 },
    { id: 'vixy', signature: 'Dig Here!', effectStyle: 'PAL_HEAL_BLOOM', screenShake: 0, particleScale: 0.95, accentColor: '#D9A441' },
    { id: 'sparkit', signature: 'Static Electricity', effectStyle: 'PAL_AURA_BUFF', screenShake: 1, particleScale: 0.95 },
    { id: 'tanzee', signature: 'Wind Cutter', effectStyle: 'PAL_LINE_CUT', screenShake: 2, particleScale: 0.9 },
    { id: 'fuack', signature: 'Surfing Slam', effectStyle: 'PAL_DASH', screenShake: 3, particleScale: 1 },
    { id: 'tocotoco', signature: 'Eggbomb', effectStyle: 'PAL_PROJECTILE', screenShake: 3, particleScale: 0.95, accentColor: '#FF7F6E' },
    { id: 'direhowl', signature: 'Fierce Fang', effectStyle: 'PAL_DASH', screenShake: 3, particleScale: 0.95 },
    { id: 'celaray', signature: 'Zephyr Glider', effectStyle: 'PAL_SHIELD_FIELD', screenShake: 0, particleScale: 1 },
    { id: 'dumud', signature: 'Earth Impact', effectStyle: 'PAL_RADIUS_BURST', screenShake: 4, particleScale: 1 },
    { id: 'dazzi', signature: 'Lady of Lightning', effectStyle: 'PAL_CHAIN', screenShake: 3, particleScale: 1.05 },
    { id: 'flambelle', signature: 'Magma Tears', effectStyle: 'PAL_PERSISTENT_ZONE', screenShake: 2, particleScale: 1 },
    { id: 'mimog', signature: 'Surprise Box', effectStyle: 'PAL_SHIELD_FIELD', screenShake: 2, particleScale: 1, accentColor: '#D9A441' },
    { id: 'cremis', signature: 'Fluffy Wool', effectStyle: 'PAL_HEAL_BLOOM', screenShake: 0, particleScale: 1 },
    { id: 'melpaca', signature: 'Fluffy Tackle', effectStyle: 'PAL_DASH', screenShake: 3, particleScale: 1 },
    { id: 'galeclaw', signature: 'Gale Claw', effectStyle: 'PAL_DASH', screenShake: 4, particleScale: 1.05 },
    { id: 'lovander', signature: 'Heart Drain', effectStyle: 'PAL_PROJECTILE', screenShake: 1, particleScale: 1, accentColor: '#FF7F6E' },
    { id: 'hoodle', signature: 'Dark Whisp', effectStyle: 'PAL_PROJECTILE', screenShake: 3, particleScale: 1.05 },
    { id: 'chillet', signature: 'Rocket Slam', effectStyle: 'PAL_DASH', screenShake: 4, particleScale: 1 },
    { id: 'penking', signature: 'Emperor Slide', effectStyle: 'PAL_DASH', screenShake: 4, particleScale: 1.1, accentColor: '#D9A441' },
    { id: 'katress', signature: 'Nightmare Ball', effectStyle: 'PAL_RADIUS_BURST', screenShake: 4, particleScale: 1.1 },
    { id: 'lunaris', signature: 'Antigravity', effectStyle: 'PAL_CONTROL_FIELD', screenShake: 2, particleScale: 1.05 },
    { id: 'quivern', signature: 'Dragon Meteor', effectStyle: 'PAL_METEOR_RAIN', screenShake: 4, particleScale: 1.1 },
    { id: 'petallia', signature: 'Blessing of the Flower Spirit', effectStyle: 'PAL_HEAL_BLOOM', screenShake: 0, particleScale: 1.1 },
    { id: 'mossanda', signature: 'Grenadier Panda', effectStyle: 'PAL_MULTI_SHOT', screenShake: 5, particleScale: 1.15 },
    { id: 'grizzbolt', signature: 'Shockwave', effectStyle: 'PAL_RADIUS_BURST', screenShake: 5, particleScale: 1.05 },
    { id: 'tarantriss', signature: 'Web Shooter', effectStyle: 'PAL_CONE_BREATH', screenShake: 3, particleScale: 1.1, accentColor: '#E2E8F0' },
    { id: 'relaxaurus', signature: 'Hungry Missile', effectStyle: 'PAL_MULTI_SHOT', screenShake: 5, particleScale: 1.15 },
    { id: 'tetroise', signature: 'Cube Press', effectStyle: 'PAL_DASH', screenShake: 6, particleScale: 1.2 },
    { id: 'anubis', signature: 'Ground Smash', effectStyle: 'PAL_RADIUS_BURST', screenShake: 6, particleScale: 1.1 },
    { id: 'shadowbeak', signature: 'Nightmare Ball', effectStyle: 'PAL_PROJECTILE', screenShake: 5, particleScale: 1.1 },
    { id: 'lyleen', signature: 'Harvest Goddess', effectStyle: 'PAL_HEAL_BLOOM', screenShake: 0, particleScale: 1.15, accentColor: '#D9A441' },
    { id: 'orserk', signature: 'Kerauno', effectStyle: 'PAL_DASH', screenShake: 6, particleScale: 1.2 },
    { id: 'selyne', signature: 'Seigetsu Blade', effectStyle: 'PAL_LINE_CUT', screenShake: 5, particleScale: 1.15 },
    { id: 'jormuntide-ignis', signature: 'Stormbringer Lava', effectStyle: 'PAL_CONE_BREATH', screenShake: 6, particleScale: 1.25 },
    { id: 'bellanoir', signature: 'Nightmare Bloom', effectStyle: 'PAL_RADIUS_BURST', screenShake: 4, particleScale: 1.1 },
    { id: 'aegidron', signature: 'Explosive Missile', effectStyle: 'PAL_MULTI_SHOT', screenShake: 6, particleScale: 1.25, accentColor: '#D9A441' },
    { id: 'renjishi', signature: 'Volcanic Rain', effectStyle: 'PAL_METEOR_RAIN', screenShake: 6, particleScale: 1.25 },
    { id: 'silvance', signature: 'Spore Burst', effectStyle: 'PAL_RADIUS_BURST', screenShake: 4, particleScale: 1.2 },
    { id: 'dandilord', signature: 'Toxic Dance', effectStyle: 'PAL_PERSISTENT_ZONE', screenShake: 4, particleScale: 1.2, accentColor: '#86EFAC' },
    { id: 'shaolong', signature: 'Azure Dracoflare', effectStyle: 'PAL_CONE_BREATH', screenShake: 6, particleScale: 1.25, accentColor: '#67E8F9' },
    { id: 'jetragon', signature: 'Dragon Burst', effectStyle: 'PAL_RADIUS_BURST', screenShake: 6, particleScale: 1.2 },
    { id: 'frostallion', signature: 'Iceberg', effectStyle: 'PAL_RADIUS_BURST', screenShake: 5, particleScale: 1.2 },
    { id: 'paladius', signature: 'Spear Thrust', effectStyle: 'PAL_DASH', screenShake: 5, particleScale: 1.2 },
    { id: 'necromus', signature: 'Twin Spears', effectStyle: 'PAL_MULTI_SHOT', screenShake: 5, particleScale: 1.2 },
    { id: 'neptilius', signature: 'Aqua Spear', effectStyle: 'PAL_LINE_CUT', screenShake: 5, particleScale: 1.2 },
    { id: 'xenolord', signature: 'Dark Arrow', effectStyle: 'PAL_LINE_CUT', screenShake: 6, particleScale: 1.25 },
    { id: 'panthalus', signature: 'Maelstrom', effectStyle: 'PAL_WHIRLWIND', screenShake: 7, particleScale: 1.45 }
]

export function getPalworldAbilityAnimationKey(definitionId: string) {
    return `pw-ability-${definitionId}`
}

export const PALWORLD_ABILITY_DEFINITION_IDS = Object.freeze(abilityEntries.map((entry) => entry.id))

export const PALWORLD_ABILITY_ANIMATIONS: Record<string, PalworldAbilityAnimationConfig> = Object.fromEntries(
    abilityEntries.map((entry) => {
        const family = PALWORLD_ABILITY_FAMILIES[entry.effectStyle]
        const palette = elementPalette('neutral')
        return [getPalworldAbilityAnimationKey(entry.id), {
            effectStyle: entry.effectStyle,
            signature: entry.signature,
            color: palette.primary,
            secondaryColor: palette.secondary,
            accentColor: entry.accentColor,
            screenShake: entry.screenShake,
            particleScale: entry.particleScale,
            durationScale: family.durationScale,
            projectileCount: family.projectileCount,
            ringCount: family.ringCount,
            trailWidth: family.trailWidth,
            glyph: palette.glyph
        }]
    })
) as Record<string, PalworldAbilityAnimationConfig>

export const PALWORLD_ABILITY_ANIMATION_KEYS = Object.freeze(abilityEntries.map((entry) => getPalworldAbilityAnimationKey(entry.id)))
