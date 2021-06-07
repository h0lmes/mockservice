<template>
    <div class="monospace">

        <p class="red">Use with caution!</p>
        <p class="red mb-4">It is easy to ruin config by editing it as plain text.</p>
        <textarea class="form-control form-control-sm v-resize" rows="16" v-model="config"></textarea>
        <div class="mt-4 pl-1">
            <div class="btn btn-sm btn-primary mr-3" @click="save">SAVE AND APPLY</div>
            <div class="btn btn-sm btn-default mr-3" @click="download">DOWNLOAD</div>
        </div>

        <Loading v-if="$fetchState.pending"></Loading>
    </div>
</template>
<script>
    import {mapActions} from 'vuex';
    import Loading from "../components/Loading";

    export default {
        name: "config",
        components: {Loading},
        data() {
            return {
                config: ''
            }
        },
        async fetch() {
            return this.fetchConfig()
                .then(response => this.config = response);
        },
        fetchDelay: 0,
        methods: {
            ...mapActions(['fetchConfig', 'saveConfig']),
            async save() {
                if (confirm('Ye be warned =)')) {
                    this.$nuxt.$loading.start();
                    this.saveConfig(this.config)
                        .then(() => this.$nuxt.$loading.finish());
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
            }
        }
    }
</script>
<style scoped>
</style>