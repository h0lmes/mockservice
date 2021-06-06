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
                        <FontAwesomeIcon icon="code" class="nav-icon"/>
                        <span class="nav-label">Config</span>
                    </NuxtLink>
                </li>
                <li>
                    <NuxtLink to="/settings">
                        <FontAwesomeIcon icon="cogs" class="nav-icon"/>
                        <span class="nav-label">Settings</span>
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
                <Nuxt/>
            </div>
        </div>

        <ErrorPanel></ErrorPanel>
        <Loading v-if="working"></Loading>
    </div>
</template>
<script>
    import ColorModePicker from "../components/ColorModePicker";
    import ErrorPanel from "../components/ErrorPanel";
    import Loading from "../components/Loading";

    export default {
        components: {ErrorPanel, ColorModePicker, Loading},
        methods: {
            toggleSidebar() {
                let page = document.querySelector('#page');
                page.classList.toggle('navbar-maximizing');
                page.classList.toggle('navbar-mini');
                setTimeout(() => {
                    page.classList.toggle('navbar-maximizing');
                }, 200);
            }
        },
        computed: {
            working() {
                return this.$store.state.working
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