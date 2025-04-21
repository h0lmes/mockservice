<template>
    <div class="monospace">
        <pre v-html="valueProcessed"></pre>
        <Loading v-if="$fetchState.pending"></Loading>
    </div>
</template>
<script>
import {mapActions} from 'vuex';
import Loading from "../components/other/Loading";

export default {
    name: "request-graph",
    components: {Loading},
    data() {
        return {
            value: ''
        }
    },
    async fetch() {
        return this.fetch()
            .then(response => {
                this.value = response;
            });
    },
    fetchDelay: 0,
    computed: {
        valueRaw() {
            return this.value || '';
        },
        valueProcessed() {
            return this.valueRaw.replaceAll('CYCLE', '<span class="red">CYCLE</span>');
        },
    },
    methods: {
        ...mapActions({
            fetch: 'request-graph/fetch',
        }),
    }
}
</script>
<style scoped>
</style>
