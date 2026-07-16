<template>
  <div class="app-wrapper">
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ collapsed: sidebarCollapsed }" :style="{ flex: `0 0 ${sidebarCollapsed ? 60 : 224}px` }">
      <div class="logo">
        <el-icon><Box /></el-icon>
        <span>WMS</span>
      </div>
      <nav class="nav">
        <a href="#" :class="{ active: currentRoute === 'Home' }" @click.prevent="navigate('Home')">
          <el-icon><HomeFilled /></el-icon>
          <span>主页</span>
        </a>
        <!-- 只有主管才可以见到“我的管理” -->
        <a v-if="userPosition === '主管'" href="#" :class="{ active: currentRoute === 'Manage' }" @click.prevent="navigate('Manage')">
          <el-icon><Setting /></el-icon>
          <span>我的管理</span>
        </a>
        <a href="#" :class="{ active: currentRoute === 'Dashboard' }" @click.prevent="navigate('Dashboard')">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据看板</span>
        </a>
        <a href="#" :class="{ active: currentRoute === 'Query' }" @click.prevent="navigate('Query')">
          <el-icon><Search /></el-icon>
          <span>明细查询</span>
        </a>
        <a href="#" :class="{ active: currentRoute === 'Tasks' || currentRoute === 'TaskForm' }" @click.prevent="navigate('Tasks')">
          <el-icon><List /></el-icon>
          <span>工作任务</span>
        </a>
        <a href="#" :class="{ active: currentRoute === 'AiChat' }" @click.prevent="navigate('AiChat')">
          <el-icon><ChatDotRound /></el-icon>
          <span>AI助手</span>
        </a>
        <a href="#" :class="{ active: currentRoute === 'Profile' }" @click.prevent="navigate('Profile')">
          <el-icon><UserFilled /></el-icon>
          <span>个人中心</span>
        </a>
      </nav>
      <div class="sidebar-bottom">
        <div class="logout-btn" @click="handleLogout" title="退出登录">
          <el-icon><SwitchButton /></el-icon>
        </div>
      </div>
    </aside>

    <!-- 主内容 -->
    <main class="main">
      <header class="topbar">
        <div class="topbar-left">
          <div class="collapse-btn" @click="toggleSidebar" :title="sidebarCollapsed ? '展开侧边栏' : '收缩侧边栏'">
            <el-icon><DArrowLeft v-if="!sidebarCollapsed" /><DArrowRight v-else /></el-icon>
          </div>
          <h1>{{ pageTitle }}</h1>
          <span class="date-text">{{ currentDate }}</span>
        </div>
      </header>

      <div class="content-area" :class="{ 'loading-active': routeLoading }">
        <div class="route-loading-overlay">
          <div class="route-loader"></div>
        </div>
        <router-view></router-view>
      </div>
    </main>
  </div>
</template>

<script>
import {
  Box, HomeFilled, Setting, Search, List, ChatDotRound, UserFilled, SwitchButton, Calendar, DataAnalysis, DArrowLeft, DArrowRight
} from '@element-plus/icons-vue'
import { ElIcon } from 'element-plus'

export default {
  name: 'MainLayout',
  components: {
    Box, HomeFilled, Setting, Search, List, ChatDotRound, UserFilled, SwitchButton, Calendar, DataAnalysis, DArrowLeft, DArrowRight, ElIcon
  },
  data() {
    return {
      currentDate: '',
      sidebarCollapsed: false,
      userPosition: '',
      routeLoading: false,
    }
  },
  computed: {
    currentRoute() {
      return this.$route.name || ''
    },
    pageTitle() {
      const titles = {
        Home: '智能仓储驾驶舱',
        Manage: '我的管理',
        Dashboard: '数据看板',
        Query: '明细查询',
        Tasks: '工作任务',
        AiChat: 'AI助手',
        Profile: '个人中心',
      }
      return titles[this.$route.name] || 'WMS'
    }
  },
  watch: {
    '$route'() {
      this.routeLoading = true
      setTimeout(() => { this.routeLoading = false }, 300)
    }
  },
  mounted() {
    const saved = localStorage.getItem('sidebarCollapsed')
    if (saved !== null) this.sidebarCollapsed = saved === 'true'
    const stored = localStorage.getItem('userInfo')
    if (stored) {
      try { this.userPosition = JSON.parse(stored).position || '' } catch (e) {}
    }
    this.updateDateTime()
    this.timer = setInterval(this.updateDateTime, 60000)
  },
  beforeDestroy() {
    clearInterval(this.timer)
  },
  methods: {
    toggleSidebar() {
      this.sidebarCollapsed = !this.sidebarCollapsed
      localStorage.setItem('sidebarCollapsed', this.sidebarCollapsed)
    },
    navigate(name) {
      this.$router.push({ name })
    },
    handleLogout() {
      // 清除当前会话的AI聊天记录
      try {
        const username = JSON.parse(localStorage.getItem('userInfo') || '{}').username || ''
        const sessionId = localStorage.getItem('ai_session_id') || ''
        if (username && sessionId) {
          localStorage.removeItem('ai_chat_msg_' + username + '_' + sessionId)
        }
      } catch (e) { /* ignore */ }
      localStorage.removeItem('token')//清除令牌
      localStorage.removeItem('userInfo')//清除用户信息
      localStorage.removeItem('ai_session_id')//清除AI会话
      this.$router.push({ name: 'Login' })//跳转登录页
    },
    updateDateTime() {
      const now = new Date()
      this.currentDate = now.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'short' })
    },
  }
}
</script>

<style scoped>
.app-wrapper {
  display: flex;
  height: 100vh;
  background: var(--page-bg);
  overflow: hidden;
}

/* ========== 侧边栏 — 渐变色 + 玻璃态 ========== */
.sidebar {
  flex: 0 0 224px;
  background: var(--sidebar-gradient);
  padding: 20px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
  transition: flex-basis 0.3s cubic-bezier(0.25, 0.8, 0.5, 1);
  overflow: hidden;
  box-shadow: 2px 0 12px hsl(var(--primary-h), var(--primary-s), calc(var(--primary-l) - 10%) / 20%);
}

.sidebar .logo {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 20px;
  width: 100%;
}
.sidebar .logo .el-icon {
  font-size: 26px;
  color: #fff;
  flex-shrink: 0;
}
.sidebar .logo span {
  font-size: 18px;
  font-weight: 700;
  color: #fff;
  white-space: nowrap;
  letter-spacing: 1px;
}

/* 导航项 */
.sidebar .nav {
  display: flex;
  flex-direction: column;
  gap: 2px;
  width: 100%;
  padding: 0 12px;
  flex: 1;
  margin-top: 32px;
}
.sidebar .nav a {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: var(--radius-md);
  color: var(--sidebar-text);
  text-decoration: none;
  font-size: 14px;
  transition: all 0.2s ease;
  white-space: nowrap;
  position: relative;
}
.sidebar .nav a .el-icon {
  font-size: 18px;
  flex-shrink: 0;
}
.sidebar .nav a:hover {
  background: var(--sidebar-item-hover-bg);
  color: var(--sidebar-text-hover);
  backdrop-filter: blur(4px);
}
.sidebar .nav a.active {
  background: var(--sidebar-item-active-bg);
  color: var(--sidebar-text-hover);
  font-weight: 500;
  box-shadow: 0 2px 8px hsl(0 0% 0% / 12%);
}

/* 侧边栏底部 */
.sidebar-bottom {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}
.logout-btn {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-md);
  background: hsl(0 0% 100% / 10%);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  backdrop-filter: blur(4px);
}
.logout-btn .el-icon {
  font-size: 18px;
  color: hsl(0 0% 100% / 75%);
  transition: color 0.2s;
}
.logout-btn:hover {
  background: hsl(5 80% 58% / 30%);
}
.logout-btn:hover .el-icon {
  color: #fff;
}

/* 收缩状态 */
.sidebar.collapsed .logo { justify-content: center; padding: 0 8px; }
.sidebar.collapsed .logo span { display: none; }
.sidebar.collapsed .nav a { justify-content: center; padding: 10px 0; }
.sidebar.collapsed .nav a span { display: none; }

/* ========== 主内容 ========== */
.main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0;
}

/* ========== 顶部栏 ========== */
.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 24px;
  background: hsl(0 0% 100% / 85%);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--border-light);
  box-shadow: var(--shadow-sm);
}
.topbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.topbar-left h1 {
  font-size: 18px;
  font-weight: 600;
  color: var(--foreground);
  margin: 0;
  letter-spacing: -0.3px;
}
.topbar-left .date-text {
  font-size: 13px;
  color: var(--foreground-muted);
}

/* 折叠按钮 */
.collapse-btn {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  color: var(--foreground-regular);
  font-size: 16px;
  flex-shrink: 0;
}
.collapse-btn:hover {
  background: var(--border-light);
  color: var(--foreground);
}

/* ========== 内容区域 ========== */
.content-area {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
  position: relative;
}

/* ========== 路由加载动画 ========== */
.route-loading-overlay {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  z-index: 999;
  display: flex;
  align-items: center;
  justify-content: center;
  background: hsl(var(--primary-h) 40% 97% / 85%);
  backdrop-filter: blur(4px);
  pointer-events: none;
  visibility: hidden;
  opacity: 0;
  transition: all 0.3s ease;
}
.loading-active .route-loading-overlay {
  pointer-events: all;
  visibility: visible;
  opacity: 1;
}
.route-loader {
  position: relative;
  width: 40px;
  height: 40px;
}
.route-loader::before {
  position: absolute;
  top: 50px;
  left: 0;
  width: 40px;
  height: 4px;
  content: '';
  background: hsl(var(--primary-h), var(--primary-s), 60% / 40%);
  border-radius: 50%;
  animation: route-shadow-ani 0.5s linear infinite;
}
.route-loader::after {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  content: '';
  background: var(--primary);
  border-radius: var(--radius-sm);
  animation: route-jump-ani 0.5s linear infinite;
}
@keyframes route-jump-ani {
  15%  { border-bottom-right-radius: 3px; }
  25%  { transform: translateY(8px) rotate(22.5deg); }
  50%  { border-bottom-right-radius: 36px; transform: translateY(16px) scale(1, 0.9) rotate(45deg); }
  75%  { transform: translateY(8px) rotate(67.5deg); }
  100% { transform: translateY(0) rotate(90deg); }
}
@keyframes route-shadow-ani {
  0%, 100% { transform: scale(1, 1); }
  50%      { transform: scale(1.2, 1); }
}

/* ========== 响应式 ========== */
@media (max-width: 768px) {
  .sidebar {
    flex: 0 0 56px !important;
    padding: 12px 0;
  }
  .sidebar .logo span,
  .sidebar .nav a span,
  .sidebar .logout-btn { display: none; }
  .sidebar .nav a { padding: 8px; justify-content: center; }
  .sidebar .logo { justify-content: center; padding: 0; }
  .content-area { padding: 14px 16px; }
}
</style>
