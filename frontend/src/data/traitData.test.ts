import { beforeEach, describe, expect, it } from 'vitest'
import {
    getTraitData,
    getTraitGlyph,
    normalizeTraitId,
    setTraitData,
    TRAIT_DATA,
} from './traitData'

describe('generic trait metadata', () => {
    beforeEach(() => {
        Object.keys(TRAIT_DATA).forEach((key) => delete TRAIT_DATA[key])
    })

    it('normalizes backend ids and preserves theme-provided type metadata', () => {
        setTraitData([{
            id: 'water-type',
            name: 'Water',
            description: 'Water trait',
            effects: [],
            type: 'element',
            iconColor: '#38bdf8',
        }])

        expect(normalizeTraitId(' Water Type ')).toBe('water_type')
        expect(getTraitData('Water Type')?.type).toBe('element')
        expect(getTraitData('water-type')?.name).toBe('Water')
    })

    it('uses a supplied glyph and falls back to the trait initial', () => {
        expect(getTraitGlyph({
            id: 'water',
            name: 'Water',
            description: '',
            effects: [],
            type: 'element',
            iconColor: '#38bdf8',
            iconGlyph: '◇',
        })).toBe('◇')
        expect(getTraitGlyph({
            id: 'fire',
            name: 'Fire',
            description: '',
            effects: [],
            type: 'element',
            iconColor: '#fb7185',
        })).toBe('F')
    })
})
