<template>
    <div class="row">
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
        <div class="item w2">
            <button type="button" class="btn btn-sm btn-default" @click="$emit('add', route)">ADD</button>
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
        margin-bottom: 0;
        padding: 0 .3rem;
        border-radius: 5px;
    }

    .item {
        flex: 1;
        box-sizing: border-box;
        margin: 0;
        padding: .8rem .5rem;
        vertical-align: top;
        text-align: center;

        &.w2 {
            flex: 2;
        }

        &.w3 {
            flex: 3;
        }

        & .btn {
            margin: -0.3rem 0;
        }
    }
</style>