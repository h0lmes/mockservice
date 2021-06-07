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

        <div class="mock-col">
            <div class="mock-col-header">ACTIVE</div>
            <div class="mock-col-value" :class="{ 'color-accent-one' : active }">{{ active }}</div>
        </div>

        <div class="mock-col w-auto">
            <a class="btn btn-sm btn-primary" @click="edit">edit</a>
            <a class="btn btn-sm btn-default" @click="activate">(re)activate</a>
            <a class="btn btn-sm btn-default" @click="deactivate">deactivate</a>
            <a class="btn btn-sm btn-danger" @click="del">delete</a>
        </div>

        <div v-show="editing" class="mock-col w100">
            <textarea class="form-control form-control-sm v-resize monospace" rows="7" v-model="editingScenario.data"></textarea>
        </div>

        <div v-show="editing" class="mock-col w100">
            <div class="btn btn-sm btn-default" @click="toggleRoutes">TOGGLE ROUTES</div>
            <div class="btn btn-sm btn-primary" @click="save">SAVE</div>
            <div class="btn btn-sm btn-primary" @click="saveAsCopy">SAVE AS COPY</div>
            <div class="btn btn-sm btn-default" @click="cancel">CANCEL</div>
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

    export default {
        name: "Scenario",
        components: {RoutesToAdd},
        data() {
            return {
                editing: false,
                editingScenario: {},
                addRoute: false
            }
        },
        props: {
            scenario: {type: Object},
            active: {type: Boolean},
            groupStart: {type: Boolean}
        },
        async fetch() {
            if (this.addRoute && (!this.routes || this.routes.length === 0)) {
                this.fetchRoutes();
            }
        },
        computed: {
            routes() {
                return this.$store.state.routes
            },
        },
        methods: {
            ...mapActions(['saveScenario', 'deleteScenario', 'activateScenario', 'deactivateScenario', 'fetchRoutes']),
            filter(value) {
                this.$emit('filter', value);
            },
            edit() {
                this.editingScenario = {...this.scenario};
                this.editing = !this.editing;
            },
            cancel() {
                this.editing = false;
            },
            activate() {
                this.$nuxt.$loading.start();
                this.activateScenario(this.scenario.alias)
                    .then(() => this.$nuxt.$loading.finish());
            },
            deactivate() {
                this.$nuxt.$loading.start();
                this.deactivateScenario(this.scenario.alias)
                    .then(() => this.$nuxt.$loading.finish());
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
                this.editingScenario.data += '\n' + route.method + ' ' + route.path + ' ' + route.suffix;
            },
            toggleRoutes() {
                this.addRoute = !this.addRoute;
                if (this.addRoute) this.$fetch();
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