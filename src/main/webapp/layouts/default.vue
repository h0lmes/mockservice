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
    export default {
        methods: {
            toggleSidebar() {
                let page = document.querySelector('#page');
                page.classList.toggle('navbar-no-labels');
                page.classList.toggle('navbar-mini');
                setTimeout(() => {
                    page.classList.toggle('navbar-no-labels');
                }, 200);
            }
        }
    }
</script>