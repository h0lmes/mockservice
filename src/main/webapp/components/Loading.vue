<template>
    <div class="modal-backdrop transparent">
        <div class="modal-border">
            <div class="modal-content monospace loading">
                <p>LOADING</p>
            </div>
        </div>
    </div>
</template>
<script>
    export default {
        name: "Loading"
    }
</script>
<style scoped>
    .loading {
        font-weight: 700;
    }
</style>