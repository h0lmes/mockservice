<template>
    <div :class="color">{{ value }}<span v-show="disabled"> (disabled)</span></div>
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
                'bold': !this.disabled,
                'green': !this.disabled && this.value === 'GET',
                'blue': !this.disabled && this.value === 'POST',
                'orange-yellow': !this.disabled && this.value === 'PUT',
                'orange': !this.disabled && this.value === 'PATCH',
                'pink': !this.disabled && this.value === 'DELETE',
            }
        }
    },
}
</script>
<style scoped>
</style>
