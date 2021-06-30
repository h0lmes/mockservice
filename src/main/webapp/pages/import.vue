<template>
    <div class="monospace">
        <div class="component-toolbar mb-4">
            <button type="button" class="btn btn-primary" @click="$fetch()">IMPORT</button>
        </div>
        <AutoSizeTextArea class="mb-4" v-model="value"></AutoSizeTextArea>
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
            }),
            add(route) {
                console.log(route);
            },
        }
    }
</script>
<style scoped>
    .min-height {
        min-height: 2rem;
    }
</style>