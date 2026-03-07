<template>
    <div class="topic-record-row">
        <div class="topic-record-col">{{ entity.offset }}</div>
        <div :class="{ 'topic-record-separator': true, show: separators }" aria-hidden="true" role="presentation"></div>

        <div v-show="showTimestamp" class="topic-record-col">{{ entity.timestamp }}</div>
        <div v-show="showTimestamp" :class="{ 'topic-record-separator': true, show: separators }" aria-hidden="true" role="presentation"></div>

        <div class="topic-record-col w2">{{ prettyKey }}</div>
        <div :class="{ 'topic-record-separator': true, show: separators }" aria-hidden="true" role="presentation"></div>

        <div class="topic-record-col w5">{{ prettyValue }}</div>
        <div v-show="showHeaders" :class="{ 'topic-record-separator': true, show: separators }" aria-hidden="true" role="presentation"></div>

        <div v-show="showHeaders" class="topic-record-col w4">{{ entity.headers }}</div>
    </div>
</template>

<script setup lang="ts">
import {computed} from 'vue'
import type {KafkaRecordEntity} from '@/types/models'

const props = defineProps<{
  entity: KafkaRecordEntity
  separators?: boolean
  showTimestamp?: boolean
  showHeaders?: boolean
}>()

const prettyKey = computed(() => props.entity.key == null ? 'null' : String(props.entity.key))
const prettyValue = computed(() => props.entity.value == null ? 'null' : String(props.entity.value))
</script>

<style scoped lang="scss">
.topic-record-row {
    display: flex;
    gap: 0.5rem 0.5rem;
    flex-wrap: wrap;
    justify-content: center;
    align-content: center;
    align-items: center;
    margin: 0;
    padding: 0;
    border: none;
}
.topic-record-col {
    flex: 1 1 0;
    position: relative;
    box-sizing: border-box;
    min-width: 5rem;
    text-align: center;

    &.w2 {
        flex: 2 1 0;
    }

    &.w4 {
        flex: 4 1 0;
    }

    &.w5 {
        flex: 5 1 0;
    }
}
.topic-record-separator {
    flex: 0 0 auto;
    border: none;
    border-left: 1px solid transparent;
    width: 1px;
    height: 1rem;

    &.show {
        border-left-color: var(--form-control-border);
    }
}
</style>
