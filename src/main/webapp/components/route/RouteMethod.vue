<template>
    <div :class="color">{{ value }}<span v-show="disabled"> (disabled)</span></div>
</template>

<script setup lang="ts">
import {computed} from 'vue'

const props = withDefaults(defineProps<{
  value?: string
  disabled?: boolean
}>(), {
  value: '',
  disabled: false,
})

const color = computed(() => ({
  bold: !props.disabled,
  green: !props.disabled && props.value === 'GET',
  blue: !props.disabled && props.value === 'POST',
  'orange-yellow': !props.disabled && props.value === 'PUT',
  orange: !props.disabled && props.value === 'PATCH',
  pink: !props.disabled && props.value === 'DELETE',
}))
</script>

<style scoped>
</style>
