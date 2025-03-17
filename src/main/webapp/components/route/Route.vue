<template>
    <div class="component component-row monospace"
         :class="{'open' : open, 'disabled' : route.disabled}"
         @click.middle.stop.prevent="edit"
         @keydown.esc.exact="cancel">

        <div v-show="editing" class="mock-col w1">
            <div class="mock-col-header">TYPE</div>
            <select class="form-control form-control-sm" v-model="editingData.type">
                <option>REST</option>
                <option>SOAP</option>
            </select>
        </div>

        <div class="mock-col w2">
            <div class="mock-col-header">GROUP</div>
            <div v-show="!editing" class="mock-col-value link" @click="filter(route.group)">{{ route.group }}</div>
            <input v-show="editing" type="text" class="form-control form-control-sm" v-model="editingData.group"/>
        </div>

        <div class="mock-col w1">
            <div class="mock-col-header">METHOD</div>
            <div v-show="!editing" class="mock-col-value link" @click="filter(route.method)">
                <route-method :value="route.method" :disabled="route.disabled"></route-method>
            </div>
            <select v-show="editing" class="form-control form-control-sm" v-model="editingData.method">
                <option>GET</option>
                <option>POST</option>
                <option>PUT</option>
                <option>DELETE</option>
                <option>PATCH</option>
            </select>
        </div>

        <div v-show="!editing" class="mock-col w3">
            <div class="mock-col-header">PATH</div>
            <div class="mock-col-value link" @click="filter(route.path)">{{ route.path }}</div>
        </div>

        <div v-show="!editing" class="mock-col w2">
            <div class="mock-col-header">ALT</div>
            <div class="mock-col-value link" @click="filter(route.alt)">{{ route.alt }}</div>
        </div>

        <div class="mock-col w-fixed-auto">
            <div v-show="editing" class="mock-col-header"></div>
            <div class="mock-col-value">
                <button type="button" class="btn btn-sm btn-default" :class="{'disabled' : !hasVariables}" @click="vars">vars</button>
                <button type="button" class="btn btn-sm btn-default" @click="edit">edit</button>
                <button type="button" class="btn btn-sm btn-default" @click="test">test</button>
                <button type="button" class="btn btn-sm btn-danger ml-4" @click="del">delete</button>
            </div>
        </div>

        <div v-show="editing" class="mock-col w100">
            <div class="mb-2 color-secondary">PATH</div>
            <input type="text" class="form-control form-control-sm" v-model="editingData.path"/>
        </div>

        <div v-show="editing" class="mock-col w100">
            <div class="mb-2 color-secondary">ALT</div>
            <input type="text" class="form-control form-control-sm" v-model="editingData.alt"
                   placeholder="can contain condition (e.g. some_field.some_inner_field = value)"/>
        </div>

        <div v-show="editing" class="mock-col w100">
            <div class="mb-2 color-secondary">RESPONSE BODY</div>
            <AutoSizeTextArea v-model="editingData.response"
                              :min-rows="1"
                              :max-rows="256"
                              placeholder="RESPONSE BODY"
                              ref="response"
            ></AutoSizeTextArea>
        </div>

        <div v-show="editing" class="mock-col w100">
            <div class="mb-2 color-secondary">RESPONSE CODE</div>
            <input type="text" class="form-control form-control-sm" v-model="editingData.responseCode"/>
        </div>

        <div v-show="editing" class="mock-col w100 mt-1">
            <ToggleSwitch class="mock-col-value" v-model="showRequestBodySchema">SHOW REQUEST BODY SCHEMA</ToggleSwitch>
        </div>
        <div v-show="editing && showRequestBodySchema" class="mock-col w100">
            <AutoSizeTextArea v-model="editingData.requestBodySchema" :min-rows="1" :max-rows="256" placeholder="REQUEST BODY SCHEMA (JSON)"
            ></AutoSizeTextArea>
        </div>

        <div v-show="editing" class="mock-col w100 mt-1">
            <ToggleSwitch class="mock-col-value" v-model="editingData.triggerRequest">TRIGGER REQUESTS</ToggleSwitch>
        </div>

        <div v-show="editing && editingData.triggerRequest" class="mock-col w100">
            <input type="text" class="form-control form-control-sm" v-model="editingData.triggerRequestIds"
                   placeholder="Comma separated request IDs to trigger after this one"/>
        </div>

        <div v-show="editing" class="mock-col w1 mt-1">
            <ToggleSwitch class="mock-col-value" v-model="editingData.disabled">DISABLED</ToggleSwitch>
        </div>

        <div v-show="editing" class="mock-col w-fixed-auto">
            <button type="button" class="btn btn-sm btn-primary" @click="save">SAVE</button>
            <button type="button" class="btn btn-sm btn-default" @click="saveAsCopy">SAVE AS COPY</button>
            <button type="button" class="btn btn-sm btn-default" @click="cancel">CANCEL</button>
        </div>

        <div v-if="testing" class="mock-col w100">
            <RouteTester :route="route" @close="testing = false"></RouteTester>
        </div>

        <div v-if="showVariables" class="mock-col w100 mt-2">
            <RouteVariables :route="route"></RouteVariables>
        </div>
    </div>
</template>
<script>
import {mapActions} from 'vuex';
import RouteTester from "./RouteTester";
import RouteMethod from "./RouteMethod";
import RouteVariables from "./RouteVariables";
import ToggleSwitch from "../other/ToggleSwitch";
import AutoSizeTextArea from "../other/AutoSizeTextArea";

export default {
        name: "Route",
        components: {AutoSizeTextArea, RouteTester, RouteMethod, ToggleSwitch, RouteVariables},
        data() {
            return {
                editing: false,
                editingData: {},
                testing: false,
                showRequestBodySchema: false,
                showVariables: false,
            }
        },
        props: {
            route: {type: Object},
        },
        computed: {
            open() {
                return this.editing || this.testing;
            },
            hasVariables() {
                return this.route.variables && this.route.variables.length > 0;
            },
        },
        mounted() {
            if (this.route._new) {
                this.edit();
            }
        },
        methods: {
            ...mapActions({
                saveRoute: 'routes/save',
                deleteRoutes: 'routes/delete',
            }),
            filter(value) {
                this.$emit('filter', value);
            },
            vars() {
                this.showVariables = !this.showVariables;
            },
            edit() {
                this.testing = false;
                this.editingData = {...this.route};
                this.showRequestBodySchema = !!this.editingData.requestBodySchema;
                if (!this.editing) this.editing = true; else this.cancel();
                if (this.editing) this.$nextTick(() => this.$refs.response.focus());
            },
            cancel() {
                if (!!this.route._new) {
                    this.deleteRoutes([this.route]);
                } else {
                    this.editing = false;
                    this.editingData = {};
                }
            },
            test() {
                this.cancel();
                this.testing = !this.testing;
            },
            del() {
                if (!!this.route._new) {
                    this.deleteRoutes([this.route]);
                } else if (confirm('Sure you want to delete?')) {
                    this.$nuxt.$loading.start();
                    this.deleteRoutes([this.route]).then(() => this.$nuxt.$loading.finish());
                }
            },
            save() {
                this.$nuxt.$loading.start();
                this.saveRoute([this.route, this.editingData]).then(() => {
                    this.$nuxt.$loading.finish();
                    this.editing = false;
                });
            },
            saveAsCopy() {
                this.$nuxt.$loading.start();
                this.saveRoute([{}, this.editingData]).then(() => {
                    this.$nuxt.$loading.finish();
                    this.editing = false;
                });
            },
        }
    }
</script>
<style scoped>
</style>
