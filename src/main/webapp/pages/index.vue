<template>
    <div class="monospace">
        <div class="component-toolbar mb-3">
            <div class="toolbar-item">
                <input ref="search"
                       type="text"
                       class="form-control monospace"
                       placeholder="type or click on values (group, path, etc)"
                       @keydown.enter.exact.stop="setFilter($event.target.value)"/>
            </div>
            <button type="button" class="toolbar-item-w-fixed-auto btn" @click="setFilter('')">Clear search</button>
            <ToggleSwitch class="toolbar-item toolbar-item-w-fixed-auto" v-model="jsSearch" @toggle="setFilter('')">JS</ToggleSwitch>
        </div>

        <div class="component-toolbar mb-3">
            <button type="button" class="toolbar-item-w-fixed-auto btn" @click="addRoute">Add route</button>
            <button type="button" class="toolbar-item-w-fixed-auto btn" @click="addScenario">Add scenario</button>
            <button type="button" class="toolbar-item-w-fixed-auto btn" @click="addRequest">Add request</button>
            <button type="button" class="toolbar-item-w-fixed-auto btn" @click="addTest">Add test</button>
            <button type="button" class="toolbar-item-w-fixed-auto btn btn-danger mr-3" @click="deleteVisibleRoutes">Delete visible routes</button>
        </div>

        <div class="component-toolbar mb-5">
            <ToggleSwitch class="toolbar-item toolbar-item-w-fixed-auto" v-model="showRoutes">Routes</ToggleSwitch>
            <ToggleSwitch class="toolbar-item toolbar-item-w-fixed-auto" v-model="showScenarios">Scenarios</ToggleSwitch>
            <ToggleSwitch class="toolbar-item toolbar-item-w-fixed-auto" v-model="showRequests">Requests</ToggleSwitch>
            <ToggleSwitch class="toolbar-item toolbar-item-w-fixed-auto" v-model="showTests">Tests</ToggleSwitch>
            <ViewSelector class="toolbar-item toolbar-item-w-fixed-auto"></ViewSelector>
        </div>

        <Routes :entities="filteredEntities" @filter="setFilter($event)"></Routes>

        <div class="color-secondary mt-4">
            <div class="mt-2 bold">Scenario types</div>
            <div class="mt-2">
                - MAP: goes over the LIST OF ROUTES in a scenario top-to-bottom looking for the first match of requested METHOD + PATH pair, returns route with matching ALT. If no match found - uses default behavior (returns route with matching METHOD + PATH and an empty ALT).
            </div>
            <div class="mt-2">
                - QUEUE: same as MAP but looks only at the topmost route, removes route from a queue if it matches. If topmost route does not match (or all routes were already matched and thus removed from a queue) - uses default behavior (returns route with matching METHOD + PATH and an empty ALT).
            </div>
            <div class="mt-2">
                - RING: same as QUEUE but restarts the queue when it depletes.
            </div>
        </div>
        <div class="color-secondary mt-4">
            <div class="mt-2 bold">Tips</div>
            <div class="mt-2">
                Click on any value (GROUP, METHOD, PATH, etc.; you'll see underline on hover) to filter by that value.
            </div>
            <div class="mt-2">
                For more precise filtering enable 'JS' and use Javascript expressions in search field (example: e.group=='default' && !e.disabled).
            </div>
            <div class="mt-2">
                Vars button toggles variables editing mode (if any vars exist in the RESPONSE BODY of the ROUTE).
            </div>
            <div class="mt-2">
                Edit button toggles edit mode.
            </div>
            <div class="mt-2">
                Test button allows for a quick test to see if Route is functional.
            </div>
            <div class="mt-2">
                Middle-click to edit, press ESC in any field to cancel.
            </div>
            <div class="mt-2">
                To alter multiple entities quickly consider navigating to Config page and editing it as plain text.
                Use Backup button there for extra caution.
            </div>
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
        methods: {
            ...mapActions({
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
            deleteVisibleRoutes() {
                if (confirm('Are you sure you want to delete all visible routes?\n'
                    + 'Note: if filter (search) is applied - you only delete those you see on the screen.')) {
                    this.$nuxt.$loading.start();
                    this.deleteRoutes(
                        this.filteredEntities.filter(e => e.hasOwnProperty('alt'))
                    ).then(() => this.$nuxt.$loading.finish());
                }
            },
            setFilter(value) {
                this.$refs.search.value = value.trim();
                this.query = value.trim();
            },
            getSearchFn() {
                if (this.jsSearch) {
                    return Function("e", "return " + this.query + ";");
                } else if (!this.query) {
                    return (e) => true;
                } else {
                    const query = this.query;
                    return (e) => {
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
</style>
