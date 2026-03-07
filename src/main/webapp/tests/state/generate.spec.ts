import {beforeEach, describe, expect, it, vi} from 'vitest'
import {frontendAppState} from '@/state/app'
import {generateJson, useGenerateState} from '@/state/generate'

describe('generate state', () => {
  beforeEach(() => {
    frontendAppState.baseUrl = 'http://localhost:8081'
    frontendAppState.lastError = ''
    useGenerateState().state.value = ''
    vi.restoreAllMocks()
  })

  it('stores generated json text from the API', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: true,
      text: vi.fn().mockResolvedValue('{"hello":true}')
    }))

    await generateJson('')

    expect(useGenerateState().state.value).toBe('{"hello":true}')
  })
})
