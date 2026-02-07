/**
 * Resolves the icon path for a unit based on its definitionId and the game mode.
 * @param definitionId The unit's definitionId (e.g., 'luffy_v1', 'charmander')
 * @param gameMode The current game mode ('onepiece' or 'pokemon')
 * @returns The relative path to the unit icon
 */
export function getUnitIconPath(definitionId: string, gameMode: string = 'onepiece'): string {
    if (!definitionId) return '/assets/units/onepiece/luffy_v1.png';

    // Use the gameMode to determine the subfolder
    const subfolder = gameMode === 'pokemon' ? 'pokemon' : 'onepiece';
    return `/assets/units/${subfolder}/${definitionId}.png`;
}
