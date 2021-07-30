<template>
    <div id="page">
        <div class="navbar" role="navigation">
            <ul class="nav">
                <li class="nav-header" @keydown.enter.exact="toggleSidebar">
                    <a type="button" class="btn-minimize-navbar" tabindex="0" @click="toggleSidebar">
                        <FontAwesomeIcon icon="bars"/>
                    </a>
                </li>
                <li class="nav-group">MOCKS</li>
                <li class="nav-item">
                    <NuxtLink to="/">
                        <FontAwesomeIcon icon="route" class="nav-icon"/>
                        <span class="nav-label">Routes</span>
                    </NuxtLink>
                </li>
                <li class="nav-item">
                    <NuxtLink to="/import">
                        <FontAwesomeIcon icon="file-import" class="nav-icon icon-width-fix"/>
                        <span class="nav-label">Import</span>
                    </NuxtLink>
                </li>
                <li class="nav-item">
                    <NuxtLink to="/scenarios">
                        <FontAwesomeIcon icon="receipt" class="nav-icon icon-width-fix"/>
                        <span class="nav-label">Scenarios</span>
                    </NuxtLink>
                </li>
                <li class="nav-group">TOOLS</li>
                <li class="nav-item">
                    <NuxtLink to="/request">
                        <FontAwesomeIcon icon="play" class="nav-icon icon-width-fix"/>
                        <span class="nav-label">Request</span>
                    </NuxtLink>
                </li>
                <li class="nav-item">
                    <NuxtLink to="/generate">
                        <FontAwesomeIcon icon="hammer" class="nav-icon icon-width-fix"/>
                        <span class="nav-label">Generate</span>
                    </NuxtLink>
                </li>
                <li class="nav-group">SERVICE</li>
                <li class="nav-item">
                    <NuxtLink to="/settings">
                        <FontAwesomeIcon icon="cogs" class="nav-icon"/>
                        <span class="nav-label">Settings</span>
                    </NuxtLink>
                </li>
                <li class="nav-item">
                    <NuxtLink to="/config">
                        <FontAwesomeIcon icon="code" class="nav-icon"/>
                        <span class="nav-label">Config</span>
                    </NuxtLink>
                </li>
                <li class="nav-item">
                    <NuxtLink to="/log">
                        <FontAwesomeIcon icon="file-alt" class="nav-icon larger icon-width-fix"/>
                        <span class="nav-label">Log</span>
                    </NuxtLink>
                </li>
                <li class="nav-item">
                    <NuxtLink to="/about">
                        <FontAwesomeIcon icon="question-circle" class="nav-icon icon-width-fix"/>
                        <span class="nav-label">About</span>
                    </NuxtLink>
                </li>
                <li class="nav-group">COLOR</li>
                <li class="container hidden-when-mini">
                    <ColorModePicker></ColorModePicker>
                </li>
            </ul>
        </div>

        <div class="page-wrapper">
            <div class="page-contents">
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

    .icon-width-fix {
        width: 1.2rem;
    }
</style>