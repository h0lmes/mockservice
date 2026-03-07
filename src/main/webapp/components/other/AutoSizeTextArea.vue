<template>
    <textarea class="form-control form-control-sm no-resize monospace"
              ref="textArea"
              :value="modelValue"
              :placeholder="placeholder"
              :rows="rows"
              :spellcheck="false"
              @input="input"
              @keydown="event => emit('keydown', event)"
    ></textarea>
</template>

<script setup lang="ts">
import {computed, ref} from 'vue'

const props = withDefaults(defineProps<{
  modelValue?: string
  placeholder?: string
  minRows?: number
  maxRows?: number
}>(), {
  modelValue: '',
  placeholder: undefined,
  minRows: 1,
  maxRows: 20,
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
  input: [value: string]
  keydown: [event: KeyboardEvent]
}>()

const textArea = ref<HTMLTextAreaElement | null>(null)
const rows = computed(() => {
  let size = props.minRows
  if (props.modelValue) {
    const lines = props.modelValue.split('\n')
    if (lines.length > size) {
      size = Math.min(lines.length, props.maxRows)
    }
  }
  return size
})

const input = (event: Event) => {
  const value = (event.target as HTMLTextAreaElement).value
  emit('update:modelValue', value)
  emit('input', value)
}

const selectionStart = () => textArea.value?.selectionStart
const selectionEnd = () => textArea.value?.selectionEnd
const focus = () => textArea.value?.focus()

defineExpose({
  selectionStart,
  selectionEnd,
  focus,
})
</script>

<style scoped>
</style>
