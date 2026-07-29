import { mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import type { GameState } from './types'
import { TRAIT_DATA } from './data/traitData'

const stomp = vi.hoisted(() => ({
    activate: vi.fn(),
    deactivate: vi.fn(),
    publish: vi.fn(),
    onConnect: undefined as (() => void) | undefined,
    subscriptions: [] as Array<{ destination: string; callback: (message: { body: string }) => void }>,
}))

vi.mock('@stomp/stompjs', () => ({
    Client: vi.fn(function MockClient(options: { onConnect?: () => void }) {
        stomp.onConnect = options.onConnect
        return {
            activate: stomp.activate,
            deactivate: stomp.deactivate,
            publish: stomp.publish,
            subscribe: vi.fn((destination: string, callback: (message: { body: string }) => void) => {
                stomp.subscriptions.push({ destination, callback })
                return { unsubscribe: vi.fn() }
            }),
        }
    }),
}))

vi.mock('./components/GameInterface.vue', () => ({
    default: { template: '<div data-test="game-interface" />' },
}))

import App from './App.vue'

function roomState(gameMode: GameState['gameMode']): GameState {
    return {
        roomId: 'mode-room',
        hostId: 'host',
        phase: 'LOBBY',
        round: 1,
        timeRemainingMs: 0,
        totalPhaseDuration: 0,
        players: {},
        matchups: {},
        recentEvents: [],
        damageLog: {},
        gameMode,
        planningTimerPaused: false,
        planningReadyPlayerId: null,
        planningPauseReason: null,
    }
}

const traits = (name: string) => [{
    id: name.toLowerCase(),
    name,
    description: `${name} trait`,
    effects: [],
    type: 'type',
    iconColor: '#5bc9e8',
}]

describe('App game-mode bootstrap', () => {
    beforeEach(() => {
        vi.restoreAllMocks()
        stomp.activate.mockClear()
        stomp.deactivate.mockClear()
        stomp.publish.mockClear()
        stomp.onConnect = undefined
        stomp.subscriptions.length = 0
        Object.keys(TRAIT_DATA).forEach((key) => delete TRAIT_DATA[key])
        document.head.innerHTML = '<link rel="icon" href="/favicon.svg"><link rel="icon" href="/duplicate.svg">'
        window.location.hash = ''
    })

    it('applies the configured Palworld title and favicon without duplicate icon links', async () => {
        vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
            ok: true,
            json: async () => ({
                defaultGameMode: 'palworld',
                availableModes: ['palworld', 'onepiece', 'pokemon'],
            }),
        }))

        sessionStorage.setItem('tactics.activeRoom', JSON.stringify({
            roomId: 'mode-room',
            playerName: 'ModeTester',
            reconnectToken: 'token',
        }))
        const wrapper = mount(App)
        await vi.waitFor(() => expect(stomp.activate).toHaveBeenCalled())

        expect(document.title).toBe('Theme Fusion Tactics — Palworld')
        expect(document.querySelectorAll('link[rel="icon"]')).toHaveLength(1)
        expect(document.querySelector('link[rel="icon"]')?.getAttribute('href')).toBe('/pal-sphere.png')
        wrapper.unmount()
    })

    it('ignores a stale trait response after switching modes', async () => {
        let resolvePokemon!: (response: unknown) => void
        let resolvePalworld!: (response: unknown) => void
        const pokemonResponse = new Promise((resolve) => { resolvePokemon = resolve })
        const palworldResponse = new Promise((resolve) => { resolvePalworld = resolve })

        vi.stubGlobal('fetch', vi.fn((request: string) => {
            if (request === '/api/config') {
                return Promise.resolve({
                    ok: true,
                    json: async () => ({ defaultGameMode: 'onepiece', availableModes: ['onepiece', 'pokemon', 'palworld'] }),
                })
            }
            if (request.includes('mode=pokemon')) return pokemonResponse
            return palworldResponse
        }))

        sessionStorage.setItem('tactics.activeRoom', JSON.stringify({
            roomId: 'mode-room',
            playerName: 'ModeTester',
            reconnectToken: 'token',
        }))
        const wrapper = mount(App)
        await vi.waitFor(() => expect(stomp.onConnect).toBeDefined())
        stomp.onConnect?.()
        await vi.waitFor(() => expect(stomp.subscriptions).toHaveLength(2))

        const stateSubscription = stomp.subscriptions.find(({ destination }) => destination.endsWith('/mode-room'))
        expect(stateSubscription).toBeDefined()
        stateSubscription?.callback({ body: JSON.stringify(roomState('pokemon')) })
        stateSubscription?.callback({ body: JSON.stringify(roomState('palworld')) })

        resolvePalworld({ ok: true, json: async () => traits('Palworld') })
        await vi.waitFor(() => expect(TRAIT_DATA.palworld?.name).toBe('Palworld'))
        resolvePokemon({ ok: true, json: async () => traits('Pokemon') })
        await Promise.resolve()

        expect(TRAIT_DATA.palworld?.name).toBe('Palworld')
        expect(TRAIT_DATA.pokemon).toBeUndefined()
        wrapper.unmount()
    })
})
