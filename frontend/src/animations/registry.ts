import type { AbilityAnimationConfig, AttackAnimationConfig } from '../data/animationConfig'
import { PALWORLD_ABILITY_ANIMATIONS, PALWORLD_ABILITY_ANIMATION_KEYS } from './abilityFamilies'
import { PALWORLD_ATTACK_ANIMATIONS, PALWORLD_ATTACK_ANIMATION_KEYS } from './attackFamilies'
import { getOnePieceAbilityConfig, getOnePieceAttackConfig } from './modes/onepiece'
import { getPokemonAbilityConfig, getPokemonAttackConfig } from './modes/pokemon'
import { elementPalette, resolvePalworldElement } from './palettes'
import type { AnimationDiagnostics, AnimationGameMode, PalworldAnimationContext } from './types'

export { PALWORLD_ABILITY_ANIMATIONS, PALWORLD_ABILITY_ANIMATION_KEYS } from './abilityFamilies'
export { PALWORLD_ATTACK_ANIMATIONS, PALWORLD_ATTACK_ANIMATION_KEYS } from './attackFamilies'

const DIAGNOSTIC_COLOR = '#FF00FF'
const NEUTRAL_COLOR = '#A8A29E'
const NEUTRAL_SECONDARY = '#FAFAF9'

const missingDiagnostics: AnimationDiagnostics[] = []

function isDevelopment() {
    return Boolean(import.meta.env?.DEV)
}

function reportMissing(kind: AnimationDiagnostics['kind'], gameMode: AnimationGameMode, key: string) {
    const message = `Missing ${gameMode} ${kind} animation config for key "${key}"`
    const diagnostic: AnimationDiagnostics = { kind, gameMode, key, message }
    missingDiagnostics.push(diagnostic)
    if (isDevelopment()) console.error(`[animation-registry] ${message}`)
    return diagnostic
}

function missingAttack(gameMode: AnimationGameMode, key: string): AttackAnimationConfig {
    reportMissing('attack', gameMode, key)
    return {
        type: 'projectile',
        color: isDevelopment() ? DIAGNOSTIC_COLOR : NEUTRAL_COLOR,
        secondaryColor: isDevelopment() ? '#FFFFFF' : NEUTRAL_SECONDARY,
        particles: 0,
        diagnostic: true
    }
}

function missingAbility(gameMode: AnimationGameMode, key: string): AbilityAnimationConfig {
    reportMissing('ability', gameMode, key)
    return {
        effectStyle: isDevelopment() ? 'PAL_DIAGNOSTIC' : 'DEFAULT',
        signature: isDevelopment() ? `Missing config: ${key}` : 'Ability',
        color: isDevelopment() ? DIAGNOSTIC_COLOR : NEUTRAL_COLOR,
        secondaryColor: isDevelopment() ? '#FFFFFF' : NEUTRAL_SECONDARY,
        screenShake: 0,
        particleScale: 0,
        diagnostic: true
    }
}

function recolorPalworldAttack(config: AttackAnimationConfig, context?: PalworldAnimationContext) {
    if (!context) return config
    const palette = elementPalette(resolvePalworldElement(context))
    return { ...config, color: palette.primary, secondaryColor: palette.secondary }
}

function recolorPalworldAbility(config: AbilityAnimationConfig, context?: PalworldAnimationContext) {
    if (!context) return config
    const palette = elementPalette(resolvePalworldElement(context))
    return { ...config, color: palette.primary, secondaryColor: palette.secondary, glyph: palette.glyph }
}

export function getAttackConfig(gameMode: AnimationGameMode, attackAnimationKey: string, context?: PalworldAnimationContext): AttackAnimationConfig {
    if (gameMode === 'palworld') {
        const config = PALWORLD_ATTACK_ANIMATIONS[attackAnimationKey]
        return config ? recolorPalworldAttack(config, context) : missingAttack(gameMode, attackAnimationKey)
    }

    const config = gameMode === 'pokemon'
        ? getPokemonAttackConfig(attackAnimationKey)
        : getOnePieceAttackConfig(attackAnimationKey)
    return config ?? missingAttack(gameMode, attackAnimationKey)
}

export function getAbilityConfig(gameMode: AnimationGameMode, abilityAnimationKey: string, context?: PalworldAnimationContext): AbilityAnimationConfig {
    if (gameMode === 'palworld') {
        const config = PALWORLD_ABILITY_ANIMATIONS[abilityAnimationKey]
        return config ? recolorPalworldAbility(config, context) : missingAbility(gameMode, abilityAnimationKey)
    }

    const config = gameMode === 'pokemon'
        ? getPokemonAbilityConfig(abilityAnimationKey)
        : getOnePieceAbilityConfig(abilityAnimationKey)
    return config ?? missingAbility(gameMode, abilityAnimationKey)
}

export function getMissingAnimationDiagnostics() {
    return [...missingDiagnostics]
}

export function clearMissingAnimationDiagnostics() {
    missingDiagnostics.length = 0
}

export function getAnimationRegistryCounts() {
    return {
        palworldAttacks: PALWORLD_ATTACK_ANIMATION_KEYS.length,
        palworldAbilities: PALWORLD_ABILITY_ANIMATION_KEYS.length
    }
}

export function isPalworldAttackAnimationKey(key: string) {
    return key in PALWORLD_ATTACK_ANIMATIONS
}

export function isPalworldAbilityAnimationKey(key: string) {
    return key in PALWORLD_ABILITY_ANIMATIONS
}
