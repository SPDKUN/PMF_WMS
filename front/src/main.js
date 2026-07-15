// 项目入口文件
import { createApp } from 'vue'//导入vue框架
import router from './router'//导入路由配置
import './style.css'//导入全局样式
import 'element-plus/dist/index.css'//导入element plus组件
import App from './App.vue'//导入根组件


//创建一个vue对象，创建一个vue对象时要一页的模板
const app = createApp(App)

//挂载路由
app.use(router)
//创建
app.mount('#app')

// 移除启动加载动画
const loadingEl = document.getElementById('app-loading')
if (loadingEl) {
  loadingEl.classList.add('hidden')
  loadingEl.addEventListener('transitionend', () => {
    loadingEl.remove()
  }, { once: true })
}
