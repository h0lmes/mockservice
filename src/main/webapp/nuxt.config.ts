import svgLoader from 'vite-svg-loader'

export default defineNuxtConfig({
  compatibilityDate: '2025-03-01',
  devtools: { enabled: false },
  ssr: false,
  modules: ['@nuxtjs/color-mode'],
  css: [
    '~/assets/theme.scss',
    '~/assets/main.scss',
    '~/assets/layout.scss',
    '~/assets/buttons.scss',
    '~/assets/forms.scss',
    '~/assets/modals.scss',
    '~/assets/layout-default.scss'
  ],
  app: {
    head: {
      title: 'Mockachu',
      meta: [
        { charset: 'utf-8' },
        { name: 'viewport', content: 'width=device-width, initial-scale=1' }
      ]
    }
  },
  colorMode: {
    preference: 'system',
    fallback: 'light',
    classSuffix: '-mode',
    storageKey: 'nuxt-color-mode'
  },
  runtimeConfig: {
    public: {
      apiBase: process.env.NUXT_PUBLIC_API_BASE ?? '',
      wsBase: process.env.NUXT_PUBLIC_WS_BASE ?? ''
    }
  },
  vite: {
    plugins: [svgLoader()]
  }
})


