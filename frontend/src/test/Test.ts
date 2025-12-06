import { mount } from '@vue/test-utils'
import { describe, it, expect, vi } from 'vitest'
import LoginView from '@/views/LoginView.vue'

const pushMock = vi.fn()

// Router mocken (ganz einfach)
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: pushMock
  })
}))

describe('LoginView (Task #43)', () => {

  it('zeigt Titel "Login"', () => {
    const wrapper = mount(LoginView)

    const title = wrapper.find('h1').text()

    expect(title).toBe('Login')
  })

  it('hat ein Eingabefeld für den Namen', () => {
    const wrapper = mount(LoginView)

    const input = wrapper.find('input')

    expect(input.exists()).toBe(true)
    expect(input.attributes('placeholder')).toBe('Dein Name')
  })

  it('ändert den v-model Wert, wenn der Nutzer tippt', async () => {
    const wrapper = mount(LoginView)

    const input = wrapper.find('input')
    await input.setValue('Alice')

    expect((input.element as HTMLInputElement).value).toBe('Alice')
  })

  it('ruft Router.push auf, wenn ein Name eingegeben wurde', async () => {
    const wrapper = mount(LoginView)

    await wrapper.find('input').setValue('Alice')
    await wrapper.find('button').trigger('click')

    expect(pushMock).toHaveBeenCalledWith({
      path: '/main',
      query: { playerName: 'Alice' }
    })
  })



  it('macht KEINE Weiterleitung, wenn Name leer ist', async () => {
    const pushMock = vi.fn()
    const alertMock = vi.spyOn(window, 'alert').mockImplementation(() => {})

    vi.doMock('vue-router', () => ({
      useRouter: () => ({ push: pushMock })
    }))

    const wrapper = mount(LoginView)

    await wrapper.find('button').trigger('click')

    expect(alertMock).toHaveBeenCalled()
    expect(pushMock).not.toHaveBeenCalled()

    alertMock.mockRestore()
  })
})