import {reactive} from 'vue'
import type {RouteDraft, RouteEntity} from '@/types/models'
import {apiJson, withApiError} from '@/utils/api'
import {setAllSelected, withSelection} from '@/state/helpers'

const createEmptyRoute = (): RouteEntity => ({
  group: '',
  type: 'REST',
  method: 'GET',
  path: '/',
  alt: '',
  response: '',
  responseCode: '200',
  requestBodySchema: '',
  disabled: false,
  triggerRequest: false,
  triggerRequestIds: '',
  triggerRequestDelay: '',
  _new: true,
  _selected: null,
})

const state = reactive({
  routes: [] as RouteEntity[]
})

const storeRoutes = (payload: RouteEntity[]) => {
  state.routes = withSelection(payload)
}

export const fetchRoutes = async () => {
  const data = await withApiError(() => apiJson<RouteEntity[]>('/__webapi__/routes'))
  if (data) {
    storeRoutes(data)
  }
}

export const saveRoutes = async (routes: [RouteEntity | Record<string, never>, RouteDraft]) => {
  const data = await withApiError(() => apiJson<RouteEntity[]>('/__webapi__/routes', {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(routes)
  }))
  if (data) {
    storeRoutes(data)
  }
}

export const saveAllRoutes = async (payload: { overwrite: boolean, routes: RouteEntity[] }) => {
  const data = await withApiError(() => apiJson<RouteEntity[]>('/__webapi__/routes', {
    method: payload.overwrite ? 'PUT' : 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload.routes)
  }))
  if (data) {
    storeRoutes(data)
  }
}

export const deleteRoutes = async (routes: RouteEntity[]) => {
  const data = await withApiError(() => apiJson<RouteEntity[]>('/__webapi__/routes', {
    method: 'DELETE',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(routes)
  }))
  if (data) {
    storeRoutes(data)
  }
}

export const addRoute = () => {
  state.routes.unshift(createEmptyRoute())
}

export const selectRoute = (payload: { route: RouteEntity, selected: boolean | null }) => {
  for (const route of state.routes) {
    if (route.method === payload.route.method && route.path === payload.route.path && route.alt === payload.route.alt) {
      route._selected = payload.selected
    }
  }
}

export const selectAllRoutes = (selected: boolean | null) => {
  setAllSelected(state.routes, selected)
}

export const useRoutesState = () => ({
  state,
  fetchRoutes,
  saveRoutes,
  saveAllRoutes,
  deleteRoutes,
  addRoute,
  selectRoute,
  selectAllRoutes,
  createEmptyRoute,
})

