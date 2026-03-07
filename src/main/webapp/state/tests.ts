import {reactive} from 'vue'
import type {TestDraft, TestEntity} from '@/types/models'
import {apiFetch, apiJson, handleApiError, withApiError} from '@/utils/api'
import {setLastError} from '@/state/app'
import {setAllSelected, withSelection} from '@/state/helpers'

const createEmptyTest = (): TestEntity => ({
  group: '',
  alias: 'New Test',
  plan: '',
  _new: true,
  _selected: null,
})

const state = reactive({
  tests: [] as TestEntity[]
})

const storeTests = (payload: TestEntity[]) => {
  state.tests = withSelection(payload)
}

export const fetchTests = async () => {
  const data = await withApiError(() => apiJson<TestEntity[]>('/__webapi__/tests'))
  if (data) {
    storeTests(data)
  }
}

export const saveTests = async (tests: [TestEntity | Record<string, never>, TestDraft]) => {
  const data = await withApiError(() => apiJson<TestEntity[]>('/__webapi__/tests', {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(tests)
  }))
  if (data) {
    storeTests(data)
  }
}

export const saveAllTests = async (payload: { overwrite: boolean, tests: TestEntity[] }) => {
  const data = await withApiError(() => apiJson<TestEntity[]>('/__webapi__/tests', {
    method: payload.overwrite ? 'PUT' : 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload.tests)
  }))
  if (data) {
    storeTests(data)
  }
}

export const deleteTests = async (tests: TestEntity[]) => {
  const data = await withApiError(() => apiJson<TestEntity[]>('/__webapi__/tests', {
    method: 'DELETE',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(tests)
  }))
  if (data) {
    storeTests(data)
  }
}

export const executeTest = async (alias: string) => {
  return withApiError(async () => {
    const response = await apiFetch('/__webapi__/tests/execute', {
      method: 'POST',
      headers: { 'Content-Type': 'text/text' },
      body: alias
    })

    if (response.status === 404) {
      setLastError('Test not found')
      return ''
    }
    if (response.status === 202) {
      setLastError('Test is already in progress')
      return ''
    }

    await handleApiError(response)
    return response.text()
  })
}

export const stopTest = async (alias: string) => {
  return withApiError(async () => {
    const response = await apiFetch('/__webapi__/tests/stop', {
      method: 'POST',
      headers: { 'Content-Type': 'text/text' },
      body: alias
    })

    if (response.status === 404) {
      setLastError('Test not found or not run yet')
      return ''
    }

    await handleApiError(response)
    return response.text()
  })
}

export const fetchTestResult = async (alias: string) => {
  return withApiError(async () => {
    const response = await apiFetch(`/__webapi__/tests/${alias}/result`, { method: 'GET' })
    await handleApiError(response)
    return response.text()
  })
}

export const clearTest = async (alias: string) => {
  return withApiError(async () => {
    const response = await apiFetch(`/__webapi__/tests/${alias}/clear`, { method: 'POST' })

    if (response.status === 404) {
      setLastError('Test not found or not run yet')
      return ''
    }
    if (response.status === 202) {
      setLastError('Test is in progress')
      return ''
    }

    await handleApiError(response)
    return response.text()
  })
}

export const addTest = () => {
  state.tests.unshift(createEmptyTest())
}

export const selectTest = (payload: { test: TestEntity, selected: boolean | null }) => {
  for (const test of state.tests) {
    if (test.alias === payload.test.alias) {
      test._selected = payload.selected
    }
  }
}

export const selectAllTests = (selected: boolean | null) => {
  setAllSelected(state.tests, selected)
}

export const useTestsState = () => ({
  state,
  fetchTests,
  saveTests,
  saveAllTests,
  deleteTests,
  executeTest,
  stopTest,
  fetchTestResult,
  clearTest,
  addTest,
  selectTest,
  selectAllTests,
  createEmptyTest,
})

