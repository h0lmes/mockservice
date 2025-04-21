<template>
    <div class="modal-backdrop transparent">
        <div class="modal-border">
            <div class="modal-content monospace loading">
                <div class="loader"></div>
                <div class="mr-6">LOADING</div>
            </div>
        </div>
    </div>
</template>
<script>
    export default {
        name: "Loading"
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
    width: 3rem;
    aspect-ratio: 1;
    display: grid;
    border: 4px solid #0000;
    border-radius: 50%;
    border-color: var(--modal-border-color-2) #0000;
    animation: l16 1s infinite linear;
}
.loader::before,
.loader::after {
    content: "";
    grid-area: 1/1;
    margin: 2px;
    border: inherit;
    border-radius: 50%;
    border-color: var(--modal-border-color-3) #0000;
}
.loader::before {
    border-color: var(--modal-border-color-1) #0000;
    animation: inherit;
    animation-duration: .5s;
    animation-direction: reverse;
}
.loader::after {
    margin: 8px;
}
@keyframes l16 {
    100% {transform: rotate(1turn)}
}
</style>
