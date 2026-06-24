import pokemonUnits from '../../../backend/src/main/resources/data/units_pokemon.json'
import type { AbilityDefinition } from '../types'
import type { UltimateGalleryUnit } from './ultimateGalleryRoster'

interface RawPokemonForm {
    starLevel: number
    definitionId: string
    name: string
    ability?: AbilityDefinition | null
}

interface RawPokemonUnit {
    id: string
    name: string
    cost: number
    ability?: AbilityDefinition | null
    forms?: RawPokemonForm[]
}

function toGalleryUnit(unit: RawPokemonUnit, id: string, name: string, ability: AbilityDefinition | null | undefined): UltimateGalleryUnit {
    return {
        id,
        name,
        cost: unit.cost,
        abilityType: ability?.type ?? 'DAMAGE',
        pattern: ability?.pattern ?? 'SINGLE',
        abilityName: ability?.name ?? 'Preview'
    }
}

export const POKEMON_ULTIMATE_GALLERY_ROSTER: UltimateGalleryUnit[] = (pokemonUnits as RawPokemonUnit[]).flatMap((unit) => {
    const seen = new Set<string>()
    const entries: UltimateGalleryUnit[] = []

    entries.push(toGalleryUnit(unit, unit.id, unit.name, unit.ability))
    seen.add(`${unit.id}:${unit.ability?.name ?? 'Preview'}`)

    for (const form of unit.forms ?? []) {
        const key = `${form.definitionId}:${form.ability?.name ?? unit.ability?.name ?? 'Preview'}`
        if (seen.has(key)) continue
        entries.push(toGalleryUnit(unit, form.definitionId, form.name, form.ability ?? unit.ability))
        seen.add(key)
    }

    return entries
})
