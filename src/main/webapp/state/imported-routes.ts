import {reactive} from 'vue'
import type {RouteEntity} from '@/types/models'
import {apiJson, withApiError} from '@/utils/api'

const state = reactive({
  routes: [] as RouteEntity[]
})

export const importOpenApiRoutes = async (spec: string) => {
  const data = await withApiError(() => apiJson<RouteEntity[]>('/__webapi__/import', {
    method: 'PUT',
    headers: { 'Content-Type': 'text/plain' },
    body: spec
  }))

  if (data) {
    state.routes = data
  }
}

export const useImportedRoutesState = () => ({
  state,
  importOpenApiRoutes,
})
