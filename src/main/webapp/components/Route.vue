<template>
    <div class="route-holder monospace" :class="{ editing : editing }">
        <div class="route-field route-field-12">
            <div class="route-field-header">GROUP</div>
            <div class="route-field-value">{{ route.group }}</div>
            <div class="route-field-editor">
                <input type="text" class="form-control form-control-sm" v-model="editingRoute.group"/>
            </div>
        </div>
        <div class="route-field">
            <div class="route-field-header">TYPE</div>
            <div class="route-field-value">{{ route.type }}</div>
            <div class="route-field-editor">
                <select class="form-control form-control-sm" v-model="editingRoute.type">
                    <option>REST</option>
                    <option>SOAP</option>
                </select>
            </div>
        </div>
        <div class="route-field">
            <div class="route-field-header">METHOD</div>
            <div class="route-field-value">{{ route.method }}</div>
            <div class="route-field-editor">
                <select class="form-control form-control-sm" v-model="editingRoute.method">
                    <option>GET</option>
                    <option>POST</option>
                    <option>PATCH</option>
                    <option>PUT</option>
                    <option>DELETE</option>
                </select>
            </div>
        </div>
        <div class="route-field route-field-path">
            <div class="route-field-header">PATH</div>
            <div class="route-field-value">{{ route.path }}</div>
            <div class="route-field-editor">
                <input type="text" class="form-control form-control-sm" v-model="editingRoute.path"/>
            </div>
        </div>
        <div class="route-field route-field-12">
            <div class="route-field-header">SUFFIX</div>
            <div class="route-field-value">{{ route.suffix }}</div>
            <div class="route-field-editor">
                <input type="text" class="form-control form-control-sm" v-model="editingRoute.suffix"/>
            </div>
        </div>
        <div class="route-field">
            <div class="route-field-header">DISABLED</div>
            <div class="route-field-value" :class="{ 'red' : route.disabled }">{{ disabled }}</div>
            <div class="route-field-editor">
                <input type="checkbox" class="form-control form-check" v-model="editingRoute.disabled"/>
            </div>
        </div>
        <div class="route-field-memo">
            <textarea class="form-control form-control-sm v-resize" rows="8" v-model="editingRoute.response"></textarea>
        </div>
        <div class="route-buttons route-buttons-normal">
            <div class="btn btn-sm btn-primary" @click="edit">EDIT</div>
        </div>
        <div class="route-buttons route-buttons-normal">
            <div class="btn btn-sm btn-danger" @click="del">DELETE</div>
        </div>
        <div class="route-buttons route-buttons-edit">
            <div class="btn btn-sm btn-primary mr-3" @click="save">SAVE</div>
            <div class="btn btn-sm btn-primary mr-3" @click="saveAsCopy">SAVE AS COPY</div>
            <div class="btn btn-sm btn-default" @click="cancel">CANCEL</div>
        </div>
    </div>
</template>
<script>
    import {mapActions} from 'vuex';

    export default {
        name: "Route",
        data() {
            return {
                editing: false,
                editingRoute: {}
            }
        },
        props: {
            route: {
                type: Object,
                default: {}
            }
        },
        computed: {
            disabled() {
                return this.route.disabled ? 'yes' : 'no'
            }
        },
        methods: {
            ...mapActions({
                saveRoute: 'saveRoute',
                deleteRoute: 'deleteRoute'
            }),
            edit() {
                this.editingRoute = {...this.route};
                this.editing = true;
            },
            cancel() {
                this.editing = false;
            },
            save() {
                this.editing = false;
                this.saveRoute(
                    [
                        {
                            type: this.route.type,
                            method: this.route.method,
                            path: this.route.path,
                            suffix: this.route.suffix
                        },
                        this.editingRoute
                    ]
                );
            },
            saveAsCopy() {
                this.editing = false;
                this.saveRoute([{}, this.editingRoute]);
            },
            del() {
                if (confirm('You\'re about to permanently delete a route.\nDo you confirm this?')) {
                    console.log("Deleted: ", this.route);
                    this.deleteRoute(this.route);
                }
            }
        }
    }
</script>
<style scoped>
    .route-holder {
        margin-bottom: 2rem;
        padding: .1rem 2rem;
        background-color: var(--bg-secondary);
        border-radius: 5px;
        font-size: smaller;
    }

    .route-buttons {
        display: inline-block;
        box-sizing: border-box;
        margin: 0;
        min-width: 7%;
        height: 100%;
        vertical-align: top;
        text-align: center;
        /*border: 1px dashed #7f828b;*/
    }

    .route-buttons-normal {
        padding: 1.3rem .5rem 1.3rem .5rem;
    }

    .editing .route-buttons-normal {
        display: none;
    }

    .route-buttons-edit {
        display: none;
        padding: 1.3rem .5rem 1.3rem .5rem;
    }

    .editing .route-buttons-edit {
        display: block;
    }

    .route-field {
        display: inline-block;
        box-sizing: border-box;
        margin: 0;
        padding: .8rem .5rem;
        width: 7%;
        height: 100%;
        vertical-align: top;
        text-align: center;
        /*border: 1px dashed #7f828b;*/
    }

    .route-field-12 {
        width: 12%;
    }

    .route-field-path {
        width: 35%;
    }

    .route-field-memo {
        display: none;
        box-sizing: border-box;
        margin: 0;
        padding: .8rem .5rem;
        width: 100%;
        /*border: 1px dashed #7f828b;*/
    }

    .editing .route-field-memo {
        display: block;
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

    .editing .route-field-value {
        display: none;
    }

    .route-field-editor {
        display: none;
        width: 100%;
        text-align: center;
    }

    .editing .route-field-editor {
        display: block;
    }

    .red {
        color: var(--red);
    }
</style>