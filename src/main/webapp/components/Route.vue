<template>
    <div class="route-holder monospace">
        <div class="field">
            <div class="header">GROUP</div>
            <div class="value">{{ route.group }}</div>
        </div>
        <div class="field">
            <div class="header">TYPE</div>
            <div class="value">{{ route.type }}</div>
        </div>
        <div class="field">
            <div class="header">METHOD</div>
            <div class="value">{{ route.method }}</div>
        </div>
        <div class="field wide-field">
            <div class="header">PATH</div>
            <div class="value">{{ route.path }}</div>
        </div>
        <div class="field">
            <div class="header">SUFFIX</div>
            <div class="value">{{ route.suffix }}</div>
        </div>
        <div class="field">
            <div class="header">DISABLED</div>
            <div class="value">{{ route.disabled }}</div>
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
    }
</script>
<style scoped>
    .route-holder {
        margin-bottom: 2rem;
        padding: .1rem;
        background-color: #212325;
        border-radius: 5px;
        font-size: smaller;
    }
    .field {
        display: inline-block;
        margin: 0;
        padding: 1rem 1.5rem;
        min-width: 6%;
        vertical-align: top;
    }
    .wide-field {
        min-width: 20%;
    }
    .header {
        color: #6a6d76;
        width: 100%;
        text-align: center;
        margin-bottom: .5rem;
    }
    .value {
        color: #c0c4d0;
        width: 100%;
        text-align: center;
    }
</style>