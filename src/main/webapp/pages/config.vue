<template>
    <div class="monospace">
        <p class="danger">Use with caution!</p>
        <p class="danger mb-5">It is easy to ruin config by editing it as plain text.</p>
        <textarea class="form-control form-control-sm v-resize" rows="16" v-model="config.data"></textarea>
        <div class="buttons mt-5">
            <div class="btn btn-sm btn-danger mr-3" @click="save">SAVE TO SERVER</div>
            <div class="btn btn-sm btn-primary mr-3" @click="saveAsFile">DOWNLOAD</div>
        </div>
    </div>
</template>
<script>
    import { mapActions } from 'vuex';

    function handleError(response) {
        if (!response.ok) {
            throw Error(response.statusText);
        }
        return response;
    }

    export default {
        name: "config",
        data() {
            return {
                config: {}
            }
        },
        async fetch() {
            this.fetchConfig();
        },
        computed: {
            BASE_URL() {
                return this.$store.state.BASE_URL
            }
        },
        methods: {
            ...mapActions({
                fetchConfig: 'fetchConfig',
                setLastError: 'setLastError',
                resetLastError: 'resetLastError'
            }),

            save() {
                if (confirm('Ye be warned =)')) {
                    this.saveConfig();
                }
            },

            fetchConfig() {
                fetch(
                    this.BASE_URL + '/web-api/config'
                ).then(
                    handleError
                ).then(response => {
                    return response.json();
                }).then(response => {
                    this.config = response;
                }).catch(error => {
                    console.log('Error: ', error);
                    this.setLastError(error);
                });
            },

            saveConfig() {
                fetch(
                    this.BASE_URL + '/web-api/config',
                    {
                        method: 'PUT',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify(this.config)
                    }
                ).then(
                    handleError
                ).then(response => {
                    return response.text();
                }).then(response => {
                    this.resetLastError();
                }).catch(error => {
                    console.log('Error: ', error);
                    this.setLastError(error);
                });
            },

            saveAsFile() {
                this.saveTextAsFile(this.config.data, 'config.yml')
            },

            saveTextAsFile(text, fileName) {
                let blob = new Blob([text], {type:'text/plain'});
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