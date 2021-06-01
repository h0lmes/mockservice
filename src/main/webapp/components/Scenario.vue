<template>
    <div class="fields-holder" :class="{'group-start' : groupStart}">

        <div class="field-holder">
            <div class="field-header">GROUP</div>
            <div v-show="!editing" class="field-value link" @click="filter(scenario.group)">{{ scenario.group }}</div>
            <input v-show="editing" type="text" class="form-control form-control-sm monospace" v-model="editingScenario.group"/>
        </div>

        <div class="field-holder">
            <div class="field-header">ALIAS</div>
            <div v-show="!editing" class="field-value link" @click="filter(scenario.alias)">{{ scenario.alias }}</div>
            <input v-show="editing" type="text" class="form-control form-control-sm monospace" v-model="editingScenario.alias"/>
        </div>

        <div class="field-holder">
            <div class="field-header">TYPE</div>
            <div v-show="!editing" class="field-value link" @click="filter(scenario.type)">{{ scenario.type }}</div>
            <select v-show="editing" class="form-control form-control-sm monospace" v-model="editingScenario.type">
                <option>MAP</option>
                <option>QUEUE</option>
            </select>
        </div>

        <div class="field-holder">
            <div class="field-header">ACTIVE</div>
            <div class="field-value" :class="{ 'color-accent-one' : active }">{{ active }}</div>
        </div>

        <div class="buttons-wrapper">
            <a class="btn btn-link ml-2 mr-2" @click="edit">edit</a>
            <a class="btn btn-link mr-2" @click="activate">(re)activate</a>
            <a class="btn btn-link mr-2" @click="deactivate">deactivate</a>
            <a class="btn btn-link btn-danger mr-2" @click="del">delete</a>
        </div>

        <div v-show="editing" class="field-memo">
            <textarea class="form-control form-control-sm v-resize monospace" rows="10" v-model="editingScenario.data"></textarea>
        </div>

        <div v-show="editing" class="edit-buttons-wrapper">
            <div class="btn btn-sm btn-primary mr-3" @click="save">SAVE</div>
            <div class="btn btn-sm btn-primary mr-3" @click="saveAsCopy">SAVE AS COPY</div>
            <div class="btn btn-sm btn-default" @click="cancel">CANCEL</div>
        </div>

    </div>
</template>
<script>
    import {mapActions} from 'vuex';

    export default {
        name: "Scenario",
        data() {
            return {
                editing: false,
                editingScenario: {}
            }
        },
        props: {
            scenario: {type: Object},
            active: {type: Boolean},
            groupStart: {type: Boolean}
        },
        methods: {
            ...mapActions([
                'saveScenario',
                'deleteScenario',
                'activateScenario',
                'deactivateScenario',
            ]),
            edit() {
                this.editingScenario = {...this.scenario};
                this.editing = !this.editing;
            },
            activate() {
                this.activateScenario(this.scenario.alias);
            },
            deactivate() {
                this.deactivateScenario(this.scenario.alias);
            },
            cancel() {
                this.editing = false;
            },
            del() {
                if (!!this.scenario._new) {
                    this.deleteScenario(this.scenario);
                    return;
                }
                if (confirm('Sure?')) {
                    this.deleteScenario(this.scenario);
                }
            },
            save() {
                this.saveScenario([{...this.scenario, data: ''}, this.editingScenario]);
                this.editing = false;
            },
            saveAsCopy() {
                this.editing = false;
                this.saveScenario([{}, this.editingScenario]);
            },
            filter(value) {
                this.$emit('filter', value);
            }
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