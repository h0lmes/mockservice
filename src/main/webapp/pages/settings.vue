<template>
    <div class="monospace">
        <p>
            <ToggleSwitch :id="'randomSuffix'"
                          v-model="randomSuffix">Random Suffix (return random of existing Suffixes for requested Route)
            </ToggleSwitch>
        </p>
        <p class="mt-2">
            <ToggleSwitch :id="'quantum'"
                          v-model="quantum">Go Quantum (Routes are now quantum objects, so don't expect anything deterministic)
            </ToggleSwitch>
        </p>
        <p class="mt-4">
            <a class="btn btn-sm btn-default" @click="save">SAVE</a>
        </p>
        <Loading v-if="$fetchState.pending"></Loading>
    </div>
</template>
<script>
    import {mapActions} from 'vuex';
    import Loading from "../components/Loading";
    import ToggleSwitch from "../components/ToggleSwitch";

    export default {
        name: "settings",
        components: {Loading, ToggleSwitch},
        data() {
            return {
                randomSuffix: false,
                quantum: false
            }
        },
        async fetch() {
            return this.fetchSettings();
        },
        computed: {
            settings() {
                return this.$store.state.settings
            },
        },
        watch: {
            settings() {
                this.randomSuffix = this.settings.randomSuffix;
                this.quantum = this.settings.quantum;
            },
        },
        methods: {
            ...mapActions(['fetchSettings', 'saveSettings']),
            async save() {
                await this.saveSettings({
                    randomSuffix: this.randomSuffix,
                    quantum: this.quantum
                });
            },
        }
    }
</script>
<style scoped>
</style>