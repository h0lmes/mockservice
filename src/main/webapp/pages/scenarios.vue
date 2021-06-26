<template>
    <div class="monospace">

        <div class="component component-toolbar mb-5">
            <input ref="search"
                   type="search"
                   placeholder="search here or click on values"
                   class="form-control"
                   @input="debounce($event.target.value)"/>
            <button type="button" class="btn" @click="setFilter('')">Clear filter</button>
            <button type="button" class="btn" @click="add">Add scenario</button>
            <ViewSelector></ViewSelector>
        </div>

        <Scenarios :scenarios="filtered"
                   :activeScenarios="activeScenarios"
                   @filter="setFilter($event)"></Scenarios>

        <Loading v-if="$fetchState.pending"></Loading>
    </div>
</template>
<script>
    import {mapActions} from 'vuex';
    import Scenarios from "../components/Scenarios";
    import Loading from "../components/Loading";
    import ViewSelector from "../components/ViewSelector";

    export default {
        name: "scenarios",
        components: {Scenarios, Loading, ViewSelector},
        data() {
            return {
                query: '',
                timeout: null
            }
        },
        async fetch() {
            return this.fetchScenarios().then(this.fetchActiveScenarios);
        },
        fetchDelay: 0,
        computed: {
            scenarios() {
                return this.$store.state.scenarios.scenarios
            },
            activeScenarios() {
                return this.$store.state.scenarios.activeScenarios
            },
            filtered() {
                if (!this.query.trim())
                    return this.scenarios;

                return this.scenarios.filter(
                    v => v.group.toLowerCase().includes(this.query)
                        || v.alias.toLowerCase().includes(this.query)
                        || v.type.toLowerCase().includes(this.query)
                );
            },
        },
        methods: {
            ...mapActions({
                fetchScenarios: 'scenarios/fetch',
                add: 'scenarios/add',
                fetchActiveScenarios: 'scenarios/fetchActive',
            }),
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