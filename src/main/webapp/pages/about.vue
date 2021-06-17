<template>
    <div class="bg monospace">
        <div class="animation">
            <div class="hex hex1"></div>
            <div class="hex hex2"></div>
            <div class="hex hex1"></div>
            <div class="hex hex2"></div>
            <div class="hex hex1"></div>
            <div class="hex hex2"></div>
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

    .animation {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        overflow: hidden;

        & .hex1 {
            clip-path: polygon(25% 6.7%, 75% 6.7%, 100% 50%, 75% 93.3%, 25% 93.3%, 0% 50%);
        }
        & .hex2 {
            clip-path: polygon(50% 0%, 93.3% 25%, 93.3% 75%, 50% 100%, 6.7% 75%, 6.7% 25%);
        }

        & .hex {
            --orange: rgba(255,178,53,1);
            --yellow: rgba(255,238,42,1);
            animation: animate 20s linear infinite;
            position: absolute;
            bottom: -150px;
            background: rgb(255,178,53);
            background: linear-gradient(72deg,
                    var(--orange) 30%,
                    var(--yellow) 30% 41%,
                    var(--orange) 41% 49%,
                    var(--yellow) 49% 55%,
                    var(--orange) 55%);
        }

        & .hex:nth-child(1) {
            left: 12%;
            width: 30px;
            height: 30px;
            animation-delay: 1.5s;
            animation-duration: 10s;
        }
        & .hex:nth-child(2) {
            left: 20%;
            width: 110px;
            height: 110px;
            animation-delay: 3.5s;
        }
        & .hex:nth-child(3) {
            left: 42%;
            width: 150px;
            height: 150px;
            animation-delay: 0s;
            animation-duration: 25s;
        }
        & .hex:nth-child(4) {
            left: 64%;
            width: 40px;
            height: 40px;
            animation-delay: 0s;
            animation-duration: 12s;
        }
        & .hex:nth-child(5) {
            left: 70%;
            width: 100px;
            height: 100px;
            animation-delay: 5.5s;
        }
        & .hex:nth-child(6) {
            left: 86%;
            width: 80px;
            height: 80px;
            animation-delay: 0s;
        }
    }

    @keyframes animate {
        0% {
            transform: translateY(0) rotate(0deg);
            opacity: 0.5;
        }
        100% {
            transform: translateY(-70vh) rotate(360deg);
            opacity: 0;
        }
    }
</style>