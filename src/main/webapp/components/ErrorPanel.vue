<template>
    <div v-if="lastError" class="wrapper monospace red mb-3">
        <p>{{ lastError }}</p>
        <a class="btn btn-link" @click="resetLastError">Click to dismiss</a>
    </div>
</template>
<script>
    import {mapActions} from 'vuex';

    export default {
        name: "ErrorPanel",
        data() {
            return {}
        },
        computed: {
            lastError () {
                return this.$store.state.lastError
            }
        },
        methods: {
            ...mapActions({
                resetLastError: 'resetLastError'
            })
        }
    }
</script>
<style scoped>
    .wrapper {
        padding: 1em 1em .5em;
        background: repeating-linear-gradient(
                -45deg,
                var(--bg-primary),
                var(--bg-primary) 10px,
                var(--bg-secondary) 10px,
                var(--bg-secondary) 20px
        );
    }
</style>