import {resolveBackendOrigin, resolveBackendWebSocketUrl} from '@/utils/backend'
import {initializeFrontendRuntime} from '@/state/app'

export default defineNuxtPlugin(() => {
  const runtimeConfig = useRuntimeConfig()
  initializeFrontendRuntime({
    baseUrl: resolveBackendOrigin(runtimeConfig),
    wsUrl: resolveBackendWebSocketUrl(runtimeConfig)
  })
})
