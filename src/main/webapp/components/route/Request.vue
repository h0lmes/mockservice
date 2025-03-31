<template>
    <div class="component component-row monospace"
         :class="{'open' : open, 'disabled' : request.disabled}"
         @click.middle.stop.prevent="edit"
         @keydown.esc.exact="cancel">

        <div v-show="editing" class="mock-col w1">
            <div class="mock-col-header">TYPE</div>
            <select class="form-control form-control-sm" v-model="editingData.type">
                <option>REST</option>
            </select>
        </div>

        <div class="mock-col w2">
            <div class="mock-col-header">GROUP</div>
            <div v-show="!editing" class="mock-col-value link" @click="filter(request.group)">{{ request.group }}</div>
            <input v-show="editing" type="text" class="form-control form-control-sm" v-model="editingData.group"/>
        </div>

        <div class="mock-col w1">
            <div class="mock-col-header">METHOD</div>
            <div v-show="!editing" class="mock-col-value link" @click="filter(request.method)">
                <route-method :value="request.method" :disabled="request.disabled"></route-method>
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
            <div class="mock-col-header">URL</div>
            <div class="mock-col-value link" @click="filter(request.path)">{{ request.path }}</div>
        </div>

        <div v-show="editing" class="mock-col w1">
            <div class="mock-col-header">ID</div>
            <input type="text" class="form-control form-control-sm" v-model="editingData.id"
                   placeholder="(empty to generate)"/>
        </div>
        <div v-show="!editing" class="mock-col w2">
            <div class="mock-col-header">ID</div>
            <div class="mock-col-value link" @click="filter(request.id)">{{ request.id }}</div>
        </div>

        <div class="mock-col w-fixed-auto">
            <div v-show="editing" class="mock-col-header"></div>
            <div class="mock-col-value">
                <button type="button" class="btn btn-sm btn-default" :class="{'disabled' : !hasVariables}" @click="vars">vars</button>
                <button type="button" class="btn btn-sm btn-default" @click="edit">edit</button>
                <button type="button" class="btn btn-sm btn-primary" @click="test">send</button>
                <button type="button" class="btn btn-sm btn-danger ml-2" @click="del">delete</button>
            </div>
        </div>

        <div v-show="editing" class="mock-col w100">
            <div class="mb-2 color-secondary">URL</div>
            <input type="text" class="form-control form-control-sm" v-model="editingData.path"/>
        </div>

        <div v-show="editing" class="mock-col w100">
            <div class="mb-2 color-secondary">REQUEST HEADERS</div>
            <AutoSizeTextArea v-model="editingData.headers"
                              :min-rows="1"
                              :max-rows="22"
            ></AutoSizeTextArea>
        </div>

        <div v-show="editing" class="mock-col w100">
            <div class="mb-2 color-secondary">REQUEST BODY</div>
            <AutoSizeTextArea v-model="editingData.body"
                              :min-rows="1"
                              :max-rows="22"
                              ref="body"
            ></AutoSizeTextArea>
        </div>

        <div v-show="editing" class="mock-col w100 mt-1">
            <ToggleSwitch class="mock-col-value" v-model="editingData.responseToVars">SAVE RESPONSE IN GLOBAL VARS</ToggleSwitch>
        </div>

        <div v-show="editing" class="mock-col w100">
            <ToggleSwitch class="mock-col-value" v-model="editingData.triggerRequest">TRIGGER REQUESTS</ToggleSwitch>
        </div>

        <div v-show="editing && editingData.triggerRequest" class="mock-col w100">
            <input type="text" class="form-control form-control-sm" v-model="editingData.triggerRequestIds"
                   placeholder="Comma separated request IDs to trigger after this one"/>
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
            <RequestTester :request="request" @close="testing = false"></RequestTester>
        </div>
    </div>
</template>
<script>
import {mapActions} from 'vuex';
import RequestTester from "./RequestTester";
import RouteMethod from "./RouteMethod";
import ToggleSwitch from "../other/ToggleSwitch";
import AutoSizeTextArea from "../other/AutoSizeTextArea";

export default {
        name: "Request",
        components: {AutoSizeTextArea, RequestTester, RouteMethod, ToggleSwitch},
        data() {
            return {
                editing: false,
                editingData: {},
                testing: false,
            }
        },
        props: {
            request: {type: Object},
        },
        computed: {
            open() {
                return this.editing || this.testing;
            },
            hasVariables() {
                return false;
            },
        },
        mounted() {
            if (this.request._new) {
                this.edit();
            }
        },
        methods: {
            ...mapActions({
                saveRequest: 'requests/save',
                deleteRequests: 'requests/delete',
            }),
            filter(value) {
                this.$emit('filter', value);
            },
            vars() {
                this.showVariables = !this.showVariables;
            },
            edit() {
                this.testing = false;
                this.editingData = {...this.request};
                if (!this.editing) this.editing = true; else this.cancel();
                if (this.editing) this.$nextTick(() => this.$refs.body.focus());
            },
            cancel() {
                if (!!this.request._new) {
                    this.deleteRequests([this.request]);
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
                if (!!this.request._new) {
                    this.deleteRequests([this.request]);
                } else if (confirm('Sure you want to delete?')) {
                    this.$nuxt.$loading.start();
                    this.deleteRequests([this.request]).then(() => this.$nuxt.$loading.finish());
                }
            },
            save() {
                this.$nuxt.$loading.start();
                this.saveRequest([this.request, this.editingData]).then(() => {
                    this.$nuxt.$loading.finish();
                    this.editing = false;
                });
            },
            saveAsCopy() {
                this.$nuxt.$loading.start();
                this.saveRequest([{}, this.editingData]).then(() => {
                    this.$nuxt.$loading.finish();
                    this.editing = false;
                });
            },
        }
    }
</script>
<style scoped>
</style>
