<template>
    <div class="config-page monospace">
        <div class="mb-4">Variables on server</div>
        <AutoSizeTextArea class="main" v-model="context" :max-rows="maxRows"></AutoSizeTextArea>
        <AutoSizeTextArea class="helper invisible" v-model="context" :min-rows="1" :max-rows="1"></AutoSizeTextArea>
        <div class="component-toolbar mt-4">
            <button type="button" class="btn btn-primary" @click="save">Save variables to server</button>
            <button type="button" class="btn btn-default" @click="download">Download variables as file</button>
        </div>
        <div class="component-row-group-boundary"></div>
        <Loading v-if="$fetchState.pending"></Loading>
    </div>
</template>
<script>
import {mapActions} from 'vuex';
import Loading from "../components/other/Loading";
import AutoSizeTextArea from "../components/other/AutoSizeTextArea";

export default {
        name: "context",
        components: {AutoSizeTextArea, Loading},
        data() {
            return {
                context: '',
                maxRows: 30,
            }
        },
        async fetch() {
            return this.fetchGlobal()
                .then(response => {
                    this.context = response;
                    this.$nextTick(() => this.calcMaxRows());
                });
        },
        fetchDelay: 0,
        methods: {
            ...mapActions({
                fetchGlobal: 'context/fetch',
                saveGlobal: 'context/save',
            }),
            calcMaxRows() {
                const ma = document.querySelector('textarea.main').getBoundingClientRect();
                const ha = document.querySelector('textarea.helper').getBoundingClientRect();
                const lineHeight = (ma.height - ha.height) / (this.maxRows - 1);
                this.maxRows += (window.innerHeight - ma.bottom) / lineHeight - 2;
            },
            async save() {
                if (confirm('Save context?')) {
                    this.$nuxt.$loading.start();
                    this.saveGlobal(this.context)
                        .then(() => this.$nuxt.$loading.finish());
                }
            },
            download() {
                this.saveTextAsFile(this.context, 'context-global.txt')
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
        left: 0;
        right: 0;
    }
</style>
