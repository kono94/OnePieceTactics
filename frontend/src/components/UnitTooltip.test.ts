import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import UnitTooltip from './UnitTooltip.vue'
import type { UnitDefinition } from '../types'

function unitDefinition(overrides: Partial<UnitDefinition> = {}): UnitDefinition {
    return {
        id: 'caterpie',
        lineId: 'caterpie',
        name: 'Caterpie',
        cost: 1,
        role: 'SUPPORT',
        maxHealth: 500,
        maxMana: 80,
        attackDamage: 30,
        abilityPower: 0,
        defense: 20,
        attackSpeed: 0.6,
        range: 3,
        traits: ['Bug'],
        ability: null,
        ...overrides
    }
}

describe('UnitTooltip', () => {
    it('renders the current role and defense stat', () => {
        const wrapper = mount(UnitTooltip, {
            props: {
                unit: unitDefinition()
            }
        })

        expect(wrapper.get('.role-badge').text()).toBe('Support')
        expect(wrapper.get('.role-badge').classes()).toContain('role-support')
        expect(wrapper.text()).toContain('DEF:')
        expect(wrapper.text()).toContain('20')
    })

    it('renders role progression for evolving shop units', () => {
        const wrapper = mount(UnitTooltip, {
            props: {
                unit: unitDefinition({
                    forms: [
                        { starLevel: 1, definitionId: 'caterpie', name: 'Caterpie', role: 'SUPPORT' },
                        { starLevel: 2, definitionId: 'metapod', name: 'Metapod', role: 'TANK' },
                        { starLevel: 3, definitionId: 'butterfree', name: 'Butterfree', role: 'SUPPORT' }
                    ]
                })
            }
        })

        expect(wrapper.get('.role-progression').text()).toContain('1★ Support')
        expect(wrapper.get('.role-progression').text()).toContain('2★ Tank')
        expect(wrapper.get('.role-progression').text()).toContain('3★ Support')
    })
})
