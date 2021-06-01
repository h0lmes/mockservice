<template>
    <div v-if="lastError" class="wrapper monospace" @click="resetLastError">
        <div class="content">
            <p class="red">{{ lastError }}</p>
            <p>Click anywhere to dismiss</p>
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
            ...mapActions({
                resetLastError: 'resetLastError'
            })
        }
    }
</script>
<style scoped>
    .wrapper {
        position: fixed;
        top: 0;
        bottom: 0;
        left: 0;
        right: 0;
        z-index: 1001;
        overflow: hidden auto;
        background: rgba(0, 0, 0, 0.6);
    }

    .content {
        position: relative;
        margin: 5em auto;
        padding: 1em;
        width: 50%;
        border-radius: 4px;
        background: repeating-linear-gradient(
                -45deg,
                var(--bg-primary),
                var(--bg-primary) 10px,
                var(--bg-secondary) 10px,
                var(--bg-secondary) 20px
        );
    }

    @media screen and (max-width: 1099px) /*and (orientation: landscape)*/ {
        .content {
            width: 90%;
        }
    }
</style>