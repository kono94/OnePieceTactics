import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import GameInterface from './GameInterface.vue'
import type { GamePhase, GameState, PlayerState } from '../types'

function player(health: number, place: number): PlayerState {
    return {
        playerId: 'player-1',
        name: 'Player',
        health,
        gold: 10,
        level: 1,
        xp: 0,
        nextLevelXp: 2,
        place,
        combatSide: null,
        bench: [],
        board: [],
        activeTraits: [],
        shop: [],
        lootOrbs: [],
        augmentChoices: [],
        selectedAugments: [],
        isGhost: false,
    }
}

function gameState(phase: GamePhase, health: number, place: number): GameState {
    return {
        roomId: 'room-1',
        hostId: 'player-1',
        phase,
        round: 1,
        timeRemainingMs: 6000,
        totalPhaseDuration: 6000,
        players: { 'player-1': player(health, place) },
        matchups: {},
        recentEvents: [],
        damageLog: {},
        gameMode: 'onepiece',
        planningTimerPaused: false,
        planningReadyPlayerId: null,
        planningPauseReason: null,
    }
}

const childStubs = {
    GameCanvas: true,
    TraitSidebar: true,
    PlayerList: true,
    AugmentSelectionOverlay: true,
    PhaseAnnouncement: true,
    UnitTooltip: true,
}

describe('GameInterface end celebration', () => {
    it.each([
        ['losing', 0, 2],
        ['winning', 100, 1],
    ])('renders the end screen immediately when END_CELEBRATION starts for a %s player', async (_outcome, health, place) => {
        const wrapper = mount(GameInterface, {
            props: {
                state: gameState('COMBAT', health, place),
                currentPlayerName: 'Player',
            },
            global: { stubs: childStubs },
        })

        expect(wrapper.find('.end-screen').exists()).toBe(false)

        await wrapper.setProps({ state: gameState('END_CELEBRATION', health, place) })

        expect(wrapper.find('.end-screen').exists()).toBe(true)
        expect(wrapper.text()).toContain(`${place === 1 ? '1st' : '2nd'} Place`)

        wrapper.unmount()
    })

    it('does not show the end screen for an eliminated player while the match continues', () => {
        const wrapper = mount(GameInterface, {
            props: {
                state: gameState('COMBAT', 0, 2),
                currentPlayerName: 'Player',
            },
            global: { stubs: childStubs },
        })

        expect(wrapper.find('.end-screen').exists()).toBe(false)

        wrapper.unmount()
    })
})
