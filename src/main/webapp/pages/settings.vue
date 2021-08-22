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
            <ToggleSwitch v-model="alt400OnFailedRequestValidation"
                          sub="When request body validation fails respond with an existing Alt = '400' route instead of standard error.
                          Variable ${requestBodyValidationErrorMessage} would be available to use in response body."
            >Alt '400' on failed request validation</ToggleSwitch>
        </p>
        <div class="mt-5 pl-1">
            <button type="button" class="btn btn-primary" @click="save">Save</button>
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
                alt400OnFailedRequestValidation: true,
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
                this.alt400OnFailedRequestValidation = this.settings.alt400OnFailedRequestValidation;
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