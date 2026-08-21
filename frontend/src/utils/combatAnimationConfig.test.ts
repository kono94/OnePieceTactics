import { describe, expect, it } from 'vitest'
import { resolveAbilityConfig, resolveAttackConfig } from './combatAnimationConfig'

describe('live combat animation config', () => {
    it('resolves both supported modes through shared definition lookups', () => {
        const onePieceUnit = { definitionId: 'luffy_v1', traits: [] }
        const pokemonUnit = { definitionId: 'pikachu', traits: ['electric'] }

        expect(resolveAttackConfig(onePieceUnit).diagnostic).not.toBe(true)
        expect(resolveAbilityConfig(onePieceUnit).diagnostic).not.toBe(true)
        expect(resolveAttackConfig(pokemonUnit).diagnostic).not.toBe(true)
        expect(resolveAbilityConfig(pokemonUnit).diagnostic).not.toBe(true)
    })
})
