<template>
    <div class="route-holder monospace">
        <div class="route-field">
            <div class="route-field-header">GROUP</div>
            <div class="route-field-value">{{ route.group }}</div>
        </div>
        <div class="route-field">
            <div class="route-field-header">TYPE</div>
            <div class="route-field-value" :class="{ 'blue' : routeIsSoap }">{{ route.type }}</div>
        </div>
        <div class="route-field">
            <div class="route-field-header">METHOD</div>
            <div class="route-field-value">{{ route.method }}</div>
        </div>
        <div class="route-field route-field-wide">
            <div class="route-field-header">PATH</div>
            <div class="route-field-value">{{ route.path }}</div>
        </div>
        <div class="route-field">
            <div class="route-field-header">SUFFIX</div>
            <div class="route-field-value">{{ route.suffix }}</div>
        </div>
        <div class="route-field">
            <div class="route-field-header">DISABLED</div>
            <div class="route-field-value" :class="{ 'red' : route.disabled }">{{ disabled }}</div>
        </div>
    </div>
</template>
<script>
    export default {
        name: "Route",
        props: {
            route: {
                type: Object,
                default: {}
            }
        },
        computed: {
            routeIsSoap() {
                return this.route.type === 'SOAP'
            },
            disabled() {
                return this.route.disabled ? 'yes' : 'no'
            }
        }
    }
</script>
<style scoped>
    .route-holder {
        margin-bottom: 2rem;
        padding: .1rem 2rem;
        background-color: var(--bg-secondary);
        border-radius: 5px;
        font-size: smaller;
    }
    .route-field {
        display: inline-block;
        box-sizing: border-box;
        margin: 0;
        padding: .8rem .5rem;
        min-width: 7%;
        vertical-align: top;
    }
    .route-field-wide {
        min-width: 25%;
    }
    .route-field-header {
        color: var(--color-primary);
        width: 100%;
        text-align: center;
        margin-bottom: .5rem;
    }
    .route-field-value {
        color: var(--color-secondary);
        width: 100%;
        text-align: center;
    }
    .red {
        color: var(--red);
    }
    .blue {
        color: var(--cyan);
    }
</style>