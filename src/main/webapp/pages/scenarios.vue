<template>
    <div class="monospace">

        <div class="mb-2">
            <input id="search" placeholder="search here or click on values" type="text" class="form-control noborder" @input="debounce($event.target.value)"/>
        </div>
        <p class="mb-2">
            <button type="button" class="btn btn-link mr-3" @click="newScenario">Add scenario</button>
            <button type="button" class="btn btn-link mr-3" @click="setFilter('')">Clear filter</button>
        </p>
        <div class="holder">
            <Scenarios :scenarios="filtered"
                       :activeScenarios="activeScenarios"
                       @filter="setFilter($event)"></Scenarios>
        </div>

        <Loading v-if="$fetchState.pending"></Loading>
    </div>
</template>
<script>
    import {mapActions} from 'vuex';
    import Scenarios from "../components/Scenarios";
    import Loading from "../components/Loading";

    export default {
        name: "scenarios",
        components: {Scenarios, Loading},
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
                return this.$store.state.scenarios
            },
            activeScenarios() {
                return this.$store.state.activeScenarios
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
            ...mapActions(['fetchScenarios', 'newScenario', 'fetchActiveScenarios']),
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