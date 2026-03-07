import {beforeEach, describe, expect, it} from 'vitest'
import {mount} from '@vue/test-utils'
import ErrorPanel from '@/components/other/ErrorPanel.vue'
import {frontendAppState} from '@/state/app'

describe('ErrorPanel', () => {
  beforeEach(() => {
    frontendAppState.lastError = ''
  })

  it('renders frontend app errors and dismisses them on click', async () => {
    frontendAppState.lastError = 'Boom'

    const wrapper = mount(ErrorPanel)

    expect(wrapper.text()).toContain('Boom')

    await wrapper.trigger('click')

    expect(frontendAppState.lastError).toBe('')
  })
})
