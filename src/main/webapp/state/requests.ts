import {reactive} from 'vue'
import type {RequestDraft, RequestEntity} from '@/types/models'
import {apiJson, apiText, withApiError} from '@/utils/api'
import {setAllSelected, withSelection} from '@/state/helpers'

const createEmptyRequest = (): RequestEntity => ({
  id: '',
  group: '',
  type: 'REST',
  method: 'GET',
  path: '/',
  headers: '',
  body: '',
  responseToVars: false,
  disabled: false,
  triggerRequest: false,
  triggerRequestIds: '',
  triggerRequestDelay: '',
  _new: true,
  _selected: null,
})

const state = reactive({
  requests: [] as RequestEntity[]
})

const storeRequests = (payload: RequestEntity[]) => {
  state.requests = withSelection(payload)
}

export const fetchRequests = async () => {
  const data = await withApiError(() => apiJson<RequestEntity[]>('/__webapi__/requests'))
  if (data) {
    storeRequests(data)
  }
}

export const saveRequests = async (requests: [RequestEntity | Record<string, never>, RequestDraft]) => {
  const data = await withApiError(() => apiJson<RequestEntity[]>('/__webapi__/requests', {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(requests)
  }))
  if (data) {
    storeRequests(data)
  }
}

export const saveAllRequests = async (payload: { overwrite: boolean, requests: RequestEntity[] }) => {
  const data = await withApiError(() => apiJson<RequestEntity[]>('/__webapi__/requests', {
    method: payload.overwrite ? 'PUT' : 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload.requests)
  }))
  if (data) {
    storeRequests(data)
  }
}

export const deleteRequests = async (requests: RequestEntity[]) => {
  const data = await withApiError(() => apiJson<RequestEntity[]>('/__webapi__/requests', {
    method: 'DELETE',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(requests)
  }))
  if (data) {
    storeRequests(data)
  }
}

export const executeRequest = async (requestId: string) => {
  return withApiError(() => apiText('/__webapi__/requests/execute', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ requestId })
  }))
}

export const addRequest = () => {
  state.requests.unshift(createEmptyRequest())
}

export const selectRequest = (payload: { request: RequestEntity, selected: boolean | null }) => {
  for (const request of state.requests) {
    if (request.id === payload.request.id) {
      request._selected = payload.selected
    }
  }
}

export const selectAllRequests = (selected: boolean | null) => {
  setAllSelected(state.requests, selected)
}

export const useRequestsState = () => ({
  state,
  fetchRequests,
  saveRequests,
  saveAllRequests,
  deleteRequests,
  executeRequest,
  addRequest,
  selectRequest,
  selectAllRequests,
  createEmptyRequest,
})

