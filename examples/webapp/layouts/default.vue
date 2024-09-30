<template>
    <div id="page" ref="page">
        <div class="navbar" role="navigation">
            <ul class="nav" ref="nav">
                <li class="nav-header"></li>
                <li class="nav-group">GENERAL</li>
                <li class="nav-item">
                    <NuxtLink to="/">
                        <FontAwesomeIcon icon="route" class="nav-icon"/>
                        <span class="nav-label">Main Page</span>
                    </NuxtLink>
                </li>
                <li class="nav-block nav-dummy"></li>
            </ul>

            <ul class="nav nav-hidden" ref="settings">
                <li class="nav-group">COLOR THEME</li>
                <li class="nav-block">
                    <ColorModePicker></ColorModePicker>
                </li>
                <li class="nav-group">COLOR ACCENT</li>
                <li class="nav-block">
                    <ColorAccentPicker></ColorAccentPicker>
                </li>
                <li class="nav-block">
                    <button type="button" class="btn monospace mt-5" @click="nav">OK</button>
                </li>
                <li class="nav-block nav-dummy"></li>
            </ul>

            <div class="nav-footer">
                <div class="nav-footer-item" tabindex="0" @click="toggleNavbar" @keydown.enter.exact="toggleNavbar">
                    <FontAwesomeIcon icon="bars"/>
                </div>
                <div class="nav-footer-item" tabindex="0" @click="toggleSettings" @keydown.enter.exact="toggleSettings">
                    <FontAwesomeIcon icon="cog"/>
                </div>
            </div>
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
    import ColorAccentPicker from "../components/ColorAccentPicker";
    import ErrorPanel from "../components/ErrorPanel";
    import Loading from "../components/Loading";

    export default {
        components: {ErrorPanel, Loading, ColorModePicker, ColorAccentPicker},
        data() {
            return {
                isOpen: true,
                isSettings: false,
            }
        },
        computed: {
            working() {
                return this.$store.state.working
            }
        },
        methods: {
            toggleNavbar() {
                if (this.isSettings) {
                    this.nav();
                }

                if (this.isOpen) {
                    this.close();
                } else {
                    this.open();
                }
            },
            toggleSettings() {
                if (!this.isOpen) {
                    this.open();
                }

                if (!this.isSettings) {
                    this.sets();
                } else {
                    this.nav();
                }
            },
            open() {
                this.isOpen = true;
                this.$refs.page.classList.add('navbar-maximizing');
                this.$refs.page.classList.remove('navbar-mini');
                setTimeout(() => this.$refs.page.classList.remove('navbar-maximizing'), 200);
            },
            close() {
                this.isOpen = false;
                this.$refs.page.classList.add('navbar-mini');
            },
            nav() {
                this.isSettings = false;
                this.$refs.nav.classList.remove('nav-hidden');
                this.$refs.settings.classList.add('nav-hidden');
            },
            sets() {
                this.isSettings = true;
                this.$refs.nav.classList.add('nav-hidden');
                this.$refs.settings.classList.remove('nav-hidden');
            },
        }
    }
</script>
<style scoped>
    .icon-width-fix {
        width: 1.2rem;
    }

    .nav-dummy {
        height: 3.3rem;
    }
</style>
