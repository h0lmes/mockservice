<template>
    <button type="button" title="edit" class="btn btn-sm" @click="emit('click', $event)">
        <Icon />
    </button>
</template>

<script setup lang="ts">
import Icon from '@/assets/icons/pencil.svg?component'

const emit = defineEmits<{
  click: [event: MouseEvent]
}>()
</script>

<style scoped>
</style>
