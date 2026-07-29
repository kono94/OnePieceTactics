import { mount } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import PhaseAnnouncement from './PhaseAnnouncement.vue'

const emergencyDrop = {
    dropId: 'drop-1',
    playerId: 'player-1',
    round: 7,
    orbIds: ['orb-1', 'orb-2'],
}

describe('PhaseAnnouncement', () => {
    beforeEach(() => {
        vi.useFakeTimers()
        vi.stubGlobal('requestAnimationFrame', (callback: FrameRequestCallback) => {
            return window.setTimeout(() => callback(0), 0)
        })
        vi.stubGlobal('cancelAnimationFrame', (handle: number) => window.clearTimeout(handle))
    })

    afterEach(() => {
        vi.useRealTimers()
        vi.unstubAllGlobals()
    })

    it('suppresses the planning splash while an emergency drop is queued', async () => {
        const wrapper = mount(PhaseAnnouncement, {
            props: {
                phase: 'PLANNING',
                suppressPlanningAnnouncement: true,
            },
        })

        await vi.advanceTimersByTimeAsync(200)

        expect(wrapper.text()).not.toContain('PLANNING PHASE')
        wrapper.unmount()
    })

    it('announces an emergency drop with accessible status text', async () => {
        const wrapper = mount(PhaseAnnouncement, {
            props: {
                phase: 'PLANNING',
                emergencyDrop,
                suppressPlanningAnnouncement: true,
            },
        })

        await vi.advanceTimersByTimeAsync(1)

        const announcement = wrapper.get('[role="status"]')
        expect(announcement.attributes('aria-live')).toBe('assertive')
        expect(announcement.text()).toContain('EMERGENCY DROP')
        expect(announcement.text()).toContain('2 bonus orbs deployed')
        expect(announcement.text()).not.toContain('PLANNING PHASE')
        wrapper.unmount()
    })
})
