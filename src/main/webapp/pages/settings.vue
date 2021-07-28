<template>
    <div class="monospace">
        <p class="mb-4">
            <ToggleSwitch v-model="randomAlt"
                          sub="Return random of existing Alt (alternatives) for requested Route"
            >Random Alt</ToggleSwitch>
        </p>
        <p class="mb-4">
            <ToggleSwitch v-model="quantum"
                          sub="Routes are now quantum objects, so don't expect anything deterministic"
            >Go Quantum</ToggleSwitch>
        </p>
        <p class="mb-2">
            <ToggleSwitch v-model="failedInputValidationAlt400"
                          sub="Variable ${requestBodyValidationErrorMessage} would be available to use in Alt = '400' response"
            >When input validation failed respond with an existing Alt = '400' route instead of validation error</ToggleSwitch>
        </p>
        <div class="mt-5 pl-1">
            <button type="button" class="btn btn-primary" @click="save">SAVE</button>
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
                quantum: false,
                failedInputValidationAlt400: true,
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
                this.failedInputValidationAlt400 = this.settings.failedInputValidationAlt400;
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
                        quantum: this.quantum,
                        failedInputValidationAlt400: this.failedInputValidationAlt400
                    }
                ).then(() => this.$nuxt.$loading.finish());
            },
        }
    }
</script>
<style scoped>
</style>