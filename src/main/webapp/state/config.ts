import {apiText, withApiError} from '@/utils/api'

export const fetchConfig = async () => withApiError(() => apiText('/__webapi__/config'))

export const saveConfig = async (config: string) => withApiError(() => apiText('/__webapi__/config', {
  method: 'PUT',
  headers: { 'Content-Type': 'text/plain' },
  body: config
}))

export const backupConfig = async () => withApiError(() => apiText('/__webapi__/config/backup'))

export const restoreConfig = async () => withApiError(() => apiText('/__webapi__/config/restore'))
