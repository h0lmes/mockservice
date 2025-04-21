<template>
    <div class="monospace">
        <div class="component-toolbar mb-3">
            <div class="toolbar-item">
                <input ref="search"
                       type="text"
                       class="form-control monospace"
                       placeholder="type in or click on values (group, path, etc)"
                       @keydown.enter.exact.stop="filter($event.target.value)"/>
            </div>
            <button type="button" class="toolbar-item-w-fixed-auto btn" @click="filter('')">Clear search</button>
            <ToggleSwitch class="toolbar-item toolbar-item-w-fixed-auto" v-model="jsSearch">JS</ToggleSwitch>
        </div>

        <div class="component-toolbar mb-3">
            <ToggleSwitch class="toolbar-item toolbar-item-w-fixed-auto" v-model="showRoutes">Routes</ToggleSwitch>
            <ToggleSwitch class="toolbar-item toolbar-item-w-fixed-auto" v-model="showScenarios">Scenarios</ToggleSwitch>
            <ToggleSwitch class="toolbar-item toolbar-item-w-fixed-auto" v-model="showRequests">Requests</ToggleSwitch>
            <ToggleSwitch class="toolbar-item toolbar-item-w-fixed-auto" v-model="showTests">Tests</ToggleSwitch>
            <ViewSelector class="toolbar-item toolbar-item-w-fixed-auto"></ViewSelector>
        </div>

        <div class="component-toolbar mb-5">
            <button type="button" class="toolbar-item-w-fixed-auto btn" @click="addRoute">Add route</button>
            <button type="button" class="toolbar-item-w-fixed-auto btn" @click="addScenario">Add scenario</button>
            <button type="button" class="toolbar-item-w-fixed-auto btn" @click="addRequest">Add request</button>
            <button type="button" class="toolbar-item-w-fixed-auto btn" @click="addTest">Add test</button>
            <button type="button" class="toolbar-item-w-fixed-auto btn btn-danger mr-3" @click="deleteVisibleRoutes">Delete visible routes</button>
        </div>

        <Routes :entities="filteredEntities"></Routes>

        <div class="color-secondary mt-6 text-indent">
            <div class="mt-2 bold">Tips</div>
            <p class="mt-2">
                - Click on any value (GROUP, METHOD, PATH, etc.; you'll see underline on hover) to filter by that value.
            </p>
            <p class="mt-2">
                - For more precise filtering enable 'JS' and use Javascript expressions in search field (example: e.group=='default' && !e.disabled).
            </p>
            <p class="mt-2">
                - Middle-click to edit, press ESC in any field to cancel.
            </p>
            <p class="mt-2">
                - To alter multiple entities quickly consider navigating to Config page and editing it as plain text.
                Use Backup button there for extra caution.
            </p>
        </div>
        <div class="color-secondary mt-6 text-indent">
            <div class="mt-2 bold">Test plan syntax</div>
            <p class="mt-2">
                - Each line is exactly one command.
            </p>
            <p class="mt-2">
                - x = some string - set value of variable 'x' in global context (see Context page).
            </p>
            <p class="mt-2">
                - x = ${item.capacity} - set value of variable 'x' in global context, value comes from variable 'item.capacity' from latest request response (assuming latest response was like <span>{"item": {"capacity": 100}}</span>) or from global context.
            </p>
            <p class="mt-2">
                - GET localhost:8081/products/${id} - execute inline request; variable comes from global context.
            </p>
            <p class="mt-2">
                - POST localhost:8081/v2/store/order {"x": 400} -> 400,404 - execute inline request and test status code is 400 or 404; if test fails - test run stops with FAILED.
            </p>
            <p class="mt-2">
                - request_id - execute request with ID 'request_id'.
            </p>
            <p class="mt-2">
                - request_id -> 200 - execute request with ID 'request_id' and test status code is 200; if test fails - test run stops with FAILED.
            </p>
            <p class="mt-2">
                - x == 2 - non-strictly test variable 'x' from the latest request response; if test fails - test run proceeds with WARNING.
            </p>
            <p class="mt-2">
                - x === some value - strictly test variable 'x' from the latest request response; if test fails - test run stops with FAILED.
            </p>
        </div>
        <div class="color-secondary mt-6 text-indent">
            <div class="mt-2 bold">Scenario types</div>
            <p class="mt-2">
                - MAP: goes over the LIST OF ROUTES in a scenario top-to-bottom looking for the first match of requested METHOD + PATH pair, returns route with matching ALT. If no match found - uses default behavior (returns route with matching METHOD + PATH and an empty ALT).
            </p>
            <p class="mt-2">
                - QUEUE: same as MAP but looks only at the topmost route, removes route from a queue if it matches. If topmost route does not match (or all routes were already matched and thus removed from a queue) - uses default behavior (returns route with matching METHOD + PATH and an empty ALT).
            </p>
            <p class="mt-2">
                - RING: same as QUEUE but restarts the queue when it depletes.
            </p>
        </div>

        <Loading v-if="$fetchState.pending"></Loading>
    </div>
</template>
<script>
import {mapActions} from 'vuex';
import Routes from "../components/route/Routes";
import Loading from "../components/other/Loading";
import ViewSelector from "../components/other/ViewSelector";
import ToggleSwitch from "../components/other/ToggleSwitch";

export default {
    name: "index",
    components: {Routes, Loading, ViewSelector, ToggleSwitch},
    data() {
        return {
            query: '',
            timeout: null,
            showRoutes: true,
            showRequests: true,
            showScenarios: true,
            showTests: true,
            jsSearch: false,
        }
    },
    async fetch() {
        return this.fetchRoutes()
            .then(this.fetchRequests())
            .then(this.fetchScenarios())
            .then(this.fetchTests());
    },
    fetchDelay: 0,
    computed: {
        searchExpression() {
            return (this.$store.state.apiSearchExpression || '').trim();
        },
        routes() {
            return this.showRoutes ? this.$store.state.routes.routes : [];
        },
        requests() {
            return this.showRequests ? this.$store.state.requests.requests : [];
        },
        scenarios() {
            return this.showScenarios ? this.$store.state.scenarios.scenarios : [];
        },
        tests() {
            return this.showTests ? this.$store.state.tests.tests : [];
        },
        entities() {
            return [...this.routes, ...this.requests, ...this.scenarios, ...this.tests];
        },
        filteredEntities() {
            if (!this.query) return this.entities;

            try {
                return this.entities.filter(this.getSearchFn());
            } catch (e) {
                console.error(e);
                return [];
            }
        },
    },
    mounted() {
        this.$refs.search.value = this.searchExpression;
        this.query = this.searchExpression;
    },
    watch: {
        searchExpression(newValue) {
            this.$refs.search.value = newValue;
            this.query = newValue;
        },
    },
    methods: {
        ...mapActions({
            filter: 'setApiSearchExpression',

            fetchRoutes: 'routes/fetch',
            addRouteAction: 'routes/add',
            deleteRoutes: 'routes/delete',

            fetchRequests: 'requests/fetch',
            addRequestAction: 'requests/add',

            fetchScenarios: 'scenarios/fetch',
            addScenarioAction: 'scenarios/add',

            fetchTests: 'tests/fetch',
            addTestAction: 'tests/add',
        }),
        addRoute() {
            this.showRoutes = true
            this.addRouteAction()
        },
        addRequest() {
            this.showRequests = true
            this.addRequestAction()
        },
        addScenario() {
            this.showScenarios = true
            this.addScenarioAction()
        },
        addTest() {
            this.showTests = true
            this.addTestAction()
        },
        async deleteVisibleRoutes() {
            if (confirm('Are you sure you want to delete all visible routes?\n'
                + 'Note: if filter (search) is applied - you only delete those you see on the screen.')) {
                this.$nuxt.$loading.start();
                await this.deleteRoutes(
                    this.filteredEntities.filter(e => e.hasOwnProperty('alt'))
                ).then(() => this.$nuxt.$loading.finish());
            }
        },
        getSearchFn() {
            if (this.jsSearch) {
                return Function("e", "return " + this.query + ";");
            } else if (!this.query) {
                return e => true;
            } else {
                const query = this.query;
                return e => {
                    if (e.method && e.path && e.id) {
                        return e.group.includes(query)
                            || e.type.includes(query)
                            || e.method.includes(query)
                            || e.path.includes(query)
                            || e.id.includes(query);
                    } else if (e.method && e.path) {
                        return e.group.includes(query)
                            || e.type.includes(query)
                            || e.method.includes(query)
                            || e.path.includes(query)
                            || e.alt.includes(query);
                    } else if (e.alias && e.plan) {
                        return e.group.includes(query)
                            || e.alias.includes(query);
                    } else {
                        return e.group.includes(query)
                            || e.alias.includes(query)
                            || e.type.includes(query);
                    }
                };
            }
        }
    }
}
</script>
<style scoped>
.text-indent > p {
    margin-left: 1rem;
    text-indent: -1.1rem;
}
</style>
