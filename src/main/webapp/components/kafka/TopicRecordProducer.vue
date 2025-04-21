<template>
    <div class="holder">
        <div class="item">
            <div class="mb-2 color-secondary">KEY</div>
            <input type="text" class="form-control form-control-sm" v-model="recordKey"/>
        </div>
        <div class="item">
            <div class="mb-2 color-secondary">VALUE</div>
            <input type="text" class="form-control form-control-sm" v-model="recordValue"/>
        </div>
        <div class="item">
            <div class="mb-2 color-secondary">HEADERS</div>
            <input type="text" class="form-control form-control-sm" v-model="recordHeaders"/>
        </div>

        <div class="item item-fixed">
            <div class="mb-2 color-secondary">&nbsp;</div>
            <button type="button" class="btn btn-sm btn-default" @click="add">add record</button>
        </div>
    </div>
</template>
<script>
import {mapActions} from 'vuex';

export default {
    name: "TopicRecordProducer",
    components: {},
    data() {
        return {
            recordKey: '',
            recordValue: '',
            recordHeaders: '',
        }
    },
    props: {
        topic: {type: String},
        partition: {type: Number},
    },
    computed: {
    },
    methods: {
        ...mapActions({
            produce: 'kafka/produce',
            setLastError: 'setLastError',
        }),
        add() {
            let rec = null;
            try {
                rec = {
                    topic: this.topic,
                    partition: this.partition,
                    timestamp: 0,
                    key: this.recordKey === '' ? null : this.recordKey,
                    value: this.recordValue === '' ? null : this.recordValue,
                    headers: this.recordHeaders === '' ? {} : JSON.parse(this.recordHeaders),
                };
            } catch (e) {
                console.error('Error building record: ', e);
                err('Error building record: ' + e.message);
            }
            this.$nuxt.$loading.start();
            let that = this;
            this.produce([rec]).then(() => {
                that.$nuxt.$loading.finish();
                that.$emit('added');
            });
        },
        err(text) {
            this.setLastError(text);
        },
    }
}
</script>
<style scoped>
.holder {
    display: flex;
    gap: 0.5rem 0.5rem;
    flex-wrap: wrap;
    justify-content: center;
    align-content: center;
    align-items: center;
    margin: 0;
    border: none;
}
.item {
    flex: 1 1 0;
    position: relative;
    box-sizing: border-box;
    min-width: 7rem;
}
.item-fixed {
    flex: 0 0 auto;
    min-width: 1rem;
}
</style>
