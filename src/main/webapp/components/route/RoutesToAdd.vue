<template>
    <div class="table">
        <p v-if="routes.length === 0">No routes</p>
        <div class="row" v-for="(route, index) in routes" :key="route.method + route.path + route.alt">
            <RouteToAdd :route="route"
                        :groupStart="groupStart(route, index)"
                        @add="$emit('add', $event)"
            ></RouteToAdd>
        </div>
    </div>
</template>
<script>
    import RouteToAdd from "./RouteToAdd";

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
        margin: 0;
        width: 100%;
        min-height: 3rem;
        max-height: 25em;
        background-color: var(--bg-page);
        border: 1px solid var(--form-control-border);
        border-radius: var(--form-control-border-radius);
        overflow: auto;

        & > .row:nth-child(2n+1) {
            background-color: var(--bg-component);
        }

        & > .row:nth-child(2n) {
            background-color: var(--bg-page);
        }
    }
</style>