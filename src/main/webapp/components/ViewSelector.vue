<template>
    <div>
        <ToggleSwitch v-model="value">Compact view</ToggleSwitch>
    </div>
</template>
<script>
    import ToggleSwitch from './ToggleSwitch';

    const storageKey = 'CompactView';
    const HTML_CLASS_COMPACT = 'compact-view';

    export default {
        name: "ViewSelector",
        components: {ToggleSwitch},
        data() {
            return {
                value: false
            }
        },
        watch: {
            value (newValue, oldValue) {
                if (oldValue !== newValue) {
                    this.setCompactView(newValue);
                    this.storeValue(newValue);
                }
            }
        },
        mounted () {
            if (window.localStorage) {
                this.value = this.getValue();
                this.setCompactView(this.value);
                this.watchStorageChange();
            }
        },
        methods: {
            watchStorageChange() {
                window.addEventListener('storage', e => {
                    if (e.key === storageKey) {
                        this.value = e.newValue === 'true'
                    }
                });
            },
            storeValue(value) {
                window.localStorage.setItem(storageKey, value)
            },
            getValue() {
                return window.localStorage.getItem(storageKey) === 'true'
            },
            setCompactView(value) {
                value ? document.documentElement.classList.add(HTML_CLASS_COMPACT) : document.documentElement.classList.remove(HTML_CLASS_COMPACT);
            },
        }
    }
</script>
<style scoped>
</style>