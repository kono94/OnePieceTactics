import { getGameModeMetadata, isGameMode } from '../data/gameModeMetadata'
import type { GameMode } from '../types'

export const UNIT_ICON_PLACEHOLDER = '/assets/units/placeholder.svg'

export function getUnitIconPath(definitionId: string | null | undefined, gameMode: GameMode | string = 'onepiece'): string {
    if (!definitionId?.trim() || !isGameMode(gameMode)) {
        if (import.meta.env.DEV) {
            console.warn('Missing unit icon identity', { gameMode, definitionId })
        }
        return UNIT_ICON_PLACEHOLDER
    }

    const metadata = getGameModeMetadata(gameMode)
    return `/assets/units/${metadata.unitAssetFolder}/${definitionId}.png`
}
