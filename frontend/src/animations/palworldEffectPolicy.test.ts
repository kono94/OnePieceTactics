import { describe, expect, it } from 'vitest'
import { PALWORLD_ABILITY_FAMILIES } from './abilityFamilies'
import {
  PALWORLD_EFFECT_GEOMETRY,
  chooseEffectEvictionIndex,
  getPalworldEffectPolicy,
  getRenderEffectPriority,
} from './palworldEffectPolicy'

describe('Palworld live effect policy', () => {
  it('covers every registered PAL style with a geometry family', () => {
    expect(Object.keys(PALWORLD_EFFECT_GEOMETRY).sort()).toEqual(
      Object.keys(PALWORLD_ABILITY_FAMILIES).sort(),
    )
    for (const effectStyle of Object.keys(PALWORLD_ABILITY_FAMILIES)) {
      const policy = getPalworldEffectPolicy({
        effectStyle: effectStyle as keyof typeof PALWORLD_ABILITY_FAMILIES,
      })
      expect(policy?.geometry).toBeTruthy()
      expect(policy?.durationScale).toBeGreaterThan(0)
      expect(policy?.projectileCount).toBeGreaterThan(0)
      expect(policy?.ringCount).toBeGreaterThan(0)
      expect(policy?.trailWidth).toBeGreaterThan(0)
    }
  })

  it('honors configured geometry controls', () => {
    expect(
      getPalworldEffectPolicy({
        effectStyle: 'PAL_MULTI_SHOT',
        durationScale: 1.4,
        projectileCount: 5.4,
        ringCount: 0,
        trailWidth: 24,
      }),
    ).toMatchObject({
      geometry: 'multiShot',
      durationScale: 1.4,
      projectileCount: 5,
      ringCount: 1,
      trailWidth: 24,
    })
  })

  it('protects ability effects from ordinary effect pressure', () => {
    const effects = [
      { priority: 'ability' as const },
      { priority: 'standard' as const },
      { priority: 'ability' as const },
    ]
    expect(chooseEffectEvictionIndex(effects, 'ability', 3)).toBe(1)
    expect(chooseEffectEvictionIndex([{ priority: 'ability' as const }], 'standard', 1)).toBe(-1)
    expect(chooseEffectEvictionIndex([{ priority: 'ability' as const }], 'ability', 1)).toBe(0)
  })

  it('prioritizes skill, heal, and shield events', () => {
    expect(getRenderEffectPriority('SKILL')).toBe('ability')
    expect(getRenderEffectPriority('HEAL')).toBe('ability')
    expect(getRenderEffectPriority('SHIELD')).toBe('ability')
    expect(getRenderEffectPriority('DAMAGE')).toBe('standard')
  })
})
