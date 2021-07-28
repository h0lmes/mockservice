<template>
    <div class="component component-row" :class="{'open' : open, 'disabled' : route.disabled}" @click.middle="edit">

        <div class="mock-col w2">
            <div class="mock-col-header" :class="{'color-accent-one' : groupStart}">GROUP</div>
            <div v-show="!editing" class="mock-col-value link"
                 :class="{'color-accent-one' : groupStart}"
                 @click="filter(route.group)">{{ route.group }}</div>
            <input v-show="editing" type="text" class="form-control form-control-sm monospace" v-model="editingRoute.group"/>
        </div>

        <div class="mock-col">
            <div class="mock-col-header">TYPE</div>
            <div v-show="!editing" class="mock-col-value link" @click="filter(route.type)">{{ route.type }}</div>
            <select v-show="editing" class="form-control form-control-sm monospace" v-model="editingRoute.type">
                <option>REST</option>
                <option>SOAP</option>
            </select>
        </div>

        <div class="mock-col">
            <div class="mock-col-header">METHOD</div>
            <div v-show="!editing" class="mock-col-value link" @click="filter(route.method)">{{ route.method }}</div>
            <select v-show="editing" class="form-control form-control-sm monospace" v-model="editingRoute.method">
                <option>GET</option>
                <option>POST</option>
                <option>PUT</option>
                <option>DELETE</option>
                <option>PATCH</option>
            </select>
        </div>

        <div class="mock-col w3">
            <div class="mock-col-header">PATH</div>
            <div v-show="!editing" class="mock-col-value link" @click="filter(route.path)">{{ route.path }}</div>
            <input v-show="editing" type="text" class="form-control form-control-sm monospace" v-model="editingRoute.path"/>
        </div>

        <div class="mock-col w2">
            <div class="mock-col-header">ALT</div>
            <div v-show="!editing" class="mock-col-value link" @click="filter(route.alt)">{{ route.alt }}</div>
            <input v-show="editing" type="text" class="form-control form-control-sm monospace" v-model="editingRoute.alt"/>
        </div>

        <div v-show="editing" class="mock-col w1">
            <div class="mock-col-header">RESPONSE CODE</div>
            <input type="text" class="form-control form-control-sm monospace" v-model="editingRoute.responseCode"/>
        </div>

        <div class="mock-col w-fixed-auto">
            <div v-show="editing" class="mock-col-header"></div>
            <div>
                <button type="button" class="btn btn-sm btn-default" @click="edit">edit</button>
                <button type="button" class="btn btn-sm btn-default" @click="test">test</button>
                <button type="button" class="btn btn-sm btn-danger" @click="del">delete</button>
            </div>
        </div>

        <div v-show="editing" class="mock-col w100">
            <AutoSizeTextArea v-model="editingRoute.response" :min-rows="1" placeholder="RESPONSE BODY"></AutoSizeTextArea>
        </div>

        <div v-show="editing" class="mock-col w100">
            <ToggleSwitch class="mock-col-value" v-model="showRequestBodySchema">SHOW REQUEST BODY SCHEMA</ToggleSwitch>
        </div>
        <div v-show="editing && showRequestBodySchema" class="mock-col w100">
            <AutoSizeTextArea v-model="editingRoute.requestBodySchema" :min-rows="1" placeholder="REQUEST BODY SCHEMA (JSON)"></AutoSizeTextArea>
        </div>

        <div v-show="editing" class="mock-col w1">
            <ToggleSwitch class="mock-col-value" v-model="editingRoute.disabled">DISABLED</ToggleSwitch>
        </div>

        <div v-show="editing" class="mock-col w-fixed-auto">
            <button type="button" class="btn btn-sm btn-primary" @click="save">SAVE</button>
            <button type="button" class="btn btn-sm btn-default" @click="cancel">CANCEL</button>
        </div>

        <div v-if="testing" class="mock-col w100">
            <RouteTester :route="route" @close="testing = false"></RouteTester>
        </div>
    </div>
</template>
<script>
    import {mapActions} from 'vuex';
    import RouteTester from "./RouteTester";
    import ToggleSwitch from "./ToggleSwitch";
    import AutoSizeTextArea from "./AutoSizeTextArea";

    export default {
        name: "Route",
        components: {AutoSizeTextArea, RouteTester, ToggleSwitch},
        data() {
            return {
                editing: false,
                editingRoute: {},
                testing: false,
                showRequestBodySchema: false,
            }
        },
        props: {
            route: {type: Object},
            groupStart: {type: Boolean}
        },
        computed: {
            open() {
                return this.editing || this.testing;
            },
        },
        methods: {
            ...mapActions({
                saveRoute: 'routes/save',
                deleteRoutes: 'routes/delete',
            }),
            filter(value) {
                this.$emit('filter', value);
            },
            edit() {
                this.testing = false;
                this.editingRoute = {...this.route};
                this.showRequestBodySchema = !!this.editingRoute.requestBodySchema;
                this.editing = !this.editing;
            },
            test() {
                this.cancel();
                this.testing = !this.testing;
            },
            cancel() {
                this.editing = false;
                this.editingRoute = {};
            },
            del() {
                if (!!this.route._new) {
                    this.deleteRoutes([this.route]);
                    return;
                }
                if (confirm('Sure?')) {
                    this.$nuxt.$loading.start();
                    this.deleteRoutes([this.route])
                        .then(() => this.$nuxt.$loading.finish());
                }
            },
            save() {
                this.editing = false;
                this.$nuxt.$loading.start();
                this.saveRoute(this.editingRoute).then(() => this.$nuxt.$loading.finish());
            },
        }
    }
</script>
<style scoped>
</style>