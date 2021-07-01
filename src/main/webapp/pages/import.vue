<template>
    <div class="monospace">
        <input type="file" ref="file_file" id="file_file" @change="openFile()"/>
        <div class="component-toolbar mb-4">
            <button type="button" class="btn btn-default" @click="$refs.file_file.click()">OPEN FILE</button>
            <button type="button" class="btn btn-default" @click="$fetch()">CONVERT</button>
        </div>
        <AutoSizeTextArea class="mb-4" v-model="value"></AutoSizeTextArea>
        <div class="component-toolbar mb-4">
            <button type="button" class="btn btn-default" @click="saveNewRoutes(routes)">ADD ALL</button>
        </div>
        <RoutesToAdd :routes="routes" @add="add($event)"></RoutesToAdd>
        <Loading v-if="$fetchState.pending"></Loading>
    </div>
</template>
<script>
    import {mapActions} from 'vuex';
    import Loading from "../components/Loading";
    import AutoSizeTextArea from "../components/AutoSizeTextArea";
    import RoutesToAdd from "../components/RoutesToAdd";

    export default {
        name: "import",
        components: {AutoSizeTextArea, Loading, RoutesToAdd},
        data() {
            return {
                value: '',
            }
        },
        computed: {
            routes() {
                return this.$store.state.import.routes
            },
        },
        async fetch() {
            return this.importOpenApi3(this.value);
        },
        fetchDelay: 0,
        methods: {
            ...mapActions({
                importOpenApi3: 'import/openapi3',
                saveRoute: 'routes/save',
                saveNewRoutes: 'routes/saveAllNew',
            }),
            add(route) {
                console.log(route);
                this.$nuxt.$loading.start();
                this.saveRoute([{}, route]).then(() => this.$nuxt.$loading.finish());
            },
            openFile() {
                const files = this.$refs.file_file.files;
                if (files && files[0]) {
                    const reader = new FileReader();
                    reader.readAsText(files[0], "UTF-8");
                    reader.onload = (e) => this.value = e.target.result;
                }
            },
        }
    }
</script>
<style scoped>
    .min-height {
        min-height: 2rem;
    }

    input[type=file] {
        width: 0.1px;
        height: 0.1px;
        opacity: 0;
        overflow: hidden;
        position: absolute;
        z-index: -1;
    }
</style>