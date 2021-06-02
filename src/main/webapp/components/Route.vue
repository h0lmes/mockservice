<template>
    <div class="fields-holder" :class="{'group-start' : groupStart}">

        <div class="field-holder w2">
            <div class="field-header">GROUP</div>
            <div v-show="!editing" class="field-value link" @click="filter(route.group)">{{ route.group }}</div>
            <input v-show="editing" type="text" class="form-control form-control-sm monospace" v-model="editingRoute.group"/>
        </div>

        <div class="field-holder">
            <div class="field-header">TYPE</div>
            <div v-show="!editing" class="field-value link" @click="filter(route.type)">{{ route.type }}</div>
            <select v-show="editing" class="form-control form-control-sm monospace" v-model="editingRoute.type">
                <option>REST</option>
                <option>SOAP</option>
            </select>
        </div>

        <div class="field-holder">
            <div class="field-header">METHOD</div>
            <div v-show="!editing" class="field-value link" @click="filter(route.method)">{{ route.method }}</div>
            <select v-show="editing" class="form-control form-control-sm monospace" v-model="editingRoute.method">
                <option>GET</option>
                <option>POST</option>
                <option>PATCH</option>
                <option>PUT</option>
                <option>DELETE</option>
            </select>
        </div>

        <div class="field-holder w3">
            <div class="field-header">PATH</div>
            <div v-show="!editing" class="field-value link" @click="filter(route.path)">{{ route.path }}</div>
            <input v-show="editing" type="text" class="form-control form-control-sm monospace" v-model="editingRoute.path"/>
        </div>

        <div class="field-holder w2">
            <div class="field-header">SUFFIX</div>
            <div v-show="!editing" class="field-value link color-accent-one" @click="filter(route.suffix)">{{ route.suffix }}</div>
            <input v-show="editing" type="text" class="form-control form-control-sm monospace" v-model="editingRoute.suffix"/>
        </div>

        <div class="field-holder">
            <div class="field-header">DISABLED</div>
            <div v-show="!editing" class="field-value" :class="{ 'red' : route.disabled }">{{ route.disabled }}</div>
            <input v-show="editing" type="checkbox" class="form-control form-check" v-model="editingRoute.disabled"/>
        </div>

        <div class="buttons-wrapper">
            <a class="btn btn-link ml-2 mr-2" @click="edit">edit</a>
            <a class="btn btn-link mr-2" @click="test">test</a>
            <a class="btn btn-link btn-danger mr-2" @click="del">delete</a>
        </div>

        <div v-show="editing" class="field-memo">
            <textarea class="form-control form-control-sm v-resize monospace" rows="10" v-model="editingRoute.response"></textarea>
        </div>

        <div v-show="editing" class="edit-buttons-wrapper">
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
    .fields-holder {
        display: flex;
        flex-wrap: wrap;
        font-size: smaller;
        margin-bottom: 1rem;
        padding: 0 .3rem;
        border-radius: 5px;
        background-color: var(--bg-secondary);

        &.group-start {
            border-left: 2px solid var(--color-primary);
        }
    }

    .field-holder {
        flex: 1;
        min-width: 5rem;
        box-sizing: border-box;
        margin: 0;
        padding: .8rem .5rem;
        vertical-align: top;
        text-align: center;

        &.w2 {
            flex: 2;
        }

        &.w3 {
            flex: 3;
        }
    }

    .field-header {
        display: block;
        color: var(--color-secondary);
        width: auto;
        margin: 0 auto .5rem;
        text-align: center;
    }

    .field-value {
        cursor: default;
        display: block;
        color: var(--color-primary);
        width: auto;
        margin: 0 auto;
        text-align: center;
        word-wrap: break-word;

        &.link:hover {
            cursor: pointer;
            text-decoration: underline;
        }
    }

    .field-memo {
        display: block;
        box-sizing: border-box;
        margin: 0;
        padding: 0 .5rem;
        width: 100%;
    }

    .buttons-wrapper {
        cursor: default;
        box-sizing: border-box;
        margin: 0;
        padding: 1rem .3rem 0.9rem;
        vertical-align: top;
        text-align: center;
    }

    .edit-buttons-wrapper {
        cursor: default;
        display: block;
        box-sizing: border-box;
        margin: 0;
        padding: 0.7rem 0;
        width: 100%;
        text-align: center;
    }

    @media screen and (max-width: 1599px) {
        .field-holder {
            flex: 2;
        }
    }

    @media screen and (max-width: 1099px) {
        .fields-holder {
            margin-bottom: 0.7rem;
            padding: .1rem 0;
        }

        .field-holder.w3 {
            flex: 2;
        }

        .buttons-wrapper {
            display: block;
            width: 100%;
            padding: .2rem .3rem .6rem;
        }
    }
</style>