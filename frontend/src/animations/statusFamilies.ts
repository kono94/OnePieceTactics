export const PALWORLD_STATUS_FAMILIES = {
    burn: { statusId: 'burn', glyph: 'ember', particleLimit: 8 },
    poison: { statusId: 'poison', glyph: 'spore', particleLimit: 6 },
    freeze: { statusId: 'freeze', glyph: 'snow', particleLimit: 0 },
    ivy: { statusId: 'ivy', glyph: 'leaf', particleLimit: 0 },
    muddy: { statusId: 'muddy', glyph: 'rock', particleLimit: 0 },
    soak: { statusId: 'soak', glyph: 'drop', particleLimit: 2 },
    electrified: { statusId: 'electrified', glyph: 'bolt', particleLimit: 0 },
    blind: { statusId: 'blind', glyph: 'void', particleLimit: 0 }
} as const

export type PalworldStatusId = keyof typeof PALWORLD_STATUS_FAMILIES
