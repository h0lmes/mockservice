<template>
    <div class="component component-row monospace"
         :class="{'open' : open}"
         @click.middle.stop.prevent="edit"
         @keydown.esc.exact="cancel">

        <div class="mock-col w2">
            <div class="mock-col-header">GROUP</div>
            <div v-show="!editing" class="mock-col-value link" @click="filter(scenario.group)">{{ scenario.group }}</div>
            <input v-show="editing" type="text" class="form-control form-control-sm" v-model="editingScenario.group"/>
        </div>

        <div class="mock-col w1">
            <div class="mock-col-header">SCENARIO TYPE</div>
            <div v-show="!editing" class="mock-col-value link" @click="filter(scenario.type)">{{ scenario.type }}</div>
            <select v-show="editing" class="form-control form-control-sm" v-model="editingScenario.type">
                <option>MAP</option>
                <option>QUEUE</option>
                <option>RING</option>
            </select>
        </div>

        <div class="mock-col w3">
            <div class="mock-col-header">SCENARIO ALIAS</div>
            <div v-show="!editing" class="mock-col-value link" @click="filter(scenario.alias)">{{ scenario.alias }}</div>
            <input v-show="editing" type="text" class="form-control form-control-sm" v-model="editingScenario.alias"/>
        </div>

        <div class="mock-col w2 text-center">
            <div v-show="editing" class="mock-col-header"></div>
            <ToggleSwitch v-model="activeSwitch" @toggle="activeToggled()">Active</ToggleSwitch>
        </div>

        <div class="mock-col w-fixed-auto">
            <div v-show="editing" class="mock-col-header"></div>
            <div class="mock-col-value">
                <button type="button" class="btn btn-sm disabled">&#xA0;&#xA0;&#xA0;&#xA0;</button>
                <button type="button" class="btn btn-sm btn-default" @click="edit">edit</button>
                <button type="button" class="btn btn-sm disabled">&#xA0;&#xA0;&#xA0;&#xA0;</button>
                <button type="button" class="btn btn-sm btn-danger ml-2" @click="del">delete</button>
            </div>
        </div>

        <div v-show="editing" class="mock-col w100">
            <div class="mb-2 color-secondary">LIST OF ROUTES</div>
            <AutoSizeTextArea v-model="editingScenario.data"
                              placeholder="click SHOW ROUTES to add routes; or just type them in as METHOD;PATH;ALT"
                              :min-rows="1"
                              :max-rows="22"
                              ref="data"
            ></AutoSizeTextArea>
        </div>

        <div v-show="editing" class="mock-col w1 mt-1">
            <ToggleSwitch class="mock-col-value" v-model="showRoutes">SHOW ROUTES</ToggleSwitch>
        </div>
        <div v-show="editing" class="mock-col w-fixed-auto">
            <button type="button" class="btn btn-sm btn-primary" @click="save">SAVE</button>
            <button type="button" class="btn btn-sm btn-default" @click="saveAsCopy">SAVE AS COPY</button>
            <button type="button" class="btn btn-sm btn-default" @click="cancel">CANCEL</button>
        </div>

        <div v-if="editing && showRoutes" class="mock-col w100">
            <RoutesToAdd :routes="routes" @add="add($event)"></RoutesToAdd>
        </div>

    </div>
</template>
<script>
import {mapActions} from 'vuex';
import RoutesToAdd from "./RoutesToAdd";
import ToggleSwitch from "../other/ToggleSwitch";
import AutoSizeTextArea from "../other/AutoSizeTextArea";

export default {
        name: "Scenario",
        components: {AutoSizeTextArea, RoutesToAdd, ToggleSwitch},
        data() {
            return {
                editing: false,
                editingScenario: {},
                activeSwitch: false,
                showRoutes: false,
            }
        },
        props: {
            scenario: {type: Object},
        },
        computed: {
            routes() {
                return this.$store.state.routes.routes;
            },
            open() {
                return this.editing || this.testing;
            },
            active() {
                return this.scenario.active;
            }
        },
        created() {
            this.activeSwitch = this.active;
        },
        mounted() {
            if (this.scenario._new) {
                this.edit();
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
                if (!this.editing) this.editing = true; else this.cancel();
                if (this.editing) this.$nextTick(() => this.$refs.data.focus());
            },
            cancel() {
                if (!!this.scenario._new) {
                    this.deleteScenario(this.scenario);
                } else {
                    this.editing = false;
                    this.editingScenario = {};
                }
            },
            del() {
                if (!!this.scenario._new) {
                    this.deleteScenario(this.scenario);
                    return;
                }
                if (confirm('Sure you want to delete?')) {
                    this.$nuxt.$loading.start();
                    this.deleteScenario(this.scenario)
                        .then(() => this.$nuxt.$loading.finish());
                }
            },
            save() {
                this.$nuxt.$loading.start();
                this.saveScenario([this.scenario, this.editingScenario])
                    .then(() => {
                        this.$nuxt.$loading.finish();
                        this.editing = false;
                    });
            },
            saveAsCopy() {
                this.$nuxt.$loading.start();
                this.editingScenario.id = '';
                this.saveScenario([{}, this.editingScenario])
                    .then(() => {
                        this.$nuxt.$loading.finish();
                        this.editing = false;
                    });
            },
            add(route) {
                this.editingScenario.data = this.editingScenario.data || '';
                if (!!this.editingScenario.data) this.editingScenario.data += '\n';
                this.editingScenario.data += route.method + ';' + route.path + ';' + route.alt;
                this.$refs.data.focus();
            },
        }
    }
</script>
<style scoped>
</style>
