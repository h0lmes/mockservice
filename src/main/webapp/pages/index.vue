<template>
    <div class="monospace">
        <Routes :routes="routes"
                :pending="$fetchState.pending"
                :error="$fetchState.error">
        </Routes>
    </div>
</template>
<script>
    import { mapActions } from 'vuex';
    import Routes from "../components/Routes";
    export default {
        name: "index",
        components: {Routes},
        data() {
            return {}
        },
        computed: {
            routes () {
                return this.$store.state.routes
            }
        },
        async fetch() {
            return this.fetchRoutes();
        },
        methods: {
            ...mapActions({
                fetchRoutes: 'fetchRoutes'
            })
        }
    }
</script>
<style scoped>

</style>