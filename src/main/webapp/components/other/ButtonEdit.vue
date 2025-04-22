<template>
    <button type="button" title="edit" class="btn btn-sm" @click="e => $emit('click', e)">
        <component :is="`icon`"/>
    </button>
</template>
<script>
import Icon from '@/assets/icons/pencil.svg?inline';

export default {
    name: "ButtonEdit",
    components: {Icon},
}
</script>
<style scoped>
</style>
