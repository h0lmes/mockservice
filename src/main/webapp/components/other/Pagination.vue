<template>
    <div class="pagination-holder">
        <button type="button" class="btn btn-sm" @click="first"><<</button>
        <button type="button" class="btn btn-sm" @click="minusTen">-10</button>
        <button type="button" class="btn btn-sm" @click="minusOne"><</button>
        <div class="pagination-separator left" aria-hidden="true" role="presentation"></div>
        <div>{{ pageLabel }}</div>
        <div class="pagination-separator" aria-hidden="true" role="presentation"></div>
        <button type="button" class="btn btn-sm" @click="plusOne">></button>
        <button type="button" class="btn btn-sm" @click="plusTen">+10</button>
        <button type="button" class="btn btn-sm" @click="last">>></button>
    </div>
</template>

<script setup lang="ts">
import {computed} from 'vue'

const props = withDefaults(defineProps<{
  offset?: number
  limit?: number
  total?: number
}>(), {
  offset: 0,
  limit: 10,
  total: 0,
})

const emit = defineEmits<{
  page: [value: number]
}>()

const page = computed(() => props.offset / props.limit)
const totalPages = computed(() => Math.ceil(props.total / props.limit))
const pageLabel = computed(() => {
  if (totalPages.value === 0) return 'empty'
  return `Page ${page.value + 1} of ${totalPages.value}`
})

const emitPage = (value: number) => {
  emit('page', value)
}

const plusOne = () => {
  if (page.value >= totalPages.value - 1) return
  emitPage(page.value + 1)
}

const plusTen = () => {
  if (page.value >= totalPages.value - 1) return
  emitPage(page.value + Math.min(10, totalPages.value - page.value - 1))
}

const last = () => {
  if (page.value >= totalPages.value - 1) return
  emitPage(totalPages.value - 1)
}

const minusOne = () => {
  if (page.value <= 0) return
  emitPage(page.value - 1)
}

const minusTen = () => {
  if (page.value <= 0) return
  emitPage(page.value - Math.min(10, page.value))
}

const first = () => {
  if (page.value <= 0) return
  emitPage(0)
}
</script>

<style lang="scss" scoped>
.pagination-holder {
    display: flex;
    gap: 0 0.25rem;
    flex-wrap: wrap;
    justify-content: center;
    align-content: center;
    align-items: center;
    width: auto;
    margin: 0;
    padding: 0;
}
.pagination-separator {
    flex: 0 0 auto;
    border: none;
    width: 1px;
    height: 1rem;
    border-left: 1px solid var(--form-control-border);
    margin: 0.5rem;
}
</style>
