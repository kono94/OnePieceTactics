import { getAbilityConfig, getAttackConfig } from '../data/animationConfig'
import type { GameUnit } from '../types'

export const resolveAttackConfig = (unit: Pick<GameUnit, 'definitionId' | 'traits'>) => {
    return getAttackConfig(unit.definitionId)
}

export const resolveAbilityConfig = (unit: Pick<GameUnit, 'definitionId' | 'traits'>) => {
    return getAbilityConfig(unit.definitionId)
}
