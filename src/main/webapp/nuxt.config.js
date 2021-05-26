export default {
    buildModules: [
        '@nuxtjs/style-resources',
        '@nuxtjs/fontawesome',
        '@nuxtjs/svg',
        '@nuxtjs/color-mode'
    ],
    fontawesome: {
        icons: {
            solid: ['faBars', 'faRoute', 'faFileCode', 'faPlay', 'faCogs', 'faQuestionCircle']
        }
    },
    css: [
        '~/assets/main.scss',
        '~/assets/buttons.less',
        '~/assets/forms.scss',
        '~/assets/default-layout.css',
        '~/assets/helpers.css'
    ],
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