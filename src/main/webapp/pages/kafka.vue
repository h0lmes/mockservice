<template>
    <div class="monospace">
        <div class="component-toolbar mb-3">
            <div class="toolbar-item">
                <input ref="search"
                       type="text"
                       class="form-control monospace"
                       placeholder="type in or click on values (group or topic)"
                       @keydown.enter.exact.stop="filter($event.target.value)"/>
            </div>
            <button type="button" class="toolbar-item-w-fixed-auto btn" @click="filter('')">Clear search</button>
            <ToggleSwitch class="toolbar-item toolbar-item-w-fixed-auto" v-model="jsSearch">JS</ToggleSwitch>
        </div>

        <div class="component-toolbar mb-3">
            <button type="button" class="toolbar-item-w-fixed-auto btn" @click="addTopic">Add topic-partition</button>
            <ViewSelector class="toolbar-item toolbar-item-w-fixed-auto" :storageKey="'CompactView-Kafka'"></ViewSelector>
        </div>

        <Topics :entities="filteredEntities"></Topics>

        <Loading v-if="$fetchState.pending"></Loading>
    </div>
</template>
<script>
import {mapActions} from 'vuex';
import Topics from "../components/kafka/Topics";
import Loading from "../components/other/Loading";
import ViewSelector from "../components/other/ViewSelector";
import ToggleSwitch from "../components/other/ToggleSwitch";

export default {
    name: "kafka",
    components: {Topics, Loading, ViewSelector, ToggleSwitch},
    data() {
        return {
            query: '',
            timeout: null,
            jsSearch: false,
        }
    },
    async fetch() {
        return this.fetchTopics();
    },
    fetchDelay: 0,
    computed: {
        searchExpression() {
            return (this.$store.state.kafkaSearchExpression || '').trim();
        },
        topics() {
            return this.$store.state.kafka.topics;
        },
        entities() {
            return [...this.topics];
        },
        filteredEntities() {
            if (!this.query) return this.entities;

            try {
                return this.entities.filter(this.getSearchFn());
            } catch (e) {
                console.error(e);
                return [];
            }
        },
    },
    mounted() {
        this.$refs.search.value = this.searchExpression;
        this.query = this.searchExpression;
    },
    watch: {
        searchExpression(newValue) {
            this.$refs.search.value = newValue;
            this.query = newValue;
        },
    },
    methods: {
        ...mapActions({
            filter: 'setKafkaSearchExpression',

            fetchTopics: 'kafka/fetch',
            addTopicAction: 'kafka/add',
        }),
        addTopic() {
            this.addTopicAction()
        },
        getSearchFn() {
            if (this.jsSearch) {
                return Function("e", "return " + this.query + ";");
            } else if (!this.query) {
                return (e) => true;
            } else {
                const query = this.query;
                return (e) => {
                    return e.group.includes(query)
                        || e.topic.includes(query);
                };
            }
        }
    }
}
</script>
<style scoped>
</style>
