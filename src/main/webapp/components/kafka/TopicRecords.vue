<template>
    <div class="topic-records-holder">
        <TopicRecord class="mb-3" :entity="header"></TopicRecord>
        <div v-if="entities.length === 0" class="mt-2 text-center">
            No records yet
        </div>
        <TopicRecord class="mt-2"
                     :entity="entity"
                     v-for="(entity, index) in sortedEntities"
                     :key="entity.offset"/>
    </div>
</template>
<script>
import TopicRecord from "./TopicRecord";

export default {
    name: "TopicRecords",
    components: {TopicRecord},
    data() {
        return {
            header: {
                offset: "OFFSET",
                timestamp: "TIMESTAMP",
                key: "KEY",
                value: "VALUE",
            }
        }
    },
    props: {
        entities: {type: Array},
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
    width: 100%;
    padding: 1rem;
    border: 1px solid var(--form-control-border);
    border-radius: var(--form-control-border-radius);
}
</style>
