import { ABILITY_ANIMATIONS, ATTACK_ANIMATIONS } from '../../data/animationConfig'
import type { AbilityAnimationConfig, AttackAnimationConfig } from '../../data/animationConfig'

export function getOnePieceAttackConfig(key: string): AttackAnimationConfig | undefined {
    return key === '_default' ? undefined : ATTACK_ANIMATIONS[key]
}

export function getOnePieceAbilityConfig(key: string): AbilityAnimationConfig | undefined {
    return key === '_default' ? undefined : ABILITY_ANIMATIONS[key]
}
