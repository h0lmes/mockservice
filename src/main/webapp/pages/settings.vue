<template>
    <div class="monospace">
        <p class="mb-4">
            <ToggleSwitch :id="'randomAlt'"
                          v-model="randomAlt">Random Alt (return random of existing alternatives for requested Route)
            </ToggleSwitch>
        </p>
        <p class="mb-4">
            <ToggleSwitch :id="'quantum'"
                          v-model="quantum">Go Quantum (Routes are now quantum objects, so don't expect anything deterministic)
            </ToggleSwitch>
        </p>
        <div class="mt-5 pl-1">
            <button type="button" class="btn btn-default" @click="save">SAVE</button>
        </div>
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
                randomAlt: false,
                quantum: false
            }
        },
        async fetch() {
            return this.fetchSettings();
        },
        fetchDelay: 0,
        computed: {
            settings() {
                return this.$store.state.settings.settings
            },
        },
        watch: {
            settings() {
                this.randomAlt = this.settings.randomAlt;
                this.quantum = this.settings.quantum;
            },
        },
        methods: {
            ...mapActions({
                fetchSettings: 'settings/fetch',
                saveSettings: 'settings/save'
            }),
            async save() {
                this.$nuxt.$loading.start();
                await this.saveSettings(
                    {
                        randomAlt: this.randomAlt,
                        quantum: this.quantum
                    }
                ).then(() => this.$nuxt.$loading.finish());
            },
        }
    }
</script>
<style scoped>
</style>