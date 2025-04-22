<template>
    <div class="component component-row monospace"
         :class="{'open' : open}"
         @click.middle.stop.prevent="edit"
         @keydown.esc.exact="cancel">

        <div v-if="!testing" class="mock-col w2">
            <div class="mock-col-header">GROUP</div>
            <div v-show="!editing" class="mock-col-value link" @click="filter(test.group)">{{ test.group }}</div>
            <input v-show="editing" type="text" class="form-control form-control-sm" v-model="editingTest.group"/>
        </div>

        <div v-show="!editing && ! testing" class="mock-col w1">
            <div class="mock-col-header">TYPE</div>
            <div v-show="!editing" class="mock-col-value">TEST</div>
        </div>

        <div v-if="!testing" class="mock-col w3">
            <div class="mock-col-header">ALIAS</div>
            <div v-show="!editing" class="mock-col-value link" @click="filter(test.alias)">{{ test.alias }}</div>
            <input v-show="editing" type="text" class="form-control form-control-sm" v-model="editingTest.alias"/>
        </div>

        <div v-show="!editing && !testing" class="mock-col w2">
            <div class="mock-col-header"></div>
            <div class="mock-col-value"></div>
        </div>

        <div v-if="!testing" class="mock-col w-fixed-auto">
            <div v-show="editing" class="mock-col-header"></div>
            <div class="mock-col-value">
                <ButtonExecute class="orange-yellow" @click="testRun"></ButtonExecute>
                <ButtonEdit @click="edit"></ButtonEdit>
                <ButtonDelete @click="del"></ButtonDelete>
            </div>
        </div>

        <div v-show="editing" class="mock-col w100 mt-2">
            <div class="mb-2 color-secondary">
                TEST PLAN
                <!--button type="button" class="btn btn-sm ml-2" @click="addPlanStep">add step</button-->
            </div>
            <AutoSizeTextArea v-model="editingTest.plan" ref="data"
                              placeholder="See test plan syntax at the bottom of page"
            ></AutoSizeTextArea>
        </div>

        <div v-show="editing" class="mock-col w1 mt-1"></div> <!-- no value here - just for alignment -->

        <div v-show="editing" class="mock-col w-fixed-auto">
            <button type="button" class="btn btn-sm btn-primary" @click="save">SAVE</button>
            <button type="button" class="btn btn-sm btn-default" @click="saveAsCopy">SAVE AS COPY</button>
            <button type="button" class="btn btn-sm btn-default" @click="cancel">CANCEL</button>
        </div>

        <div v-if="testing" class="mock-col w100">
            <TestRun :test="test" @close="testing = false" @edit="edit"></TestRun>
        </div>
    </div>
</template>
<script>
import {mapActions} from 'vuex';
import AutoSizeTextArea from "../other/AutoSizeTextArea";
import TestRun from "@/components/route/TestRun";
import ButtonDelete from "@/components/other/ButtonDelete";
import ButtonEdit from "@/components/other/ButtonEdit";
import ButtonExecute from "@/components/other/ButtonExecute";

export default {
    name: "Test",
    components: {ButtonExecute, ButtonEdit, ButtonDelete, TestRun, AutoSizeTextArea},
    data() {
        return {
            editing: false,
            editingTest: {},
            testing: false,
        }
    },
    props: {
        test: {type: Object},
    },
    computed: {
        open() {
            return this.editing || this.testing;
        },
    },
    mounted() {
        if (this.test._new) this.edit();
    },
    methods: {
        ...mapActions({
            filter: 'setApiSearchExpression',
            saveTest: 'tests/save',
            deleteTest: 'tests/delete',
        }),
        edit() {
            this.testing = false;
            this.editingTest = {...this.test};
            if (!this.editing) this.editing = true; else this.cancel();
            if (this.editing) this.$nextTick(() => this.$refs.data.focus());
        },
        cancel() {
            if (!!this.test._new) {
                this.deleteTest([this.test]);
            } else {
                this.editing = false;
                this.editingTest = {};
            }
        },
        del() {
            if (!!this.test._new) {
                this.deleteTest([this.test]);
                return;
            }
            if (confirm('Sure you want to delete?')) {
                this.$nuxt.$loading.start();
                this.deleteTest([this.test])
                    .then(() => this.$nuxt.$loading.finish());
            }
        },
        save() {
            this.$nuxt.$loading.start();
            this.saveTest([this.test, this.editingTest])
                .then(() => {
                    this.$nuxt.$loading.finish();
                    this.editing = false;
                });
        },
        saveAsCopy() {
            this.$nuxt.$loading.start();
            this.saveTest([{}, this.editingTest])
                .then(() => {
                    this.$nuxt.$loading.finish();
                    this.editing = false;
                });
        },
        testRun() {
            this.cancel();
            this.testing = !this.testing;
        },
        addPlanStep() {
            // TODO
        },
    }
}
</script>
<style scoped>
</style>
