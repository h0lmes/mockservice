<template>
    <div class="monospace">
        <input type="file" ref="file" id="file_file" @change="openFile()"/>
        <div class="component-toolbar mb-4">
            <div>{{ fileName }}</div>
            <button type="button" class="btn btn-primary" @click="selectFile()">Select Open API file</button>
            <button type="button" class="btn btn-default" @click="addAll()">Add all routes</button>
            <ToggleSwitch v-model="overwrite">Overwrite existing routes</ToggleSwitch>
        </div>
        <ImportedRoutes :imported-routes="importedRoutes"
                        :existing-routes="existingRoutes"
                        @add="add($event)"
        ></ImportedRoutes>
        <div class="color-secondary mt-4 smaller">(click route to expand)</div>
        <Loading v-if="$fetchState.pending"></Loading>
    </div>
</template>
<script>
    import {mapActions} from 'vuex';
    import Loading from "../components/Loading";
    import ToggleSwitch from "../components/ToggleSwitch";
    import ImportedRoutes from "../components/ImportedRoutes";

    export default {
        name: "import",
        components: {Loading, ToggleSwitch, ImportedRoutes},
        data() {
            return {
                value: '',
                overwrite: false,
                fileName: ''
            }
        },
        computed: {
            importedRoutes() {
                return this.$store.state.import.routes;
            },
            existingRoutes() {
                return this.$store.state.routes.routes;
            },
        },
        async fetch() {
            if (!this.value) {
                return Promise.resolve();
            }
            return this.import(this.value);
        },
        fetchDelay: 0,
        methods: {
            ...mapActions({
                import: 'import/import',
                fetchRoutes: 'routes/fetch',
                saveRoute: 'routes/save',
                saveAllRoutes: 'routes/saveAll',
            }),
            add(route) {
                this.$nuxt.$loading.start();
                this.saveRoute(route)
                    .then(() => this.$nuxt.$loading.finish());
            },
            addAll() {
                if (this.importedRoutes) {
                    this.$nuxt.$loading.start();
                    this.saveAllRoutes(this.importedRoutes, this.overwrite)
                        .then(() => this.$nuxt.$loading.finish());
                }
            },
            selectFile() {
                this.fileName = '';
                this.$refs.file.value = null;
                this.$refs.file.click()
            },
            openFile() {
                const files = this.$refs.file.files;
                if (files && files[0]) {
                    this.fileName = 'Loading...';
                    this.$nuxt.$loading.start();
                    const reader = new FileReader();
                    reader.readAsText(files[0], "UTF-8");
                    reader.onload = (e) => {
                        this.$nuxt.$loading.finish();
                        this.value = e.target.result;
                        this.$fetch();
                        this.fileName = files[0].name;
                    };
                    reader.onerror = () => {
                        this.$nuxt.$loading.finish();
                        this.fileName = '[Error]';
                    }
                }
            },
        }
    }
</script>
<style scoped>
    input[type=file] {
        width: 0.1px;
        height: 0.1px;
        opacity: 0;
        overflow: hidden;
        position: absolute;
        z-index: -1;
    }
</style>