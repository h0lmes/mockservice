<template>
    <div class="route__variables-container">
        <div class="mock-col-header w100">
            <span>VARIABLES (FROM RESPONSE BODY)</span>
        </div>
        <div v-for="variable in route.variables" :key="variable.name">
            <RouteVariable :route="route" :variable="variable"></RouteVariable>
        </div>
    </div>
</template>
<script>
    import RouteVariable from "./RouteVariable";

    export default {
        name: "RouteVariables",
        components: {RouteVariable},
        data() {
            return {}
        },
        props: {
            route: {type: Object}
        },
    }
</script>
<style scoped>
.route__variables-container {
    border-top: 1px solid var(--form-control-border);
    padding-top: 1rem;
}
</style>
