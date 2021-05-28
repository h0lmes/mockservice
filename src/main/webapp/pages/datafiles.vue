<template>
    <div>
        <DataFileList :datafiles="datafiles"
                      :pending="$fetchState.pending"
                      :error="$fetchState.error">
        </DataFileList>
    </div>
</template>
<script>
    import { mapActions } from 'vuex';
    import DataFileList from "../components/DataFileList";
    export default {
        name: "datafiles",
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
            return this.fetchDataFiles();
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