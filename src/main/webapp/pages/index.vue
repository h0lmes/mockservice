<template>
    <div class="monospace">
        <div class="component-toolbar mb-5">
            <div class="toolbar-item">
                <input ref="search"
                       type="text"
                       class="form-control monospace"
                       placeholder="type or click on values (group, path, etc)"
                       @keydown.enter.exact.stop="setFilter($event.target.value)"/>
            </div>
            <ToggleSwitch class="toolbar-item toolbar-item-w-fixed-auto" v-model="jsSearch" @toggle="setFilter('')">JS</ToggleSwitch>
            <div>
                <button type="button" class="toolbar-item-w-fixed-auto btn" @click="setFilter('')">Clear search</button>
                <button type="button" class="toolbar-item-w-fixed-auto btn" @click="addRoute">Add route</button>
                <button type="button" class="toolbar-item-w-fixed-auto btn" @click="addScenario">Add scenario</button>
                <button type="button" class="toolbar-item-w-fixed-auto btn btn-danger" @click="deleteVisibleRoutes">Delete visible routes</button>
            </div>
        </div>

        <div class="component-toolbar mb-5">
            <ViewSelector class="toolbar-item toolbar-item-w-fixed-auto"></ViewSelector>
            <ToggleSwitch class="toolbar-item toolbar-item-w-fixed-auto" v-model="showRoutes">Routes</ToggleSwitch>
            <ToggleSwitch class="toolbar-item toolbar-item-w-fixed-auto" v-model="showScenarios">Scenarios</ToggleSwitch>
        </div>

        <Routes :entities="filteredEntities" @filter="setFilter($event)"></Routes>

        <div class="color-secondary mt-4">
            <div class="mt-2 bold">Scenario types</div>
            <div class="mt-2">
                MAP: searches LIST OF ROUTES top-to-bottom for the requested METHOD + PATH, returns the first match. If no match - returns route with matching METHOD + PATH and an empty ALT.
            </div>
            <div class="mt-2">
                QUEUE: same as MAP but tries to match only the topmost route, removes matched route from the queue.
            </div>
            <div class="mt-2">
                CIRCULAR_QUEUE: same as QUEUE; auto-restarts queue when it depletes.
            </div>
        </div>
        <div class="color-secondary mt-4">
            <div class="mt-2 bold">Tips</div>
            <div class="mt-2">
                Vars button toggles variables editing mode (if any vars exist in the response body).
            </div>
            <div class="mt-2">
                Edit button toggles edit mode; same for the Test button.
            </div>
            <div class="mt-2">
                Middle-click route or scenario to edit, press Esc in any field to cancel.
            </div>
            <div class="mt-2">
                For more precise filtering enable 'JS' and use Javascript expressions in search field (example: e.group=='default' && !e.disabled).
            </div>
            <div class="mt-2">
                To alter multiple routes or scenarios quickly consider navigating to Config page and editing it as plain text.
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
                showScenarios: true,
                jsSearch: false,
            }
        },
        async fetch() {
            return this.fetchRoutes()
                .then(this.fetchScenarios());
        },
        fetchDelay: 0,
        computed: {
            routes() {
                return this.showRoutes ? this.$store.state.routes.routes : [];
            },
            scenarios() {
                return this.showScenarios ? this.$store.state.scenarios.scenarios : [];
            },
            entities() {
                return [...this.routes, ...this.scenarios];
            },
            filteredEntities() {
                if (!this.query) {
                    return this.entities;
                }

                let searchFn;
                if (this.jsSearch) {
                    searchFn = Function("e", "return " + this.query + ";");
                } else {
                    const query = this.query;
                    searchFn = function (e) {
                        if (e.method && e.path) {
                            return e.group.includes(query)
                                || e.type.includes(query)
                                || e.method.includes(query)
                                || e.path.includes(query)
                                || e.alt.includes(query);
                        } else {
                            return e.group.includes(query)
                                || e.alias.includes(query)
                                || e.type.includes(query);
                        }
                    }
                }
                try {
                    return this.entities.filter(searchFn);
                } catch (e) {
                    console.error(e);
                    return [];
                }
            },
            filteredRoutes() {
                return this.filteredEntities.filter(e => e.hasOwnProperty('alt'));
            },
        },
        methods: {
            ...mapActions({
                fetchRoutes: 'routes/fetch',
                addRouteAction: 'routes/add',
                deleteRoutes: 'routes/delete',
                fetchScenarios: 'scenarios/fetch',
                addScenarioAction: 'scenarios/add',
            }),
            addRoute() {
                this.showRoutes = true
                this.addRouteAction()
            },
            addScenario() {
                this.showScenarios = true
                this.addScenarioAction()
            },
            deleteVisibleRoutes() {
                if (confirm('Are you sure you want to delete all visible routes?\n'
                    + 'Note: if filtered you only delete those you see on the screen.')) {
                    this.$nuxt.$loading.start();
                    this.deleteRoutes(this.filteredRoutes)
                        .then(() => this.$nuxt.$loading.finish());
                }
            },
            setFilter(value) {
                this.$refs.search.value = value.trim();
                this.query = value.trim();
            },
        }
    }
</script>
<style scoped>
</style>
