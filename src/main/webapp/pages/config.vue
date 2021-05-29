<template>
    <div class="monospace">

        <p v-if="$fetchState.pending">Loading...</p>
        <p v-else-if="lastError"></p>

        <p class="danger">Use with caution!</p>
        <p class="danger mb-5">It is easy to ruin config by editing it as plain text.</p>
        <textarea class="form-control form-control-sm v-resize" rows="16" v-model="config"></textarea>
        <div class="buttons mt-5">
            <div class="btn btn-sm btn-danger mr-3" @click="save">SAVE TO SERVER</div>
            <div class="btn btn-sm btn-primary mr-3" @click="saveAsFile">DOWNLOAD</div>
        </div>

    </div>
</template>
<script>
    import {mapActions} from 'vuex';

    async function handleError(response) {
        if (response.status === 400) {
            const errorInfo = await response.json();
            throw Error(errorInfo.message || errorInfo);
        }
        if (!response.ok) {
            throw Error(response.statusText || response);
        }
        return response;
    }

    export default {
        name: "config",
        data() {
            return {
                config: ''
            }
        },
        async fetch() {
            return this.fetchConfig();
        },
        computed: {
            BASE_URL() {
                return this.$store.state.BASE_URL
            },
            lastError() {
                return this.$store.state.lastError
            }
        },
        methods: {
            ...mapActions({
                setLastError: 'setLastError',
                resetLastError: 'resetLastError'
            }),

            async save() {
                if (confirm('Ye be warned =)')) {
                    await this.saveConfig();
                }
            },

            fetchConfig() {
                this.resetLastError();
                return fetch(this.BASE_URL + '/web-api/config'
                ).then(handleError
                ).then(response => response.text()
                ).then(response => this.config = response
                ).catch(error => this.setLastError(error));
            },

            saveConfig() {
                return fetch(this.BASE_URL + '/web-api/config',
                    {
                        method: 'PUT',
                        headers: {'Content-Type': 'text/plain'},
                        body: this.config
                    }
                ).then(handleError
                ).then(response => response.text()
                ).then(this.resetLastError()
                ).catch(error => this.setLastError(error));
            },

            saveAsFile() {
                this.saveTextAsFile(this.config, 'config.yml')
            },

            saveTextAsFile(text, fileName) {
                let blob = new Blob([text], {type: 'text/plain'});
                let link = document.createElement("a");
                link.download = fileName;
                link.innerHTML = "Download File";
                link.href = URL.createObjectURL(blob);
                link.style.display = "none";
                document.body.appendChild(link);
                link.click();
                link.remove();
            }
        }
    }
</script>
<style scoped>
    .danger {
        color: var(--red);
    }
</style>