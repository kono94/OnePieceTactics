import type { AbilityAnimationConfig } from '../data/animationConfig'
import { PALWORLD_ABILITY_FAMILIES } from './abilityFamilies'
import type { PalworldAbilityEffectStyle } from './types'

export type PalworldEffectGeometry =
  | 'projectile'
  | 'melee'
  | 'line'
  | 'cone'
  | 'dash'
  | 'chain'
  | 'multiShot'
  | 'radius'
  | 'aura'
  | 'heal'
  | 'shield'
  | 'control'
  | 'meteor'
  | 'zone'
  | 'beam'
  | 'whirlwind'
  | 'bubble'
  | 'diagnostic'

export const PALWORLD_EFFECT_GEOMETRY: Record<PalworldAbilityEffectStyle, PalworldEffectGeometry> =
  {
    PAL_DIAGNOSTIC: 'diagnostic',
    PAL_PROJECTILE: 'projectile',
    PAL_MELEE_BURST: 'melee',
    PAL_LINE_CUT: 'line',
    PAL_CONE_BREATH: 'cone',
    PAL_DASH: 'dash',
    PAL_CHAIN: 'chain',
    PAL_MULTI_SHOT: 'multiShot',
    PAL_RADIUS_BURST: 'radius',
    PAL_AURA_BUFF: 'aura',
    PAL_HEAL_BLOOM: 'heal',
    PAL_SHIELD_FIELD: 'shield',
    PAL_CONTROL_FIELD: 'control',
    PAL_METEOR_RAIN: 'meteor',
    PAL_PERSISTENT_ZONE: 'zone',
    PAL_BEAM: 'beam',
    PAL_WHIRLWIND: 'whirlwind',
    PAL_BUBBLE_FIELD: 'bubble',
  }

export interface PalworldEffectPolicy {
  effectStyle: PalworldAbilityEffectStyle
  geometry: PalworldEffectGeometry
  durationScale: number
  projectileCount: number
  ringCount: number
  trailWidth: number
}

type PalworldPolicyConfig = Pick<
  AbilityAnimationConfig,
  'effectStyle' | 'durationScale' | 'projectileCount' | 'ringCount' | 'trailWidth'
>

export function getPalworldEffectPolicy(
  config: PalworldPolicyConfig | undefined,
): PalworldEffectPolicy | null {
  const effectStyle = config?.effectStyle
  if (!effectStyle || !(effectStyle in PALWORLD_EFFECT_GEOMETRY)) return null

  const style = effectStyle as PalworldAbilityEffectStyle
  const family = PALWORLD_ABILITY_FAMILIES[style]
  return {
    effectStyle: style,
    geometry: PALWORLD_EFFECT_GEOMETRY[style],
    durationScale: Math.max(0.2, config.durationScale ?? family.durationScale),
    projectileCount: Math.max(1, Math.round(config.projectileCount ?? family.projectileCount ?? 1)),
    ringCount: Math.max(1, Math.round(config.ringCount ?? family.ringCount ?? 1)),
    trailWidth: Math.max(3, config.trailWidth ?? family.trailWidth ?? 10),
  }
}

export type RenderEffectPriority = 'ability' | 'standard'

export function getRenderEffectPriority(type: string): RenderEffectPriority {
  return type === 'SKILL' || type === 'HEAL' || type === 'SHIELD' ? 'ability' : 'standard'
}

export function chooseEffectEvictionIndex(
  effects: readonly { priority: RenderEffectPriority }[],
  incomingPriority: RenderEffectPriority,
  maxEffects: number,
) {
  if (effects.length < maxEffects) return -1

  const standardIndex = effects.findIndex((effect) => effect.priority === 'standard')
  if (standardIndex >= 0) return standardIndex

  return incomingPriority === 'ability' && effects.length > 0 ? 0 : -1
}
