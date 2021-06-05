<template>
    <div class="monospace">

        <div class="mb-2">
            <input id="search" placeholder="search here or click on values" type="text" class="form-control noborder" @input="debounce($event.target.value)"/>
        </div>
        <p class="mb-2">
            <a class="btn btn-link mr-4" @click="newScenario">Add scenario</a>
            <a class="btn btn-link mr-4" @click="setFilter('')">Clear filter</a>
        </p>
        <div class="holder">
            <Scenarios :scenarios="filtered"
                       :activeScenarios="activeScenarios"
                       @filter="setFilter($event)"></Scenarios>
        </div>

        <div v-if="addRouteToScenarioAlias" class="routes" @click="closeAddRouteToScenario()">
            routes
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
                queryApplied: '',
                timeout: null
            }
        },
        async fetch() {
            return this.fetchScenarios().then(this.fetchActiveScenarios);
        },
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
            addRouteToScenarioAlias() {
                return this.$store.state.addRouteToScenarioAlias
            }
        },
        methods: {
            ...mapActions(['fetchScenarios', 'newScenario', 'fetchActiveScenarios', 'closeAddRouteToScenario']),
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
    .routes {
        display: block;
        position: absolute;
        top: 0;
        bottom: 0;
        left: 0;
        right: 0;
        z-index: 1000;
        background-color: var(--bg-primary);
        padding: 2em;
    }
</style>