<template>
    <div v-if="lastError" class="modal-backdrop" @click="dismiss">
        <div class="modal-border">
            <div class="modal-content monospace">
                <p class="red">{{ lastError }}</p>
                <p class="color-secondary">Click anywhere to dismiss</p>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import {computed} from 'vue'
import {useFrontendApp} from '@/state/app'

const frontendApp = useFrontendApp()
const lastError = computed(() => frontendApp.state.lastError)

const dismiss = () => {
  frontendApp.resetLastError()
}
</script>

<style scoped>
</style>
