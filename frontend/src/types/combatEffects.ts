import type { AttackAnimationConfig, AbilityAnimationConfig } from '../data/animationConfig'
import type { CombatEvent, RenderedUnit } from './game'

export type CombatVisualEventType = CombatEvent['type']
export type EffectIntensity = 'low' | 'normal' | 'ultimate'
export type RenderLayer = 'ground' | 'trail' | 'impact' | 'over-unit'

export interface CombatEffectPoint {
    x: number
    y: number
}

export interface NormalizedCombatVisualEvent {
    id: number
    timestamp: number
    type: CombatVisualEventType
    sourceId: string
    targetId: string
    value: number
    skillName?: string
    source: RenderedUnit
    target: RenderedUnit
    start: CombatEffectPoint
    end: CombatEffectPoint
    definitionId: string
    attack?: AttackAnimationConfig
    ability?: AbilityAnimationConfig
    pattern?: string
    starLevel: number
    intensity: EffectIntensity
    batchSize: number
    crowded: boolean
}
