<template>
    <div id="page">
        <div class="navbar" role="navigation">
            <ul class="nav">
                <li class="nav-header mb-2">
                    <a class="btn-minimize-navbar" @click="toggleSidebar">
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
                    <NuxtLink to="/request">
                        <FontAwesomeIcon icon="at" class="nav-icon"/>
                        <span class="nav-label">Request</span>
                    </NuxtLink>
                </li>
                <li>
                    <NuxtLink to="/settings">
                        <FontAwesomeIcon icon="cogs" class="nav-icon"/>
                        <span class="nav-label">Settings</span>
                    </NuxtLink>
                </li>
                <li>
                    <NuxtLink to="/config">
                        <FontAwesomeIcon icon="code" class="nav-icon"/>
                        <span class="nav-label">Config</span>
                    </NuxtLink>
                </li>
                <li>
                    <NuxtLink to="/log">
                        <FontAwesomeIcon icon="file-alt" class="nav-icon larger icon-log"/>
                        <span class="nav-label">Log</span>
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
                <li class="container hidden-when-mini smaller">
                    <ViewSelector></ViewSelector>
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
    import ViewSelector from "../components/ViewSelector";

    export default {
        components: {ViewSelector, ErrorPanel, ColorModePicker, Loading},
        data() {
            return {}
        },
        computed: {
            working() {
                return this.$store.state.working
            }
        },
        methods: {
            toggleSidebar() {
                let page = document.querySelector('#page');
                page.classList.toggle('navbar-maximizing');
                page.classList.toggle('navbar-mini');
                setTimeout(() => {
                    page.classList.toggle('navbar-maximizing');
                }, 200);
            },
        }
    }
</script>
<style scoped>
    .container {
        width: 100%;
        text-align: center;
    }

    .icon-log {
        width: 1.2rem;
    }
</style>