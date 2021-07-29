<template>
    <div class="bold" :class="color">{{ value }}</div>
</template>
<script>
    export default {
        name: "RouteMethod",
        props: {
            value: {type: String},
            disabled: {type: Boolean, default: false},
        },
        computed: {
            color() {
                return {
                    'teal': !this.disabled && this.value === 'GET',
                    'blue': !this.disabled && this.value === 'POST',
                    'yellow': !this.disabled && this.value === 'PUT',
                    'orange': !this.disabled && this.value === 'PATCH',
                    'pink': !this.disabled && this.value === 'DELETE',
                }
            }
        },
    }
</script>
<style scoped>
</style>