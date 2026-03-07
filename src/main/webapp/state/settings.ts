import {reactive} from 'vue'
import type {ServiceSettings} from '@/types/models'
import {apiFetch, apiJson, handleApiError, withApiError} from '@/utils/api'

const state = reactive({
  settings: {} as ServiceSettings
})

export const fetchSettings = async () => {
  const data = await withApiError(() => apiJson<ServiceSettings>('/__webapi__/settings'))
  if (data) {
    state.settings = data
  }
}

export const saveSettings = async (settings: ServiceSettings) => {
  const data = await withApiError(() => apiJson<ServiceSettings>('/__webapi__/settings', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ ...state.settings, ...settings })
  }))
  if (data) {
    state.settings = data
  }
}

export const setCertificatePassword = async (password: string) => {
  return withApiError(async () => {
    const response = await apiFetch('/__webapi__/settings/certificatePassword', {
      method: 'POST',
      body: password
    })
    await handleApiError(response)
    await response.text()
    return true
  })
}

export const useSettingsState = () => ({
  state,
  fetchSettings,
  saveSettings,
  setCertificatePassword,
})
