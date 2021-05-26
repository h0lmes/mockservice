<template>
    <div>
        <div class="mb-2">
            <input id="search" placeholder="search here or click on values" type="text" class="form-control noborder" @input="debounce($event.target.value)"/>
        </div>
        <p class="mb-2">
            <a class="btn btn-link mr-4" @click="newRoute">Add route</a>
            <a class="btn btn-link mr-4" @click="setFilter('')">Clear filter</a>
        </p>
        <div class="routes-holder">
            <p v-if="pending">Loading...</p>
            <p v-else-if="error">Error while fetching</p>
            <p v-else-if="routes.empty">
                No routes found
            </p>
            <div v-else>
                <div v-for="(route, index) in filtered" :key="route.type + route.method + route.path + route.suffix">
                    <Route :route="route" @filter="setFilter($event)"></Route>
                </div>
            </div>
        </div>
    </div>
</template>
<script>
    import {mapActions} from 'vuex';
    import Route from "../components/Route";

    export default {
        name: "Routes",
        data() {
            return {
                query: '',
                queryApplied: '',
                timeout: null
            }
        },
        components: {Route},
        props: ["routes", "pending", "error"],
        computed: {
            filtered() {
                if (!this.query.trim())
                    return this.routes;

                return this.routes.filter(
                    r => r.group.toLowerCase().includes(this.query)
                        | r.path.toLowerCase().includes(this.query)
                        | r.type.toLowerCase().includes(this.query)
                        | r.method.toLowerCase().includes(this.query)
                        | r.suffix.toLowerCase().includes(this.query)
                );
            }
        },
        methods: {
            ...mapActions({
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
            }
        }
    }
</script>
<style scoped>
    .routes-holder {
        margin: 0 auto;
    }
</style>