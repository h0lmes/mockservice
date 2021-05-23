<template>
    <div class="holder">
        <span class="part group">{{ datafile.group }}</span>
        <span class="part name" v-if="datafile.group">/</span>
        <span class="part http-method">{{ datafile.method }}</span>
        <span class="part name" v-if="datafile.method">-</span>
        <span class="part name">{{ datafile.path }}</span>
        <span class="part name" v-if="datafile.suffix">--</span>
        <span class="part suffix">{{ datafile.suffix }}</span>
        <span class="part ext">{{ ext }}</span>
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
            ext() {
                return this.datafile.type === 'REST' ? '.json' : '.xml';
            }
        }
    }
</script>
<style scoped>
    .holder {
        padding: .1rem;
    }
    .part {
        display: inline;
        margin: 0;
        padding: 0;
    }
    .group {
        color: var(--purple);
    }
    .http-method {
        color: var(--teal);
    }
    .suffix {
        color: var(--yellow);
    }
    .ext {
        color: var(--pink);
    }
</style>