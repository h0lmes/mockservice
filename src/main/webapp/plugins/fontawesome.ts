import {library} from '@fortawesome/fontawesome-svg-core'
import {
    faBars,
    faCode,
    faCog,
    faCogs,
    faDice,
    faFileAlt,
    faFileImport,
    faHome,
    faProjectDiagram,
    faQuestionCircle,
    faSubscript
} from '@fortawesome/free-solid-svg-icons'
import {FontAwesomeIcon} from '@fortawesome/vue-fontawesome'

export default defineNuxtPlugin((nuxtApp) => {
  library.add(
    faBars,
    faCode,
    faCog,
    faCogs,
    faDice,
    faFileAlt,
    faFileImport,
    faHome,
    faProjectDiagram,
    faQuestionCircle,
    faSubscript
  )

  nuxtApp.vueApp.component('FontAwesomeIcon', FontAwesomeIcon)
})
