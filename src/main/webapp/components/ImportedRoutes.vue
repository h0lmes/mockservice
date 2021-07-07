<template>
    <div class="table">
        <p v-if="routes.length === 0">No routes</p>
        <div class="row" v-for="(route, index) in routes" :key="route.type + route.method + route.path + route.alt">
            <RouteToAdd :route="route"
                        :groupStart="groupStart(route, index)"
                        @add="$emit('add', $event)"
            ></RouteToAdd>
        </div>
    </div>
</template>
<script>
    import RouteToAdd from "../components/RouteToAdd";

    export default {
        name: "ImportedRoutes",
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
        margin: 0;
        width: 100%;
        min-height: 3rem;
        background-color: var(--bg-page);
        overflow: auto;

        & > .row:nth-child(2n+1) {
            background-color: var(--bg-component);
        }

        & > .row:nth-child(2n) {
            background-color: var(--bg-page);
        }
    }
</style>