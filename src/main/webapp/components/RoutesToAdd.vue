<template>
    <div class="table">
        <p v-if="routes.length === 0">No routes found</p>
        <div class="row" v-for="(route, index) in routes" :key="route.type + route.method + route.path + route.alt">
            <RouteToAdd :route="route"
                        :groupStart="groupStart(route, index)"
                        @filter="$emit('filter', $event)"
                        @add="$emit('add', $event)"></RouteToAdd>
        </div>
    </div>
</template>
<script>
    import RouteToAdd from "../components/RouteToAdd";

    export default {
        name: "RoutesToAdd",
        data() {
            return {}
        },
        components: {RouteToAdd},
        props: {
            routes: {type: Array}
        },
        methods: {
            groupStart(route, index) {
                return index === 0 || route.group !== this.routes[index - 1].group;
            }
        }
    }
</script>
<style lang="scss" scoped>
    .table {
        & > .row:nth-child(2n+1) {
            background-color: var(--bg-secondary);
        }

        & > .row:nth-child(2n) {
            background-color: var(--bg-primary);
        }
    }
</style>