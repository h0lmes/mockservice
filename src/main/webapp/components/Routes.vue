<template>
    <div>
        <p v-if="routes.length === 0">No routes found</p>
        <div v-for="(route, index) in routes" :key="route.type + route.method + route.path + route.alt">
            <Route :route="route"
                   :index="index"
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
        props: ["routes"],
        methods: {
            groupStart(route, index) {
                return index === 0 || route.group !== this.routes[index - 1].group;
            }
        }
    }
</script>
<style scoped>
</style>