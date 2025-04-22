<template>
    <button type="button" title="execute" class="btn btn-sm" @click="e => $emit('click', e)">
        <component :is="`icon`"/>
    </button>
</template>
<script>
import Icon from '@/assets/icons/bolt.svg?inline';

export default {
    name: "ButtonExecute",
    components: {Icon},
}
</script>
<style scoped>
</style>
