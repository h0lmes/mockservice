<template>
    <div class="bold" :class="color">{{ value }}</div>
</template>
<script>
    export default {
        name: "RouteMethod",
        props: {
            value: {type: String}
        },
        computed: {
            color() {
                return {
                    'green': this.value === 'GET',
                    'blue': this.value === 'POST',
                    'yellow': this.value === 'PUT',
                    'orange': this.value === 'PATCH',
                    'red': this.value === 'DELETE',
                }
            }
        },
    }
</script>
<style scoped>
</style>