// 项目入口文件
import { createApp } from 'vue'
import router from './router'
import './style.css'
import 'element-plus/dist/index.css'
import App from './App.vue'


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
