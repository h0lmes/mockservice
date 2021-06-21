<template>
    <div v-if="lastError" class="modal-backdrop" @click="resetLastError">
        <div class="modal-border">
            <div class="modal-content monospace red">
                <p>{{ lastError }}</p>
                <p>Click anywhere to dismiss</p>
            </div>
        </div>
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
            lastError() {
                return this.$store.state.lastError
            }
        },
        methods: {
            ...mapActions(['resetLastError'])
        }
    }
</script>
<style scoped>
</style>