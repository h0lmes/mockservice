export default {
    buildModules: [
        '@nuxtjs/fontawesome',
        '@nuxtjs/svg',
        '@nuxtjs/color-mode'
    ],
    fontawesome: {
        icons: {
            solid: ['faBars', 'faRoute', 'faFileCode', 'faPlay']
        }
    },
    css: ['~/assets/main.css', '~/assets/default-layout.css'],
    head: {
        title: 'Mock Service',
        meta: [
            { charset: 'utf-8' },
            { name: 'viewport', content: 'width=device-width, initial-scale=1' }
        ]
    },
    loading: {
        color: 'blue',
        height: '5px'
    },
    ssr: false
}