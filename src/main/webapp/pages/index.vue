<template>
    <div ref="routes" class="monospace">
        <div class="component-toolbar mb-5">
            <div class="toolbar-item">
                <input ref="search"
                       type="search"
                       class="form-control monospace"
                       placeholder="search here or click on values"
                       @input="debounce($event.target.value)"/>
            </div>
            <button type="button" class="toolbar-item-fixed btn" @click="setFilter('')">Clear filter</button>
            <button type="button" class="toolbar-item-fixed btn" @click="addRoute">Add route</button>
            <button type="button" class="toolbar-item-fixed btn btn-danger" @click="deleteVisibleRoutes">Delete visible routes</button>
            <ViewSelector class="toolbar-item-fixed"></ViewSelector>
        </div>

        <Routes :routes="filtered" @filter="setFilter($event)"></Routes>

        <div class="color-secondary mt-4 smaller">(middle-click Route to edit)</div>

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
                return this.$store.state.routes.routes
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
            ...mapActions({
                fetchRoutes: 'routes/fetch',
                newRoute: 'routes/add',
                deleteRoutes: 'routes/delete',
            }),
            addRoute() {
                this.newRoute();
                this.$refs.routes.scrollTop = 0;
            },
            deleteVisibleRoutes() {
                if (confirm('Are you sure you want to delete all visible routes?\n'
                    + 'Note: if filtered you only delete those you see on the screen.')) {
                    this.$nuxt.$loading.start();
                    this.deleteRoutes(this.filtered)
                        .then(() => this.$nuxt.$loading.finish());
                }
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