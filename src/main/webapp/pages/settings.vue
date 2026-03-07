<template>
    <div class="monospace">
        <p class="mb-4">
            <ToggleSwitch v-model="randomAlt" sub="Return random of existing Alt (alternatives) for requested Route">Random Alt</ToggleSwitch>
        </p>
        <p class="mb-4">
            <ToggleSwitch v-model="quantum" sub="Routes are now quantum objects, so don't expect anything deterministic">Go Quantum</ToggleSwitch>
        </p>
        <p class="mb-2">
            <ToggleSwitch v-model="alt400OnFailedRequestValidation" sub="When request body validation fails respond with an existing Alt = '400' route instead of standard error.
                          Variable ${requestBodyValidationErrorMessage} would be available to use in response body.">Alt '400' on failed request validation</ToggleSwitch>
        </p>
        <p class="mb-2">
            <ToggleSwitch v-model="useContextInRouteResponse" sub="When enabled variables from Context can be used in Route response body like ${var_name}">Use Context in Route response</ToggleSwitch>
        </p>
        <div class="component px-4 py-3 mt-6">
            <div>Proxying</div>

            <p class="mb-3 mt-3">
                <ToggleSwitch v-model="proxyEnabled" sub="When no Route is found for an incoming request then proxy request to the
                              specified location. Useful when your application-in-development is partially done.">Proxy not found Route</ToggleSwitch>
            </p>

            <div>Proxy location</div>
            <input v-model="proxyLocation" type="text" class="form-control form-control-sm mt-3" placeholder="E.g. http://other.server:80"/>
        </div>

        <div class="component px-4 py-3 mt-6">
            <div>SSL certificate</div>
            <div class="color-secondary nowrap mt-3">{{ certificateHumanReadable }}</div>
            <input ref="certFile" type="file" class="hidden">
            <button type="button" class="btn btn-default mt-3" @click="selectCertFile">Select local file</button>
            <button type="button" class="btn btn-default mt-3" @click="resetCertFile">Forget certificate</button>
            <div class="color-secondary mt-4">
                If [No certificate] all remote certificates are considered trusted.
            </div>
            <div class="color-secondary mt-3">
                To use a certificate:<br>
                - select a PKCS #12 certificate file<br>
                - save settings<br>
                - set a password; password input will show once you have certificate saved.
            </div>

            <div v-show="hasCertificateOnServer" class="mt-4">
                <div>SSL certificate password</div>
                <input v-model="password" type="text" class="form-control form-control-sm mt-3"/>
                <button type="button" class="btn btn-default mt-3" @click="setPass">
                    Set password
                    <span v-show="passwordSet">&#9989;</span>
                </button>
                <div class="color-secondary mt-3">
                    Password is not stored in Config (so, no need to save settings after you set a password).
                    Once you set it a new SSL context is created until service is restarted.
                </div>
            </div>
        </div>

        <div class="mt-6 pl-1">
            <button type="button" class="btn btn-primary" @click="save">
                <span class="mx-5">Save settings</span>
            </button>
        </div>

        <Loading v-if="pageLoading"></Loading>
    </div>
</template>

<script setup lang="ts">
import {computed, onMounted, ref, watch} from 'vue'
import type {ServiceSettings} from '@/types/models'
import {usePageLoader, useWorkingAction} from '@/composables/useAsyncState'
import Loading from '../components/other/Loading'
import ToggleSwitch from '../components/other/ToggleSwitch'
import {fetchSettings, saveSettings, setCertificatePassword, useSettingsState} from '@/state/settings'

const { pageLoading, runWhilePageLoading } = usePageLoader()
const { runWhileWorking } = useWorkingAction()
const certFile = ref<HTMLInputElement | null>(null)
const randomAlt = ref(false)
const quantum = ref(false)
const alt400OnFailedRequestValidation = ref(false)
const proxyEnabled = ref(false)
const proxyLocation = ref('')
const certificate = ref<string | null>(null)
const password = ref('')
const passwordSet = ref(false)
const useContextInRouteResponse = ref(false)
const settings = computed<ServiceSettings>(() => useSettingsState().state.settings)
const hasCertificateOnServer = computed(() => settings.value.certificate !== null && settings.value.certificate !== '')
const certificateHumanReadable = computed(() => certificate.value == null ? 'No certificate' : 'Certificate: ' + certificate.value)

watch(settings, () => {
  randomAlt.value = !!settings.value.randomAlt
  quantum.value = !!settings.value.quantum
  alt400OnFailedRequestValidation.value = !!settings.value.alt400OnFailedRequestValidation
  proxyEnabled.value = !!settings.value.proxyEnabled
  proxyLocation.value = settings.value.proxyLocation || ''
  certificate.value = settings.value.certificate ?? null
  useContextInRouteResponse.value = !!settings.value.useContextInRouteResponse
}, { deep: true, immediate: true })

const loadPage = async () => runWhilePageLoading(async () => {
  await fetchSettings()
})

const save = async () => {
  await runWhileWorking(() => saveSettings({
    randomAlt: randomAlt.value,
    quantum: quantum.value,
    alt400OnFailedRequestValidation: alt400OnFailedRequestValidation.value,
    proxyEnabled: proxyEnabled.value,
    proxyLocation: proxyLocation.value,
    certificate: certificate.value,
    useContextInRouteResponse: useContextInRouteResponse.value,
  }))
}

const selectCertFile = () => {
  const el = certFile.value
  if (!el) return

  el.onchange = () => {
    certificate.value = null
    passwordSet.value = false
    try {
      const file = el.files?.[0]
      if (!file) {
        return
      }
      const reader = new FileReader()
      reader.readAsDataURL(file)
      reader.onload = () => {
        certificate.value = String(reader.result)
          .replace('data:', '')
          .replace(/^.+,/, '')
      }
      reader.onerror = (error) => console.log('Error: ', error)
    } catch (error) {
      console.error(error)
    }
  }
  el.click()
}

const resetCertFile = () => {
  certificate.value = null
  passwordSet.value = false
}

const setPass = async () => {
  await runWhileWorking(async () => {
    const result = await setCertificatePassword(password.value)
    password.value = ''
    passwordSet.value = !!result
  })
}

onMounted(loadPage)
</script>

<style scoped>
</style>
