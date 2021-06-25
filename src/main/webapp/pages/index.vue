<template>
    <div ref="routes" class="monospace">
        <div class="mb-3">
            <input ref="search" placeholder="search here or click on values" type="text" class="form-control"
                   @input="debounce($event.target.value)"/>
        </div>

        <div class="component component-toolbar mb-3">
            <button type="button" class="btn" @click="addRoute">Add route</button>
            <button type="button" class="btn" @click="setFilter('')">Clear filter</button>
            <ViewSelector></ViewSelector>
        </div>

        <Routes :routes="filtered" @filter="setFilter($event)"></Routes>

        <Loading v-if="$fetchState.pending"></Loading>
    </div>
</template>
<script>
    import {mapActions} from 'vuex';
    import Routes from "../components/Routes";
    import Loading from "../components/Loading";
    import ViewSelector from "../components/ViewSelector";

    export default {
        name: "index",
        components: {Routes, Loading, ViewSelector},
        data() {
            return {
                query: '',
                queryApplied: '',
                timeout: null
            }
        },
        async fetch() {
            return this.fetchRoutes();
        },
        fetchDelay: 0,
        computed: {
            routes() {
                return this.$store.state.routes
            },
            filtered() {
                if (!this.query.trim())
                    return this.routes;

                return this.routes.filter(
                    v => v.group.toLowerCase().includes(this.query)
                        || v.path.toLowerCase().includes(this.query)
                        || v.type.toLowerCase().includes(this.query)
                        || v.method.toLowerCase().includes(this.query)
                        || v.alt.toLowerCase().includes(this.query)
                );
            },
        },
        methods: {
            ...mapActions(['fetchRoutes', 'newRoute']),
            addRoute() {
                this.newRoute();
                this.$refs.routes.scrollTop = 0;
            },
            debounce(value) {
                clearTimeout(this.timeout);
                let that = this;
                this.timeout = setTimeout(function () {
                    that.query = value.toLowerCase();
                }, 500);
            },
            setFilter(value) {
                this.$refs.search.value = value;
                this.query = value.toLowerCase();
            },
        }
    }
</script>
<style scoped>
</style>