<template>
    <div>
        <p v-if="entities.length === 0">
            Nothing here ¯\_(ツ)_/¯
        </p>
        <div v-for="(entity, index) in entities" :key="entity.id">
            <StoreItem :item="entity" @filter="$emit('filter', $event)"></StoreItem>
        </div>
    </div>
</template>
<script>
    import StoreItem from "./StoreItem";

    export default {
        name: "StoreItems",
        components: {StoreItem},
        data() {
            return {}
        },
        props: {
            entities: {type: Array},
        },
    }
</script>
<style scoped>
</style>
