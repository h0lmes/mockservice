<template>
    <div class="row">
        <div class="item w-fixed-auto">
            <button type="button" class="btn btn-sm btn-default" @click="$emit('add', route)">ADD</button>
        </div>
        <div class="item w2">
            <div class="mock-col-value">{{ route.group }}</div>
        </div>
        <div class="item">
            <div class="mock-col-value">{{ route.method }}</div>
        </div>
        <div class="item w3">
            <div class="mock-col-value">{{ route.path }}</div>
        </div>
        <div class="item w2">
            <div class="mock-col-value">{{ route.alt }}</div>
        </div>
    </div>
</template>
<script>
    export default {
        name: "RouteToAdd",
        data() {
            return {}
        },
        props: {
            route: {type: Object},
            groupStart: {type: Boolean}
        },
    }
</script>
<style lang="scss" scoped>
    .row {
        display: flex;
        flex-wrap: wrap;
        align-items: center;
        row-gap: 0.3rem;
        column-gap: 0.5rem;
        margin: 0;
        padding: 0.5rem 0.7rem;
    }

    .item {
        flex: 1 1 0;
        box-sizing: border-box;
        margin: 0;
        padding: 0;

        &.w-fixed-auto {
            flex: 0 0 auto;
            min-width: initial;
        }

        &.w2 {
            flex: 2 1 0;
        }

        &.w3 {
            flex: 3 1 0;
        }
    }
</style>