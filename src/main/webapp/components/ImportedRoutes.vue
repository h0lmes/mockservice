<template>
    <div class="table">
        <p v-if="importedRoutes.length === 0">No routes</p>
        <div class="row" v-for="(route, index) in importedRoutes" :key="route.method + route.path + route.alt">
            <ImportedRoute
                    :route="route"
                    :existing-routes="existingRoutes"
                    :groupStart="groupStart(route, index)"
                    @add="$emit('add', $event)"
            ></ImportedRoute>
        </div>
    </div>
</template>
<script>
    import ImportedRoute from "../components/ImportedRoute";

    export default {
        name: "ImportedRoutes",
        data() {
            return {}
        },
        components: {ImportedRoute},
        props: {
            importedRoutes: {type: Array},
            existingRoutes: {type: Array},
        },
        methods: {
            groupStart(route, index) {
                return index === 0 || route.group !== this.importedRoutes[index - 1].group;
            },
        }
    }
</script>
<style lang="scss" scoped>
    .table {
        margin: 0;
        width: 100%;
        min-height: 3rem;
    }
</style>