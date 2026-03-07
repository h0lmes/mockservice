<template>
    <div class="monospace">
        <input id="file_file" ref="file" type="file" @change="openFile"/>
        <div class="component-toolbar mb-4">
            <div v-show="fileName">{{ fileName }}</div>
            <button type="button" class="btn btn-primary" @click="selectFile">Select Open API file</button>
            <button type="button" class="btn btn-default" @click="addAll">Add all as routes</button>
            <ToggleSwitch v-model="overwrite">Overwrite existing routes</ToggleSwitch>
            <ToggleSwitch v-model="no400500">Ignore 4xx and 5xx</ToggleSwitch>
        </div>
        <ImportedRoutes :imported-routes="importedRoutes" :existing-routes="existingRoutes" @add="add($event)"></ImportedRoutes>
        <div class="color-secondary mt-4 smaller">(click route to expand)</div>
        <Loading v-if="pageLoading"></Loading>
    </div>
</template>

<script setup lang="ts">
import {computed, onMounted, ref} from 'vue'
import type {RouteEntity} from '@/types/models'
import {usePageLoader, useWorkingAction} from '@/composables/useAsyncState'
import ImportedRoutes from '../components/other/ImportedRoutes'
import Loading from '../components/other/Loading'
import ToggleSwitch from '../components/other/ToggleSwitch'
import {importOpenApiRoutes, useImportedRoutesState} from '@/state/imported-routes'
import {fetchRoutes, saveAllRoutes, useRoutesState} from '@/state/routes'

const { pageLoading, runWhilePageLoading } = usePageLoader()
const { runWhileWorking } = useWorkingAction()
const file = ref<HTMLInputElement | null>(null)
const value = ref('')
const overwrite = ref(false)
const no400500 = ref(false)
const fileName = ref('')
const importedRoutes = computed(() => {
  const routes = useImportedRoutesState().state.routes
  if (no400500.value) return routes.filter((route) => !route.alt || Number(route.alt) < 400)
  return routes
})
const existingRoutes = computed(() => useRoutesState().state.routes)

const loadPage = async () => {
  if (!value.value) {
    return
  }
  await runWhilePageLoading(async () => {
    await importOpenApiRoutes(value.value)
  })
}

const add = async (route: RouteEntity) => {
  await runWhileWorking(() => saveAllRoutes({ routes: [route], overwrite: true }))
}

const addAll = async () => {
  if (importedRoutes.value.length === 0) {
    return
  }
  await runWhileWorking(() => saveAllRoutes({ routes: importedRoutes.value, overwrite: overwrite.value }))
}

const selectFile = () => {
  fileName.value = 'Loading...'
  const fileInput = file.value
  if (!fileInput) {
    fileName.value = '[Error]'
    return
  }
  fileInput.value = ''
  fileInput.click()
}

const openFile = () => {
  const files = file.value?.files
  if (!files || !files[0]) {
    fileName.value = '[Error]'
    return
  }

  const reader = new FileReader()
  reader.readAsText(files[0], 'UTF-8')
  reader.onload = async (event) => {
    value.value = String(event.target?.result ?? '')
    await loadPage()
    fileName.value = files[0].name
  }
  reader.onerror = () => {
    fileName.value = '[Error]'
  }
}

onMounted(fetchRoutes)
</script>

<style scoped>
input[type=file] {
    width: 0.1px;
    height: 0.1px;
    opacity: 0;
    overflow: hidden;
    position: absolute;
    z-index: -1;
}
</style>
