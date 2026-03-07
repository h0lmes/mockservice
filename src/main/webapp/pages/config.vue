<template>
    <div class="config-page monospace">
        <div class="component-toolbar mb-4">
            <button type="button" class="btn btn-primary" @click="save">Save and apply</button>
            <button type="button" class="btn btn-default" @click="download">Download as file</button>
            <button type="button" class="btn btn-default" @click="backup">Backup  config on server</button>
            <button type="button" class="btn btn-default" @click="restore">Restore from server backup</button>
        </div>
        <AutoSizeTextArea v-model="config" class="main" :max-rows="maxRows"></AutoSizeTextArea>
        <AutoSizeTextArea v-model="config" class="helper invisible" :min-rows="1" :max-rows="1"></AutoSizeTextArea>
        <Loading v-if="pageLoading"></Loading>
    </div>
</template>

<script setup lang="ts">
import {nextTick, onMounted, ref} from 'vue'
import {usePageLoader, useWorkingAction} from '@/composables/useAsyncState'
import AutoSizeTextArea from '../components/other/AutoSizeTextArea'
import Loading from '../components/other/Loading'
import {backupConfig, fetchConfig, restoreConfig, saveConfig} from '@/state/config'
import {saveTextAsFile} from '@/utils/download'

const { pageLoading, runWhilePageLoading } = usePageLoader()
const { runWhileWorking } = useWorkingAction()
const config = ref('')
const maxRows = ref(30)

const calcMaxRows = () => {
  const main = document.querySelector('textarea.main')?.getBoundingClientRect()
  const helper = document.querySelector('textarea.helper')?.getBoundingClientRect()
  if (!main || !helper) {
    return
  }
  const lineHeight = (main.height - helper.height) / (maxRows.value - 1)
  maxRows.value += (window.innerHeight - main.bottom) / lineHeight - 2
}

const loadPage = async () => runWhilePageLoading(async () => {
  config.value = (await fetchConfig()) ?? ''
  await nextTick()
  calcMaxRows()
})

const save = async () => {
  if (!confirm('Ye be warned =)')) {
    return
  }
  await runWhileWorking(() => saveConfig(config.value))
}

const backup = async () => {
  await runWhileWorking(() => backupConfig())
}

const restore = async () => {
  if (!confirm('Confirm restore ?')) {
    return
  }
  await runWhileWorking(async () => {
    await restoreConfig()
    await loadPage()
  })
}

const download = () => {
  saveTextAsFile(config.value, 'config.yml')
}

onMounted(loadPage)
</script>

<style scoped>
.invisible {
    position: absolute;
    top: -20rem;
    left: 0;
    right: 0;
}
</style>
