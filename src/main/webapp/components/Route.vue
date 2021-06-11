<template>
    <div class="mock-row">

        <div class="mock-col w2">
            <div class="mock-col-header" :class="{'color-accent-one' : groupStart}">GROUP</div>
            <div v-show="!editing" class="mock-col-value link" @click="filter(route.group)">{{ route.group }}</div>
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

        <div class="mock-col text-center">
            <div class="mock-col-header">DISABLED</div>
            <div v-show="!editing" class="mock-col-value" :class="{ 'red' : route.disabled }">{{ route.disabled }}</div>
            <!--<input v-show="editing" type="checkbox" class="form-control form-check" v-model="editingRoute.disabled"/>-->
            <ToggleSwitch v-show="editing"
                          :id="'disabled' + index"
                          v-model="editingRoute.disabled"></ToggleSwitch>
        </div>

        <div class="mock-col w-auto">
            <div class="buttons-spacer">
                <button type="button" class="btn btn-sm btn-default" @click="edit">edit</button>
                <button type="button" class="btn btn-sm btn-default" @click="test">test</button>
                <button type="button" class="btn btn-sm btn-danger" @click="del">delete</button>
            </div>
        </div>

        <div v-show="editing" class="mock-col w100">
            <textarea class="form-control form-control-sm v-resize monospace" rows="10" v-model="editingRoute.response"></textarea>
        </div>

        <div v-show="editing" class="mock-col w100">
            <button type="button" class="btn btn-sm btn-primary" @click="save">SAVE</button>
            <button type="button" class="btn btn-sm btn-primary" @click="saveAsCopy">SAVE AS COPY</button>
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

    export default {
        name: "Route",
        components: {RouteTester, ToggleSwitch},
        data() {
            return {
                editing: false,
                editingRoute: {},
                testing: false
            }
        },
        props: {
            index: {type: Number},
            route: {type: Object},
            groupStart: {type: Boolean}
        },
        methods: {
            ...mapActions({
                saveRoute: 'saveRoute',
                deleteRoute: 'deleteRoute'
            }),
            filter(value) {
                this.$emit('filter', value);
            },
            edit() {
                this.editingRoute = {...this.route};
                this.editing = !this.editing;
                this.testing = false;
            },
            test() {
                this.editing = false;
                this.testing = !this.testing;
            },
            cancel() {
                this.editing = false;
            },
            del() {
                if (!!this.route._new) {
                    this.deleteRoute(this.route);
                    return;
                }
                if (confirm('Sure?')) {
                    this.$nuxt.$loading.start();
                    this.deleteRoute(this.route)
                        .then(() => this.$nuxt.$loading.finish());
                }
            },
            save() {
                this.editing = false;
                this.$nuxt.$loading.start();
                this.saveRoute([{...this.route, response: ''}, this.editingRoute])
                    .then(() => this.$nuxt.$loading.finish());
            },
            saveAsCopy() {
                this.editing = false;
                this.$nuxt.$loading.start();
                this.saveRoute([{}, this.editingRoute])
                    .then(() => this.$nuxt.$loading.finish());
            },
        }
    }
</script>
<style lang="scss" scoped>
</style>