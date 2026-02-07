export const RARITY_COLORS: Record<number, string> = {
    1: '#94a3b8', // Gray/Slate
    2: '#22c55e', // Green
    3: '#3b82f6', // Blue
    4: '#a855f7', // Purple
    5: '#eab308'  // Gold/Yellow
};

export const TEAM_COLORS = {
    FRIENDLY: '#10b981', // Emerald
    OPPONENT: '#ef4444'  // Red
};

export function getRarityColor(cost: number): string {
    return RARITY_COLORS[cost] || RARITY_COLORS[1];
}

export function getTeamColor(isMine: boolean): string {
    return isMine ? TEAM_COLORS.FRIENDLY : TEAM_COLORS.OPPONENT;
}
