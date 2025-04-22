<template>
    <button type="button" title="delete" class="btn btn-sm" @click="e => $emit('click', e)">
        <component :is="`icon`"/>
    </button>
</template>
<script>
import Icon from '@/assets/icons/trash.svg?inline';

export default {
    name: "ButtonDelete",
    components: {Icon},
}
</script>
<style scoped>
</style>
