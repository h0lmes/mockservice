<template>
    <div class="monospace">
        <pre v-html="valueProcessed"></pre>
        <Loading v-if="pageLoading"></Loading>
    </div>
</template>

<script setup lang="ts">
import {computed, onMounted, ref} from 'vue'
import {usePageLoader} from '@/composables/useAsyncState'
import Loading from '../components/other/Loading'
import {fetchRequestGraph} from '@/state/request-graph'

const { pageLoading, runWhilePageLoading } = usePageLoader()
const value = ref('')
const valueRaw = computed(() => value.value || '')
const valueProcessed = computed(() => valueRaw.value.replaceAll('CYCLE', '<span class="red">CYCLE</span>'))

const loadPage = async () => runWhilePageLoading(async () => {
  value.value = (await fetchRequestGraph()) ?? ''
})

onMounted(loadPage)
</script>

<style scoped>
</style>
