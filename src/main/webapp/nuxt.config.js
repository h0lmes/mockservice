export default {
    buildModules: [
        '@nuxtjs/style-resources',
        '@nuxtjs/fontawesome',
        '@nuxtjs/svg',
        '@nuxtjs/color-mode'
    ],
    fontawesome: {
        icons: {
            solid: ['faBars', 'faRoute', 'faPlay', 'faCode', 'faFileAlt', 'faCogs', 'faQuestionCircle', 'faHammer', 'faReceipt', 'faFileImport']
        }
    },
    css: [
        '~/assets/main.scss',
        '~/assets/helpers.css',
        '~/assets/buttons.less',
        '~/assets/forms.scss',
        '~/assets/modals.scss',
        '~/assets/layout-default.scss'
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
        height: '.7rem',
        continuous: true
    },
    ssr: false
}