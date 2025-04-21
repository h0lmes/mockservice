<template>
    <div class="topic-records-holder">
        <TopicRecord class="mb-3" :entity="header"
                     :separators="headerSeparators"
                     :show-timestamp="showTimestamp"
                     :show-headers="showHeaders"
        ></TopicRecord>
        <div v-if="entities.length === 0" class="mt-2 text-center">
            ...
        </div>
        <TopicRecord class="mt-2"
                     :entity="entity"
                     v-for="(entity, index) in sortedEntities"
                     :key="entity.offset"
                     :show-timestamp="showTimestamp"
                     :show-headers="showHeaders"/>
    </div>
</template>
<script>
import TopicRecord from "./TopicRecord";

export default {
    name: "TopicRecords",
    components: {TopicRecord},
    data() {
        return {
            headerSeparators: true,
            header: {
                offset: "OFFSET",
                timestamp: "TIMESTAMP",
                key: "KEY",
                value: "VALUE",
                headers: "HEADERS",
            },
        }
    },
    props: {
        entities: {type: Array},
        showTimestamp: {type: Boolean, default: false},
        showHeaders: {type: Boolean, default: false},
    },
    computed: {
        sortedEntities() {
            return this.entities.sort(
                (a, b) => this.compare(a.offset, b.offset)
            );
        },
    },
    methods: {
        compare(a, b) {
            if (a < b) return -1;
            else if (a > b) return 1;
            return 0;
        },
    }
}
</script>
<style scoped>
.topic-records-holder {
    padding: 1rem 1rem 0.75rem;
    border: 1px solid var(--form-control-border);
    border-radius: var(--form-control-border-radius);
}
</style>
