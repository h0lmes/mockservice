<template>
    <div class="monospace">

        <div class="component component-toolbar mb-3">
            <button type="button" class="btn btn-sm btn-default mr-3" @click="download">DOWNLOAD</button>
        </div>
        <pre class="form-control form-control-sm smaller">{{ value }}</pre>

        <Loading v-if="$fetchState.pending"></Loading>
    </div>
</template>
<script>
    import {mapActions} from 'vuex';
    import Loading from "../components/Loading";

    export default {
        name: "log",
        components: {Loading},
        data() {
            return {
                value: '',
            }
        },
        async fetch() {
            return this.fetchLog().then(res => this.value = res);
        },
        fetchDelay: 0,
        methods: {
            ...mapActions({fetchLog: 'log/fetch'}),
            download() {
                this.saveTextAsFile(this.value, 'mockservice.log')
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
</style>