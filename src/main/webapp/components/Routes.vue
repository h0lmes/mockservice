<template>
    <div>
        <p v-if="routes.length === 0">No routes found</p>
        <div v-for="(route, index) in routes" :key="route.method + route.path + route.alt">
            <Route :route="route"
                   :groupStart="groupStart(route, index)"
                   @filter="$emit('filter', $event)"></Route>
        </div>
    </div>
</template>
<script>
    import Route from "../components/Route";

    export default {
        name: "Routes",
        data() {
            return {}
        },
        components: {Route},
        props: {
            routes: {type: Array},
        },
        methods: {
            groupStart(route, index) {
                return index === 0 || route.group !== this.routes[index - 1].group;
            }
        }
    }
</script>
<style scoped>
</style>