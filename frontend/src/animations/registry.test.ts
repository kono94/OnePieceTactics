import { describe, expect, it, beforeEach } from 'vitest'
import {
    PALWORLD_ABILITY_ANIMATION_KEYS,
    PALWORLD_ABILITY_ANIMATIONS,
    PALWORLD_ATTACK_ANIMATION_KEYS,
    PALWORLD_ATTACK_ANIMATIONS,
    clearMissingAnimationDiagnostics,
    getAbilityConfig,
    getAnimationRegistryCounts,
    getAttackConfig,
    getMissingAnimationDiagnostics
} from './index'
import { PALWORLD_ELEMENT_PALETTES } from './palettes'

describe('Palworld animation registry', () => {
    beforeEach(() => clearMissingAnimationDiagnostics())

    it('contains the exact plan coverage', () => {
        expect(getAnimationRegistryCounts()).toEqual({ palworldAttacks: 55, palworldAbilities: 55 })
        expect(new Set(PALWORLD_ATTACK_ANIMATION_KEYS).size).toBe(55)
        expect(new Set(PALWORLD_ABILITY_ANIMATION_KEYS).size).toBe(55)
        expect(PALWORLD_ATTACK_ANIMATION_KEYS.every((key) => key.startsWith('pw-attack-'))).toBe(true)
        expect(PALWORLD_ABILITY_ANIMATION_KEYS.every((key) => key.startsWith('pw-ability-'))).toBe(true)
        expect(PALWORLD_ABILITY_ANIMATION_KEYS.every((key) => !/-s[123]$/.test(key))).toBe(true)
        expect(Object.values(PALWORLD_ABILITY_ANIMATIONS).every((config) => !('element' in config))).toBe(true)
    })

    it('resolves Palworld by explicit namespace and key', () => {
        const attack = getAttackConfig('palworld', 'pw-attack-lamball')
        const ability = getAbilityConfig('palworld', 'pw-ability-panthalus', { traits: ['water'] })

        expect(attack).toBe(PALWORLD_ATTACK_ANIMATIONS['pw-attack-lamball'])
        expect(ability.effectStyle).toBe('PAL_WHIRLWIND')
        expect(ability.color).toBe(PALWORLD_ELEMENT_PALETTES.water.primary)
    })

    it('does not cross-resolve a Palworld key into another mode', () => {
        const config = getAbilityConfig('onepiece', 'pw-lamball-fluffy-shield')

        expect(config.diagnostic).toBe(true)
        expect(getMissingAnimationDiagnostics()).toEqual([
            expect.objectContaining({ kind: 'ability', gameMode: 'onepiece', key: 'pw-lamball-fluffy-shield' })
        ])
    })

    it('returns a loud diagnostic for an unknown Palworld key', () => {
        const config = getAttackConfig('palworld', 'pw-attack-not-a-pal')

        expect(config.diagnostic).toBe(true)
        expect(config.color).toBe('#FF00FF')
        expect(getMissingAnimationDiagnostics()[0]?.key).toBe('pw-attack-not-a-pal')
    })
})
