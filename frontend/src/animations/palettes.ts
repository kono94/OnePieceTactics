import type { PalworldElement } from './types'
import type { PalworldAnimationContext } from './types'

export interface ElementPalette {
    primary: string
    secondary: string
    glyph: 'leaf' | 'ember' | 'drop' | 'bolt' | 'snow' | 'rock' | 'void' | 'dragon' | 'star'
}

export const PALWORLD_ELEMENT_PALETTES: Record<PalworldElement, ElementPalette> = {
    neutral: { primary: '#A8A29E', secondary: '#FAFAF9', glyph: 'star' },
    fire: { primary: '#F97316', secondary: '#FDE68A', glyph: 'ember' },
    water: { primary: '#2563EB', secondary: '#67E8F9', glyph: 'drop' },
    electric: { primary: '#FACC15', secondary: '#FFF7AE', glyph: 'bolt' },
    grass: { primary: '#22C55E', secondary: '#BBF7D0', glyph: 'leaf' },
    ice: { primary: '#67E8F9', secondary: '#F0F9FF', glyph: 'snow' },
    ground: { primary: '#A16207', secondary: '#FDE68A', glyph: 'rock' },
    dark: { primary: '#6D28D9', secondary: '#C4B5FD', glyph: 'void' },
    dragon: { primary: '#6366F1', secondary: '#C7D2FE', glyph: 'dragon' }
}

export const PALWORLD_ACCENTS = {
    coral: '#FF7F6E',
    ink: '#173443',
    gold: '#D9A441'
} as const

export function normalizePalworldElement(value: unknown): PalworldElement {
    const normalized = String(value ?? 'neutral').toLowerCase() as PalworldElement
    return normalized in PALWORLD_ELEMENT_PALETTES ? normalized : 'neutral'
}

export function resolvePalworldElement(context: PalworldAnimationContext = {}): PalworldElement {
    if (context.element) return normalizePalworldElement(context.element)

    const traitElement = context.traits?.find((trait) => trait.toLowerCase() in PALWORLD_ELEMENT_PALETTES)
    return normalizePalworldElement(traitElement)
}

export function elementPalette(value: unknown) {
    return PALWORLD_ELEMENT_PALETTES[normalizePalworldElement(value)]
}
