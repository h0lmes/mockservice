import {reactive} from 'vue'
import {apiFetch, handleApiError, withApiError} from '@/utils/api'

const state = reactive({
  value: ''
})

export const generateJson = async (schema: string) => {
  const data = await withApiError(async () => {
    const response = schema
      ? await apiFetch('/__webapi__/generate/json', { method: 'POST', body: schema })
      : await apiFetch('/__webapi__/generate/json')
    await handleApiError(response)
    return response.text()
  })

  if (data !== undefined) {
    state.value = data
  }
}

export const useGenerateState = () => ({ state, generateJson })
