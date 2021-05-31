<template>
    <div id="page">
        <div class="navbar" role="navigation">
            <ul class="nav">
                <li class="nav-header mb-2">
                    <a class="btn-minimize-navbar" v-on:click="toggleSidebar">
                        <FontAwesomeIcon icon="bars"/>
                    </a>
                </li>
                <li>
                    <NuxtLink to="/">
                        <FontAwesomeIcon icon="route" class="nav-icon"/>
                        <span class="nav-label">Routes</span>
                    </NuxtLink>
                </li>
                <li>
                    <NuxtLink to="/scenarios">
                        <FontAwesomeIcon icon="play" class="nav-icon"/>
                        <span class="nav-label">Scenarios</span>
                    </NuxtLink>
                </li>
                <li>
                    <NuxtLink to="/config">
                        <FontAwesomeIcon icon="cogs" class="nav-icon"/>
                        <span class="nav-label">Config</span>
                    </NuxtLink>
                </li>
                <li>
                    <NuxtLink to="/about">
                        <FontAwesomeIcon icon="question-circle" class="nav-icon"/>
                        <span class="nav-label">About</span>
                    </NuxtLink>
                </li>
                <li class="container hidden-when-mini mt-3">
                    <ColorModePicker></ColorModePicker>
                </li>
            </ul>
        </div>

        <div class="page-wrapper">
            <div id="page-contents">
                <ErrorPanel></ErrorPanel>
                <Nuxt/>
            </div>
        </div>
    </div>
</template>
<script>
    import ColorModePicker from "../components/ColorModePicker";
    import ErrorPanel from "../components/ErrorPanel";

    export default {
        components: {ErrorPanel, ColorModePicker},
        methods: {
            toggleSidebar() {
                let page = document.querySelector('#page');
                page.classList.toggle('navbar-maximizing');
                page.classList.toggle('navbar-mini');
                setTimeout(() => {
                    page.classList.toggle('navbar-maximizing');
                }, 200);
            }
        }
    }
</script>
<style scoped>
    .container {
        width: 100%;
        text-align: center;
        padding: 5px;
    }
</style>