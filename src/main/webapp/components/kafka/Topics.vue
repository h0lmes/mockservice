<template>
    <div>
        <p v-if="entities.length === 0">
            Nothing here ¯\_(ツ)_/¯
        </p>
        <div v-for="(entity, index) in sortedEntities" :key="entityKey(entity)">
            <div v-if="groupStart(sortedEntities, entity, index)" class="component-row-group-boundary"></div>
            <Topic :topic="entity"></Topic>
        </div>
    </div>
</template>

<script setup lang="ts">
import {computed} from 'vue'
import type {KafkaTopicEntity} from '@/types/models'
import Topic from './Topic'

const props = withDefaults(defineProps<{
  entities?: KafkaTopicEntity[]
}>(), {
  entities: () => [],
})

const compare = (a: unknown, b: unknown) => {
  if (a == null && b == null) return 0
  if (a == null) return 1
  if (b == null) return -1
  if (a < b) return -1
  if (a > b) return 1
  return 0
}

const sortedEntities = computed(() => [...props.entities].sort((a, b) => {
  let c = compare(a._new, b._new)
  if (c !== 0) return c
  c = compare(a.group, b.group)
  if (c !== 0) return c
  c = compare(a.topic, b.topic)
  if (c !== 0) return c
  return compare(a.partition, b.partition)
}))

const groupStart = (entities: KafkaTopicEntity[], entity: KafkaTopicEntity, index: number) => index > 0 && entity.group !== entities[index - 1].group
const entityKey = (entity: KafkaTopicEntity) => entity.topic + entity.partition
</script>

<style scoped>
</style>
