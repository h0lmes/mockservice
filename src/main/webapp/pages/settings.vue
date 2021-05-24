<template>
    <div class="monospace">
        <p class="danger">Use with caution!</p>
        <p class="danger mb-5">It is easy to ruin config by editing it as plain text.</p>
        <textarea class="form-control form-control-sm v-resize" rows="16" v-model="config.data"></textarea>
        <!--<div class="btn btn-sm btn-danger mt-5" @click="save">SAVE</div>-->
    </div>
</template>
<script>
    import { mapActions } from 'vuex';
    export default {
        name: "scenarios",
        data() {
            return {}
        },
        computed: {
            config () {
                return this.$store.state.config
            }
        },
        async fetch() {
            this.fetchConfig();
        },
        methods: {
            ...mapActions({
                fetchConfig: 'fetchConfig'
            }),
            save() {
                if (confirm('Ye be warned =)')) {
                    //
                }
            }
        }
    }
</script>
<style scoped>
    .danger {
        color: var(--red);
    }
</style>