<template>
    <div>
        <p v-if="entities.length === 0">
            Nothing here yet ¯\_(ツ)_/¯
        </p>
        <div v-for="(entity, index) in sortedEntities" :key="entityKey(entity)">
            <div v-if="groupStart(sortedEntities, entity, index)" class="component-row-group-boundary"></div>
            <Topic
                :topic="entity"
                @filter="$emit('filter', $event)"
            ></Topic>
        </div>
    </div>
</template>
<script>
import Topic from "./Topic";

export default {
        name: "Topics",
        components: {Topic},
        data() {
            return {}
        },
        props: {
            entities: {type: Array},
        },
        computed: {
            sortedEntities() {
                return this.entities.sort((a, b) => {
                    // newly created entities at the very top
                    let c = this.compare(a._new, b._new);
                    if (c !== 0) return c;

                    // keep same group entities together
                    c = this.compare(a.group, b.group);
                    if (c !== 0) return c;

                    c = this.compare(a.topic, b.topic);
                    if (c !== 0) return c;

                    return this.compare(a.partition, b.partition);
                });
            },
        },
        methods: {
            compare(a, b) {
                if (a < b) return -1;
                else if (a > b) return 1;
                return 0;
            },
            groupStart(arr, entity, index) {
                return index > 0 && entity.group !== arr[index - 1].group;
            },
            entityKey(entity) {
                return entity.topic;
            },
        }
    }
</script>
<style scoped>
</style>
