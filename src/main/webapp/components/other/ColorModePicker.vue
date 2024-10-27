<template>
    <div class="color-picker-base">
        <ul>
            <li v-for="color of colors" :key="color"
                @keydown.enter.exact="$colorMode.preference = color"
                @click="$colorMode.preference = color"
                tabindex="0">
                <component :is="`icon-${color}`" :class="getClasses(color)"/>
                <span :class="getClasses(color)">{{color}}</span>
            </li>
        </ul>
    </div>
</template>
<script>
import IconSystem from '@/assets/icons/system.svg?inline';
import IconLight from '@/assets/icons/light.svg?inline';
import IconSepia from '@/assets/icons/sepia.svg?inline';
import IconDark from '@/assets/icons/dark.svg?inline';
import IconDark2 from '@/assets/icons/cloud.svg?inline';

export default {
    name: "ColorModePicker",
    components: {
        IconSystem,
        IconLight,
        IconSepia,
        IconDark,
        IconDark2
    },
    data() {
        return {
            colors: ['system', 'light', 'sepia', 'dark', 'dark2']
        }
    },
    methods: {
        getClasses (color) {
            // Does not set classes on ssr when preference is system (because we don't know the preference until client-side)
            if (this.$colorMode.unknown) {
                return {}
            }
            return {
                preferred: color === this.$colorMode.preference,
                selected: color === this.$colorMode.value
            }
        }
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
