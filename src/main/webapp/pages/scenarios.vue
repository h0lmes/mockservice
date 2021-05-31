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
            <p v-if="$fetchState.pending" class="mb-3">Loading...</p>
            <p v-if="filtered.length === 0">No scenarios found</p>
            <div v-else>
                <Scenarios :scenarios="filtered" @filter="setFilter($event)"></Scenarios>
            </div>
        </div>
    </div>
</template>
<script>
    import {mapActions} from 'vuex';
    import Scenarios from "../components/Scenarios";

    export default {
        name: "scenarios",
        components: {Scenarios},
        data() {
            return {
                query: '',
                queryApplied: '',
                timeout: null
            }
        },
        computed: {
            scenarios () {
                return this.$store.state.scenarios
            },
            filtered() {
                if (!this.query.trim())
                    return this.scenarios;

                return this.scenarios.filter(
                    o => o.group.toLowerCase().includes(this.query)
                        | o.alias.toLowerCase().includes(this.query)
                        | o.type.toLowerCase().includes(this.query)
                );
            },
        },
        async fetch() {
            return this.fetchScenarios();
        },
        methods: {
            ...mapActions({
                fetchScenarios: 'fetchScenarios',
                newScenario: 'newScenario'
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