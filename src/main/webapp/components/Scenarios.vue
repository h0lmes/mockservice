<template>
    <div>
        <div v-for="(scenario, index) in scenarios" :key="scenario.alias">
            <Scenario :scenario="scenario"
                      :active="false"
                      :groupStart="groupStart(scenario, index)"
                      @filter="$emit('filter', $event)"></Scenario>
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
        props: ["scenarios"],
        methods: {
            groupStart(scenario, index) {
                return index === 0 || scenario.group !== this.scenarios[index - 1].group;
            }
        }
    }
</script>
<style scoped>
</style>