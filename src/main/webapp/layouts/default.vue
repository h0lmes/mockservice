<template>
    <div id="page" class="sidebar">
        <div class="navbar" role="navigation">
            <ul class="nav">
                <li class="nav-header">
                    <span class="nav-label">Mock Service</span>
                    <span class="nav-label-mini">&nbsp;</span>
                </li>
                <li>
                    <NuxtLink to="/">
                        <i class="fa fa-bar-chart-o"></i>
                        <span class="nav-label">Routes</span>
                    </NuxtLink>
                </li>
                <li>
                    <NuxtLink to="/datafiles">
                        <i class="fa fa-bar-chart-o"></i>
                        <span class="nav-label">Data files</span>
                    </NuxtLink>
                </li>
            </ul>
        </div>

        <div class="page-wrapper">
            <div class="row row-header">
                <a class="navbar-minimize btn" v-on:click="toggleSidebar"><i class="fa fa-bars"></i>_</a>
            </div>
            <div class="row">
                <div id="page-contents">
                    <Nuxt/>
                </div>
            </div>
        </div>
    </div>
</template>
<script>
    export default {
        methods: {
            toggleSidebar() {
                var menu = document.querySelector('#page');
                menu.classList.toggle('sidebar-mini');
            }
        }
    }
</script>