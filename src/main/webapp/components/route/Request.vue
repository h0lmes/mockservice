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

        <div v-if="!testing" class="mock-col w2">
            <div class="mock-col-header">GROUP</div>
            <div v-show="!editing" class="mock-col-value link" @click="filter(request.group)">{{ request.group }}</div>
            <input v-show="editing" type="text" class="form-control form-control-sm" v-model="editingData.group"/>
        </div>

        <div v-if="!testing"  class="mock-col w1">
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

        <div v-show="!editing && !testing" class="mock-col w3">
            <div class="mock-col-header">URL</div>
            <div class="mock-col-value link" @click="filter(request.path)">{{ request.path }}</div>
        </div>

        <div v-show="editing" class="mock-col w1">
            <div class="mock-col-header">ID</div>
            <input type="text" class="form-control form-control-sm" v-model="editingData.id"
                   placeholder="(empty to generate)"/>
        </div>
        <div v-show="!editing && !testing" class="mock-col w2">
            <div class="mock-col-header">ID</div>
            <div class="mock-col-value link" @click="filter(request.id)">{{ request.id }}</div>
        </div>

        <div v-if="!testing" class="mock-col w-fixed-auto">
            <div v-show="editing" class="mock-col-header"></div>
            <div class="mock-col-value">
                <ButtonExecute class="orange-yellow" @click="test"></ButtonExecute>
                <ButtonEdit @click="edit"></ButtonEdit>
                <ButtonDelete @click="del"></ButtonDelete>
            </div>
        </div>

        <div v-show="editing" class="mock-col w100">
            <div class="mb-2 color-secondary">URL</div>
            <input type="text" class="form-control form-control-sm" v-model="editingData.path"/>
        </div>

        <div v-show="editing" class="mock-col w100">
            <div class="mb-2 color-secondary">REQUEST HEADERS</div>
            <AutoSizeTextArea v-model="editingData.headers"></AutoSizeTextArea>
        </div>

        <div v-show="editing" class="mock-col w100">
            <div class="mb-2 color-secondary">REQUEST BODY</div>
            <AutoSizeTextArea v-model="editingData.body" ref="body"></AutoSizeTextArea>
        </div>

        <div v-show="editing" class="mock-col w100 mt-1">
            <ToggleSwitch class="mock-col-value" v-model="editingData.responseToVars">SAVE RESPONSE IN GLOBAL VARS</ToggleSwitch>
        </div>

        <div v-show="editing" class="mock-group w100">
            <div v-show="editing" class="mock-col w100">
                <ToggleSwitch class="mock-col-value" v-model="editingData.triggerRequest">TRIGGER REQUESTS</ToggleSwitch>
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
            <RequestTester :request="request" @close="testing = false" @edit="edit"></RequestTester>
        </div>
    </div>
</template>
<script>
import {mapActions} from 'vuex';
import RequestTester from "./RequestTester";
import RouteMethod from "./RouteMethod";
import ToggleSwitch from "../other/ToggleSwitch";
import AutoSizeTextArea from "../other/AutoSizeTextArea";
import ButtonDelete from "@/components/other/ButtonDelete";
import ButtonEdit from "@/components/other/ButtonEdit";
import ButtonExecute from "@/components/other/ButtonExecute";

export default {
    name: "Request",
    components: {ButtonExecute, ButtonEdit, ButtonDelete, AutoSizeTextArea, RequestTester, RouteMethod, ToggleSwitch},
    data() {
        return {
            editing: false,
            editingData: {},
            testing: false,
        }
    },
    props: {
        request: {type: Object, default: {}},
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
        if (this.request._new) this.edit();
    },
    methods: {
        ...mapActions({
            filter: 'setApiSearchExpression',
            saveRequest: 'requests/save',
            deleteRequests: 'requests/delete',
        }),
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
