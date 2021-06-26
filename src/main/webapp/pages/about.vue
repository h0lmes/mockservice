<template>
    <div class="bg monospace">
        <div style="height: 150px; overflow: hidden; position: absolute; width: 100%">
            <svg viewBox="0 0 500 150" preserveAspectRatio="none" style="height: 100%; width: 100%;">
                <path d="M0,150 C150,150 272,-50 500,50 V0 H0 Z" style="stroke: none; fill: var(--bg-component);"></path>
            </svg>
        </div>
        <div class="about">
            <h1>Mock Service</h1>
            <p></p>
            <p>An easy to use service to mock REST and SOAP services. Suitable for integration testing and similar purposes.</p>
            <p></p>
            <p>
                <a target="_blank" href="https://github.com/h0lmes/mockservice" rel="noopener noreferrer">GitHub</a>
            </p>
            <p></p>
            <p></p>
            <pre>         --------                       </pre>
            <pre>    V   | Mock ? |   -------       V    </pre>
            <pre>  (o o) |/-------   | Mock ! \   (o o)  </pre>
            <pre> (  V  )             ---------- (  V  ) </pre>
            <pre>...m.m............................m.m...</pre>
        </div>
    </div>
</template>
<script>
    export default {
        name: "about"
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
        max-width: 30rem;
        margin: 0 auto;
        top: 50%;
        transform: translateY(-50%);
        background: transparent;
        text-align: center;
    }
</style>