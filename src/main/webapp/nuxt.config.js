export default {
    buildModules: [
        '@nuxtjs/style-resources',
        '@nuxtjs/fontawesome',
        '@nuxtjs/svg',
        '@nuxtjs/color-mode'
    ],
    fontawesome: {
        icons: {
            solid: ['faBars', 'faRoute', 'faPlay', 'faCode', 'faCogs', 'faQuestionCircle']
        }
    },
    css: [
        '~/assets/main.scss',
        '~/assets/buttons.less',
        '~/assets/forms.scss',
        '~/assets/layout-default.scss',
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
        color: '#158876',
        height: '5px',
        continuous: true
    },
    ssr: false
}