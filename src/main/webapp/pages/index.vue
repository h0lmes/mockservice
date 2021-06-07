<template>
    <div class="monospace">

        <div class="mb-2">
            <input id="search" placeholder="search here or click on values" type="text" class="form-control noborder" @input="debounce($event.target.value)"/>
        </div>
        <p class="mb-2">
            <a class="btn btn-link mr-3" @click="newRoute">Add route</a>
            <a class="btn btn-link mr-3" @click="setFilter('')">Clear filter</a>
        </p>
        <div class="holder">
            <Routes :routes="filtered" @filter="setFilter($event)"></Routes>
        </div>

        <Loading v-if="$fetchState.pending"></Loading>
    </div>
</template>
<script>
    import {mapActions} from 'vuex';
    import Routes from "../components/Routes";
    import Loading from "../components/Loading";

    export default {
        name: "index",
        components: {Routes, Loading},
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
            routes () {
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
            ...mapActions({
                fetchRoutes: 'fetchRoutes',
                newRoute: 'newRoute'
            }),
            debounce(value) {
                clearTimeout(this.timeout);
                let that = this;
                this.timeout = setTimeout(function () {
                    that.query = value.toLowerCase();
                }, 500);
            },
            setFilter(value) {
                document.querySelector('#search').value = value;
                this.query = value.toLowerCase();
            },
        }
    }
</script>
<style scoped>
    .holder {
        margin: 0 auto;
    }
</style>