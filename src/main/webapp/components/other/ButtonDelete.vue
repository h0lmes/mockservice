<template>
    <button type="button" title="delete" class="btn btn-sm" @click="emit('click', $event)">
        <Icon />
    </button>
</template>

<script setup lang="ts">
import Icon from '@/assets/icons/trash.svg?component'

const emit = defineEmits<{
  click: [event: MouseEvent]
}>()
</script>

<style scoped>
</style>
