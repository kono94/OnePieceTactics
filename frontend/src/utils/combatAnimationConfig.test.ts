import { describe, expect, it } from 'vitest'
import { resolveAbilityConfig, resolveAttackConfig } from './combatAnimationConfig'

describe('live combat animation config', () => {
    it('uses Palworld registry keys and element context', () => {
        const unit = { definitionId: 'lamball', traits: ['neutral'] }

        expect(resolveAttackConfig('palworld', unit).diagnostic).not.toBe(true)
        expect(resolveAbilityConfig('palworld', unit).diagnostic).not.toBe(true)
    })
})
