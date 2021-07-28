<template>
    <div class="component component-row" :class="{'open' : open}" @click.middle="edit">

        <div class="mock-col">
            <div class="mock-col-header" :class="{'color-accent-one' : groupStart}">GROUP</div>
            <div v-show="!editing" class="mock-col-value link" @click="filter(scenario.group)">{{ scenario.group }}</div>
            <input v-show="editing" type="text" class="form-control form-control-sm monospace" v-model="editingScenario.group"/>
        </div>

        <div class="mock-col">
            <div class="mock-col-header">ALIAS</div>
            <div v-show="!editing" class="mock-col-value link" @click="filter(scenario.alias)">{{ scenario.alias }}</div>
            <input v-show="editing" type="text" class="form-control form-control-sm monospace" v-model="editingScenario.alias"/>
        </div>

        <div class="mock-col">
            <div class="mock-col-header">TYPE</div>
            <div v-show="!editing" class="mock-col-value link" @click="filter(scenario.type)">{{ scenario.type }}</div>
            <select v-show="editing" class="form-control form-control-sm monospace" v-model="editingScenario.type">
                <option>MAP</option>
                <option>QUEUE</option>
                <option>CIRCULAR_QUEUE</option>
            </select>
        </div>

        <div class="mock-col text-center">
            <div v-show="editing" class="mock-col-header"></div>
            <ToggleSwitch v-model="activeSwitch" @toggle="activeToggled()">Active</ToggleSwitch>
        </div>

        <div class="mock-col w-fixed-auto">
            <div v-show="editing" class="mock-col-header"></div>
            <div>
                <button type="button" class="btn btn-sm btn-default" @click="edit">edit</button>
                <button type="button" class="btn btn-sm btn-danger" @click="del">delete</button>
            </div>
        </div>

        <div v-show="editing" class="mock-col w100">
            <div class="mock-col-header">SCENARIO (ROUTES)</div>
            <AutoSizeTextArea v-model="editingScenario.data"></AutoSizeTextArea>
        </div>

        <div v-show="editing" class="mock-col w-fixed-auto">
            <button type="button" class="btn btn-sm btn-default" @click="toggleRoutes">TOGGLE ROUTES</button>
            <button type="button" class="btn btn-sm btn-primary" @click="save">SAVE</button>
            <button type="button" class="btn btn-sm btn-default" @click="saveAsCopy">SAVE AS COPY</button>
            <button type="button" class="btn btn-sm btn-default" @click="cancel">CANCEL</button>
        </div>

        <div v-if="editing && addRoute" class="mock-col w100">
            <RoutesToAdd :routes="routes" @add="add($event)"></RoutesToAdd>
        </div>

    </div>
</template>
<script>
    import {mapActions} from 'vuex';
    import RoutesToAdd from "../components/RoutesToAdd";
    import ToggleSwitch from "./ToggleSwitch";
    import AutoSizeTextArea from "./AutoSizeTextArea";

    export default {
        name: "Scenario",
        components: {AutoSizeTextArea, RoutesToAdd, ToggleSwitch},
        data() {
            return {
                editing: false,
                editingScenario: {},
                addRoute: false,
                activeSwitch: false
            }
        },
        props: {
            scenario: {type: Object},
            active: {type: Boolean},
            groupStart: {type: Boolean}
        },
        computed: {
            routes() {
                return this.$store.state.routes.routes;
            },
            open() {
                return this.editing || this.testing;
            },
        },
        created() {
            this.activeSwitch = this.active;
        },
        watch: {
            active() {
                this.activeSwitch = this.active;
            }
        },
        methods: {
            ...mapActions({
                saveScenario: 'scenarios/save',
                deleteScenario: 'scenarios/delete',
                activateScenario: 'scenarios/activate',
                deactivateScenario: 'scenarios/deactivate',
                fetchRoutes: 'routes/fetch',
            }),
            filter(value) {
                this.$emit('filter', value);
            },
            activeToggled() {
                if (this.activeSwitch) this.activate(); else this.deactivate();
            },
            activate() {
                this.$nuxt.$loading.start();
                this.activateScenario(this.scenario.alias)
                    .then(() => {
                        this.$nuxt.$loading.finish();
                        this.activeSwitch = this.active;
                    });
            },
            deactivate() {
                this.$nuxt.$loading.start();
                this.deactivateScenario(this.scenario.alias)
                    .then(() => {
                        this.$nuxt.$loading.finish();
                        this.activeSwitch = this.active;
                    });
            },
            edit() {
                this.editingScenario = {...this.scenario};
                this.editing = !this.editing;
            },
            cancel() {
                this.editing = false;
                this.editingScenario = {};
            },
            del() {
                if (!!this.scenario._new) {
                    this.deleteScenario(this.scenario);
                    return;
                }
                if (confirm('Sure?')) {
                    this.$nuxt.$loading.start();
                    this.deleteScenario(this.scenario)
                        .then(() => this.$nuxt.$loading.finish());
                }
            },
            save() {
                this.editing = false;
                this.$nuxt.$loading.start();
                this.saveScenario([{...this.scenario, data: ''}, this.editingScenario])
                    .then(() => this.$nuxt.$loading.finish());
            },
            saveAsCopy() {
                this.editing = false;
                this.$nuxt.$loading.start();
                this.saveScenario([{}, this.editingScenario])
                    .then(() => this.$nuxt.$loading.finish());
            },
            add(route) {
                this.editingScenario.data += '\n' + route.method + ';' + route.path + ';' + route.alt;
            },
            toggleRoutes() {
                this.addRoute = !this.addRoute;
            },
        }
    }
</script>
<style scoped>
</style>