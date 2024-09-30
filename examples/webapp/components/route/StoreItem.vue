<template>
    <div class="component component-row monospace">
        <div class="mock-col w-fixed-auto">
            <div class="mock-col-header">ID</div>
            <div class="mock-col-value">{{ item.id }}</div>
        </div>

        <div v-show="!editing" class="mock-col w1">
            <div class="mock-col-header">CATEGORY</div>
            <div class="mock-col-value link" @click="filter(item.category)">{{ item.category }}</div>
        </div>

        <div class="mock-col w1">
            <div class="mock-col-header">TITLE</div>
            <div class="mock-col-value">{{ item.title }}</div>
        </div>

        <div class="mock-col w1">
            <div class="mock-col-header">PRICE</div>
            <div class="mock-col-value">{{ item.price }}</div>
        </div>

        <div v-show="!editing" class="mock-col w3">
            <div class="mock-col-header">DESCRIPTION</div>
            <div class="mock-col-value mock-col-value-left">{{ item.description }}</div>
        </div>
    </div>
</template>
<script>
    export default {
        name: "StoreItem",
        components: {},
        data() {
            return {}
        },
        props: {
            item: {type: Object},
        },
        methods: {
            filter(value) {
                this.$emit('filter', value);
            },
        }
    }
</script>
<style scoped>
</style>
