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
<script>
    const storageKey = 'ColorAccent';
    const HTML_CLASS_PREFIX = 'accent-';

    export default {
        name: "ColorAccentPicker",
        components: {},
        data() {
            return {
                colors: ['default', 'gray', 'cyan-blue', 'blue', 'violet', 'purple', 'pink',
                    'red', 'orange', 'orange-yellow', 'yellow', 'lime', 'green', 'teal', 'cyan'],
                value: 'default',
            }
        },
        watch: {
            value(newValue, oldValue) {
                if (oldValue !== newValue) {
                    this.setValue(oldValue, newValue);
                    this.storeValue(newValue);
                    console.log(newValue)
                }
            }
        },
        mounted() {
            if (window.localStorage) {
                this.value = this.getValue();
                this.setValue(null, this.value);
                this.watchStorageChange();
            }
        },
        methods: {
            watchStorageChange() {
                window.addEventListener('storage', e => {
                    if (e.key === storageKey) {
                        this.value = e.newValue
                    }
                });
            },
            storeValue(value) {
                window.localStorage.setItem(storageKey, value);
            },
            getValue() {
                return window.localStorage.getItem(storageKey) || this.value;
            },
            setValue(oldValue, newValue) {
                if (oldValue) {
                    document.documentElement.classList.remove(HTML_CLASS_PREFIX + oldValue);
                }
                document.documentElement.classList.add(HTML_CLASS_PREFIX + newValue);
            },
            getClasses(color) {
                return color;
            },
            select(color) {
                this.value = color;
            },
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
        cursor: pointer;
        display: inline-block;
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
        padding: 0.5rem;
        outline: none;
    }

    .color-item:focus {
        background-color: var(--nav-bg-active);
    }
</style>
