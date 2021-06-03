<template>
    <div class="fancy-row">

        <div class="fancy-row-block">
            <div class="fancy-row-block-header" :class="{'color-accent-one' : groupStart}">GROUP</div>
            <div v-show="!editing" class="fancy-row-block-value link" @click="filter(scenario.group)">{{ scenario.group }}</div>
            <input v-show="editing" type="text" class="form-control form-control-sm monospace" v-model="editingScenario.group"/>
        </div>

        <div class="fancy-row-block">
            <div class="fancy-row-block-header">ALIAS</div>
            <div v-show="!editing" class="fancy-row-block-value link" @click="filter(scenario.alias)">{{ scenario.alias }}</div>
            <input v-show="editing" type="text" class="form-control form-control-sm monospace" v-model="editingScenario.alias"/>
        </div>

        <div class="fancy-row-block">
            <div class="fancy-row-block-header">TYPE</div>
            <div v-show="!editing" class="fancy-row-block-value link" @click="filter(scenario.type)">{{ scenario.type }}</div>
            <select v-show="editing" class="form-control form-control-sm monospace" v-model="editingScenario.type">
                <option>MAP</option>
                <option>QUEUE</option>
            </select>
        </div>

        <div class="fancy-row-block">
            <div class="fancy-row-block-header">ACTIVE</div>
            <div class="fancy-row-block-value" :class="{ 'color-accent-one' : active }">{{ active }}</div>
        </div>

        <div class="fancy-row-block-buttons">
            <a class="btn btn-link ml-2 mr-2" @click="edit">edit</a>
            <a class="btn btn-link mr-2" @click="activate">(re)activate</a>
            <a class="btn btn-link mr-2" @click="deactivate">deactivate</a>
            <a class="btn btn-link btn-danger mr-2" @click="del">delete</a>
        </div>

        <div v-show="editing" class="fancy-row-block-memo">
            <textarea class="form-control form-control-sm v-resize monospace" rows="10" v-model="editingScenario.data"></textarea>
        </div>

        <div v-show="editing" class="fancy-row-block-edit-buttons">
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
            ...mapActions(['saveScenario', 'deleteScenario', 'activateScenario', 'deactivateScenario',]),
            filter(value) {
                this.$emit('filter', value);
            },
            edit() {
                this.editingScenario = {...this.scenario};
                this.editing = !this.editing;
            },
            cancel() {
                this.editing = false;
            },
            activate() {
                this.$nuxt.$loading.start();
                this.activateScenario(this.scenario.alias)
                    .then(() => this.$nuxt.$loading.finish());
            },
            deactivate() {
                this.$nuxt.$loading.start();
                this.deactivateScenario(this.scenario.alias)
                    .then(() => this.$nuxt.$loading.finish());
            },
            del() {
                if (!!this.scenario._new) {
                    this.deleteScenario(this.scenario);
                    return;
                }
                if (confirm('Sure?')) {
                    this.$nuxt.$loading.start();
                    this.deleteScenario(this.scenario)
                        .then(() => this.$nuxt.$loading.finish());
                }
            },
            save() {
                this.editing = false;
                this.$nuxt.$loading.start();
                this.saveScenario([{...this.scenario, data: ''}, this.editingScenario])
                    .then(() => this.$nuxt.$loading.finish());
            },
            saveAsCopy() {
                this.editing = false;
                this.$nuxt.$loading.start();
                this.saveScenario([{}, this.editingScenario])
                    .then(() => this.$nuxt.$loading.finish());
            },
        }
    }
</script>
<style lang="scss" scoped>
</style>