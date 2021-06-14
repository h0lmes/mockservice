<template>
    <div class="bg monospace">
        <ul class="animation">
            <li></li>
            <li></li>
            <li></li>
            <li></li>
            <li></li>
            <li></li>
        </ul>
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
            <pre>             --------                           </pre>
            <pre>        V   | Mock ? |   -------       V        </pre>
            <pre>      (o o) |/-------   | Mock ! \   (o o)      </pre>
            <pre>     (  V  )             ---------- (  V  )     </pre>
            <pre>.......m.m............................m.m.......</pre>
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

    .animation {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        overflow: hidden;

        & li {
            position: absolute;
            display: block;
            list-style: none;
            width: 25px;
            height: 25px;
            background: var(--color-accent-one);
            animation: animate 20s linear infinite;
            bottom: -150px;
        }

        & li:nth-child(1) {
            left: 86%;
            width: 80px;
            height: 80px;
            animation-delay: 0s;
        }
        & li:nth-child(2) {
            left: 12%;
            width: 30px;
            height: 30px;
            animation-delay: 1.5s;
            animation-duration: 10s;
        }
        & li:nth-child(3) {
            left: 70%;
            width: 100px;
            height: 100px;
            animation-delay: 5.5s;
        }
        & li:nth-child(4) {
            left: 42%;
            width: 150px;
            height: 150px;
            animation-delay: 0s;
            animation-duration: 25s;
        }
        & li:nth-child(5) {
            left: 65%;
            width: 40px;
            height: 40px;
            animation-delay: 0s;
            animation-duration: 12s;
        }
        & li:nth-child(6) {
            left: 15%;
            width: 110px;
            height: 110px;
            animation-delay: 3.5s;
        }
    }

    @keyframes animate {
        0% {
            transform: translateY(0) rotate(0deg);
            opacity: 0.3;
        }
        100% {
            transform: translateY(-70vh) rotate(360deg);
            opacity: 0;
        }
    }
</style>