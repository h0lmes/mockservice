<template>
    <div class="fancy-row">

        <div class="fancy-row-block w2">
            <div class="fancy-row-block-header" :class="{'color-accent-one' : groupStart}">GROUP</div>
            <div v-show="!editing" class="fancy-row-block-value link" @click="filter(route.group)">{{ route.group }}</div>
            <input v-show="editing" type="text" class="form-control form-control-sm monospace" v-model="editingRoute.group"/>
        </div>

        <div class="fancy-row-block">
            <div class="fancy-row-block-header">TYPE</div>
            <div v-show="!editing" class="fancy-row-block-value link" @click="filter(route.type)">{{ route.type }}</div>
            <select v-show="editing" class="form-control form-control-sm monospace" v-model="editingRoute.type">
                <option>REST</option>
                <option>SOAP</option>
            </select>
        </div>

        <div class="fancy-row-block">
            <div class="fancy-row-block-header">METHOD</div>
            <div v-show="!editing" class="fancy-row-block-value link" @click="filter(route.method)">{{ route.method }}</div>
            <select v-show="editing" class="form-control form-control-sm monospace" v-model="editingRoute.method">
                <option>GET</option>
                <option>POST</option>
                <option>PATCH</option>
                <option>PUT</option>
                <option>DELETE</option>
            </select>
        </div>

        <div class="fancy-row-block w3">
            <div class="fancy-row-block-header">PATH</div>
            <div v-show="!editing" class="fancy-row-block-value link" @click="filter(route.path)">{{ route.path }}</div>
            <input v-show="editing" type="text" class="form-control form-control-sm monospace" v-model="editingRoute.path"/>
        </div>

        <div class="fancy-row-block w2">
            <div class="fancy-row-block-header">SUFFIX</div>
            <div v-show="!editing" class="fancy-row-block-value link color-accent-one" @click="filter(route.suffix)">{{ route.suffix }}</div>
            <input v-show="editing" type="text" class="form-control form-control-sm monospace" v-model="editingRoute.suffix"/>
        </div>

        <div class="fancy-row-block">
            <div class="fancy-row-block-header">DISABLED</div>
            <div v-show="!editing" class="fancy-row-block-value" :class="{ 'red' : route.disabled }">{{ route.disabled }}</div>
            <input v-show="editing" type="checkbox" class="form-control form-check" v-model="editingRoute.disabled"/>
        </div>

        <div class="fancy-row-block-buttons">
            <a class="btn btn-link ml-2 mr-2" @click="edit">edit</a>
            <a class="btn btn-link mr-2" @click="test">test</a>
            <a class="btn btn-link btn-danger mr-2" @click="del">delete</a>
        </div>

        <div v-show="editing" class="fancy-row-block-memo">
            <textarea class="form-control form-control-sm v-resize monospace" rows="10" v-model="editingRoute.response"></textarea>
        </div>

        <div v-show="editing" class="fancy-row-block-edit-buttons">
            <div class="btn btn-sm btn-primary mr-3" @click="save">SAVE</div>
            <div class="btn btn-sm btn-primary mr-3" @click="saveAsCopy">SAVE AS COPY</div>
            <div class="btn btn-sm btn-default" @click="cancel">CANCEL</div>
        </div>

        <RouteTester v-if="testing" :route="route" @close="testing = false"></RouteTester>

    </div>
</template>
<script>
    import {mapActions} from 'vuex';
    import RouteTester from "./RouteTester";

    export default {
        name: "Route",
        components: {RouteTester},
        data() {
            return {
                editing: false,
                editingRoute: {},
                testing: false
            }
        },
        props: {
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