import {beforeEach, describe, expect, it, vi} from 'vitest'
import {frontendAppState} from '@/state/app'
import {executeTest, useTestsState} from '@/state/tests'

describe('tests state', () => {
  beforeEach(() => {
    frontendAppState.baseUrl = 'http://localhost:8081'
    frontendAppState.lastError = ''
    useTestsState().state.tests = []
    vi.restoreAllMocks()
  })

  it('sets a friendly error when executeTest gets 404', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      status: 404,
      ok: false,
      text: vi.fn(),
    }))

    const result = await executeTest('missing-test')

    expect(result).toBe('')
    expect(frontendAppState.lastError).toBe('Test not found')
  })

  it('returns server text for successful executeTest responses', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      status: 200,
      ok: true,
      text: vi.fn().mockResolvedValue('started'),
    }))

    const result = await executeTest('sample-test')

    expect(result).toBe('started')
    expect(frontendAppState.lastError).toBe('')
  })
})
