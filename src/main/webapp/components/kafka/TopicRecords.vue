<template>
    <div class="topic-records-holder">
        <TopicRecord class="mb-3" :entity="header"
                     :separators="headerSeparators"
                     :show-timestamp="showTimestamp"
                     :show-headers="showHeaders"
        ></TopicRecord>
        <div v-if="entities.length === 0" class="mt-2 text-center">
            ...
        </div>
        <TopicRecord class="mt-2"
                     v-for="entity in sortedEntities"
                     :key="entity.offset"
                     :entity="entity"
                     :show-timestamp="showTimestamp"
                     :show-headers="showHeaders"/>
    </div>
</template>

<script setup lang="ts">
import {computed} from 'vue'
import type {KafkaRecordEntity} from '@/types/models'
import TopicRecord from '@/components/kafka/TopicRecord.vue'

const props = withDefaults(defineProps<{
  entities?: KafkaRecordEntity[]
  showTimestamp?: boolean
  showHeaders?: boolean
}>(), {
  entities: () => [],
  showTimestamp: false,
  showHeaders: false,
})

const headerSeparators = true
const header: KafkaRecordEntity = {
  offset: 'OFFSET',
  timestamp: 'TIMESTAMP',
  key: 'KEY',
  value: 'VALUE',
  headers: 'HEADERS',
}

const compare = (a: string | number, b: string | number) => {
  if (a < b) return -1
  if (a > b) return 1
  return 0
}

const sortedEntities = computed(() => [...props.entities].sort((a, b) => compare(a.offset, b.offset)))
</script>

<style scoped>
.topic-records-holder {
    padding: 1rem 1rem 0.75rem;
    border: 1px solid var(--form-control-border);
    border-radius: var(--form-control-border-radius);
}
</style>

