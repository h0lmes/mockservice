<template>
    <div>
        <ul>
            <li v-for="color of colors" :key="color" tabindex="0" @keydown.enter.exact="$colorMode.preference = color">
                <component :is="`icon-${color}`"
                           @click="$colorMode.preference = color"
                           :class="getClasses(color)" />
            </li>
        </ul>
        <p class="scheme-info">
            <ColorScheme placeholder="..." tag="span">
                Color mode: <b>{{ $colorMode.preference }}</b>
                <span v-if="$colorMode.preference === 'system'">(<i>{{ $colorMode.value }}</i>)</span>
            </ColorScheme>
        </p>
    </div>
</template>
<script>
    import IconSystem from '@/assets/icons/system.svg?inline';
    import IconLight from '@/assets/icons/light.svg?inline';
    import IconDark from '@/assets/icons/dark.svg?inline';
    import IconSepia from '@/assets/icons/sepia.svg?inline';

    export default {
        name: "ColorModePicker",
        components: {
            IconSystem,
            IconLight,
            IconDark,
            IconSepia
        },
        data() {
            return {
                colors: ['system', 'light', 'dark', 'sepia']
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
    ul {
        list-style: none;
        padding: 0;
        margin: 0;
    }
    ul li {
        display: inline-block;
        padding: 5px;
        outline: 0;
    }
    ul li:focus {
        background-color: var(--nav-bg-active);
    }
    p {
        margin: 0;
        padding-top: 5px;
        padding-bottom: 20px;
    }
    .feather {
        box-sizing: content-box;
        cursor: pointer;
        position: relative;
        top: 0;
        margin: 0;
        padding: 4px;
        background-color: transparent;
        color: var(--nav-color);
        border-radius: 5px;
        transition: all 0.1s ease;
    }
    .feather.preferred {
        color: var(--color-accent-one);
    }
    /*.feather.selected {*/
        /*color: var(--color-accent-one);*/
    /*}*/
    .scheme-info {
        font-size: smaller;
        font-weight: lighter;
    }
</style>