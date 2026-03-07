import type {RequestEntity, RouteEntity, ScenarioEntity, SelectableEntity, TestEntity} from '@/types/models'

export type ApiEntity = RouteEntity | RequestEntity | ScenarioEntity | TestEntity

interface ErrorResponse {
  message?: string
}

export const handleError = async (response: Response) => {
  if (response.status === 400) {
    const error = await response.json() as ErrorResponse
    throw new Error(error.message || JSON.stringify(error))
  }
  if (!response.ok) {
    const error = response.statusText || JSON.stringify({ status: response.status })
    throw new Error(error)
  }
  return response
}

export const addSelectedProperty = <T extends SelectableEntity>(data: T[] | null | undefined) => {
  if (Array.isArray(data)) {
    for (let index = 0; index < data.length; index += 1) {
      data[index]._selected = null
    }
  }
}

export const selectAll = <T extends SelectableEntity>(data: T[] | null | undefined, selected: boolean | null) => {
  if (Array.isArray(data)) {
    for (let index = 0; index < data.length; index += 1) {
      data[index]._selected = selected
    }
  }
}

const hasOwn = <K extends PropertyKey>(entity: unknown, key: K): entity is Record<K, unknown> =>
  typeof entity === 'object' && entity !== null && Object.prototype.hasOwnProperty.call(entity, key)

export const _isRoute = (entity: unknown): entity is RouteEntity => hasOwn(entity, 'alt')
export const _isRequest = (entity: unknown): entity is RequestEntity => hasOwn(entity, 'id')
export const _isScenario = (entity: unknown): entity is ScenarioEntity => hasOwn(entity, 'data')
export const _isTest = (entity: unknown): entity is TestEntity => hasOwn(entity, 'plan')
