import {reactive} from 'vue'
import type {ScenarioDraft, ScenarioEntity} from '@/types/models'
import {apiFetch, apiJson, handleApiError, withApiError} from '@/utils/api'
import {setAllSelected, withSelection} from '@/state/helpers'

const createEmptyScenario = (): ScenarioEntity => ({
  id: '',
  group: '',
  alias: 'New Scenario',
  type: 'MAP',
  data: '',
  active: false,
  _new: true,
  _selected: null,
})

const state = reactive({
  scenarios: [] as ScenarioEntity[]
})

const storeScenarios = (payload: ScenarioEntity[]) => {
  state.scenarios = withSelection(payload)
}

export const fetchScenarios = async () => {
  const data = await withApiError(() => apiJson<ScenarioEntity[]>('/__webapi__/scenarios'))
  if (data) {
    storeScenarios(data)
  }
}

export const saveScenarios = async (scenarios: [ScenarioEntity | Record<string, never>, ScenarioDraft]) => {
  const data = await withApiError(() => apiJson<ScenarioEntity[]>('/__webapi__/scenarios', {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(scenarios)
  }))
  if (data) {
    storeScenarios(data)
  }
}

export const deleteScenarios = async (scenarios: ScenarioEntity[]) => {
  const data = await withApiError(() => apiJson<ScenarioEntity[]>('/__webapi__/scenarios', {
    method: 'DELETE',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(scenarios)
  }))
  if (data) {
    storeScenarios(data)
  }
}

export const addScenario = () => {
  state.scenarios.unshift(createEmptyScenario())
}

export const activateScenario = async (alias: string) => {
  const result = await withApiError(async () => {
    const response = await apiFetch('/__webapi__/scenarios/active', { method: 'PUT', body: alias })
    await handleApiError(response)
    return response.json() as Promise<string[]>
  })

  if (result?.includes(alias)) {
    for (const scenario of state.scenarios) {
      if (scenario.alias === alias) {
        scenario.active = true
      }
    }
  }
}

export const deactivateScenario = async (alias: string) => {
  const result = await withApiError(async () => {
    const response = await apiFetch('/__webapi__/scenarios/active', { method: 'DELETE', body: alias })
    await handleApiError(response)
    return response.json() as Promise<string[]>
  })

  if (result && !result.includes(alias)) {
    for (const scenario of state.scenarios) {
      if (scenario.alias === alias) {
        scenario.active = false
      }
    }
  }
}

export const selectScenario = (payload: { scenario: ScenarioEntity, selected: boolean | null }) => {
  for (const scenario of state.scenarios) {
    if (scenario.alias === payload.scenario.alias) {
      scenario._selected = payload.selected
    }
  }
}

export const selectAllScenarios = (selected: boolean | null) => {
  setAllSelected(state.scenarios, selected)
}

export const useScenariosState = () => ({
  state,
  fetchScenarios,
  saveScenarios,
  deleteScenarios,
  addScenario,
  activateScenario,
  deactivateScenario,
  selectScenario,
  selectAllScenarios,
  createEmptyScenario,
})

