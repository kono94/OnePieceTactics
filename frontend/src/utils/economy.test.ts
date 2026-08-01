import { describe, expect, it } from 'vitest'
import { calculateSellRefund } from './economy'
import type { GameUnit } from '../types'

describe('sell refund preview', () => {
    it.each([
        [1, 2],
        [2, 6],
        [3, 12],
    ])('matches the backend refund for a %s-star unit', (starLevel, refund) => {
        expect(calculateSellRefund({ cost: 2, starLevel } as GameUnit)).toBe(refund)
    })
})
