<template>
    <div class="monospace">

        <div class="mb-4">Variables on server now</div>
        <AutoSizeTextArea class="main" v-model="context"></AutoSizeTextArea>
        <div class="component-toolbar mt-4">
            <button type="button" class="btn btn-primary" @click="saveGlobal">Save variables to server</button>
            <button type="button" class="btn btn-default" @click="downloadGlobal">Download variables as file</button>
        </div>

        <div class="group-boundary"></div>

        <div class="mb-4">Initialize these variables on server startup</div>
        <AutoSizeTextArea class="main" v-model="initial"></AutoSizeTextArea>
        <div class="component-toolbar mt-4">
            <button type="button" class="btn btn-primary" @click="saveInitial">Save to server</button>
            <button type="button" class="btn btn-default" @click="downloadInitial">Download as file</button>
        </div>

        <div class="group-boundary"></div>

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
            initial: '',
        }
    },
    async fetch() {
        return this.fetchGlobalContext().then(response => {
            this.context = response;
        }).then(await this.fetchInitialContext().then(response => {
            this.initial = response;
        }));
    },
    fetchDelay: 0,
    methods: {
        ...mapActions({
            fetchGlobalContext: 'context/fetch',
            saveGlobalContext: 'context/save',
            fetchInitialContext: 'context/fetchInitial',
            saveInitialContext: 'context/saveInitial',
        }),
        async saveGlobal() {
            if (!confirm('Save global context?')) return;
            this.$nuxt.$loading.start();
            this.saveGlobalContext(this.context).then(() => this.$nuxt.$loading.finish());
        },
        async saveInitial() {
            if (!confirm('Save initial context?')) return;
            this.$nuxt.$loading.start();
            this.saveInitialContext(this.initial).then(() => this.$nuxt.$loading.finish());
        },
        downloadGlobal() {
            this.saveTextAsFile(this.context, 'context-global.txt')
        },
        downloadInitial() {
            this.saveTextAsFile(this.initial, 'context-initial.txt')
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
.group-boundary {
    background: transparent;
    margin: 1.5rem 0;
    padding: 0;
    border-top: 1px solid var(--bg-component-active);
}
</style>
