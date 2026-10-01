import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// En desarrollo el frontend y el backend viven en puertos distintos; el proxy evita CORS
// y hace que el navegador solo hable con un origen. Backend por defecto en :8080.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': process.env.POLARIS_API_URL ?? 'http://localhost:8080',
    },
  },
});
