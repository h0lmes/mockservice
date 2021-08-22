export default {
    buildModules: [
        '@nuxtjs/style-resources',
        '@nuxtjs/fontawesome',
        '@nuxtjs/svg',
        '@nuxtjs/color-mode'
    ],
    fontawesome: {
        icons: {
            solid: ['faBars', 'faRoute', 'faPlay', 'faCode', 'faFileAlt', 'faCog', 'faCogs', 'faQuestionCircle', 'faHammer', 'faReceipt', 'faFileImport']
        }
    },
    css: [
        '~/assets/main.scss',
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
        color: '#ab1147',
        height: '.7rem',
        continuous: true
    },
    // plugins: [{
    //     src: '~/plugins/onload.js',
    //     ssr: false
    // }],
    ssr: false
}