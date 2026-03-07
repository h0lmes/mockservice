type RuntimeConfigLike = ReturnType<typeof useRuntimeConfig>

const normalizeBase = (value?: string | null) => {
  if (!value) {
    return ''
  }

  return value.replace(/\/+$/, '')
}

const toWebSocketBase = (httpBase: string) => {
  if (httpBase.startsWith('https://')) {
    return `wss://${httpBase.slice('https://'.length)}`
  }

  if (httpBase.startsWith('http://')) {
    return `ws://${httpBase.slice('http://'.length)}`
  }

  return httpBase
}

export const resolveBackendOrigin = (runtimeConfig: RuntimeConfigLike) => {
  const configuredBase = normalizeBase(runtimeConfig.public.apiBase)
  if (configuredBase) {
    return configuredBase
  }

  if (typeof window !== 'undefined') {
    const { protocol, hostname, port, origin } = window.location
    if (port === '3000') {
      return `${protocol}//${hostname}:8081`
    }

    return origin
  }

  return 'http://localhost:8081'
}

export const resolveBackendWebSocketUrl = (runtimeConfig: RuntimeConfigLike) => {
  const configuredBase = normalizeBase(runtimeConfig.public.wsBase)
  if (configuredBase) {
    return `${configuredBase}/__wsapi__`
  }

  return `${toWebSocketBase(resolveBackendOrigin(runtimeConfig))}/__wsapi__`
}
