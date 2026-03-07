import {beforeEach, describe, expect, it, vi} from 'vitest'
import {frontendAppState} from '@/state/app'
import {fetchSettings, saveSettings, setCertificatePassword, useSettingsState} from '@/state/settings'

describe('settings state', () => {
  beforeEach(() => {
    frontendAppState.baseUrl = 'http://localhost:8081'
    frontendAppState.lastError = ''
    useSettingsState().state.settings = {}
    vi.restoreAllMocks()
  })

  it('fetches and stores service settings', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: true,
      json: vi.fn().mockResolvedValue({ quantum: true })
    }))

    await fetchSettings()

    expect(useSettingsState().state.settings.quantum).toBe(true)
  })

  it('returns true when certificate password is accepted', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: true,
      text: vi.fn().mockResolvedValue('ok')
    }))

    const result = await setCertificatePassword('secret')

    expect(result).toBe(true)
  })

  it('merges saved settings with existing state', async () => {
    useSettingsState().state.settings = { proxyEnabled: true }
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: true,
      json: vi.fn().mockResolvedValue({ proxyEnabled: true, proxyLocation: 'http://x' })
    }))

    await saveSettings({ proxyLocation: 'http://x' })

    expect(useSettingsState().state.settings.proxyEnabled).toBe(true)
    expect(useSettingsState().state.settings.proxyLocation).toBe('http://x')
  })
})
