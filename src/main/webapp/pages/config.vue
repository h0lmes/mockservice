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
    export default {
        name: "config",
        data() {
            return {
                config: ''
            }
        },
        async fetch() {
            await this.fetchConfig();
            this.config = this.$store.state.config;
        },
        methods: {
            ...mapActions({
                fetchConfig: 'fetchConfig',
                saveConfig: 'saveConfig'
            }),
            save() {
                if (confirm('Ye be warned =)')) {
                    this.saveConfig(this.config);
                }
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