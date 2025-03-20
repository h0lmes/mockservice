<template>
    <div>
        <p v-if="entities.length === 0">
            Nothing here yet ¯\_(ツ)_/¯
        </p>
        <div v-for="(entity, index) in sortedEntities" :key="entityKey(entity)">
            <div v-if="groupStart(sortedEntities, entity, index)" class="component-row-group-boundary"></div>
            <Route
                v-if="isRoute(entity)"
                :route="entity"
                @filter="$emit('filter', $event)"
            ></Route>
            <Request
                v-if="isRequest(entity)"
                :request="entity"
                @filter="$emit('filter', $event)"
            ></Request>
            <Scenario
                v-if="isScenario(entity)"
                :scenario="entity"
                @filter="$emit('filter', $event)"
            ></Scenario>
        </div>
    </div>
</template>
<script>
import Route from "./Route";
import Request from "./Request";
import Scenario from "./Scenario";

export default {
        name: "Routes",
        components: {Route, Request, Scenario},
        data() {
            return {}
        },
        props: {
            entities: {type: Array},
        },
        computed: {
            sortedEntities() {
                return this.entities.sort((a, b) => {
                    // newly created entities at the very top
                    let c = this.compare(a._new, b._new);
                    if (c !== 0) return c;

                    // keep same group entities together
                    c = this.compare(a.group, b.group);
                    if (c !== 0) return c;

                    // within a group requests go first
                    if (this.isRequest(a)) {
                        if (!this.isRequest(b)) return -1;
                        // sorted by id
                        return this.compare(a.id, b.id);
                    }

                    // scenarios go next
                    if (this.isScenario(a)) {
                        if (this.isRequest(b)) return 1;
                        if (this.isRoute(b)) return -1;
                        // sorted by alias
                        return this.compare(a.alias, b.alias);
                    }

                    // routes go last
                    let isRouteA = this.isRoute(a);
                    let isRouteB = this.isRoute(b);
                    c = this.compare(isRouteA, isRouteB);
                    if (c !== 0) return c;

                    if (!isRouteA) return 0;

                    c = this.compare(a.type, b.type);
                    if (c !== 0) return c;
                    c = this.compare(a.path, b.path);
                    if (c !== 0) return c;
                    c = this.compare(a.method, b.method);
                    if (c !== 0) return c;
                    return this.compare(a.alt, b.alt);
                });
            },
        },
        methods: {
            compare(a, b) {
                if (a < b) return -1;
                else if (a > b) return 1;
                return 0;
            },
            groupStart(arr, entity, index) {
                return index > 0 && entity.group !== arr[index - 1].group;
            },
            isRoute(entity) {
                return entity.hasOwnProperty('alt');
            },
            isRequest(entity) {
                return entity.hasOwnProperty('id') && !entity.hasOwnProperty('alt');
            },
            isScenario(entity) {
                return entity.hasOwnProperty('alias');
            },
            entityKey(entity) {
                if (this.isRoute(entity)) return entity.method + entity.path + entity.alt;
                if (this.isRequest(entity)) return entity.id;
                return entity.alias;
            },
        }
    }
</script>
<style scoped>
</style>
