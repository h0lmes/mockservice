<template>
    <div id="page" ref="page">
        <div class="navbar" role="navigation">
            <ul class="nav" ref="nav">
                <li class="nav-header"></li>
                <li class="nav-group">MOCKS</li>
                <li class="nav-item">
                    <NuxtLink to="/">
                        <FontAwesomeIcon icon="home" class="nav-icon"/>
                        <span class="nav-label">API</span>
                    </NuxtLink>
                </li>
                <li class="nav-item">
                    <NuxtLink to="/request-graph">
                        <FontAwesomeIcon icon="project-diagram" class="nav-icon"/>
                        <span class="nav-label">API graph</span>
                    </NuxtLink>
                </li>
                <li class="nav-item">
                    <NuxtLink to="/kafka">
                        <IconKafka class="nav-icon size-24"/>
                        <span class="nav-label">Kafka</span>
                    </NuxtLink>
                </li>
                <li class="nav-item">
                    <NuxtLink to="/context">
                        <FontAwesomeIcon icon="subscript" class="nav-icon"/>
                        <span class="nav-label">Context</span>
                    </NuxtLink>
                </li>
                <li class="nav-group">TOOLS</li>
                <li class="nav-item">
                    <NuxtLink to="/import">
                        <FontAwesomeIcon icon="file-import" class="nav-icon icon-width-fix"/>
                        <span class="nav-label">Import</span>
                    </NuxtLink>
                </li>
                <li class="nav-item">
                    <NuxtLink to="/generate">
                        <FontAwesomeIcon icon="dice" class="nav-icon icon-width-fix"/>
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
                <li class="nav-block nav-dummy"></li>
            </ul>

            <ul class="nav nav-hidden" ref="settings">
                <li class="nav-group">COLOR THEME
                    <button type="button" class="btn monospace nav-close-button" @click="navMode">x</button>
                </li>
                <li class="nav-block">
                    <ColorModePicker></ColorModePicker>
                </li>
                <li class="nav-group">COLOR ACCENT</li>
                <li class="nav-block">
                    <ColorAccentPicker></ColorAccentPicker>
                </li>
                <li class="nav-group">PAGE</li>
                <li class="nav-block">
                    <WidthSelector></WidthSelector>
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
                <slot />
            </div>
        </div>

        <ErrorPanel></ErrorPanel>
        <Loading v-if="working"></Loading>
    </div>
</template>

<script setup lang="ts">
import {computed, ref} from 'vue'
import IconKafka from '@/assets/icons/kafka.svg?component'
import ColorAccentPicker from '../components/other/ColorAccentPicker'
import ColorModePicker from '../components/other/ColorModePicker'
import ErrorPanel from '../components/other/ErrorPanel'
import Loading from '../components/other/Loading'
import WidthSelector from '@/components/other/WidthSelector'
import {frontendAppState} from '@/state/app'

const page = ref<HTMLElement | null>(null)
const nav = ref<HTMLElement | null>(null)
const settings = ref<HTMLElement | null>(null)
const isOpen = ref(true)
const isSettings = ref(false)
const working = computed(() => frontendAppState.working)

const open = () => {
  isOpen.value = true
  page.value?.classList.add('navbar-maximizing')
  page.value?.classList.remove('navbar-mini')
  window.setTimeout(() => page.value?.classList.remove('navbar-maximizing'), 200)
}

const close = () => {
  isOpen.value = false
  page.value?.classList.add('navbar-mini')
}

const navMode = () => {
  isSettings.value = false
  nav.value?.classList.remove('nav-hidden')
  settings.value?.classList.add('nav-hidden')
}

const settingsMode = () => {
  isSettings.value = true
  nav.value?.classList.add('nav-hidden')
  settings.value?.classList.remove('nav-hidden')
}

const toggleNavbar = () => {
  if (isSettings.value) {
    navMode()
  }

  if (isOpen.value) {
    close()
  } else {
    open()
  }
}

const toggleSettings = () => {
  if (!isOpen.value) {
    open()
  }

  if (!isSettings.value) {
    settingsMode()
  } else {
    navMode()
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

    .size-24 {
        width: 24px;
        height: 24px;
        margin: -5px 0 -3px 0;
        padding: 0;
        vertical-align: middle;
    }
</style>
