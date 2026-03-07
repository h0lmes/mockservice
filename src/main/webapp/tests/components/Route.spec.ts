import {mount} from '@vue/test-utils'
import {beforeEach, describe, expect, it, vi} from 'vitest'
import type {RouteEntity} from '@/types/models'
import Route from '@/components/route/Route.vue'

const mocks = vi.hoisted(() => ({
  saveRoutes: vi.fn(),
  deleteRoutes: vi.fn(),
  selectRoute: vi.fn(),
  setApiSearchExpression: vi.fn(),
}))

vi.mock('@/assets/icons/star.svg?component', () => ({
  default: { template: '<svg />' },
}))

vi.mock('@/composables/useAsyncState', () => ({
  useWorkingAction: () => ({
    runWhileWorking: async <T>(action: () => Promise<T> | T) => await action(),
  }),
}))

vi.mock('@/state/app', () => ({
  setApiSearchExpression: mocks.setApiSearchExpression,
}))

vi.mock('@/state/routes', () => ({
  deleteRoutes: mocks.deleteRoutes,
  saveRoutes: mocks.saveRoutes,
  selectRoute: mocks.selectRoute,
}))

vi.mock('@/components/other/ButtonDelete.vue', () => ({
  default: { emits: ['click'], template: '<button data-test="delete" @click="$emit(\'click\')">delete</button>' },
}))

vi.mock('@/components/other/ButtonEdit.vue', () => ({
  default: { emits: ['click'], template: '<button data-test="edit" @click="$emit(\'click\')">edit</button>' },
}))

vi.mock('@/components/other/ButtonExecute.vue', () => ({
  default: { emits: ['click'], template: '<button data-test="execute" @click="$emit(\'click\')">execute</button>' },
}))

vi.mock('@/components/other/ToggleSwitch.vue', () => ({
  default: {
    props: ['modelValue', 'value'],
    emits: ['toggle', 'update:modelValue'],
    template: '<button data-test="toggle" @click="$emit(\'update:modelValue\', !(modelValue ?? value ?? false)); $emit(\'toggle\', !(modelValue ?? value ?? false))"><slot /></button>',
  },
}))

vi.mock('@/components/other/AutoSizeTextArea.vue', () => ({
  default: {
    props: ['modelValue'],
    emits: ['update:modelValue'],
    methods: { focus() {} },
    template: '<textarea :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
  },
}))

vi.mock('@/components/route/RouteTester.vue', () => ({
  default: { template: '<div data-test="route-tester" />' },
}))

vi.mock('@/components/route/RouteMethod.vue', () => ({
  default: { template: '<span>GET</span>' },
}))

const route: RouteEntity = {
  group: 'pets',
  type: 'REST',
  method: 'GET',
  path: '/pets',
  alt: '',
  response: '{"ok":true}',
  responseCode: '200',
  requestBodySchema: '',
  disabled: false,
  triggerRequest: false,
  triggerRequestIds: '',
  triggerRequestDelay: '',
  _selected: null,
}

describe('Route', () => {
  beforeEach(() => {
    mocks.saveRoutes.mockReset()
    mocks.deleteRoutes.mockReset()
    mocks.selectRoute.mockReset()
    mocks.setApiSearchExpression.mockReset()
    mocks.saveRoutes.mockResolvedValue(undefined)
    mocks.deleteRoutes.mockResolvedValue(undefined)
    vi.stubGlobal('confirm', vi.fn(() => true))
  })

  it('saves edited route data', async () => {
    const wrapper = mount(Route, { props: { route } })

    await wrapper.get('[data-test="edit"]').trigger('click')
    await wrapper.get('input[placeholder="/api/v1/etc"]').setValue('/pets/featured')
    await wrapper.get('button.btn-primary').trigger('click')

    expect(mocks.saveRoutes).toHaveBeenCalledWith([
      route,
      expect.objectContaining({ path: '/pets/featured' }),
    ])
  })

  it('deletes a persisted route after confirmation', async () => {
    const wrapper = mount(Route, { props: { route } })

    await wrapper.get('[data-test="delete"]').trigger('click')

    expect(confirm).toHaveBeenCalledWith('Sure you want to delete?')
    expect(mocks.deleteRoutes).toHaveBeenCalledWith([route])
  })
})
