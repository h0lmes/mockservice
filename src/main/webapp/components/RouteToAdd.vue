<template>
    <div class="row">
        <div class="item w2">
            <div class="fancy-row-block-value link" @click="filter(route.group)">{{ route.group }}</div>
        </div>
        <div class="item">
            <div class="fancy-row-block-value link" @click="filter(route.method)">{{ route.method }}</div>
        </div>
        <div class="item w3">
            <div class="fancy-row-block-value link" @click="filter(route.path)">{{ route.path }}</div>
        </div>
        <div class="item w2">
            <div class="fancy-row-block-value link color-accent-one" @click="filter(route.suffix)">{{ route.suffix }}</div>
        </div>
        <div class="item w2">
            <a class="btn btn-default" @click="$emit('add', route)">add</a>
        </div>
    </div>
</template>
<script>
    import {mapActions} from 'vuex';

    export default {
        name: "RouteToAdd",
        data() {
            return {}
        },
        props: {
            route: {type: Object},
            groupStart: {type: Boolean}
        },
        methods: {
            ...mapActions(['saveRoute']),
            filter(value) {
                this.$emit('filter', value);
            },
        }
    }
</script>
<style lang="scss" scoped>
    .row {
        display: flex;
        flex-wrap: wrap;
        font-size: smaller;
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

        &.w1 {
            flex: 1;
        }

        &.w2 {
            flex: 2;
        }

        &.w3 {
            flex: 3;
        }

        & .btn {
            padding: 0 0.3rem;
            margin: -0.3rem 0;
        }
    }
</style>