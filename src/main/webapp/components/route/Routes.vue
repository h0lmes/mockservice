<template>
    <div>
        <p v-if="entities.length === 0">
            Nothing here ¯\_(ツ)_/¯
        </p>
        <div v-for="(entity, index) in sortedEntities" :key="entityKey(entity)">
            <div v-if="groupStart(sortedEntities, entity, index)" class="component-row-group-boundary"></div>
            <Route
                    v-if="isRoute(entity)"
                    :route="entity"
                    @filter="$emit('filter', $event)"
            ></Route>
            <Scenario
                    v-else
                    :scenario="entity"
                    @filter="$emit('filter', $event)"
            ></Scenario>
        </div>
    </div>
</template>
<script>
    import Route from "./Route";
    import Scenario from "./Scenario";

    export default {
        name: "Routes",
        components: {Route, Scenario},
        data() {
            return {}
        },
        props: {
            entities: {type: Array},
        },
        computed: {
            sortedEntities() {
                let compare = function(a, b) {
                    if (a < b) return -1;
                    else if (a > b) return 1;
                    return 0;
                };
                return this.entities.sort((a, b) => {
                    let c;
                    c = compare(a._new, b._new);
                    if (c !== 0) return c;
                    c = compare(a.group, b.group);
                    if (c !== 0) return c;

                    let isRouteA = a.hasOwnProperty('alt');
                    let isRouteB = b.hasOwnProperty('alt');
                    c = compare(isRouteA, isRouteB);
                    if (c !== 0) return c;

                    if (!isRouteA) return 0;

                    c = compare(a.type, b.type);
                    if (c !== 0) return c;
                    c = compare(a.path, b.path);
                    if (c !== 0) return c;
                    c = compare(a.method, b.method);
                    if (c !== 0) return c;
                    return compare(a.alt, b.alt);
                });
            },
        },
        methods: {
            groupStart(arr, entity, index) {
                return index > 0 && entity.group !== arr[index - 1].group;
            },
            isRoute(entity) {
                return entity.hasOwnProperty('alt');
            },
            entityKey(entity) {
                return this.isRoute(entity) ? entity.method + entity.path + entity.alt : entity.alias;
            },
        }
    }
</script>
<style scoped>
</style>