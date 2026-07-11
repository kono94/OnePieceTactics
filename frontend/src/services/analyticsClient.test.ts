import { beforeEach, describe, expect, it, vi } from 'vitest'
import { clearAdminSession, getRuns, loadAdminSession, login, saveAdminSession } from './analyticsClient'

describe('analytics client', () => {
    beforeEach(() => {
        vi.restoreAllMocks()
        sessionStorage.clear()
        vi.useRealTimers()
    })

    it('sends the password only in the login request body', async () => {
        const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue(
            new Response(JSON.stringify({ accessToken: 'token', expiresAt: '2099-01-01T00:00:00Z' }), {
                status: 200,
                headers: { 'Content-Type': 'application/json' },
            }),
        )
        await login('tft123')
        expect(fetchMock).toHaveBeenCalledWith(
            '/api/admin/auth/login',
            expect.objectContaining({ body: JSON.stringify({ password: 'tft123' }) }),
        )
        const headers = new Headers(fetchMock.mock.calls[0][1]?.headers)
        expect(headers.has('Authorization')).toBe(false)
    })

    it('keeps only a valid bearer session in session storage', () => {
        saveAdminSession({ accessToken: 'token', expiresAt: '2099-01-01T00:00:00Z' })
        expect(loadAdminSession()?.accessToken).toBe('token')
        clearAdminSession()
        expect(loadAdminSession()).toBeNull()
    })

    it('discards expired sessions', () => {
        saveAdminSession({ accessToken: 'expired', expiresAt: '2000-01-01T00:00:00Z' })
        expect(loadAdminSession()).toBeNull()
    })

    it('serializes the anonymous player filter expected by the backend', async () => {
        const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue(
            new Response(JSON.stringify({ items: [], nextCursor: null }), {
                status: 200,
                headers: { 'Content-Type': 'application/json' },
            }),
        )

        await getRuns('token', {
            from: '2026-07-01T00:00:00.000Z',
            to: '2026-07-11T00:00:00.000Z',
            analyticsClientId: 'browser-123',
        })

        expect(fetchMock.mock.calls[0][0]).toContain('analyticsClientId=browser-123')
    })
})
