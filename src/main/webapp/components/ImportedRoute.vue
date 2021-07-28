<template>
    <div class="component row" :class="{'open' : open}" @click="toggle">
        <div class="item w-fixed-auto">
            <button type="button" class="btn btn-sm btn-default" @click.stop="$emit('add', route)">{{ addLabel }}</button>
        </div>
        <div class="item">
            <div class="mock-col-value" :class="{'color-accent-one' : exists}">{{ existsLabel }}</div>
        </div>
        <div class="item">
            <div class="mock-col-value">{{ route.group }}</div>
        </div>
        <div class="item">
            <div class="mock-col-value">
                <route-method :value="route.method">{{ route.method }}</route-method>
            </div>
        </div>
        <div class="item w3">
            <div class="mock-col-value">{{ route.path }}</div>
        </div>
        <div class="item">
            <div class="mock-col-value">{{ route.alt }}</div>
        </div>
        <div class="item">
            <div class="mock-col-value link" :class="{'color-accent-one' : more}">{{ moreLabel }}</div>
        </div>
        <div class="item w100" v-show="open" @click.stop>
            <AutoSizeTextArea v-model="route.response"></AutoSizeTextArea>
        </div>
        <div class="item w100" v-show="open && !!route.requestBodySchema" @click.stop>
            <div class="mock-col-header">REQUEST BODY SCHEMA</div>
            <AutoSizeTextArea v-model="route.requestBodySchema"></AutoSizeTextArea>
        </div>
    </div>
</template>
<script>
    import AutoSizeTextArea from "./AutoSizeTextArea";
    import RouteMethod from "./RouteMethod";

    export default {
        name: "ImportedRoute",
        components: {AutoSizeTextArea, RouteMethod},
        data() {
            return {
                open: false,
            }
        },
        props: {
            route: {type: Object},
            existingRoutes: {type: Array},
        },
        computed: {
            more() {
                return !!this.route.response || !!this.route.requestBodySchema;
            },
            moreLabel() {
                let label = '';
                if (!!this.route.response) label += 'has response';
                if (!!this.route.requestBodySchema) label += (!!label ? ', ' : '') + 'has request body schema';
                return !!label ? label : '-';
            },
            exists() {
                return this.existingRoutes.some(e => e.method === this.route.method && e.path === this.route.path && e.alt === this.route.alt);
            },
            existsLabel() {
                return this.exists ? 'exists' : '-';
            },
            addLabel() {
                return this.exists ? 'overwrite' : '\u00A0\u00A0 add \u00A0\u00A0';
            }
        },
        methods: {
            toggle() {
                this.open = !this.open;
            },
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
        margin-bottom: 1px;
        padding: 0.2rem 0.7rem;

        &.open {
            background-color: var(--bg-component-active);
        }
    }

    .item {
        flex: 1 1 0;
        box-sizing: border-box;
        margin: 0;
        padding: 0.3rem 0;

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

        &.w100 {
            flex: 0 0 100%;
            text-align: center;
        }
    }
</style>