<template>
    <div id="page">
        <div class="navbar" role="navigation">
            <ul class="nav">
                <li class="nav-header">
                    <a class="btn btn-minimize-navbar" v-on:click="toggleSidebar">
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
                    <NuxtLink to="/datafiles">
                        <FontAwesomeIcon icon="file-code" class="nav-icon"/>
                        <span class="nav-label">Data files</span>
                    </NuxtLink>
                </li>
                <li>
                    <NuxtLink to="/scenarios">
                        <FontAwesomeIcon icon="play" class="nav-icon"/>
                        <span class="nav-label">Scenarios</span>
                    </NuxtLink>
                </li>
                <li class="container hidden-when-mini">
                    <ColorModePicker></ColorModePicker>
                </li>
            </ul>
        </div>

        <div class="page-wrapper">
            <div id="page-contents">
                <Nuxt/>
            </div>
        </div>
    </div>
</template>
<script>
    import ColorModePicker from "../components/ColorModePicker";
    export default {
        components: {ColorModePicker},
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