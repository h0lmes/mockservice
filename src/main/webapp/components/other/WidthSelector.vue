<template>
    <div>
        <ToggleSwitch v-model="value">{{ label }}</ToggleSwitch>
    </div>
</template>

<script setup lang="ts">
import {onBeforeUnmount, onMounted, ref, watch} from 'vue'
import ToggleSwitch from './ToggleSwitch'

const props = withDefaults(defineProps<{
  storageKey?: string
}>(), {
  storageKey: 'LimitWidth',
})

const label = 'Limit width'
const cssClass = 'page-narrow'
const value = ref(false)

const setCssClass = (enabled: boolean) => {
  if (enabled) {
    document.documentElement.classList.add(cssClass)
    return
  }
  document.documentElement.classList.remove(cssClass)
}

const storeValue = (enabled: boolean) => {
  window.localStorage.setItem(props.storageKey, String(enabled))
}

const getValue = () => window.localStorage.getItem(props.storageKey) === 'true'

const onStorage = (event: StorageEvent) => {
  if (event.key === props.storageKey) {
    value.value = event.newValue === 'true'
  }
}

watch(value, (newValue, oldValue) => {
  if (oldValue !== newValue) {
    setCssClass(newValue)
    storeValue(newValue)
  }
})

onMounted(() => {
  if (!window.localStorage) {
    return
  }
  value.value = getValue()
  setCssClass(value.value)
  window.addEventListener('storage', onStorage)
})

onBeforeUnmount(() => {
  window.removeEventListener('storage', onStorage)
})
</script>

<style scoped>
</style>
