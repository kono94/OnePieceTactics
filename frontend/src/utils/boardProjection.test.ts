import { describe, expect, it } from 'vitest'
import {
  COMBAT_ROWS,
  PLANNING_ROWS,
  projectBoardCoordinate,
  isValidAuthoritativeCoordinate,
} from './boardProjection'

describe('board projection', () => {
  it('projects planning coordinates onto the local bottom half', () => {
    expect(projectBoardCoordinate(0, 0, 'PLANNING', false)).toEqual({ x: 0, y: PLANNING_ROWS })
    expect(projectBoardCoordinate(8, 2, 'PLANNING', false)).toEqual({ x: 8, y: 5 })
  })

  it('keeps bottom-side combat coordinates unchanged', () => {
    expect(projectBoardCoordinate(2, 0, 'COMBAT', false)).toEqual({ x: 2, y: 0 })
    expect(projectBoardCoordinate(7, 5, 'COMBAT', false)).toEqual({ x: 7, y: COMBAT_ROWS - 1 })
  })

  it('flips top-side combat coordinates around the arena center', () => {
    expect(projectBoardCoordinate(2, 0, 'COMBAT', true)).toEqual({ x: 2, y: 5 })
    expect(projectBoardCoordinate(7, 5, 'COMBAT', true)).toEqual({ x: 7, y: 0 })
  })

  it('rejects invalid authoritative coordinates without clamping', () => {
    expect(isValidAuthoritativeCoordinate(-1, 0, 'PLANNING')).toBe(false)
    expect(isValidAuthoritativeCoordinate(9, 0, 'COMBAT')).toBe(false)
    expect(isValidAuthoritativeCoordinate(0, 3, 'PLANNING')).toBe(false)
    expect(isValidAuthoritativeCoordinate(0, 6, 'COMBAT')).toBe(false)
    expect(isValidAuthoritativeCoordinate(1.5, 1, 'COMBAT')).toBe(false)
    expect(projectBoardCoordinate(0, 6, 'COMBAT', false)).toBeNull()
  })
})
