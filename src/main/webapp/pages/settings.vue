<template>
    <div class="monospace">
        <p>
            <ToggleSwitch :id="'randomAlt'"
                          v-model="randomAlt">Random Alt (return random of existing alternatives for requested Route)
            </ToggleSwitch>
        </p>
        <p class="mt-3">
            <ToggleSwitch :id="'quantum'"
                          v-model="quantum">Go Quantum (Routes are now quantum objects, so don't expect anything deterministic)
            </ToggleSwitch>
        </p>
        <div class="mt-4 pl-1">
            <div class="btn btn-sm btn-primary" @click="save">SAVE</div>
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
                return this.$store.state.settings
            },
        },
        watch: {
            settings() {
                this.randomAlt = this.settings.randomAlt;
                this.quantum = this.settings.quantum;
            },
        },
        methods: {
            ...mapActions(['fetchSettings', 'saveSettings']),
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