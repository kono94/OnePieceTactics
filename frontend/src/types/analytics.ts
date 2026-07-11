export type AnalyticsMatchStatus = 'STARTED' | 'COMPLETED' | 'INTERRUPTED'
export type AnalyticsRunStatus = AnalyticsMatchStatus
export type AnalyticsRoundOutcome = 'WIN' | 'LOSS' | 'DRAW' | null

export interface AnalyticsBotRoundOutcome {
    round: number
    wins: number
    losses: number
    draws: number
}

export interface AnalyticsSummary {
    gamesStarted: number
    gamesCompleted: number
    gamesInterrupted: number
    humanRuns: number
    abandonmentCount: number
    abandonmentRate: number
    averageFinalRound: number
    maxFinalRound: number
    modeCounts: Record<string, number>
    placementDistribution: Record<string, number>
    outcomeDistribution: Record<string, number>
    botRoundOutcomes: AnalyticsBotRoundOutcome[]
}

export interface AnalyticsRunSummary {
    runId: string
    matchId: string
    anonymousPlayerId: string
    mode: string
    startedAt: string
    status: AnalyticsRunStatus
    abandonedAt: string | null
    finalPlacement: number | null
    finalRound: number | null
    finalHealth: number | null
    wins: number
    losses: number
    draws: number
}

export interface AnalyticsBoardUnit {
    definitionId: string
    lineId: string
    starLevel: number
}

export interface AnalyticsAugment {
    id: string
    tier: string
}

export interface AnalyticsRound {
    round: number
    capturedAt: string
    resolvedAt: string | null
    healthBefore: number
    healthAfter: number | null
    gold: number
    level: number
    xp: number
    outcome: AnalyticsRoundOutcome
    opponentType: string | null
    board: AnalyticsBoardUnit[]
    augments: AnalyticsAugment[]
}

export interface AnalyticsRunDetail {
    run: AnalyticsRunSummary
    rounds: AnalyticsRound[]
}

export interface AnalyticsRunsPage {
    items: AnalyticsRunSummary[]
    nextCursor: string | null
}

export interface AnalyticsFilters {
    from: string
    to: string
    mode?: string
}

export interface AnalyticsRunFilters extends AnalyticsFilters {
    analyticsClientId?: string
    abandoned?: boolean
    cursor?: string
    size?: number
}

export interface AdminSession {
    accessToken: string
    expiresAt: string
}
