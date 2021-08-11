<template>
    <div class="monospace">

        <div class="component-toolbar mb-4">
            <button type="button" class="btn btn-primary" @click="$fetch()">Generate JSON</button>
            <button type="button" class="btn btn-default" @click="copyToClipboard(value)">
                Copy to clipboard
                <span v-show="copied">&#9989;</span>
            </button>
            <button type="button" class="btn btn-default" @click="download">Download as file</button>
        </div>
        <AutoSizeTextArea class="mb-4"
                          :placeholder="'Paste JSON schema here (in JSON or YAML format) or leave empty to generate random JSON\nRead more about JSON schema at https://json-schema.org/'"
                          v-model="schema"
        ></AutoSizeTextArea>
        <pre class="smaller">{{ value }}</pre>

        <Loading v-if="$fetchState.pending"></Loading>
    </div>
</template>
<script>
    import {mapActions} from 'vuex';
    import Loading from "../components/Loading";
    import AutoSizeTextArea from "../components/AutoSizeTextArea";
    import copy from "../js/clipboard";

    export default {
        name: "generate",
        components: {Loading, AutoSizeTextArea},
        data() {
            return {
                copied: false,
                schema: ''
            }
        },
        computed: {
            value() {
                return this.$store.state.generate.value;
            }
        },
        async fetch() {
            this.copied = false;
            return this.generateJson(this.schema);
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