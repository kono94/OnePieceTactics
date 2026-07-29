import { describe, expect, it } from 'vitest'
import {
    hasDuplicateGalleryEntries,
    PALWORLD_ATTACK_GALLERY_ROSTER,
    PALWORLD_ULTIMATE_GALLERY_ROSTER,
    getPalworldGalleryStats
} from './palworldUltimateGalleryRoster'

describe('Palworld animation gallery roster', () => {
    it('keeps one stable ability preview and attack preview per Pal', () => {
        expect(getPalworldGalleryStats()).toEqual({
            abilityEntries: 55,
            abilityKeys: 55,
            attackEntries: 55,
            attackKeys: 55,
            showcaseVariants: 0
        })
        expect(PALWORLD_ULTIMATE_GALLERY_ROSTER).toHaveLength(55)
        expect(PALWORLD_ATTACK_GALLERY_ROSTER).toHaveLength(55)
    })

    it('reuses the same ability identity at every star level', () => {
        expect(hasDuplicateGalleryEntries()).toBe(false)
        expect(PALWORLD_ULTIMATE_GALLERY_ROSTER.filter((unit) => unit.id === 'lifmunk')).toHaveLength(1)
        expect(PALWORLD_ULTIMATE_GALLERY_ROSTER.every((unit) => unit.abilityAnimationKey === `pw-ability-${unit.id}`)).toBe(true)
        expect(PALWORLD_ULTIMATE_GALLERY_ROSTER.every((unit) => unit.starLevel === undefined)).toBe(true)
        expect(PALWORLD_ULTIMATE_GALLERY_ROSTER.find((unit) => unit.id === 'pengullet')?.traits).toEqual(['water', 'ice'])
    })
})
