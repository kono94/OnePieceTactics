import type {
    AdminSession,
    AnalyticsFilters,
    AnalyticsRunDetail,
    AnalyticsRunFilters,
    AnalyticsRunsPage,
    AnalyticsSummary,
} from '../types/analytics'

const SESSION_KEY = 'tactics.analytics.adminSession'

export class AnalyticsApiError extends Error {
    constructor(
        public readonly status: number,
        message: string,
    ) {
        super(message)
    }
}

const readErrorMessage = async (response: Response) => {
    try {
        const body = (await response.json()) as { message?: string; error?: string }
        return body.message || body.error || `Request failed (${response.status})`
    } catch {
        return `Request failed (${response.status})`
    }
}

const request = async <T>(url: string, init: RequestInit = {}, token?: string): Promise<T> => {
    const headers = new Headers(init.headers)
    headers.set('Accept', 'application/json')
    if (init.body) headers.set('Content-Type', 'application/json')
    if (token) headers.set('Authorization', `Bearer ${token}`)

    const response = await fetch(url, { ...init, headers, cache: 'no-store' })
    if (!response.ok) throw new AnalyticsApiError(response.status, await readErrorMessage(response))
    if (response.status === 204) return undefined as T
    return (await response.json()) as T
}

const queryString = (values: object) => {
    const params = new URLSearchParams()
    Object.entries(values).forEach(([key, value]) => {
        if (value !== undefined && value !== '') params.set(key, String(value))
    })
    return params.toString()
}

export const login = (password: string, signal?: AbortSignal) =>
    request<AdminSession>('/api/admin/auth/login', {
        method: 'POST',
        body: JSON.stringify({ password }),
        signal,
    })

export const logout = (token: string) =>
    request<void>('/api/admin/auth/logout', { method: 'POST' }, token)

export const getSummary = (token: string, filters: AnalyticsFilters, signal?: AbortSignal) =>
    request<AnalyticsSummary>(`/api/admin/analytics/summary?${queryString(filters)}`, { signal }, token)

export const getRuns = (token: string, filters: AnalyticsRunFilters, signal?: AbortSignal) =>
    request<AnalyticsRunsPage>(`/api/admin/analytics/runs?${queryString(filters)}`, { signal }, token)

export const getRun = (token: string, runId: string, signal?: AbortSignal) =>
    request<AnalyticsRunDetail>(
        `/api/admin/analytics/runs/${encodeURIComponent(runId)}`,
        { signal },
        token,
    )

export const loadAdminSession = (): AdminSession | null => {
    const raw = sessionStorage.getItem(SESSION_KEY)
    if (!raw) return null
    try {
        const session = JSON.parse(raw) as AdminSession
        if (!session.accessToken || new Date(session.expiresAt).getTime() <= Date.now()) {
            sessionStorage.removeItem(SESSION_KEY)
            return null
        }
        return session
    } catch {
        sessionStorage.removeItem(SESSION_KEY)
        return null
    }
}

export const saveAdminSession = (session: AdminSession) =>
    sessionStorage.setItem(SESSION_KEY, JSON.stringify(session))

export const clearAdminSession = () => sessionStorage.removeItem(SESSION_KEY)
