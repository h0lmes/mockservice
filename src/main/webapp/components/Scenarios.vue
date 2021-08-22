<template>
    <div>
        <p v-if="scenarios.length === 0">No scenarios</p>
        <div v-for="(scenario, index) in scenarios" :key="scenario.alias">
            <div v-if="groupStart(scenario, index)" class="component-row-group-boundary"></div>
            <Scenario :scenario="scenario" @filter="$emit('filter', $event)"></Scenario>
        </div>
    </div>
</template>
<script>
    import Scenario from "../components/Scenario";

    export default {
        name: "Scenarios",
        data() {
            return {}
        },
        components: {Scenario},
        props: {
            scenarios: {type: Array},
        },
        methods: {
            groupStart(scenario, index) {
                return index > 0
                    && scenario.group !== this.scenarios[index - 1].group;
            },
        }
    }
</script>
<style scoped>
</style>