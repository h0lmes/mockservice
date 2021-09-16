<template>
    <div class="component component-row" :class="{'open' : open}" @click="toggle">
        <div class="mock-col w-fixed-auto">
            <button type="button" class="btn btn-sm btn-default" @click.stop="$emit('add', route)">{{ addLabel }}</button>
        </div>
        <div class="mock-col">
            <div class="mock-col-value" :class="{'color-accent-one' : exists}">{{ existsLabel }}</div>
        </div>
        <div class="mock-col">
            <div class="mock-col-value">{{ route.group }}</div>
        </div>
        <div class="mock-col">
            <div class="mock-col-value">
                <route-method :value="route.method">{{ route.method }}</route-method>
            </div>
        </div>
        <div class="mock-col w3">
            <div class="mock-col-value">{{ route.path }}</div>
        </div>
        <div class="mock-col">
            <div class="mock-col-value">{{ route.alt }}</div>
        </div>
        <div class="mock-col">
            <div class="mock-col-value link" :class="{'color-accent-one' : more}">{{ moreLabel }}</div>
        </div>
        <div class="mock-col w100" v-show="open" @click.stop>
            <AutoSizeTextArea v-model="route.response" placeholder="NO RESPONSE" :min-rows="1"></AutoSizeTextArea>
        </div>
        <div class="mock-col w100" v-show="open" @click.stop>
            <AutoSizeTextArea v-model="route.requestBodySchema" placeholder="NO REQUEST BODY SCHEMA" :min-rows="1"></AutoSizeTextArea>
        </div>
    </div>
</template>
<script>
    import AutoSizeTextArea from "./AutoSizeTextArea";
    import RouteMethod from "./route/RouteMethod";

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
</style>