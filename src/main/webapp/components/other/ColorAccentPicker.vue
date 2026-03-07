<template>
    <div>
        <ul>
            <li v-for="color of colors" :key="color">
                <div class="color-item" :class="getClasses(color)"
                     tabindex="0"
                     @keydown.enter.exact="select(color)"
                     @click="select(color)"
                >{{ color }}</div>
            </li>
        </ul>
    </div>
</template>

<script setup lang="ts">
import {onBeforeUnmount, onMounted, ref, watch} from 'vue'

const storageKey = 'ColorAccent'
const htmlClassPrefix = 'accent-'
const colors = ['default', 'gray', 'purple', 'pink', 'red', 'orange', 'orange-yellow', 'yellow', 'lime', 'green', 'teal', 'cyan', 'blue', 'violet']
const value = ref('default')

const setValue = (oldValue: string | null, newValue: string) => {
  if (oldValue) {
    document.documentElement.classList.remove(htmlClassPrefix + oldValue)
  }
  document.documentElement.classList.add(htmlClassPrefix + newValue)
}

const storeValue = (nextValue: string) => {
  window.localStorage.setItem(storageKey, nextValue)
}

const getValue = () => window.localStorage.getItem(storageKey) || value.value
const getClasses = (color: string) => color
const select = (color: string) => {
  value.value = color
}

const onStorage = (event: StorageEvent) => {
  if (event.key === storageKey) {
    value.value = event.newValue || 'default'
  }
}

watch(value, (newValue, oldValue) => {
  if (oldValue !== newValue) {
    setValue(oldValue, newValue)
    storeValue(newValue)
    console.log(newValue)
  }
})

onMounted(() => {
  if (!window.localStorage) {
    return
  }
  value.value = getValue()
  setValue(null, value.value)
  window.addEventListener('storage', onStorage)
})

onBeforeUnmount(() => {
  window.removeEventListener('storage', onStorage)
})
</script>

<style scoped>
    ul {
        list-style: none;
        padding: 0;
        margin: 0;
        text-align: start;
    }

    ul li {
        cursor: pointer;
        display: inline-block;
        text-align: start;
        padding: 0;
        outline: none;
        font-size: smaller;
        font-weight: lighter;
        font-family: monospace, monospace;
    }

    ul li:hover {
        background-color: var(--nav-bg-active);
    }

    .color-item {
        padding: 0.7rem;
        outline: none;
    }

    .color-item:focus {
        background-color: var(--nav-bg-active);
    }
</style>
