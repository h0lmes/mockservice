<template>
    <button type="button" title="view" class="btn btn-sm" @click="emit('click', $event)">
        <Icon />
    </button>
</template>

<script setup lang="ts">
import Icon from '@/assets/icons/eye.svg?component'

const emit = defineEmits<{
  click: [event: MouseEvent]
}>()
</script>

<style scoped>
</style>
