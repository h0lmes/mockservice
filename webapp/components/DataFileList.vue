<template>
    <div class="card monospace">
        <h2>Data files</h2>
        <p v-if="pending">Loading...</p>
        <p v-else-if="error">Error while fetching</p>
        <p v-else-if="datafiles.empty">No files</p>
        <ul v-else>
            <li v-for="(datafile, index) in datafiles" :key="index">
                <DataFileInfo :datafile="datafile"></DataFileInfo>
            </li>
        </ul>
    </div>
</template>
<script>
    import DataFileInfo from "../components/DataFileInfo";
    export default {
        name: "DataFileList",
        components: {DataFileInfo},
        data() {
            return {};
        },
        props: ["datafiles", "pending", "error"],
    }
</script>
<style scoped>
    ul {
        list-style: none;
        padding: 0;
        margin: 0;
    }
    li {
        margin: .2rem 0;
    }
</style>