// vite.config.js
import { defineConfig } from "file:///E:/project/orter/myself/edu-training/edu-admin/src/main/resources/static-vue/node_modules/vite/dist/node/index.js";
import vue from "file:///E:/project/orter/myself/edu-training/edu-admin/src/main/resources/static-vue/node_modules/@vitejs/plugin-vue/dist/index.mjs";
import { resolve } from "path";
var __vite_injected_original_dirname = "E:\\project\\orter\\myself\\edu-training\\edu-admin\\src\\main\\resources\\static-vue";
var vite_config_default = defineConfig({
  plugins: [vue()],
  base: "/",
  server: {
    port: 5173,
    host: true,
    proxy: {
      "/auth": {
        target: "http://localhost:8080",
        changeOrigin: true
      },
      "/system": {
        target: "http://localhost:8080",
        changeOrigin: true
      },
      "/course": {
        target: "http://localhost:8080",
        changeOrigin: true,
        bypass: (path) => {
          if (path === "/course" || path === "/course/") {
            return path;
          }
        }
      },
      "/exam": {
        target: "http://localhost:8080",
        changeOrigin: true,
        bypass: (path) => {
          if (path === "/exam" || path === "/exam/") {
            return path;
          }
        }
      },
      "/stats": {
        target: "http://localhost:8080",
        changeOrigin: true,
        bypass: (path) => {
          if (path === "/stats" || path === "/stats/") {
            return path;
          }
        }
      }
    }
  },
  build: {
    outDir: "../static",
    emptyOutDir: true,
    rollupOptions: {
      output: {
        entryFileNames: "js/[name]-[hash].js",
        chunkFileNames: "js/[name]-[hash].js",
        assetFileNames: (assetInfo) => {
          const info = assetInfo.name.split(".");
          const ext = info[info.length - 1];
          if (/\.(png|jpe?g|gif|svg|webp|ico)$/i.test(assetInfo.name)) {
            return "img/[name]-[hash][extname]";
          }
          if (/\.(woff2?|eot|ttf|otf)$/i.test(assetInfo.name)) {
            return "fonts/[name]-[hash][extname]";
          }
          if (ext === "css") {
            return "css/[name]-[hash][extname]";
          }
          return "[name]-[hash][extname]";
        }
      }
    }
  },
  resolve: {
    alias: {
      "@": resolve(__vite_injected_original_dirname, "src")
    }
  }
});
export {
  vite_config_default as default
};
//# sourceMappingURL=data:application/json;base64,ewogICJ2ZXJzaW9uIjogMywKICAic291cmNlcyI6IFsidml0ZS5jb25maWcuanMiXSwKICAic291cmNlc0NvbnRlbnQiOiBbImNvbnN0IF9fdml0ZV9pbmplY3RlZF9vcmlnaW5hbF9kaXJuYW1lID0gXCJFOlxcXFxwcm9qZWN0XFxcXG9ydGVyXFxcXG15c2VsZlxcXFxuZXdwcm9qZWN0XFxcXGVkdS1hZG1pblxcXFxzcmNcXFxcbWFpblxcXFxyZXNvdXJjZXNcXFxcc3RhdGljLXZ1ZVwiO2NvbnN0IF9fdml0ZV9pbmplY3RlZF9vcmlnaW5hbF9maWxlbmFtZSA9IFwiRTpcXFxccHJvamVjdFxcXFxvcnRlclxcXFxteXNlbGZcXFxcbmV3cHJvamVjdFxcXFxlZHUtYWRtaW5cXFxcc3JjXFxcXG1haW5cXFxccmVzb3VyY2VzXFxcXHN0YXRpYy12dWVcXFxcdml0ZS5jb25maWcuanNcIjtjb25zdCBfX3ZpdGVfaW5qZWN0ZWRfb3JpZ2luYWxfaW1wb3J0X21ldGFfdXJsID0gXCJmaWxlOi8vL0U6L3Byb2plY3Qvb3J0ZXIvbXlzZWxmL25ld3Byb2plY3QvZWR1LWFkbWluL3NyYy9tYWluL3Jlc291cmNlcy9zdGF0aWMtdnVlL3ZpdGUuY29uZmlnLmpzXCI7aW1wb3J0IHsgZGVmaW5lQ29uZmlnIH0gZnJvbSAndml0ZSdcbmltcG9ydCB2dWUgZnJvbSAnQHZpdGVqcy9wbHVnaW4tdnVlJ1xuaW1wb3J0IHsgcmVzb2x2ZSB9IGZyb20gJ3BhdGgnXG5cbmV4cG9ydCBkZWZhdWx0IGRlZmluZUNvbmZpZyh7XG4gIHBsdWdpbnM6IFt2dWUoKV0sXG4gIGJhc2U6ICcvJyxcbiAgc2VydmVyOiB7XG4gICAgcG9ydDogNTE3MyxcbiAgICBob3N0OiB0cnVlLFxuICAgIHByb3h5OiB7XG4gICAgICAnL2F1dGgnOiB7XG4gICAgICAgIHRhcmdldDogJ2h0dHA6Ly9sb2NhbGhvc3Q6ODA4MCcsXG4gICAgICAgIGNoYW5nZU9yaWdpbjogdHJ1ZVxuICAgICAgfSxcbiAgICAgICcvc3lzdGVtJzoge1xuICAgICAgICB0YXJnZXQ6ICdodHRwOi8vbG9jYWxob3N0OjgwODAnLFxuICAgICAgICBjaGFuZ2VPcmlnaW46IHRydWVcbiAgICAgIH0sXG4gICAgICAnL2NvdXJzZSc6IHtcbiAgICAgICAgdGFyZ2V0OiAnaHR0cDovL2xvY2FsaG9zdDo4MDgwJyxcbiAgICAgICAgY2hhbmdlT3JpZ2luOiB0cnVlLFxuICAgICAgICBieXBhc3M6IChwYXRoKSA9PiB7XG4gICAgICAgICAgLy8gXHU1M0VBXHU0RUUzXHU3NDA2IEFQSSBcdThCRjdcdTZDNDJcdUZGMENcdTRFMERcdTRFRTNcdTc0MDZcdTUyNERcdTdBRUZcdThERUZcdTc1MzFcbiAgICAgICAgICBpZiAocGF0aCA9PT0gJy9jb3Vyc2UnIHx8IHBhdGggPT09ICcvY291cnNlLycpIHtcbiAgICAgICAgICAgIHJldHVybiBwYXRoXG4gICAgICAgICAgfVxuICAgICAgICB9XG4gICAgICB9LFxuICAgICAgJy9leGFtJzoge1xuICAgICAgICB0YXJnZXQ6ICdodHRwOi8vbG9jYWxob3N0OjgwODAnLFxuICAgICAgICBjaGFuZ2VPcmlnaW46IHRydWUsXG4gICAgICAgIGJ5cGFzczogKHBhdGgpID0+IHtcbiAgICAgICAgICBpZiAocGF0aCA9PT0gJy9leGFtJyB8fCBwYXRoID09PSAnL2V4YW0vJykge1xuICAgICAgICAgICAgcmV0dXJuIHBhdGhcbiAgICAgICAgICB9XG4gICAgICAgIH1cbiAgICAgIH0sXG4gICAgICAnL3N0YXRzJzoge1xuICAgICAgICB0YXJnZXQ6ICdodHRwOi8vbG9jYWxob3N0OjgwODAnLFxuICAgICAgICBjaGFuZ2VPcmlnaW46IHRydWUsXG4gICAgICAgIGJ5cGFzczogKHBhdGgpID0+IHtcbiAgICAgICAgICBpZiAocGF0aCA9PT0gJy9zdGF0cycgfHwgcGF0aCA9PT0gJy9zdGF0cy8nKSB7XG4gICAgICAgICAgICByZXR1cm4gcGF0aFxuICAgICAgICAgIH1cbiAgICAgICAgfVxuICAgICAgfVxuICAgIH1cbiAgfSxcbiAgYnVpbGQ6IHtcbiAgICBvdXREaXI6ICcuLi9zdGF0aWMnLFxuICAgIGVtcHR5T3V0RGlyOiB0cnVlLFxuICAgIHJvbGx1cE9wdGlvbnM6IHtcbiAgICAgIG91dHB1dDoge1xuICAgICAgICBlbnRyeUZpbGVOYW1lczogJ2pzL1tuYW1lXS1baGFzaF0uanMnLFxuICAgICAgICBjaHVua0ZpbGVOYW1lczogJ2pzL1tuYW1lXS1baGFzaF0uanMnLFxuICAgICAgICBhc3NldEZpbGVOYW1lczogKGFzc2V0SW5mbykgPT4ge1xuICAgICAgICAgIGNvbnN0IGluZm8gPSBhc3NldEluZm8ubmFtZS5zcGxpdCgnLicpXG4gICAgICAgICAgY29uc3QgZXh0ID0gaW5mb1tpbmZvLmxlbmd0aCAtIDFdXG4gICAgICAgICAgaWYgKC9cXC4ocG5nfGpwZT9nfGdpZnxzdmd8d2VicHxpY28pJC9pLnRlc3QoYXNzZXRJbmZvLm5hbWUpKSB7XG4gICAgICAgICAgICByZXR1cm4gJ2ltZy9bbmFtZV0tW2hhc2hdW2V4dG5hbWVdJ1xuICAgICAgICAgIH1cbiAgICAgICAgICBpZiAoL1xcLih3b2ZmMj98ZW90fHR0ZnxvdGYpJC9pLnRlc3QoYXNzZXRJbmZvLm5hbWUpKSB7XG4gICAgICAgICAgICByZXR1cm4gJ2ZvbnRzL1tuYW1lXS1baGFzaF1bZXh0bmFtZV0nXG4gICAgICAgICAgfVxuICAgICAgICAgIGlmIChleHQgPT09ICdjc3MnKSB7XG4gICAgICAgICAgICByZXR1cm4gJ2Nzcy9bbmFtZV0tW2hhc2hdW2V4dG5hbWVdJ1xuICAgICAgICAgIH1cbiAgICAgICAgICByZXR1cm4gJ1tuYW1lXS1baGFzaF1bZXh0bmFtZV0nXG4gICAgICAgIH1cbiAgICAgIH1cbiAgICB9XG4gIH0sXG4gIHJlc29sdmU6IHtcbiAgICBhbGlhczoge1xuICAgICAgJ0AnOiByZXNvbHZlKF9fZGlybmFtZSwgJ3NyYycpXG4gICAgfVxuICB9XG59KVxuIl0sCiAgIm1hcHBpbmdzIjogIjtBQUFvYSxTQUFTLG9CQUFvQjtBQUNqYyxPQUFPLFNBQVM7QUFDaEIsU0FBUyxlQUFlO0FBRnhCLElBQU0sbUNBQW1DO0FBSXpDLElBQU8sc0JBQVEsYUFBYTtBQUFBLEVBQzFCLFNBQVMsQ0FBQyxJQUFJLENBQUM7QUFBQSxFQUNmLE1BQU07QUFBQSxFQUNOLFFBQVE7QUFBQSxJQUNOLE1BQU07QUFBQSxJQUNOLE1BQU07QUFBQSxJQUNOLE9BQU87QUFBQSxNQUNMLFNBQVM7QUFBQSxRQUNQLFFBQVE7QUFBQSxRQUNSLGNBQWM7QUFBQSxNQUNoQjtBQUFBLE1BQ0EsV0FBVztBQUFBLFFBQ1QsUUFBUTtBQUFBLFFBQ1IsY0FBYztBQUFBLE1BQ2hCO0FBQUEsTUFDQSxXQUFXO0FBQUEsUUFDVCxRQUFRO0FBQUEsUUFDUixjQUFjO0FBQUEsUUFDZCxRQUFRLENBQUMsU0FBUztBQUVoQixjQUFJLFNBQVMsYUFBYSxTQUFTLFlBQVk7QUFDN0MsbUJBQU87QUFBQSxVQUNUO0FBQUEsUUFDRjtBQUFBLE1BQ0Y7QUFBQSxNQUNBLFNBQVM7QUFBQSxRQUNQLFFBQVE7QUFBQSxRQUNSLGNBQWM7QUFBQSxRQUNkLFFBQVEsQ0FBQyxTQUFTO0FBQ2hCLGNBQUksU0FBUyxXQUFXLFNBQVMsVUFBVTtBQUN6QyxtQkFBTztBQUFBLFVBQ1Q7QUFBQSxRQUNGO0FBQUEsTUFDRjtBQUFBLE1BQ0EsVUFBVTtBQUFBLFFBQ1IsUUFBUTtBQUFBLFFBQ1IsY0FBYztBQUFBLFFBQ2QsUUFBUSxDQUFDLFNBQVM7QUFDaEIsY0FBSSxTQUFTLFlBQVksU0FBUyxXQUFXO0FBQzNDLG1CQUFPO0FBQUEsVUFDVDtBQUFBLFFBQ0Y7QUFBQSxNQUNGO0FBQUEsSUFDRjtBQUFBLEVBQ0Y7QUFBQSxFQUNBLE9BQU87QUFBQSxJQUNMLFFBQVE7QUFBQSxJQUNSLGFBQWE7QUFBQSxJQUNiLGVBQWU7QUFBQSxNQUNiLFFBQVE7QUFBQSxRQUNOLGdCQUFnQjtBQUFBLFFBQ2hCLGdCQUFnQjtBQUFBLFFBQ2hCLGdCQUFnQixDQUFDLGNBQWM7QUFDN0IsZ0JBQU0sT0FBTyxVQUFVLEtBQUssTUFBTSxHQUFHO0FBQ3JDLGdCQUFNLE1BQU0sS0FBSyxLQUFLLFNBQVMsQ0FBQztBQUNoQyxjQUFJLG1DQUFtQyxLQUFLLFVBQVUsSUFBSSxHQUFHO0FBQzNELG1CQUFPO0FBQUEsVUFDVDtBQUNBLGNBQUksMkJBQTJCLEtBQUssVUFBVSxJQUFJLEdBQUc7QUFDbkQsbUJBQU87QUFBQSxVQUNUO0FBQ0EsY0FBSSxRQUFRLE9BQU87QUFDakIsbUJBQU87QUFBQSxVQUNUO0FBQ0EsaUJBQU87QUFBQSxRQUNUO0FBQUEsTUFDRjtBQUFBLElBQ0Y7QUFBQSxFQUNGO0FBQUEsRUFDQSxTQUFTO0FBQUEsSUFDUCxPQUFPO0FBQUEsTUFDTCxLQUFLLFFBQVEsa0NBQVcsS0FBSztBQUFBLElBQy9CO0FBQUEsRUFDRjtBQUNGLENBQUM7IiwKICAibmFtZXMiOiBbXQp9Cg==
