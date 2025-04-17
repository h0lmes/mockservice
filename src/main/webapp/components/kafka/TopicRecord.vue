<template>
    <div class="topic-record-row">
        <div class="topic-record-col">{{entity.offset}}</div>
        <div class="topic-record-col">{{entity.timestamp}}</div>
        <div class="topic-record-col w2">{{entity.key}}</div>
        <div class="topic-record-col w5">{{entity.value}}</div>
    </div>
</template>
<script>
export default {
        name: "TopicRecord",
        data() {
            return {}
        },
        props: {
            entity: {type: Object},
        },
        computed: {
            open() {
                return true;
            },
        },
        methods: {
        }
    }
</script>
<style scoped lang="scss">
.topic-record-row {
    display: flex;
    gap: 0.5rem 1rem;
    flex-wrap: wrap;
    justify-content: center;
    align-content: center;
    align-items: center;
    margin: 0;
    padding: 0;
    border: none;
}
.topic-record-col {
    flex: 1 1 0;
    position: relative;
    box-sizing: border-box;
    min-width: 5rem;
    text-align: center;

    &.w2 {
        flex: 3 1 0;
    }

    &.w5 {
        flex: 5 1 0;
    }
}
</style>
