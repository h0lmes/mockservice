<template>
    <div class="monospace">
        <div class="component-toolbar mb-5">
            <div class="toolbar-item">
                <input ref="search"
                       type="search"
                       placeholder="search here or click on values"
                       class="form-control monospace"
                       @input="debounce($event.target.value)"/>
            </div>
            <button type="button" class="toolbar-item-w-fixed-auto btn" @click="setFilter('')">Clear filter</button>
            <button type="button" class="toolbar-item-w-fixed-auto btn" @click="add">Add scenario</button>
        </div>

        <Scenarios :scenarios="filtered"
                   :activeScenarios="activeScenarios"
                   @filter="setFilter($event)"
        ></Scenarios>

        <div class="color-secondary mt-4 smaller">(middle-click to edit, Esc on any field to cancel)</div>
        <div class="color-secondary mt-2 smaller">MAP: search all routes by METHOD + PATH, return ALT from the first match or empty if not found.</div>
        <div class="color-secondary mt-2 smaller">QUEUE: same as MAP but tries to match only topmost route, removes matched route from the queue.</div>
        <div class="color-secondary mt-2 smaller">CIRCULAR QUEUE: same as QUEUE plus auto-restart when queue depleted.</div>

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