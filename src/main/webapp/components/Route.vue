<template>
    <div class="route-holder monospace">

        <div class="route-field w2">
            <div class="route-field-header">GROUP</div>
            <div v-show="!editing" class="route-field-value">{{ route.group }}</div>
            <input v-show="editing" type="text" class="form-control form-control-sm" v-model="editingRoute.group"/>
        </div>
        <div class="route-field">
            <div class="route-field-header">TYPE</div>
            <div v-show="!editing" class="route-field-value">{{ route.type }}</div>
            <select v-show="editing" class="form-control form-control-sm" v-model="editingRoute.type">
                <option>REST</option>
                <option>SOAP</option>
            </select>
        </div>
        <div class="route-field">
            <div class="route-field-header">METHOD</div>
            <div v-show="!editing" class="route-field-value">{{ route.method }}</div>
            <select v-show="editing" class="form-control form-control-sm" v-model="editingRoute.method">
                <option>GET</option>
                <option>POST</option>
                <option>PATCH</option>
                <option>PUT</option>
                <option>DELETE</option>
            </select>
        </div>
        <div class="route-field w3">
            <div class="route-field-header">PATH</div>
            <div v-show="!editing" class="route-field-value">{{ route.path }}</div>
            <input v-show="editing" type="text" class="form-control form-control-sm" v-model="editingRoute.path"/>
        </div>
        <div class="route-field w2">
            <div class="route-field-header">SUFFIX</div>
            <div v-show="!editing" class="route-field-value">{{ route.suffix }}</div>
            <input v-show="editing" type="text" class="form-control form-control-sm" v-model="editingRoute.suffix"/>
        </div>
        <div class="route-field">
            <div class="route-field-header">DISABLED</div>
            <div v-show="!editing" class="route-field-value" :class="{ 'red' : route.disabled }">{{ disabled }}</div>
            <input v-show="editing" type="checkbox" class="form-control form-check" v-model="editingRoute.disabled"/>
        </div>

        <div class="route-buttons">
            <div class="btn btn-sm btn-primary mr-2" @click="edit">EDIT</div>
            <div class="btn btn-sm mr-2" @click="test">TEST</div>
            <div class="btn btn-sm btn-danger mr-2" @click="del">DELETE</div>
        </div>

        <div v-show="editing" class="route-field-memo">
            <textarea class="form-control form-control-sm v-resize" rows="10" v-model="editingRoute.response"></textarea>
        </div>

        <div v-show="editing" class="edit-buttons">
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
            route: {type: Object}
        },
        computed: {
            disabled() {
                return this.route.disabled ? 'yes' : 'no'
            },
        },
        methods: {
            ...mapActions({
                saveRoute: 'saveRoute',
                deleteRoute: 'deleteRoute'
            }),
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
                if (confirm('You\'re about to permanently delete a route.\nWe need your confirmation for such a devastating action =)')) {
                    this.deleteRoute(this.route);
                }
            },
            save() {
                this.saveRoute([{...this.route, response: ''}, this.editingRoute]);
                this.editing = false;
            },
            saveAsCopy() {
                this.editing = false;
                this.saveRoute([{}, this.editingRoute]);
            },
        }
    }
</script>
<style scoped>
    .route-holder {
        margin-bottom: 1.1rem;
        padding: .1rem .3rem;
        background-color: var(--bg-secondary);
        border-radius: 5px;
        font-size: smaller;
    }

    .route-field {
        display: inline-block;
        box-sizing: border-box;
        margin: 0;
        padding: .8rem .5rem;
        width: 8%;
        height: 100%;
        vertical-align: top;
        text-align: center;
        /*border: 1px dashed #7f828b;*/
    }

    .route-field.w2 {
        width: 12%;
    }

    .route-field.w3 {
        width: 34%;
    }

    .route-field-header {
        display: block;
        color: var(--color-primary);
        width: 100%;
        text-align: center;
        margin-bottom: .5rem;
    }

    .route-field-value {
        display: block;
        color: var(--color-secondary);
        width: 100%;
        text-align: center;
    }

    .route-field-memo {
        display: block;
        box-sizing: border-box;
        margin: 0;
        padding: 0 .5rem;
        width: 100%;
        /*border: 1px dashed #7f828b;*/
    }

    .route-buttons {
        cursor: default;
        display: inline-block;
        box-sizing: border-box;
        margin: 0;
        padding: 1.3rem 1rem 1.2rem;
        min-width: 5%;
        height: 100%;
        vertical-align: top;
        text-align: center;
        /*border: 1px dashed #7f828b;*/
    }

    .edit-buttons {
        cursor: default;
        display: block;
        box-sizing: border-box;
        margin: 0;
        padding: 1.3rem 0 1.2rem;
        width: 100%;
        text-align: center;
        /*border: 1px dashed #7f828b;*/
    }

    .red {
        color: var(--red);
    }
</style>