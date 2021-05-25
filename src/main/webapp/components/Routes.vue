<template>
    <div>
        <p class="mb-3">
            <input placeholder="search..." type="text" class="form-control noborder" @keyup="debounce($event.target.value)"/>
        </p>
        <div class="mb-2">
            <a class="btn btn-link" @click="newRoute">New route</a>
        </div>
        <div class="routes-holder">
            <p v-if="pending">Loading...</p>
            <p v-else-if="error">Error while fetching</p>
            <p v-else-if="routes.empty">No routes</p>
            <div v-else>
                <div v-for="route in filtered" :key="route.type + route.method + route.path + route.suffix">
                    <Route :route="route"></Route>
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
            }
        }
    }
</script>
<style scoped>
    .routes-holder {
        margin: 0 auto;
    }
</style>