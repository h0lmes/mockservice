<template>
    <div class="bg monospace">
        <div class="about">
            <h1 class="mb-5">Mock Service</h1>
            <p class="mb-2">An easy to use service to mock REST and SOAP services. Suitable for integration testing and similar purposes.</p>
            <p class="mb-2">
                <a target="_blank" href="https://github.com/h0lmes/mockservice" rel="noopener noreferrer">GitHub</a>
            </p>
            <pre class="ascii">         --------                       </pre>
            <pre class="ascii">    V   | Mock ? |   -------       V    </pre>
            <pre class="ascii">  (o o) |/-------   | Mock ! \   (o o)  </pre>
            <pre class="ascii"> (  V  )             ---------- (  V  ) </pre>
            <pre class="ascii">...m.m............................m.m...</pre>
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
        box-sizing: border-box;
        max-width: 30rem;
        margin: 0 auto;
        top: 50%;
        transform: translateY(-50%);
        background: transparent;
        text-align: center;
    }

    @media screen and (max-width: 599px) {
        .ascii {
            display: none;
        }
    }
</style>