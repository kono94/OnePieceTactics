import { POKEMON_ULTIMATE_GALLERY_ROSTER } from '../data/pokemonUltimateGalleryRoster'
import { PALWORLD_ULTIMATE_GALLERY_ROSTER } from '../data/palworldUltimateGalleryRoster'
import { ULTIMATE_GALLERY_ROSTER, type GalleryGameMode, type UltimateGalleryUnit } from '../data/ultimateGalleryRoster'

export const GALLERY_ROSTERS: Record<GalleryGameMode, UltimateGalleryUnit[]> = {
    onepiece: ULTIMATE_GALLERY_ROSTER,
    pokemon: POKEMON_ULTIMATE_GALLERY_ROSTER,
    palworld: PALWORLD_ULTIMATE_GALLERY_ROSTER
}

export function getGalleryRoster(mode: GalleryGameMode) {
    return GALLERY_ROSTERS[mode]
}

export function getGalleryAbilityRoster(mode: GalleryGameMode) {
    return getGalleryRoster(mode)
}

export function getGalleryAttackRoster(mode: GalleryGameMode) {
    const seen = new Set<string>()
    return getGalleryRoster(mode).filter((unit) => {
        const key = unit.attackAnimationKey ?? unit.id
        if (seen.has(key)) return false
        seen.add(key)
        return true
    })
}
