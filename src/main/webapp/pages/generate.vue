<template>
    <div class="monospace">

        <div class="component-toolbar mb-5">
            <button type="button" class="btn btn-default" @click="$fetch()">GENERATE</button>
            <button type="button" class="btn btn-default" @click="copyToClipboard(value)">
                TO CLIPBOARD
                <span v-show="copied">&#9989;</span>
            </button>
            <button type="button" class="btn btn-default" @click="download">DOWNLOAD</button>
        </div>
        <pre class="smaller">{{ value }}</pre>

        <Loading v-if="$fetchState.pending"></Loading>
    </div>
</template>
<script>
    import {mapActions} from 'vuex';
    import Loading from "../components/Loading";
    import copy from "../assets/clipboard";

    export default {
        name: "generate",
        components: {Loading},
        data() {
            return {
                copied: false,
            }
        },
        computed: {
            value() {
                return this.$store.state.generate.value;
            }
        },
        async fetch() {
            this.copied = false;
            return this.generateJson();
        },
        fetchDelay: 0,
        methods: {
            ...mapActions({generateJson: 'generate/json'}),
            download() {
                this.saveTextAsFile(this.value, 'generated.json')
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
            copyToClipboard(text) {
                copy(text).then(
                    () => this.copied = true
                ).catch(
                    console.error
                );
            },
        }
    }
</script>
<style scoped>
</style>