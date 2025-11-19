import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

import { templateCompilerOptions } from '@tresjs/core'
// https://vite.dev/config/
export default defineConfig({
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
      '/images': 'http://localhost:8080',
      '/stompbroker': {
        target: 'http://localhost:8080/',
        ws: true,
      },
    },
    host: true
  },
  plugins: [
    vue( {
      ... templateCompilerOptions
    } ),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
})


