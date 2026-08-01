import { getAbilityConfig, getAttackConfig } from '../data/animationConfig'
import type { GameMode, GameUnit } from '../types'

export const resolveAttackConfig = (gameMode: GameMode | undefined, unit: Pick<GameUnit, 'definitionId' | 'traits'>) => {
    if (gameMode === 'palworld') {
        return getAttackConfig('palworld', `pw-attack-${unit.definitionId}`, { traits: unit.traits })
    }
    return getAttackConfig(unit.definitionId)
}

export const resolveAbilityConfig = (gameMode: GameMode | undefined, unit: Pick<GameUnit, 'definitionId' | 'traits'>) => {
    if (gameMode === 'palworld') {
        return getAbilityConfig('palworld', `pw-ability-${unit.definitionId}`, { traits: unit.traits })
    }
    return getAbilityConfig(unit.definitionId)
}
