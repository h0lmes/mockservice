import {mount} from '@vue/test-utils'
import {beforeEach, describe, expect, it, vi} from 'vitest'
import type {RouteEntity, ScenarioEntity} from '@/types/models'
import Scenario from '@/components/route/Scenario.vue'

const mocks = vi.hoisted(() => ({
  activateScenario: vi.fn(),
  deactivateScenario: vi.fn(),
  deleteScenarios: vi.fn(),
  saveScenarios: vi.fn(),
  selectScenario: vi.fn(),
  setApiSearchExpression: vi.fn(),
  routesState: {
    state: {
      routes: [] as RouteEntity[],
    },
  },
}))

vi.mock('@/assets/icons/scroll.svg?component', () => ({
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
  useRoutesState: () => mocks.routesState,
}))

vi.mock('@/state/scenarios', () => ({
  activateScenario: mocks.activateScenario,
  deactivateScenario: mocks.deactivateScenario,
  deleteScenarios: mocks.deleteScenarios,
  saveScenarios: mocks.saveScenarios,
  selectScenario: mocks.selectScenario,
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

vi.mock('@/components/route/RoutesToAdd.vue', () => ({
  default: {
    emits: ['add'],
    template: '<button data-test="add-route" @click="$emit(\'add\', null)">add-route</button>',
  },
}))

const scenario: ScenarioEntity = {
  id: 'scenario-1',
  group: 'robot',
  alias: 'robot sequence',
  type: 'MAP',
  data: '',
  active: false,
  _selected: null,
}

describe('Scenario', () => {
  beforeEach(() => {
    mocks.activateScenario.mockReset()
    mocks.deactivateScenario.mockReset()
    mocks.deleteScenarios.mockReset()
    mocks.saveScenarios.mockReset()
    mocks.selectScenario.mockReset()
    mocks.setApiSearchExpression.mockReset()
    mocks.activateScenario.mockResolvedValue(undefined)
    mocks.deactivateScenario.mockResolvedValue(undefined)
    mocks.saveScenarios.mockResolvedValue(undefined)
    mocks.routesState.state.routes = [{
      group: 'robot',
      type: 'REST',
      method: 'GET',
      path: '/api/robot',
      alt: 'R2D2',
      response: '',
      responseCode: '200',
      requestBodySchema: '',
      disabled: false,
      triggerRequest: false,
      triggerRequestIds: '',
      triggerRequestDelay: '',
      _selected: null,
    }]
  })

  it('activates an inactive scenario through the active toggle', async () => {
    const wrapper = mount(Scenario, { props: { scenario } })

    await wrapper.findAll('[data-test="toggle"]').find((button) => button.text() === 'Active')!.trigger('click')

    expect(mocks.activateScenario).toHaveBeenCalledWith('robot sequence')
    expect(mocks.deactivateScenario).not.toHaveBeenCalled()
  })

  it('clears the scenario id when saving as a copy', async () => {
    const wrapper = mount(Scenario, { props: { scenario } })

    await wrapper.get('[data-test="edit"]').trigger('click')
    await wrapper.findAll('button').find((button) => button.text() === 'SAVE AS COPY')!.trigger('click')

    expect(mocks.saveScenarios).toHaveBeenCalledWith([
      {},
      expect.objectContaining({ id: '', alias: 'robot sequence' }),
    ])
  })
})
