<template>
    <div class="config-page monospace">
        <div class="component-toolbar mb-5">
            <button type="button" class="btn btn-danger" @click="save">SAVE AND APPLY</button>
            <button type="button" class="btn btn-default" @click="download">DOWNLOAD AS FILE</button>
            <button type="button" class="btn btn-default" @click="backup">BACKUP ON SERVER</button>
            <button type="button" class="btn btn-default" @click="restore">RESTORE FROM BACKUP</button>
        </div>
        <AutoSizeTextArea v-model="config" :max-rows="30"></AutoSizeTextArea>
        <Loading v-if="$fetchState.pending"></Loading>
    </div>
</template>
<script>
    import {mapActions} from 'vuex';
    import Loading from "../components/Loading";
    import AutoSizeTextArea from "../components/AutoSizeTextArea";

    export default {
        name: "config",
        components: {AutoSizeTextArea, Loading},
        data() {
            return {
                config: '',
            }
        },
        async fetch() {
            return this.fetchConfig()
                .then(response => this.config = response);
        },
        fetchDelay: 0,
        methods: {
            ...mapActions({
                fetchConfig: 'config/fetch',
                saveConfig: 'config/save',
                backupConfig: 'config/backup',
                restoreConfig: 'config/restore'
            }),
            async save() {
                if (confirm('Ye be warned =)')) {
                    this.$nuxt.$loading.start();
                    this.saveConfig(this.config)
                        .then(() => this.$nuxt.$loading.finish());
                }
            },
            async backup() {
                this.$nuxt.$loading.start();
                this.backupConfig()
                    .then(() => this.$nuxt.$loading.finish());
            },
            async restore() {
                if (confirm('Confirm restore ?')) {
                    this.$nuxt.$loading.start();
                    this.restoreConfig()
                        .then(() => {
                            this.$nuxt.$loading.finish();
                            this.$fetch();
                        });
                }
            },
            download() {
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
            },
        }
    }
</script>
<style scoped>
    .invisible {
        position: absolute;
        top: -20rem;
    }
</style>