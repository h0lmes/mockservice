<template>
    <div class="mock-row">

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
            </select>
        </div>

        <div class="mock-col v-center text-center">
            <ToggleSwitch :id="'disabled' + index"
                          v-model="activeSwitch"
                          @toggle="activeToggled()">Active
            </ToggleSwitch>
        </div>

        <div class="mock-col w-auto">
            <div class="buttons-spacer">
                <button type="button" class="btn btn-sm btn-default" @click="edit">edit</button>
                <button type="button" class="btn btn-sm btn-danger" @click="del">delete</button>
            </div>
        </div>

        <div v-show="editing" class="mock-col w100">
            <textarea class="form-control form-control-sm v-resize monospace" rows="7" v-model="editingScenario.data"></textarea>
        </div>

        <div v-show="editing" class="mock-col w100">
            <button type="button" class="btn btn-sm btn-default" @click="toggleRoutes">TOGGLE ROUTES</button>
            <button type="button" class="btn btn-sm btn-primary" @click="save">SAVE</button>
            <button type="button" class="btn btn-sm btn-primary" @click="saveAsCopy">SAVE AS COPY</button>
            <button type="button" class="btn btn-sm btn-default" @click="cancel">CANCEL</button>
        </div>

        <div v-if="editing && addRoute" class="mock-col w100">
            <div class="routes">
                <RoutesToAdd
                        :routes="routes"
                        @filter="setFilter($event)"
                        @add="add($event)"></RoutesToAdd>
            </div>
        </div>

    </div>
</template>
<script>
    import {mapActions} from 'vuex';
    import RoutesToAdd from "../components/RoutesToAdd";
    import ToggleSwitch from "./ToggleSwitch";

    export default {
        name: "Scenario",
        components: {RoutesToAdd, ToggleSwitch},
        data() {
            return {
                editing: false,
                editingScenario: {},
                addRoute: false,
                activeSwitch: false
            }
        },
        props: {
            index: {type: Number},
            scenario: {type: Object},
            active: {type: Boolean},
            groupStart: {type: Boolean}
        },
        computed: {
            routes() {
                return this.$store.state.routes;
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
            ...mapActions(['saveScenario', 'deleteScenario', 'activateScenario', 'deactivateScenario', 'fetchRoutes']),
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
                if (this.addRoute && (!this.routes || this.routes.length === 0)) {
                    this.$nuxt.$loading.start();
                    this.fetchRoutes()
                        .then(() => this.$nuxt.$loading.finish());
                }
            },
        }
    }
</script>
<style lang="scss" scoped>
    .routes {
        display: block;
        position: relative;
        margin: 0;
        width: 100%;
        min-height: 3rem;
        max-height: 25em;
        background-color: var(--bg-primary);
        border: 1px solid var(--form-control-border);
        border-radius: var(--form-control-border-radius);
        overflow: auto;
    }
</style>