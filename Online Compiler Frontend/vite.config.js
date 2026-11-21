import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    host: 'localhost',
    port: 5173,
    proxy: {
      // Same-origin dev proxy avoids CORS and allows HttpOnly cookies to work.
      '/ws/execute': { target: 'ws://localhost:8080', ws: true, changeOrigin: true },
      '/api': { target: 'http://localhost:8080', changeOrigin: true },
    },
  },
});
