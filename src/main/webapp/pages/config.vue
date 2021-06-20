<template>
    <div class="config-page monospace">

        <p class="red">Use with caution!</p>
        <p class="red mb-4">It is easy to ruin config by editing it as plain text.</p>
        <div class="toolbar mb-3">
            <button type="button" class="btn btn-sm btn-primary" @click="save">SAVE AND APPLY</button>
            <button type="button" class="btn btn-sm btn-default" @click="download">DOWNLOAD AS FILE</button>
            <button type="button" class="btn btn-sm btn-default" @click="backup">BACKUP ON SERVER</button>
            <button type="button" class="btn btn-sm btn-danger" @click="restore">RESTORE FROM BACKUP</button>
        </div>
        <textarea id="textarea" class="form-control form-control-sm v-resize monospace" :rows="rows" v-model="config"></textarea>

        <textarea id="hta1" class="form-control form-control-sm invisible" :rows="1"></textarea>
        <textarea id="hta2" class="form-control form-control-sm invisible" :rows="2"></textarea>

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
                oneRowHeight: 16,
                resizeTimeoutId: null,
                rows: 16
            }
        },
        mounted() {
            this.oneRowHeight = document.getElementById('hta2').clientHeight - document.getElementById('hta1').clientHeight;
            window.addEventListener("resize", this.onResize);
            this.$nextTick(function () {
                this.onResize();
            });
        },
        beforeDestroy() {
            window.removeEventListener("resize", this.onResize);
        },
        async fetch() {
            return this.fetchConfig()
                .then(response => this.config = response);
        },
        fetchDelay: 0,
        methods: {
            ...mapActions(['fetchConfig', 'saveConfig', 'backupConfig', 'restoreConfig']),
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
            onResize() {
                clearTimeout(this.resizeTimeoutId);
                this.resizeTimeoutId = setTimeout(() => {
                    this.rows = (
                        window.innerHeight
                        - document.getElementById('textarea').offsetTop
                        - 50
                    ) / this.oneRowHeight;
                }, 500);
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