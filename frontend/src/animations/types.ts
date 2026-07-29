import type {
    AbilityAnimationConfig,
    AbilityEffectStyle,
    AttackAnimationConfig,
    AttackType
} from '../data/animationConfig'
import type { GameMode } from '../types'

export type AnimationGameMode = GameMode | 'palworld'

export type PalworldElement =
    | 'neutral'
    | 'fire'
    | 'water'
    | 'electric'
    | 'grass'
    | 'ice'
    | 'ground'
    | 'dark'
    | 'dragon'

export type PalworldAbilityEffectStyle = Extract<
    AbilityEffectStyle,
    `PAL_${string}`
>

export interface PalworldAnimationContext {
    traits?: readonly string[]
    element?: string
}

export type PalworldAttackAnimationConfig = AttackAnimationConfig

export interface PalworldAbilityAnimationConfig extends AbilityAnimationConfig {
    effectStyle: PalworldAbilityEffectStyle
    signature: string
    color: string
    secondaryColor: string
    screenShake: number
    particleScale: number
    glyph: NonNullable<AbilityAnimationConfig['glyph']>
}

export interface AttackFamilyDefinition {
    type: AttackType
    particles: number
}

export interface AbilityFamilyDefinition {
    effectStyle: PalworldAbilityEffectStyle
    durationScale: number
    projectileCount?: number
    ringCount?: number
    trailWidth?: number
}

export interface AnimationDiagnostics {
    kind: 'attack' | 'ability'
    gameMode: AnimationGameMode
    key: string
    message: string
}
