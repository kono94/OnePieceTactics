import { getAbilityConfig as getLegacyAbilityConfig, getAttackConfig as getLegacyAttackConfig } from '../../data/animationConfig'
import type { AbilityAnimationConfig, AttackAnimationConfig } from '../../data/animationConfig'

export function getPokemonAttackConfig(key: string): AttackAnimationConfig | undefined {
    return getLegacyAttackConfig(key)
}

export function getPokemonAbilityConfig(key: string): AbilityAnimationConfig | undefined {
    return getLegacyAbilityConfig(key)
}
