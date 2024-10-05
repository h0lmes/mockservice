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
                <b>{{ $colorMode.preference }}</b>
                <span v-if="$colorMode.preference === 'system'">(<i>{{ $colorMode.value }}</i>)</span>
            </ColorScheme>
        </p>
    </div>
</template>
<script>
    import IconSystem from 'assets/icons/system.svg';
    import IconLight from 'assets/icons/light.svg';
    import IconSepia from 'assets/icons/sepia.svg';
    import IconDark from 'assets/icons/dark.svg';
    import IconDark2 from 'assets/icons/cloud.svg';

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
    ul {
        list-style: none;
        padding: 0;
        margin: 0;
    }
    ul li {
        display: inline-block;
        padding: 0;
        outline: 0;
    }
    ul li:hover,
    ul li:focus {
        background-color: var(--nav-bg-active);
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
    .scheme-info {
        font-size: smaller;
        font-weight: lighter;
    }
</style>
