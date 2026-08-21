import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import Lobby from './Lobby.vue'

describe('Lobby mode theme', () => {
    it('renders the selected lobby theme from the metadata theme class', () => {
        const wrapper = mount(Lobby, {
            props: {
                title: 'Pokemon Tactics',
                themeClass: 'theme-pokemon',
            },
        })

        expect(wrapper.find('.lobby').classes()).toContain('theme-pokemon')
        expect(wrapper.find('img').exists()).toBe(false)
        expect(wrapper.find('.subtitle').text()).toBe('Create or join a tactics room')
    })
})
