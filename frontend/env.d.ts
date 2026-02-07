/// <reference types="vite/client" />

declare module '*.vue' {
    import type { DefineComponent } from 'vue'
    const component: DefineComponent<{}, {}, any>
    export default component
}

interface ImportMetaEnv {
    readonly VITE_WS_URL?: string
    readonly VITE_GIT_TAG?: string
    readonly VITE_GIT_COMMIT?: string
    readonly VITE_BUILD_TIME?: string
}

interface ImportMeta {
    readonly env: ImportMetaEnv
}
