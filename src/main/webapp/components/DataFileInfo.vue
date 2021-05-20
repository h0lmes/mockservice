<template>
    <div class="holder">
        <span class="source">RESOURCE</span>
        <span class="part group">{{ datafile.group }}</span><span class="part name" v-if="datafile.group">-</span><span class="part http-method">{{ datafile.method }}</span><span class="part name" v-if="datafile.method">-</span><span class="part name">{{ datafile.path }}</span><span class="part name" v-if="datafile.suffix">--</span><span class="part suffix">{{ datafile.suffix }}</span>
    </div>
</template>
<script>
    export default {
        name: "DataFileInfo",
        data() {
            return {};
        },
        props: {
            datafile: Object
        },
        computed: {

        }
    }
</script>
<style scoped>
    .holder {
        padding: .1rem;
    }
    .source {
        font-size: .7rem;
        background-color: #317373;
        color: rgba(255,255,255,.5);
        border: 0 none;
        border-radius: .25rem;
        padding: .1rem .3rem;
        margin: 0 .3rem 0 0;
    }
    .part {
        display: inline;
        margin: 0;
        padding: 0;
    }
    .group {
        color: rgb(203 141 255);
    }
    .http-method {
        color: rgb(140 255 153);
    }
    .suffix {
        color: rgb(255 218 47);
    }
    .ext {
        color: rgb(255 91 143);
    }
</style>