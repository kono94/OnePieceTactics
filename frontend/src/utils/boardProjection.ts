export const BOARD_COLUMNS = 9
export const PLANNING_ROWS = 3
export const COMBAT_ROWS = 6

export type BoardProjectionPhase = 'PLANNING' | 'COMBAT'

export interface BoardProjection {
  x: number
  y: number
}

export function isBenchCoordinate(x: number, y: number): boolean {
  return Number.isInteger(x) && x >= 0 && x < BOARD_COLUMNS && y === -1
}

export function isValidAuthoritativeCoordinate(
  x: number,
  y: number,
  phase: BoardProjectionPhase,
): boolean {
  const rowCount = phase === 'COMBAT' ? COMBAT_ROWS : PLANNING_ROWS
  return (
    Number.isInteger(x) &&
    Number.isInteger(y) &&
    x >= 0 &&
    x < BOARD_COLUMNS &&
    y >= 0 &&
    y < rowCount
  )
}

export function projectBoardCoordinate(
  x: number,
  y: number,
  phase: BoardProjectionPhase,
  shouldFlip: boolean,
): BoardProjection | null {
  if (!isValidAuthoritativeCoordinate(x, y, phase)) return null

  if (phase === 'PLANNING') {
    return { x, y: y + PLANNING_ROWS }
  }

  return { x, y: shouldFlip ? COMBAT_ROWS - 1 - y : y }
}
