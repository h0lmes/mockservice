<template>
    <div class="component component-row monospace"
         :class="{'open' : open}"
         @click.middle.stop.prevent="edit"
         @keydown.esc.exact="cancel">

        <div class="mock-col w2">
            <div class="mock-col-header">GROUP</div>
            <div v-show="!editing" class="mock-col-value link" @click="filter(topic.group)">{{ topic.group }}</div>
            <input v-show="editing" type="text" class="form-control form-control-sm" v-model="editingData.group"/>
        </div>

        <div class="mock-col w3">
            <div class="mock-col-header">TOPIC</div>
            <div v-show="!editing" class="mock-col-value link" @click="filter(topic.topic)">{{ topic.topic }}</div>
            <input v-show="editing" type="text" class="form-control form-control-sm"
                   v-model="editingData.topic"
                   ref="topic"
            />
        </div>

        <div class="mock-col w1">
            <div class="mock-col-header">PARTITION</div>
            <div v-show="!editing" class="mock-col-value">{{ topic.partition }}</div>
            <input v-show="editing" type="text" class="form-control form-control-sm" v-model="editingData.partition"/>
        </div>

        <div v-show="!editing" class="mock-col w2">
            <div class="mock-col-header">OFFSETS</div>
            <div class="mock-col-value">{{ topic.producerOffset }}/{{ topic.consumerOffset }}</div>
        </div>

        <div class="mock-col w-fixed-auto">
            <div v-show="editing" class="mock-col-header"></div>
            <div class="mock-col-value">
                <button type="button" class="btn btn-sm btn-default" @click="view">view</button>
                <button type="button" class="btn btn-sm btn-default" @click="edit">edit</button>
                <button type="button" class="btn btn-sm btn-danger ml-2" @click="del">delete</button>
            </div>
        </div>

        <div v-show="editing" class="mock-col w100 mb-2">
            <div class="color-secondary">
                INITIAL DATA
                <div class="btn btn-link" @click="addRecordToInitialData">+add record</div>
            </div>
            <AutoSizeTextArea v-model="editingData.initialData"
                              :min-rows="1"
                              :max-rows="20"
            ></AutoSizeTextArea>
        </div>

        <div v-show="viewing" class="mock-col w100 mt-4">
            <TopicRecordProducer :topic="topic.topic" :partition="topic.partition" @added="recordAdded" />
        </div>

        <div v-show="viewing" class="mock-col w100 mt-2">
            <div class="mb-2 color-secondary">RECORDS IN TOPIC</div>
            <TopicRecords :entities="topicRecords"
                          :show-timestamp="showTimestamp"
                          :show-headers="showHeaders" />
        </div>
        <div v-show="viewing"  class="mock-col w1">
            <ToggleSwitch class="mock-col-value" v-model="showHeaders">SHOW HEADERS</ToggleSwitch>
        </div>
        <div v-show="viewing"  class="mock-col w-fixed-auto">
            <Pagination :limit="limit" :offset="offset" :total="total" @page="page" />
        </div>

        <div v-show="editing" class="mock-col w-fixed-auto">
            <button type="button" class="btn btn-sm btn-primary" @click="save">SAVE</button>
            <button type="button" class="btn btn-sm btn-default" @click="saveAsCopy">SAVE AS COPY</button>
            <button type="button" class="btn btn-sm btn-default" @click="cancel">CANCEL</button>
        </div>
    </div>
</template>
<script>
import {mapActions} from 'vuex';
import TopicRecordProducer from './TopicRecordProducer'
import ToggleSwitch from "../other/ToggleSwitch";
import AutoSizeTextArea from "../other/AutoSizeTextArea";
import TopicRecords from "@/components/kafka/TopicRecords";
import Pagination from "@/components/other/Pagination";

export default {
    name: "Topic",
    components: {TopicRecordProducer, Pagination, TopicRecords, AutoSizeTextArea, ToggleSwitch},
    data() {
        return {
            editing: false,
            editingData: {},
            viewing: false,
            topicRecords: [],
            offset: 0,
            limit: 10,
            total: 0,
            showTimestamp: false,
            showHeaders: false,
        }
    },
    props: {
        topic: {type: Object},
    },
    computed: {
        open() {
            return this.editing || this.viewing;
        },
    },
    mounted() {
        if (this.topic._new) {
            this.edit();
        }
    },
    methods: {
        ...mapActions({
            saveTopic: 'kafka/save',
            deleteTopics: 'kafka/delete',
            getRecords: 'kafka/records',
            appendItem: 'kafka/appendItem',
        }),
        async page(value) {
            this.offset = value * this.limit;
            if (this.viewing) await this.loadRecords();
        },
        filter(value) {
            this.$emit('filter', value);
        },
        async view() {
            this.cancel();
            this.viewing = !this.viewing;
            if (this.viewing) await this.loadRecords();
        },
        async recordAdded() {
            if (this.viewing) await this.loadRecords();
        },
        async loadRecords() {
            this.$nuxt.$loading.start();
            this.getRecords({
                topic: this.topic.topic,
                partition: this.topic.partition,
                offset: this.offset,
                limit: this.limit
            }).then(response => {
                this.$nuxt.$loading.finish();
                this.topicRecords = response.records;
                this.total = response.total;
            });
        },
        async addRecordToInitialData() {
            this.$nuxt.$loading.start();
            let that = this;
            this.appendItem({
                text: this.editingData.initialData,
                topic: this.topic.topic,
                partition: this.topic.partition
            }).then(response => {
                that.$nuxt.$loading.finish();
                that.editingData.initialData = response;
            });
        },
        edit() {
            this.viewing = false;
            this.editingData = {initialData: '', ...this.topic};
            if (!this.editing) this.editing = true; else this.cancel();
            if (this.editing) this.$nextTick(() => this.$refs.topic.focus());
        },
        cancel() {
            if (!!this.topic._new) {
                this.deleteTopics([this.topic]);
            } else {
                this.editing = false;
                this.editingData = {};
            }
        },
        del() {
            if (!!this.topic._new) {
                this.deleteTopics([this.topic]);
            } else if (confirm('Sure you want to delete?')) {
                this.$nuxt.$loading.start();
                this.deleteTopics([this.topic]).then(() => this.$nuxt.$loading.finish());
            }
        },
        save() {
            this.$nuxt.$loading.start();
            this.saveTopic([this.topic, this.editingData]).then(() => {
                this.$nuxt.$loading.finish();
                this.editing = false;
            });
        },
        saveAsCopy() {
            this.$nuxt.$loading.start();
            this.saveTopic([{}, this.editingData]).then(() => {
                this.$nuxt.$loading.finish();
                this.editing = false;
            });
        },
    }
}
</script>
<style scoped>
</style>
