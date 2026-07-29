import type { AbilityAnimationConfig, AttackAnimationConfig } from '../data/animationConfig'

export interface AnimationRenderOptions {
    crowded?: boolean
    reducedMotion?: boolean
}

export interface AnimationRenderPolicy {
    particleScale: number
    screenShake: number
    durationScale: number
    particleCap: number
}

export function getAnimationRenderPolicy(
    config: Pick<AbilityAnimationConfig, 'particleScale' | 'screenShake' | 'durationScale'>,
    options: AnimationRenderOptions = {}
): AnimationRenderPolicy {
    const crowdedScale = options.crowded ? 0.7 : 1
    const reducedScale = options.reducedMotion ? 0.18 : 1
    const particleScale = (config.particleScale ?? 1) * crowdedScale * reducedScale
    const screenShake = options.reducedMotion
        ? 0
        : Math.min(10, (config.screenShake ?? 0) * (options.crowded ? 0.5 : 1))

    return {
        particleScale,
        screenShake,
        durationScale: (config.durationScale ?? 1) * (options.reducedMotion ? 0.35 : 1),
        particleCap: options.reducedMotion ? 24 : options.crowded ? 90 : 180
    }
}

export function getAttackParticleBudget(
    config: Pick<AttackAnimationConfig, 'particles'>,
    options: AnimationRenderOptions = {}
) {
    const scale = options.reducedMotion ? 0.18 : options.crowded ? 0.42 : 1
    return Math.min(options.reducedMotion ? 4 : options.crowded ? 10 : 24, Math.round((config.particles ?? 0) * scale))
}

export function prefersReducedMotion() {
    return typeof window !== 'undefined' && window.matchMedia?.('(prefers-reduced-motion: reduce)').matches === true
}
