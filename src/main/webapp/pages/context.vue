<template>
    <div class="monospace">
        <div class="mb-4">Variables on server now</div>
        <AutoSizeTextArea v-model="context" class="main"></AutoSizeTextArea>
        <div class="component-toolbar mt-4">
            <button type="button" class="btn btn-primary" @click="saveGlobal">Save to server</button>
            <button type="button" class="btn btn-default" @click="downloadGlobal">Download as file</button>
        </div>

        <div class="group-boundary"></div>

        <div class="mb-4">Initialize these variables on server startup</div>
        <AutoSizeTextArea v-model="initial" class="main"></AutoSizeTextArea>
        <div class="component-toolbar mt-4">
            <button type="button" class="btn btn-primary" @click="saveInitial">Save to server</button>
            <button type="button" class="btn btn-default" @click="downloadInitial">Download as file</button>
        </div>

        <div class="group-boundary"></div>

        <Loading v-if="pageLoading"></Loading>
    </div>
</template>

<script setup lang="ts">
import {onMounted, ref} from 'vue'
import {usePageLoader, useWorkingAction} from '@/composables/useAsyncState'
import AutoSizeTextArea from '../components/other/AutoSizeTextArea'
import Loading from '../components/other/Loading'
import {fetchGlobalContext, fetchInitialContext, saveGlobalContext, saveInitialContext} from '@/state/context'
import {saveTextAsFile} from '@/utils/download'

const { pageLoading, runWhilePageLoading } = usePageLoader()
const { runWhileWorking } = useWorkingAction()
const context = ref('')
const initial = ref('')

const loadPage = async () => runWhilePageLoading(async () => {
  context.value = (await fetchGlobalContext()) ?? ''
  initial.value = (await fetchInitialContext()) ?? ''
})

const saveGlobal = async () => {
  if (!confirm('Save global context?')) return
  await runWhileWorking(() => saveGlobalContext(context.value))
}

const saveInitial = async () => {
  if (!confirm('Save initial context?')) return
  await runWhileWorking(() => saveInitialContext(initial.value))
}

const downloadGlobal = () => {
  saveTextAsFile(context.value, 'context-global.txt')
}

const downloadInitial = () => {
  saveTextAsFile(initial.value, 'context-initial.txt')
}

onMounted(loadPage)
</script>

<style scoped>
.group-boundary {
    background: transparent;
    margin: 1.5rem 0;
    padding: 0;
    border-top: 1px solid var(--bg-component-active);
}
</style>
