<template>
    <div id="page">
        <div id="page-contents">
            <div class="card">
                <h1>Data files</h1>

                <p v-if="$fetchState.pending">Loading....</p>
                <p v-else-if="$fetchState.error">Error while fetching</p>
                <ul v-else>
                    <li v-for="(datafile, index) in datafiles" :key="index">
                        {{ datafile }}
                    </li>
                </ul>
            </div>
        </div>
    </div>
</template>

<script>
    export default {
        name: "index",
        data() {
            return {
                datafiles: []
            }
        },
        async fetch() {
            // TODO remove hardcoded localhost
            this.datafiles = await fetch(
                'http://localhost:8081/web-api/datafiles'
            ).then(res => res.json())
        }
    }
</script>

<style scoped>

</style>