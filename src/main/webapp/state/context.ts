import {apiText, withApiError} from '@/utils/api'

export const fetchGlobalContext = async () => withApiError(() => apiText('/__webapi__/context'))

export const saveGlobalContext = async (data: string) => withApiError(() => apiText('/__webapi__/context', {
  method: 'POST',
  headers: { 'Content-Type': 'text/plain' },
  body: data || ''
}))

export const fetchInitialContext = async () => withApiError(() => apiText('/__webapi__/context/initial'))

export const saveInitialContext = async (data: string) => withApiError(() => apiText('/__webapi__/context/initial', {
  method: 'POST',
  headers: { 'Content-Type': 'text/plain' },
  body: data || ''
}))
