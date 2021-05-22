<template>
    <div class="routes-holder">
        <p v-if="pending">Loading...</p>
        <p v-else-if="error">Error while fetching</p>
        <p v-else-if="routes.empty">No routes</p>
        <div v-else>
            <div v-for="(route, index) in routes" :key="index">
                <Route :route="route"></Route>
            </div>
        </div>
    </div>
</template>
<script>
    import Route from "../components/Route";
    export default {
        name: "Routes",
        components: {Route},
        props: ["routes", "pending", "error"],
    }
</script>
<style scoped>
    .routes-holder {
        margin: 0 auto;
    }
</style>