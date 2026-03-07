<template>
    <div class="monospace">
        <div class="component-toolbar mb-5">
            <button type="button" class="btn btn-primary" @click="download">Download</button>
        </div>
        <pre class="smaller">{{ value }}</pre>
        <Loading v-if="pageLoading"></Loading>
    </div>
</template>

<script setup lang="ts">
import {onMounted, ref} from 'vue'
import {usePageLoader} from '@/composables/useAsyncState'
import Loading from '../components/other/Loading'
import {fetchLog} from '@/state/log'
import {saveTextAsFile} from '@/utils/download'

const { pageLoading, runWhilePageLoading } = usePageLoader()
const value = ref('')

const loadPage = async () => runWhilePageLoading(async () => {
  value.value = (await fetchLog()) ?? ''
})

const download = () => {
  saveTextAsFile(value.value, 'mockservice.log')
}

onMounted(loadPage)
</script>

<style scoped>
</style>
