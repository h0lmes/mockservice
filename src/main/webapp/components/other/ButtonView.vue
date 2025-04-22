<template>
    <button type="button" title="view" class="btn btn-sm" @click="e => $emit('click', e)">
        <component :is="`icon`"/>
    </button>
</template>
<script>
import Icon from '@/assets/icons/eye.svg?inline';

export default {
    name: "ButtonView",
    components: {Icon},
}
</script>
<style scoped>
</style>
