import {frontendAppState, setLastError} from '@/state/app'

const buildUrl = (path: string) => `${frontendAppState.baseUrl}${path}`

export const handleApiError = async (response: Response) => {
  if (response.status === 400) {
    const err = await response.json()
    throw new Error(err.message || JSON.stringify(err))
  }

  if (!response.ok) {
    const err = response.statusText || JSON.stringify(response)
    throw new Error(err)
  }

  return response
}

export const apiFetch = async (path: string, init?: RequestInit) => {
  return fetch(buildUrl(path), init)
}

export const apiJson = async <T>(path: string, init?: RequestInit): Promise<T> => {
  const response = await apiFetch(path, init)
  await handleApiError(response)
  return response.json() as Promise<T>
}

export const apiText = async (path: string, init?: RequestInit): Promise<string> => {
  const response = await apiFetch(path, init)
  await handleApiError(response)
  return response.text()
}

export const withApiError = async <T>(action: () => Promise<T>): Promise<T | undefined> => {
  try {
    return await action()
  } catch (error) {
    setLastError(error)
    return undefined
  }
}
