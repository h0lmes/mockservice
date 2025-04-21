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

        <div class="component px-4 py-3 mt-6">
            <div>SSL certificate</div>
            <div class="color-secondary nowrap mt-3">{{certificateHumanReadable}}</div>
            <input type="file" class="hidden">
            <button type="button" class="btn btn-default mt-3" @click="selectCertFile">Select local file</button>
            <button type="button" class="btn btn-default mt-3" @click="resetCertFile">Forget certificate</button>
            <div class="color-secondary mt-4">
                If [No certificate] all remote certificates are considered trusted.
            </div>
            <div class="color-secondary mt-3">
                To use a certificate:<br>
                - select a PKCS #12 certificate file<br>
                - save settings<br>
                - set a password; password input will show once you have certificate saved.
            </div>

            <div class="mt-4" v-show="hasCertificateOnServer">
                <div>SSL certificate password</div>
                <input type="password" class="form-control form-control-sm mt-3" v-model="password"/>
                <button type="button" class="btn btn-default mt-3" @click="setPass">
                    Set password
                    <span v-show="passwordSet">&#9989;</span>
                </button>
                <div class="color-secondary mt-3">
                    Password is not stored in Config (so, no need to save settings after you set a password).
                    Once you set it a new SSL context is created until service is restarted.
                </div>
            </div>
        </div>

        <div class="mt-6 pl-1">
            <button type="button" class="btn btn-primary" @click="save">
                <span class="mx-5">Save settings</span>
            </button>
        </div>

        <Loading v-if="$fetchState.pending"></Loading>
    </div>
</template>
<script>
import {mapActions} from 'vuex';
import Loading from "../components/other/Loading";
import ToggleSwitch from "../components/other/ToggleSwitch";

export default {
    name: "settings",
    components: {Loading, ToggleSwitch},
    data() {
        return {
            randomAlt: false,
            quantum: false,
            alt400OnFailedRequestValidation: true,
            certificate: null,
            password: '',
            passwordSet: false,
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
        hasCertificateOnServer() {
            return this.settings.certificate !== null && this.settings.certificate !== ''
        },
        certificateHumanReadable() {
            return this.certificate == null ? 'No certificate' : 'Certificate: ' + this.certificate
        }
    },
    watch: {
        settings() {
            this.randomAlt = this.settings.randomAlt;
            this.quantum = this.settings.quantum;
            this.alt400OnFailedRequestValidation = this.settings.alt400OnFailedRequestValidation;
            this.certificate = this.settings.certificate;
        },
    },
    methods: {
        ...mapActions({
            fetchSettings: 'settings/fetch',
            saveSettings: 'settings/save',
            setCertificatePassword: 'settings/certificatePassword',
        }),
        async save() {
            this.$nuxt.$loading.start();
            await this.saveSettings(
                {
                    randomAlt: this.randomAlt,
                    quantum: this.quantum,
                    failedInputValidationAlt400: this.failedInputValidationAlt400,
                    certificate: this.certificate,
                }
            ).then(() => this.$nuxt.$loading.finish());
        },
        selectCertFile() {
            const el = document.querySelector('input[type="file"]');
            if (el === null) return;

            el.onchange = () => {
                this.$nuxt.$loading.finish();
                this.certificate = null;
                this.passwordSet = false;
                try {
                    const file = el.files[0];
                    const reader = new FileReader();
                    reader.readAsDataURL(file);
                    reader.onload = () => {
                        this.certificate = reader.result
                            .replace('data:', '')
                            .replace(/^.+,/, '');
                        console.log('Certificate: ', this.certificate);
                    }
                    reader.onerror = (error) => console.log('Error: ', error);
                } catch (e) {
                    console.error(e);
                }
            };
            this.$nuxt.$loading.start();
            el.click();
        },
        resetCertFile() {
            this.certificate = null;
            this.passwordSet = false;
        },
        async setPass() {
            this.$nuxt.$loading.start();
            await this.setCertificatePassword(this.password)
                .then((result, error) => {
                    this.$nuxt.$loading.finish();
                    this.password = '';
                    this.passwordSet = result;
                });
        },
    }
}
</script>
<style scoped>
</style>
