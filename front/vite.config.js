import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
const path = require('path')
// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  // http:localhost:8080/api/login ==> http://localhost:8088/login
  // 浏览器不能直接访问后端的域名，这样配置后浏览器认为访问的是同一个域名的数据，解决跨域问题
  server : {
    port: 8080,
    open: true,
    proxy:{
    '/api': {
        target: 'http://localhost:8088', // 后端接口地址
        changeOrigin: true, // 开启跨域
        rewrite: (path) => path.replace(/^\/api/, '') // 重写路径
      }      
    }
  }
})