import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  base: '/',
  server: {
    port: 5173,
    host: true,
    proxy: {
      '/auth': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/system': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/course': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        bypass: (path) => {
          // 只代理 API 请求，不代理前端路由
          if (path === '/course' || path === '/course/') {
            return path
          }
        }
      },
      '/exam': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        bypass: (path) => {
          if (path === '/exam' || path === '/exam/') {
            return path
          }
        }
      },
      '/stats': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        bypass: (path) => {
          if (path === '/stats' || path === '/stats/') {
            return path
          }
        }
      }
    }
  },
  build: {
    outDir: 'dist',
    emptyOutDir: true,
    rollupOptions: {
      output: {
        entryFileNames: 'js/[name]-[hash].js',
        chunkFileNames: 'js/[name]-[hash].js',
        assetFileNames: (assetInfo) => {
          const info = assetInfo.name.split('.')
          const ext = info[info.length - 1]
          if (/\.(png|jpe?g|gif|svg|webp|ico)$/i.test(assetInfo.name)) {
            return 'img/[name]-[hash][extname]'
          }
          if (/\.(woff2?|eot|ttf|otf)$/i.test(assetInfo.name)) {
            return 'fonts/[name]-[hash][extname]'
          }
          if (ext === 'css') {
            return 'css/[name]-[hash][extname]'
          }
          return '[name]-[hash][extname]'
        }
      }
    }
  },
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  }
})
