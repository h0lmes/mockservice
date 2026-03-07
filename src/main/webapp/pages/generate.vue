<template>
    <div class="monospace">
        <div class="component-toolbar mb-4">
            <button type="button" class="btn btn-primary" @click="loadPage">Generate JSON</button>
            <button type="button" class="btn btn-default" @click="copyToClipboard(value)">
                Copy to clipboard
                <span v-show="copied">&#9989;</span>
            </button>
            <button type="button" class="btn btn-default" @click="download">Download as file</button>
        </div>
        <AutoSizeTextArea v-model="schema" class="mb-4" :min-rows="2" :placeholder="placeholder"></AutoSizeTextArea>
        <pre>{{ value }}</pre>

        <Loading v-if="pageLoading"></Loading>
    </div>
</template>

<script setup lang="ts">
import {computed, onMounted, ref} from 'vue'
import {usePageLoader} from '@/composables/useAsyncState'
import AutoSizeTextArea from '../components/other/AutoSizeTextArea'
import Loading from '../components/other/Loading'
import copy from '../js/clipboard'
import {generateJson, useGenerateState} from '@/state/generate'
import {saveTextAsFile} from '@/utils/download'

const { pageLoading, runWhilePageLoading } = usePageLoader()
const copied = ref(false)
const schema = ref('')
const placeholder = 'Paste JSON schema here (in JSON or YAML format) or leave empty to generate random JSON\nRead more about JSON schema at https://json-schema.org/'
const value = computed(() => useGenerateState().state.value)

const loadPage = async () => runWhilePageLoading(async () => {
  copied.value = false
  await generateJson(schema.value)
})

const download = () => {
  saveTextAsFile(value.value, 'generated.json')
}

const copyToClipboard = (text: string) => {
  copy(text).then(() => {
    copied.value = true
  }).catch(console.error)
}

onMounted(loadPage)
</script>

<style scoped>
</style>
