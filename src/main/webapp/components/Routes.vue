<template>
    <div>
        <p v-if="routes.length === 0">No routes</p>
        <div v-for="(route, index) in routes" :key="route.method + route.path + route.alt">
            <div v-if="groupStart(route, index)" class="component-row-group-boundary"></div>
            <Route :route="route"
                   @filter="$emit('filter', $event)"></Route>
        </div>
    </div>
</template>
<script>
    import Route from "../components/Route";

    export default {
        name: "Routes",
        components: {Route},
        data() {
            return {}
        },
        props: {
            routes: {type: Array},
        },
        methods: {
            groupStart(route, index) {
                return index > 0
                    && route.group !== this.routes[index - 1].group;
            },
        }
    }
</script>
<style scoped>
</style>