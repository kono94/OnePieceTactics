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
        localStorage.clear()
        sessionStorage.clear()
        Object.keys(TRAIT_DATA).forEach((key) => delete TRAIT_DATA[key])
        document.head.innerHTML = '<link rel="icon" href="/favicon.svg"><link rel="icon" href="/duplicate.svg">'
        window.location.hash = ''
    })

    it('ignores unsupported configured modes and applies the supported default metadata', async () => {
        vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
            ok: true,
            json: async () => ({
                defaultGameMode: 'retired-mode',
                availableModes: ['retired-mode', 'onepiece', 'pokemon'],
            }),
        }))

        sessionStorage.setItem('tactics.activeRoom', JSON.stringify({
            roomId: 'mode-room',
            playerName: 'ModeTester',
            reconnectToken: 'token',
        }))
        const wrapper = mount(App)
        await vi.waitFor(() => expect(stomp.activate).toHaveBeenCalled())

        expect(document.title).toBe('Theme Fusion Tactics — One Piece')
        expect(document.querySelectorAll('link[rel="icon"]')).toHaveLength(1)
        expect(document.querySelector('link[rel="icon"]')?.getAttribute('href')).toBe('/favicon.svg')
        wrapper.unmount()
    })

    it('ignores a stale trait response after switching modes', async () => {
        let resolvePokemon!: (response: unknown) => void
        let resolveOnepiece!: (response: unknown) => void
        const pokemonResponse = new Promise((resolve) => { resolvePokemon = resolve })
        const onepieceResponse = new Promise((resolve) => { resolveOnepiece = resolve })

        vi.stubGlobal('fetch', vi.fn((request: string) => {
            if (request === '/api/config') {
                return Promise.resolve({
                    ok: true,
                    json: async () => ({ defaultGameMode: 'onepiece', availableModes: ['onepiece', 'pokemon'] }),
                })
            }
            if (request.includes('mode=pokemon')) return pokemonResponse
            return onepieceResponse
        }))

        sessionStorage.setItem('tactics.activeRoom', JSON.stringify({
            roomId: 'mode-room',
            playerName: 'ModeTester',
            reconnectToken: 'token',
        }))
        const wrapper = mount(App)
        await vi.waitFor(() => expect(stomp.onConnect).toBeDefined())
        stomp.onConnect?.()
        await vi.waitFor(() => expect(stomp.subscriptions).toHaveLength(3))

        const stateSubscription = stomp.subscriptions.find(({ destination }) => destination.endsWith('/mode-room'))
        expect(stateSubscription).toBeDefined()
        stateSubscription?.callback({ body: JSON.stringify(roomState('pokemon')) })
        stateSubscription?.callback({ body: JSON.stringify(roomState('onepiece')) })

        resolveOnepiece({ ok: true, json: async () => traits('One Piece') })
        await vi.waitFor(() => expect(TRAIT_DATA.one_piece?.name).toBe('One Piece'))
        resolvePokemon({ ok: true, json: async () => traits('Pokemon') })
        await Promise.resolve()

        expect(TRAIT_DATA.one_piece?.name).toBe('One Piece')
        expect(TRAIT_DATA.pokemon).toBeUndefined()
        wrapper.unmount()
    })

    it('returns to the lobby when a room state contains an unsupported mode', async () => {
        const fetchMock = vi.fn((request: string) => {
            if (request === '/api/config') {
                return Promise.resolve({
                    ok: true,
                    json: async () => ({ defaultGameMode: 'onepiece', availableModes: ['onepiece', 'pokemon'] }),
                })
            }
            return Promise.resolve({ ok: true, json: async () => traits('Pokemon') })
        })
        vi.stubGlobal('fetch', fetchMock)

        sessionStorage.setItem('tactics.activeRoom', JSON.stringify({
            roomId: 'mode-room',
            playerName: 'ModeTester',
            reconnectToken: 'token',
        }))
        const wrapper = mount(App)
        await vi.waitFor(() => expect(stomp.onConnect).toBeDefined())
        stomp.onConnect?.()
        await vi.waitFor(() => expect(stomp.subscriptions).toHaveLength(3))

        const stateSubscription = stomp.subscriptions.find(({ destination }) => destination.endsWith('/mode-room'))
        expect(stateSubscription).toBeDefined()
        stateSubscription?.callback({ body: JSON.stringify(roomState('pokemon')) })
        stateSubscription?.callback({
            body: JSON.stringify({ ...roomState('pokemon'), gameMode: 'retired-mode' }),
        })

        await vi.waitFor(() => expect(wrapper.find('.lobby-error').text()).toContain(
            'Unsupported game mode received from server: retired-mode',
        ))
        expect(wrapper.find('.lobby').exists()).toBe(true)
        expect(fetchMock.mock.calls.some(([request]) => String(request).includes('mode=retired-mode'))).toBe(false)
        expect(sessionStorage.getItem('tactics.activeRoom')).toBeNull()
        wrapper.unmount()
    })

    it('persists the server-acknowledged player id for room identity', async () => {
        vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
            ok: true,
            json: async () => ({
                defaultGameMode: 'onepiece',
                availableModes: ['onepiece', 'pokemon'],
            }),
        }))
        sessionStorage.setItem('tactics.activeRoom', JSON.stringify({
            roomId: 'mode-room',
            playerName: 'DuplicateName',
            reconnectToken: 'token',
        }))
        const wrapper = mount(App)
        await vi.waitFor(() => expect(stomp.onConnect).toBeDefined())

        stomp.onConnect?.()
        await vi.waitFor(() => expect(stomp.subscriptions).toHaveLength(3))
        const resultSubscription = stomp.subscriptions.find(({ destination }) => destination === '/user/queue/room-result')
        resultSubscription?.callback({
            body: JSON.stringify({
                accepted: true,
                roomId: 'mode-room',
                playerId: 'server-player-id',
                code: null,
                message: null,
            }),
        })

        await vi.waitFor(() => {
            const session = JSON.parse(sessionStorage.getItem('tactics.activeRoom') || '{}')
            expect(session.playerId).toBe('server-player-id')
        })
        wrapper.unmount()
    })

    it.each([
        { inputIndex: 0, destination: '/app/create' },
        { inputIndex: 1, destination: '/app/join' },
    ])('normalizes room ids before publishing to $destination', async ({ inputIndex, destination }) => {
        vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
            ok: true,
            json: async () => ({ defaultGameMode: 'onepiece', availableModes: ['onepiece', 'pokemon'] }),
        }))
        const wrapper = mount(App)
        await vi.waitFor(() => expect(stomp.onConnect).toBeDefined())

        stomp.onConnect?.()
        await vi.waitFor(() => expect(wrapper.findAll('.card input')).toHaveLength(2))
        await wrapper.findAll('.card input')[inputIndex].setValue('  canonical-room  ')
        await wrapper.findAll('.card button')[inputIndex].trigger('click')

        expect(stomp.subscriptions.map(({ destination: subscribedTo }) => subscribedTo)).toEqual(
            expect.arrayContaining([
                '/topic/room/canonical-room',
                '/topic/room/canonical-room/event',
            ])
        )
        const publishedRequest = stomp.publish.mock.calls.find(([request]) => request.destination === destination)?.[0]
        expect(publishedRequest).toBeDefined()
        expect(JSON.parse(publishedRequest.body).roomId).toBe('canonical-room')
        expect(JSON.parse(sessionStorage.getItem('tactics.activeRoom') || '{}').roomId).toBe('canonical-room')
        wrapper.unmount()
    })
})
