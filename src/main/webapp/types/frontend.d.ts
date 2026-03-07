declare module '*.svg?inline' {
  import type {DefineComponent} from 'vue'

  const component: DefineComponent<Record<string, never>, Record<string, never>, any>
  export default component
}

declare module '*.svg?component' {
  import type {DefineComponent} from 'vue'

  const component: DefineComponent<Record<string, never>, Record<string, never>, any>
  export default component
}

declare module '*.svg' {
  const src: string
  export default src
}

export {}
