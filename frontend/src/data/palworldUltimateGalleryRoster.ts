import palworldUnits from '../../../backend/src/main/resources/data/units_palworld.json'
import {
    getPalworldAbilityAnimationKey,
    PALWORLD_ABILITY_DEFINITION_IDS
} from '../animations/abilityFamilies'
import type { UltimateGalleryUnit } from './ultimateGalleryRoster'

interface RawPalworldAbility {
    name?: string
    type?: string
    pattern?: string
}

interface RawPalworldUnit {
    id: string
    name: string
    cost: number
    traits?: string[]
    ability?: RawPalworldAbility | null
}

function toGalleryUnit(unit: RawPalworldUnit): UltimateGalleryUnit {
    return {
        id: unit.id,
        name: unit.name,
        cost: unit.cost,
        abilityType: unit.ability?.type ?? 'DAMAGE',
        pattern: unit.ability?.pattern ?? 'SINGLE',
        abilityName: unit.ability?.name ?? 'Preview',
        gameMode: 'palworld',
        traits: (unit.traits ?? []).map((trait) => trait.toLowerCase()),
        attackAnimationKey: `pw-attack-${unit.id}`,
        abilityAnimationKey: getPalworldAbilityAnimationKey(unit.id)
    }
}

export const PALWORLD_ULTIMATE_GALLERY_ROSTER: UltimateGalleryUnit[] = (palworldUnits as RawPalworldUnit[]).map(toGalleryUnit)

if (
    PALWORLD_ULTIMATE_GALLERY_ROSTER.length !== 55
    || new Set(PALWORLD_ULTIMATE_GALLERY_ROSTER.map((unit) => unit.id)).size !== 55
    || new Set(PALWORLD_ULTIMATE_GALLERY_ROSTER.map((unit) => unit.id)).size !== PALWORLD_ABILITY_DEFINITION_IDS.length
) {
    throw new Error('Palworld gallery must contain exactly one entry for each of the 55 Pals')
}

export const PALWORLD_ATTACK_GALLERY_ROSTER: UltimateGalleryUnit[] = PALWORLD_ULTIMATE_GALLERY_ROSTER.map((unit) => unit)

export function getPalworldGalleryStats() {
    return {
        abilityEntries: PALWORLD_ULTIMATE_GALLERY_ROSTER.length,
        abilityKeys: new Set(PALWORLD_ULTIMATE_GALLERY_ROSTER.map((unit) => unit.abilityAnimationKey)).size,
        attackEntries: PALWORLD_ATTACK_GALLERY_ROSTER.length,
        attackKeys: new Set(PALWORLD_ATTACK_GALLERY_ROSTER.map((unit) => unit.attackAnimationKey)).size,
        showcaseVariants: 0
    }
}

export function hasDuplicateGalleryEntries() {
    const keys = PALWORLD_ULTIMATE_GALLERY_ROSTER.map((unit) => `${unit.id}:${unit.abilityAnimationKey}`)
    return new Set(keys).size !== keys.length
}
