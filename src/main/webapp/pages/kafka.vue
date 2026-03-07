<template>
    <div class="monospace">
        <div class="component-toolbar mb-3">
            <div class="toolbar-item">
                <input ref="search"
                       type="text"
                       class="form-control monospace"
                       placeholder="type in or click on values (group or topic)"
                       @keydown.enter.exact.stop="filter(($event.target as HTMLInputElement).value)"/>
            </div>
            <button type="button" class="toolbar-item-w-fixed-auto btn" @click="filter('')">Clear search</button>
            <ToggleSwitch v-model="jsSearch" class="toolbar-item toolbar-item-w-fixed-auto">JS</ToggleSwitch>
        </div>

        <div class="component-toolbar mb-3">
            <button type="button" class="toolbar-item-w-fixed-auto btn" @click="addTopic">Add topic-partition</button>
            <ViewSelector class="toolbar-item toolbar-item-w-fixed-auto" :storageKey="'CompactView-Kafka'"></ViewSelector>
        </div>

        <Topics :entities="filteredEntities"></Topics>

        <Loading v-if="pageLoading"></Loading>
    </div>
</template>

<script setup lang="ts">
import {computed, ref} from 'vue'
import type {KafkaTopicEntity} from '@/types/models'
import {usePageLoader} from '@/composables/useAsyncState'
import {useSyncedSearch} from '@/composables/useSyncedSearch'
import Loading from '../components/other/Loading'
import ToggleSwitch from '../components/other/ToggleSwitch'
import ViewSelector from '../components/other/ViewSelector'
import Topics from '../components/kafka/Topics'
import {frontendAppState, setKafkaSearchExpression} from '@/state/app'
import {addKafkaTopic, fetchKafkaTopics, useKafkaState} from '@/state/kafka'

const { pageLoading, runWhilePageLoading } = usePageLoader()
const jsSearch = ref(false)
const searchExpression = computed(() => (frontendAppState.kafkaSearchExpression || '').trim())
const { search, query } = useSyncedSearch(searchExpression)
const topics = computed<KafkaTopicEntity[]>(() => useKafkaState().state.topics)
const entities = computed<KafkaTopicEntity[]>(() => [...topics.value])

const getSearchFn = () => {
  if (jsSearch.value) {
    return Function('e', 'return ' + query.value + ';') as (entity: KafkaTopicEntity) => boolean
  }
  if (!query.value) {
    return () => true
  }
  const nextQuery = query.value
  return (entity: KafkaTopicEntity) => entity.group.includes(nextQuery) || entity.topic.includes(nextQuery)
}

const filteredEntities = computed<KafkaTopicEntity[]>(() => {
  if (!query.value) return entities.value
  try {
    return entities.value.filter(getSearchFn())
  } catch (error) {
    console.error(error)
    return []
  }
})

const filter = (value: string) => {
  setKafkaSearchExpression(value)
}

const loadPage = async () => runWhilePageLoading(async () => {
  await fetchKafkaTopics()
})

const addTopic = () => {
  addKafkaTopic()
}

await loadPage()
</script>

<style scoped>
</style>
