<template>
    <div class="component component-row monospace"
         :class="{'open' : open}"
         @click.middle.stop.prevent="edit"
         @keydown.esc.exact="cancel">

        <div class="mock-col w2">
            <div class="mock-col-header">GROUP</div>
            <div v-show="!editing" class="mock-col-value link" @click="filter(test.group)">{{ test.group }}</div>
            <input v-show="editing" type="text" class="form-control form-control-sm" v-model="editingTest.group"/>
        </div>

        <div class="mock-col w1" v-show="!editing">
            <div class="mock-col-header">TYPE</div>
            <div v-show="!editing" class="mock-col-value">💡 TEST</div>
        </div>

        <div class="mock-col w3">
            <div class="mock-col-header">ALIAS</div>
            <div v-show="!editing" class="mock-col-value link" @click="filter(test.alias)">{{ test.alias }}</div>
            <input v-show="editing" type="text" class="form-control form-control-sm" v-model="editingTest.alias"/>
        </div>

        <div class="mock-col w2" v-show="!editing">
            <div class="mock-col-header"></div>
            <div class="mock-col-value"></div>
        </div>

        <div class="mock-col w-fixed-auto">
            <div v-show="editing" class="mock-col-header"></div>
            <div class="mock-col-value">
                <button type="button" class="btn btn-sm btn-primary" @click="testRun">test</button>
                <button type="button" class="btn btn-sm btn-default" @click="edit">edit</button>
                <button type="button" class="btn btn-sm btn-danger ml-2" @click="del">delete</button>
            </div>
        </div>

        <div v-show="editing" class="mock-col w100">
            <div class="mb-2 color-secondary">TEST PLAN</div>
            <AutoSizeTextArea v-model="editingTest.plan"
                              placeholder="See test plan syntax at the bottom of page"
                              :min-rows="1"
                              :max-rows="22"
                              ref="data"
            ></AutoSizeTextArea>
        </div>

        <div v-show="editing" class="mock-col w1 mt-1"></div> <!-- no value here - just for alignment -->

        <div v-show="editing" class="mock-col w-fixed-auto">
            <button type="button" class="btn btn-sm btn-primary" @click="save">SAVE</button>
            <button type="button" class="btn btn-sm btn-default" @click="saveAsCopy">SAVE AS COPY</button>
            <button type="button" class="btn btn-sm btn-default" @click="cancel">CANCEL</button>
        </div>

        <div v-if="testing" class="mock-col w100">
            <TestRun :test="test" @close="testing = false"></TestRun>
        </div>
    </div>
</template>
<script>
import {mapActions} from 'vuex';
import AutoSizeTextArea from "../other/AutoSizeTextArea";
import TestRun from "@/components/route/TestRun";

export default {
        name: "Test",
        components: {TestRun, AutoSizeTextArea},
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
        created() {
            //this.activeSwitch = this.active;
        },
        mounted() {
            if (this.test._new) {
                this.edit();
            }
        },
        methods: {
            ...mapActions({
                saveTest: 'tests/save',
                deleteTest: 'tests/delete',
            }),
            filter(value) {
                this.$emit('filter', value);
            },
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
        }
    }
</script>
<style scoped>
</style>
