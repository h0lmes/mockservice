import {reactive} from 'vue'

export interface FrontendAppState {
  baseUrl: string
  wsUrl: string
  working: boolean
  lastError: string
  apiSearchExpression: string
  kafkaSearchExpression: string
}

export const frontendAppState = reactive<FrontendAppState>({
  baseUrl: '',
  wsUrl: '',
  working: false,
  lastError: '',
  apiSearchExpression: '',
  kafkaSearchExpression: '',
})

export const initializeFrontendRuntime = (payload: Pick<FrontendAppState, 'baseUrl' | 'wsUrl'>) => {
  frontendAppState.baseUrl = payload.baseUrl
  frontendAppState.wsUrl = payload.wsUrl
}

export const setWorking = (value: boolean) => {
  frontendAppState.working = value
}

export const setLastError = (value: unknown) => {
  frontendAppState.lastError = value == null ? '' : String(value)
  if (value) {
    console.log('Error: ', value)
  }
}

export const resetLastError = () => {
  frontendAppState.lastError = ''
}

export const setApiSearchExpression = (value: unknown) => {
  frontendAppState.apiSearchExpression = value == null ? '' : String(value).trim()
}

export const setKafkaSearchExpression = (value: unknown) => {
  frontendAppState.kafkaSearchExpression = value == null ? '' : String(value).trim()
}

export const useFrontendApp = () => ({
  state: frontendAppState,
  initializeFrontendRuntime,
  setWorking,
  setLastError,
  resetLastError,
  setApiSearchExpression,
  setKafkaSearchExpression,
})
