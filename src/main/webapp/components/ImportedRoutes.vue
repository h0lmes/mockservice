<template>
    <div class="monospace smaller">
        <p v-if="importedRoutes.length === 0">No routes</p>
        <div v-for="(route, index) in importedRoutes" :key="route.method + route.path + route.alt">
            <div v-if="groupStart(route, index)" class="component-row-group-boundary"></div>
            <ImportedRoute
                    :route="route"
                    :existing-routes="existingRoutes"
                    @add="$emit('add', $event)"
            ></ImportedRoute>
        </div>
    </div>
</template>
<script>
    import ImportedRoute from "../components/ImportedRoute";

    export default {
        name: "ImportedRoutes",
        components: {ImportedRoute},
        data() {
            return {}
        },
        props: {
            importedRoutes: {type: Array},
            existingRoutes: {type: Array},
        },
        methods: {
            groupStart(route, index) {
                return index > 0
                    && route.group !== this.importedRoutes[index - 1].group;
            },
        }
    }
</script>
<style scoped>
</style>