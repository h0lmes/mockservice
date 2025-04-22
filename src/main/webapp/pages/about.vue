<template>
    <div class="bg monospace">
        <div class="about">
            <h1 class="mb-6">Mockachu</h1>
            <p class="mb-2">Effortless API Mocking and Testing</p>
            <p class="mb-2">
                <a target="_blank" href="https://github.com/h0lmes/mockservice" rel="noopener noreferrer">GitHub</a>
            </p>
            <pre class="ascii mt-4 orange-yellow">≽(◕ ᴗ ◕)≼</pre>
            <component :is="`icon`" class="icon orange-yellow mt-2"/>
        </div>
    </div>
</template>
<script>
import Icon from '@/assets/icons/bolt.svg?inline';

export default {
    name: "about",
    components: {Icon},
}
</script>
<style lang="scss" scoped>
    .bg {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100vh;
    }

    .about {
        position: relative;
        box-sizing: border-box;
        max-width: 30rem;
        margin: 0 auto;
        top: 50%;
        transform: translateY(-50%);
        background: transparent;
        text-align: center;
    }

    .icon {
        width: 1rem;
        height: 1rem;
    }
</style>
