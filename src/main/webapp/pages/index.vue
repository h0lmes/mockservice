<template>
    <div class="monospace">
        <div class="mb-2">
            <input id="search" placeholder="search here or click on values" type="text" class="form-control noborder" @input="debounce($event.target.value)"/>
        </div>
        <p class="mb-2">
            <a class="btn btn-link mr-4" @click="newRoute">Add route</a>
            <a class="btn btn-link mr-4" @click="setFilter('')">Clear filter</a>
        </p>
        <div class="holder">
            <p v-if="$fetchState.pending" class="mb-3">Loading...</p>
            <p v-if="filtered.length === 0">No routes found</p>
            <div v-else>
                <Routes :routes="filtered" @filter="setFilter($event)"></Routes>
            </div>
        </div>
    </div>
</template>
<script>
    import {mapActions} from 'vuex';
    import Routes from "../components/Routes";

    export default {
        name: "index",
        components: {Routes},
        data() {
            return {
                query: '',
                queryApplied: '',
                timeout: null
            }
        },
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
                        || v.suffix.toLowerCase().includes(this.query)
                );
            },
        },
        async fetch() {
            return this.fetchRoutes();
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