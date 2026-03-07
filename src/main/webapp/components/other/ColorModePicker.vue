<template>
    <div class="color-picker-base">
        <ul>
            <li v-for="color of colors" :key="color"
                tabindex="0"
                @keydown.enter.exact="colorMode.preference = color"
                @click="colorMode.preference = color">
                <component :is="getIconComponent(color)" :class="getClasses(color)"/>
                <span :class="getClasses(color)">{{ color }}</span>
            </li>
        </ul>
    </div>
</template>

<script setup lang="ts">
import IconCode from '@/assets/icons/cloud.svg?component'
import IconDark from '@/assets/icons/dark.svg?component'
import IconLight from '@/assets/icons/light.svg?component'
import IconSepia from '@/assets/icons/sepia.svg?component'
import IconSystem from '@/assets/icons/system.svg?component'

const colorMode = useColorMode()
const colors = ['system', 'light', 'sepia', 'dark', 'code']

const getIconComponent = (color: string) => ({
  system: IconSystem,
  light: IconLight,
  sepia: IconSepia,
  dark: IconDark,
  code: IconCode,
}[color] || IconSystem)

const getClasses = (color: string) => {
  if (!colorMode || colorMode.unknown) {
    return {}
  }
  return {
    preferred: color === colorMode.preference,
    selected: color === colorMode.value,
  }
}
</script>

<style scoped>
.color-picker-base {
    width: 100%;
}
ul {
    width: 100%;
    list-style: none;
    padding: 0;
    margin: 0;
}
ul li {
    width: 100%;
    display: block;
    text-align: start;
    padding: 0;
    outline: 0;
    cursor: pointer;
}

ul li:hover,
ul li:focus {
    background-color: var(--nav-bg-active);
}

ul li > * {
    display: inline-block;
    vertical-align: middle;
}

p {
    margin: 0;
    padding: 5px 0;
}
.feather {
    box-sizing: content-box;
    cursor: pointer;
    position: relative;
    top: 0;
    margin: 0;
    padding: 0.7rem;
    background-color: transparent;
    color: var(--nav-color);
    border-radius: 5px;
    transition: all 0.1s ease;
}
.feather.preferred {
    color: var(--color-accent-one);
}
.feather.selected {
    color: var(--color-accent-one);
}
span.preferred {
    color: var(--nav-color-active);
}
span.selected {
    color: var(--nav-color-active);
}
</style>
