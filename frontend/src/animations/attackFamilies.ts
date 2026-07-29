import type { AttackType } from '../data/animationConfig'
import { elementPalette, PALWORLD_ACCENTS } from './palettes'
import type { PalworldAttackAnimationConfig, PalworldElement } from './types'

interface PalworldAttackFamilyEntry {
    key: string
    type: AttackType
    element: PalworldElement
    particles: number
    accentColor?: string
}

const attackEntries: PalworldAttackFamilyEntry[] = [
    { key: 'pw-attack-lamball', type: 'blunt', element: 'neutral', particles: 10 },
    { key: 'pw-attack-cattiva', type: 'claw', element: 'neutral', particles: 10 },
    { key: 'pw-attack-chikipi', type: 'peck', element: 'neutral', particles: 9 },
    { key: 'pw-attack-foxparks', type: 'flameBurst', element: 'fire', particles: 12 },
    { key: 'pw-attack-lifmunk', type: 'sniperShot', element: 'grass', particles: 12 },
    { key: 'pw-attack-pengullet', type: 'aquaJet', element: 'water', particles: 12 },
    { key: 'pw-attack-daedream', type: 'shadowOrb', element: 'dark', particles: 12 },
    { key: 'pw-attack-depresso', type: 'shadowOrb', element: 'dark', particles: 10 },
    { key: 'pw-attack-gumoss', type: 'blunt', element: 'ground', particles: 10 },
    { key: 'pw-attack-vixy', type: 'projectile', element: 'neutral', particles: 10 },
    { key: 'pw-attack-sparkit', type: 'thunderJolt', element: 'electric', particles: 12 },
    { key: 'pw-attack-tanzee', type: 'sniperShot', element: 'grass', particles: 12 },
    { key: 'pw-attack-fuack', type: 'aquaJet', element: 'water', particles: 14 },
    { key: 'pw-attack-tocotoco', type: 'projectile', element: 'neutral', particles: 14, accentColor: PALWORLD_ACCENTS.coral },
    { key: 'pw-attack-direhowl', type: 'bite', element: 'neutral', particles: 12 },
    { key: 'pw-attack-celaray', type: 'aquaJet', element: 'water', particles: 13 },
    { key: 'pw-attack-dumud', type: 'blunt', element: 'ground', particles: 12 },
    { key: 'pw-attack-dazzi', type: 'thunderJolt', element: 'electric', particles: 15 },
    { key: 'pw-attack-flambelle', type: 'flameBurst', element: 'fire', particles: 13 },
    { key: 'pw-attack-mimog', type: 'bite', element: 'neutral', particles: 12 },
    { key: 'pw-attack-cremis', type: 'projectile', element: 'neutral', particles: 11 },
    { key: 'pw-attack-melpaca', type: 'blunt', element: 'neutral', particles: 12 },
    { key: 'pw-attack-galeclaw', type: 'wing', element: 'neutral', particles: 14 },
    { key: 'pw-attack-lovander', type: 'claw', element: 'neutral', particles: 12, accentColor: PALWORLD_ACCENTS.coral },
    { key: 'pw-attack-hoodle', type: 'shadowOrb', element: 'dark', particles: 15 },
    { key: 'pw-attack-chillet', type: 'bite', element: 'ice', particles: 15 },
    { key: 'pw-attack-penking', type: 'blunt', element: 'water', particles: 15 },
    { key: 'pw-attack-katress', type: 'shadowOrb', element: 'dark', particles: 17 },
    { key: 'pw-attack-lunaris', type: 'projectile', element: 'neutral', particles: 16 },
    { key: 'pw-attack-quivern', type: 'dragonSpark', element: 'dragon', particles: 16 },
    { key: 'pw-attack-petallia', type: 'leafCut', element: 'grass', particles: 16 },
    { key: 'pw-attack-mossanda', type: 'sniperShot', element: 'grass', particles: 17 },
    { key: 'pw-attack-grizzbolt', type: 'thunderJolt', element: 'electric', particles: 18 },
    { key: 'pw-attack-tarantriss', type: 'bite', element: 'dark', particles: 17 },
    { key: 'pw-attack-relaxaurus', type: 'dragonSpark', element: 'dragon', particles: 17 },
    { key: 'pw-attack-tetroise', type: 'stoneToss', element: 'ground', particles: 16 },
    { key: 'pw-attack-anubis', type: 'forcePalm', element: 'ground', particles: 19 },
    { key: 'pw-attack-shadowbeak', type: 'shadowOrb', element: 'dark', particles: 20 },
    { key: 'pw-attack-lyleen', type: 'leafCut', element: 'grass', particles: 18 },
    { key: 'pw-attack-orserk', type: 'thunderJolt', element: 'electric', particles: 21 },
    { key: 'pw-attack-selyne', type: 'slash', element: 'neutral', particles: 19 },
    { key: 'pw-attack-jormuntide-ignis', type: 'flameBurst', element: 'fire', particles: 21 },
    { key: 'pw-attack-bellanoir', type: 'shadowOrb', element: 'dark', particles: 21 },
    { key: 'pw-attack-aegidron', type: 'horn', element: 'ground', particles: 19 },
    { key: 'pw-attack-renjishi', type: 'claw', element: 'fire', particles: 20, accentColor: PALWORLD_ACCENTS.coral },
    { key: 'pw-attack-silvance', type: 'leafCut', element: 'grass', particles: 19 },
    { key: 'pw-attack-dandilord', type: 'shadowOrb', element: 'dark', particles: 20, accentColor: '#86EFAC' },
    { key: 'pw-attack-shaolong', type: 'dragonSpark', element: 'dragon', particles: 21 },
    { key: 'pw-attack-jetragon', type: 'dragonSpark', element: 'dragon', particles: 24 },
    { key: 'pw-attack-frostallion', type: 'iceShard', element: 'ice', particles: 23 },
    { key: 'pw-attack-paladius', type: 'spear', element: 'neutral', particles: 22 },
    { key: 'pw-attack-necromus', type: 'spear', element: 'dark', particles: 22 },
    { key: 'pw-attack-neptilius', type: 'spear', element: 'water', particles: 23 },
    { key: 'pw-attack-xenolord', type: 'shadowOrb', element: 'dark', particles: 24 },
    { key: 'pw-attack-panthalus', type: 'bubble', element: 'water', particles: 24 }
]

export const PALWORLD_ATTACK_FAMILIES: Record<string, PalworldAttackFamilyEntry> = Object.fromEntries(
    attackEntries.map((entry) => [entry.key, entry])
)

export const PALWORLD_ATTACK_ANIMATIONS: Record<string, PalworldAttackAnimationConfig> = Object.fromEntries(
    attackEntries.map((entry) => {
        const palette = elementPalette(entry.element)
        return [entry.key, {
            type: entry.type,
            color: palette.primary,
            secondaryColor: palette.secondary,
            particles: entry.particles,
            accentColor: entry.accentColor
        }]
    })
) as Record<string, PalworldAttackAnimationConfig>

export const PALWORLD_ATTACK_ANIMATION_KEYS = Object.freeze(attackEntries.map((entry) => entry.key))
