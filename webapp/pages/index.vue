<template>
    <div id="page">
        <div id="page-contents">
            <DataFileList :datafiles="datafiles"
                          :pending="$fetchState.pending"
                          :error="$fetchState.error">
            </DataFileList>
        </div>
    </div>
</template>
<script>
    import { mapActions } from 'vuex';
    import DataFileList from "../components/DataFileList";
    export default {
        name: "index",
        components: {DataFileList},
        data() {
            return {}
        },
        computed: {
            datafiles () {
                return this.$store.state.datafiles
            }
        },
        async fetch() {
            this.fetchDataFiles();
        },
        methods: {
            ...mapActions({
                fetchDataFiles: 'fetchDataFiles'
            })
        }
    }
</script>
<style scoped>

</style>