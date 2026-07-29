import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import Lobby from './Lobby.vue'

describe('Lobby mode theme', () => {
    it('renders the Palworld lobby decoration from the metadata theme class', () => {
        const wrapper = mount(Lobby, {
            props: {
                title: 'Palworld Tactics',
                themeClass: 'theme-palworld',
            },
        })

        expect(wrapper.find('.lobby').classes()).toContain('theme-palworld')
        expect(wrapper.find('.pal-sphere').attributes('aria-hidden')).toBe('true')
        expect(wrapper.find('.subtitle').text()).toBe('Create or join a tactics room')
    })
})
