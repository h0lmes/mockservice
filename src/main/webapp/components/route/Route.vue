<template>
    <div class="component component-row monospace"
         :class="{'open' : open, 'disabled' : route.disabled}"
         @click.middle.stop.prevent="edit"
         @keydown.esc.exact="cancel">

        <div v-show="!open" class="mock-col w-fixed-auto">
            <component :is="`icon`" class="component-row-icon grey"/>
        </div>

        <div v-show="selecting && !open" class="mock-col w-fixed-auto">
            <ToggleSwitch class="mock-col-value"
                          :value="selected" @toggle="select"></ToggleSwitch>
        </div>

        <div v-show="editing" class="mock-col w1">
            <div class="mock-col-header">TYPE</div>
            <select class="form-control form-control-sm" v-model="editingData.type">
                <option>REST</option>
                <option>SOAP</option>
            </select>
        </div>

        <div v-show="!testing" class="mock-col w2">
            <div class="mock-col-header">GROUP</div>
            <div v-show="!editing" class="mock-col-value link" @click="filter(route.group)">{{ route.group }}</div>
            <input v-show="editing" type="text" class="form-control form-control-sm" v-model="editingData.group"/>
        </div>

        <div v-show="!testing" class="mock-col w1">
            <div class="mock-col-header">METHOD</div>
            <div v-show="!editing" class="mock-col-value link" @click="filter(route.method)">
                <route-method :value="route.method" :disabled="route.disabled"></route-method>
            </div>
            <select v-show="editing" class="form-control form-control-sm" v-model="editingData.method">
                <option>GET</option>
                <option>POST</option>
                <option>PUT</option>
                <option>PATCH</option>
                <option>DELETE</option>
            </select>
        </div>

        <div v-show="!editing && !testing" class="mock-col w3">
            <div class="mock-col-header">PATH</div>
            <div class="mock-col-value link" @click="filter(route.path)">{{ route.path }}</div>
        </div>

        <div v-show="!editing && !testing" class="mock-col w2">
            <div class="mock-col-header">ALT</div>
            <div class="mock-col-value link" @click="filter(route.alt)">{{ route.alt }}</div>
        </div>

        <div v-show="!testing" class="mock-col w-fixed-auto">
            <div v-show="editing" class="mock-col-header"></div>
            <div class="mock-col-value">
                <ButtonExecute @click="test"></ButtonExecute>
                <ButtonEdit @click="edit"></ButtonEdit>
                <ButtonDelete @click="del"></ButtonDelete>
            </div>
        </div>

        <div v-show="editing" class="mock-col w100">
            <div class="mb-2 color-secondary">PATH</div>
            <input type="text" class="form-control form-control-sm" v-model="editingData.path"
                   placeholder="/api/v1/etc"/>
        </div>

        <div v-show="editing" class="mock-group w100">
            <div class="mock-col w1">
                <div class="mb-2 color-secondary">ALT</div>
                <input type="text" class="form-control form-control-sm"
                       v-model="editingData.alt"
                       placeholder="can contain condition like: id = 1"/>
            </div>
            <div class="mock-col w1">
                <div class="mb-2 color-secondary">RESPONSE CODE</div>
                <input type="text" class="form-control form-control-sm" v-model="editingData.responseCode"
                       placeholder="200"/>
            </div>
        </div>

        <div v-show="editing" class="mock-col w100">
            <div class="mb-2 color-secondary">RESPONSE BODY</div>
            <AutoSizeTextArea v-model="editingData.response" ref="response"
                              placeholder='{"id": 1, "name": "admin"}'>
            </AutoSizeTextArea>
        </div>

        <div v-show="editing" class="mock-col w100 mt-1">
            <ToggleSwitch class="mock-col-value" v-model="showRequestBodySchema">SHOW REQUEST BODY SCHEMA</ToggleSwitch>
        </div>
        <div v-show="editing && showRequestBodySchema" class="mock-col w100">
            <AutoSizeTextArea v-model="editingData.requestBodySchema"
                              :min-rows="1" :max-rows="40"
                              placeholder="REQUEST BODY VALIDATION SCHEMA (JSON)"
            ></AutoSizeTextArea>
        </div>

        <div v-show="editing" class="mock-group w100">
            <div v-show="editing" class="mock-col w100">
                <ToggleSwitch class="mock-col-value" v-model="editingData.triggerRequest">TRIGGER REQUESTS
                </ToggleSwitch>
            </div>
            <div v-show="editing && editingData.triggerRequest" class="mock-col w2">
                <input type="text" class="form-control form-control-sm" v-model="editingData.triggerRequestIds"
                       placeholder="Comma separated request IDs to trigger after this one"/>
            </div>
            <div v-show="editing && editingData.triggerRequest">after (millis)</div>
            <div v-show="editing && editingData.triggerRequest" class="mock-col w1">
                <input type="text" class="form-control form-control-sm" v-model="editingData.triggerRequestDelay"
                       placeholder="Comma separated delays (default is 100)"/>
            </div>
        </div>

        <div v-show="editing" class="mock-col w1">
            <ToggleSwitch class="mock-col-value" v-model="editingData.disabled">DISABLED</ToggleSwitch>
        </div>

        <div v-show="editing" class="mock-col w-fixed-auto">
            <button type="button" class="btn btn-sm btn-primary" @click="save">SAVE</button>
            <button type="button" class="btn btn-sm btn-default" @click="saveAsCopy">SAVE AS COPY</button>
            <button type="button" class="btn btn-sm btn-default" @click="cancel">CANCEL</button>
        </div>

        <div v-if="testing" class="mock-col w100">
            <RouteTester :route="route" @close="testing = false" @edit="edit"></RouteTester>
        </div>
    </div>
</template>
<script>
import {mapActions} from 'vuex';
import RouteTester from "./RouteTester";
import RouteMethod from "./RouteMethod";
import ToggleSwitch from "../other/ToggleSwitch";
import AutoSizeTextArea from "../other/AutoSizeTextArea";
import ButtonDelete from "@/components/other/ButtonDelete";
import ButtonEdit from "@/components/other/ButtonEdit";
import ButtonExecute from "@/components/other/ButtonExecute";
import Icon from '@/assets/icons/star.svg?inline';

export default {
    name: "Route",
    components: {ButtonExecute, ButtonEdit, ButtonDelete, Icon,
        AutoSizeTextArea, RouteTester, RouteMethod, ToggleSwitch},
    data() {
        return {
            editing: false,
            editingData: {},
            testing: false,
            showRequestBodySchema: false,
        }
    },
    props: {
        route: {type: Object, default: {}},
    },
    computed: {
        open() {
            return this.editing || this.testing;
        },
        hasVariables() {
            return this.route.variables && this.route.variables.length > 0;
        },
        selected() {
            if (!this.route) return null;
            return this.route._selected;
        },
        selecting() {
            return this.selected !== undefined && this.selected !== null;
        },
    },
    mounted() {
        if (this.route._new) this.edit();
    },
    methods: {
        ...mapActions({
            filter: 'setApiSearchExpression',
            saveRoute: 'routes/save',
            deleteRoutes: 'routes/delete',
            selectRoute: 'routes/select',
        }),
        select(value) {
            this.selectRoute({route: this.route, selected: value});
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
