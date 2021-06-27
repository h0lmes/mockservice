<template>
    <div class="monospace">

        <div class="component-toolbar mb-5">
            <button type="button" class="btn btn-default" @click="$fetch()">GENERATE</button>
            <button type="button" class="btn btn-default" @click="copyToClipboard(value)">TO CLIPBOARD</button>
            <button type="button" class="btn btn-default" @click="download">DOWNLOAD</button>
        </div>
        <pre class="smaller">{{ value }}</pre>

        <Loading v-if="$fetchState.pending"></Loading>
    </div>
</template>
<script>
    import {mapActions} from 'vuex';
    import Loading from "../components/Loading";

    export default {
        name: "generate",
        components: {Loading},
        data() {
            return {}
        },
        computed: {
            value() {
                return this.$store.state.generate.value;
            }
        },
        async fetch() {
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
            fallbackCopyToClipboard(text) {
                var textArea = document.createElement("textarea");
                textArea.value = text;

                textArea.style.top = "0";
                textArea.style.left = "0";
                textArea.style.position = "fixed";

                document.body.appendChild(textArea);
                textArea.focus();
                textArea.select();

                try {
                    const successful = document.execCommand('copy');
                    if (!successful) {
                        console.log('Fallback copy to clipboard failed');
                    }
                } catch (err) {
                    console.error('Fallback copy to clipboard failed');
                }

                document.body.removeChild(textArea);
            },
            copyToClipboard(text) {
                if (!navigator.clipboard) {
                    this.fallbackCopyToClipboard(text);
                    return;
                }
                navigator.clipboard.writeText(text).catch(
                    () => console.error('Async copy to clipboard failed')
                );
            }
        }
    }
</script>
<style scoped>
</style>