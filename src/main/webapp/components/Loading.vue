<template>
    <div class="wrapper monospace">
        <div class="content">
            <p>Loading ...</p>
        </div>
    </div>
</template>
<script>
    export default {
        name: "Loading"
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
        background: rgba(0, 0, 0, 0.1);
    }

    .content {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        padding: 1em;
        border-radius: 4px;
        background: var(--bg-primary);
    }

    @media screen and (max-width: 1099px) /*and (orientation: landscape)*/ {
        .content {
            width: 90%;
        }
    }
</style>