/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** Origen del backend para el login de Google. Vacio = mismo origen. */
  readonly VITE_BACKEND_URL?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
