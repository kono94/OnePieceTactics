import { beforeEach, describe, expect, it, vi } from 'vitest'
import {
    clearActiveRoomSession,
    createActiveRoomSession,
    getAnalyticsClientId,
    loadActiveRoomSession,
} from './clientIdentity'

describe('client identity', () => {
    beforeEach(() => {
        localStorage.clear()
        sessionStorage.clear()
        vi.spyOn(crypto, 'randomUUID')
            .mockReturnValueOnce('10000000-0000-4000-8000-000000000001')
            .mockReturnValueOnce('10000000-0000-4000-8000-000000000002')
    })

    it('keeps the anonymous analytics id across visits', () => {
        expect(getAnalyticsClientId()).toBe('10000000-0000-4000-8000-000000000001')
        expect(getAnalyticsClientId()).toBe('10000000-0000-4000-8000-000000000001')
    })

    it('stores reconnect credentials only for the active tab', () => {
        expect(createActiveRoomSession('room-1', 'Player_1')).toEqual({
            roomId: 'room-1',
            playerName: 'Player_1',
            reconnectToken: '10000000-0000-4000-8000-000000000001',
        })
        expect(loadActiveRoomSession()?.roomId).toBe('room-1')
        clearActiveRoomSession()
        expect(loadActiveRoomSession()).toBeNull()
    })
})
