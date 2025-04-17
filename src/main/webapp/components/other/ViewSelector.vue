<template>
    <div>
        <ToggleSwitch v-model="value">{{label}}</ToggleSwitch>
    </div>
</template>
<script>
import ToggleSwitch from './ToggleSwitch';

export default {
        name: "ViewSelector",
        components: {ToggleSwitch},
        data() {
            return {
                label: 'Compact',
                cssClass: 'compact-view',
                value: false,
            }
        },
        props: {
            storageKey: {type: String, default: 'CompactView'},
        },
        watch: {
            value (newValue, oldValue) {
                if (oldValue !== newValue) {
                    this.setCssClass(newValue);
                    this.storeValue(newValue);
                }
            }
        },
        mounted () {
            if (window.localStorage) {
                this.value = this.getValue();
                this.setCssClass(this.value);
                this.watchStorageChange();
            }
        },
        methods: {
            watchStorageChange() {
                window.addEventListener('storage', e => {
                    if (e.key === this.storageKey) {
                        this.value = e.newValue === 'true'
                    }
                });
            },
            storeValue(value) {
                window.localStorage.setItem(this.storageKey, value)
            },
            getValue() {
                return window.localStorage.getItem(this.storageKey) === 'true'
            },
            setCssClass(value) {
                value ? document.documentElement.classList.add(this.cssClass)
                    : document.documentElement.classList.remove(this.cssClass);
            },
        }
    }
</script>
<style scoped>
</style>
