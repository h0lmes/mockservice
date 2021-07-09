<template>
    <div class="monospace">
        <input type="file" ref="file_file" id="file_file" @change="openFile()"/>
        <div class="component-toolbar mb-4">
            <button type="button" class="btn btn-primary" @click="$refs.file_file.click()">IMPORT OPENAPI FILE</button>
            <button type="button" class="btn btn-default" @click="addAll()">ADD ALL ROUTES</button>
            <ToggleSwitch v-model="overwrite">Overwrite existing routes</ToggleSwitch>
        </div>
        <ImportedRoutes class="smaller"
                        :imported-routes="importedRoutes"
                        :existing-routes="existingRoutes"
                        @add="add($event)"
        ></ImportedRoutes>
        <Loading v-if="$fetchState.pending"></Loading>
    </div>
</template>
<script>
    import {mapActions} from 'vuex';
    import Loading from "../components/Loading";
    import AutoSizeTextArea from "../components/AutoSizeTextArea";
    import ToggleSwitch from "../components/ToggleSwitch";
    import ImportedRoutes from "../components/ImportedRoutes";

    export default {
        name: "import",
        components: {AutoSizeTextArea, Loading, ToggleSwitch, ImportedRoutes},
        data() {
            return {
                value: '',
                overwrite: false,
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
                this.saveRoute(route).then(() => this.$nuxt.$loading.finish());
            },
            addAll() {
                this.$nuxt.$loading.start();
                this.saveAllRoutes(routes, this.overwrite).then(() => this.$nuxt.$loading.finish());
            },
            openFile() {
                const files = this.$refs.file_file.files;
                if (files && files[0]) {
                    this.$nuxt.$loading.start();
                    const reader = new FileReader();
                    reader.readAsText(files[0], "UTF-8");
                    reader.onload = (e) => {
                        this.$nuxt.$loading.finish();
                        this.value = e.target.result;
                        this.$fetch();
                    };
                    reader.onerror = () => this.$nuxt.$loading.finish();
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