<template>
    <div class="modal-backdrop transparent">
        <div class="modal-border">
            <div class="modal-content monospace loading">
                <div class="loader">
                    <component :is="`icon`" class="orange-yellow"/>
                </div>
                <div class="mr-2">LOADING</div>
            </div>
        </div>
    </div>
</template>
<script>
import Icon from '@/assets/icons/bolt.svg?inline';

export default {
    name: "Loading",
    components: {Icon},
}
</script>
<style lang="scss" scoped>
.loading {
    font-weight: 700;
    min-width: 14rem;
    min-height: 6rem;
    display: flex;
    gap: 1rem 1rem;
    align-items: center;
    justify-items: center;
    justify-content: center;
}

.loader {
    width: 1rem;
    aspect-ratio: 1;
    animation: anim-rotate 1s infinite linear;
}
@keyframes anim-rotate {
    100% {transform: rotate(1turn)}
}
</style>
