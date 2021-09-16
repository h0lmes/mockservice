<template>
    <div>
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
</style>