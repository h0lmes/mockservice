<template>
    <div style="width: 100%">
        <p v-if="entities.length === 0">
            Nothing here ¯\_(ツ)_/¯
        </p>
        <div v-for="(entity, index) in sortedEntities" :key="entityKey(entity)">
            <div v-if="groupStart(sortedEntities, entity, index)" class="component-row-group-boundary"></div>

            <Route v-if="isRoute(entity)" :route="entity"></Route>
            <Scenario v-if="isScenario(entity)" :scenario="entity"></Scenario>
            <Request v-if="isRequest(entity)" :request="entity"></Request>
            <Test v-if="isTest(entity)" :test="entity"></Test>
        </div>
    </div>
</template>
<script>
import Route from "./Route";
import Request from "./Request";
import Scenario from "./Scenario";
import Test from "./Test";
import {_isRequest, _isRoute, _isScenario, _isTest} from "@/js/common";

export default {
    name: "Routes",
    components: {Route, Request, Scenario, Test},
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

                // within a group tests go first
                if (_isTest(a)) {
                    if (!_isTest(b)) return -1;
                    // sorted by alias
                    return this.compare(a.alias, b.alias);
                }

                // requests
                if (_isRequest(a)) {
                    if (_isTest(b)) return 1;
                    if (!_isRequest(b)) return -1;
                    // sorted by id
                    return this.compare(a.id, b.id);
                }

                // scenarios
                if (_isScenario(a)) {
                    if (_isTest(b)) return 1;
                    if (_isRequest(b)) return 1;
                    if (_isRoute(b)) return -1;
                    // sorted by alias
                    return this.compare(a.alias, b.alias);
                }

                // routes go last
                let isRouteA = _isRoute(a);
                let isRouteB = _isRoute(b);
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
        isRoute(e) {
            return _isRoute(e);
        },
        isRequest(e) {
            return _isRequest(e);
        },
        isScenario(e) {
            return _isScenario(e);
        },
        isTest(e) {
            return _isTest(e);
        },
        entityKey(entity) {
            if (_isRoute(entity)) return entity.method + entity.path + entity.alt;
            if (_isRequest(entity)) return entity.id;
            return entity.alias;
        },
    }
}
</script>
<style scoped>
</style>
