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
        <a href="#" :class="{ active: currentRoute === 'Manage' }" @click.prevent="navigate('Manage')">
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

      <div class="content-area">
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
  mounted() {
    const saved = localStorage.getItem('sidebarCollapsed')
    if (saved !== null) this.sidebarCollapsed = saved === 'true'
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
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      this.$router.push({ name: 'Login' })
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
  color: var(--text-primary);
  overflow: hidden;
}

/* 侧边栏 */
.sidebar {
  flex: 0 0 224px;
  background: #fff;
  border-right: 1px solid var(--border-color-light);
  padding: 20px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
  transition: flex-basis 0.3s ease;
  overflow: hidden;
}

.sidebar .logo {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 20px;
  width: 100%;
}
.sidebar .logo .el-icon {
  font-size: 24px;
  color: var(--primary-color);
  flex-shrink: 0;
}
.sidebar .logo span {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  white-space: nowrap;
}

.sidebar .nav {
  display: flex;
  flex-direction: column;
  gap: 2px;
  width: 100%;
  padding: 0 12px;
  flex: 1;
  margin-top: 24px;
}
.sidebar .nav a {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: 6px;
  color: var(--text-regular);
  text-decoration: none;
  font-size: 14px;
  transition: all 0.2s ease;
  white-space: nowrap;
}
.sidebar .nav a .el-icon {
  font-size: 18px;
  flex-shrink: 0;
}
.sidebar .nav a:hover {
  background: var(--primary-bg);
  color: var(--primary-color);
}
.sidebar .nav a.active {
  background: var(--primary-bg);
  color: var(--primary-color);
  font-weight: 500;
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
  border-radius: 6px;
  background: #fef0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.2s;
}
.logout-btn .el-icon {
  font-size: 18px;
  color: #f56c6c;
}
.logout-btn:hover {
  background: #fde2e2;
}

/* 收缩按钮（顶部栏） */
.collapse-btn {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.2s;
  color: #606266;
  font-size: 16px;
  flex-shrink: 0;
}
.collapse-btn:hover {
  background: #f0f2f5;
}

/* 收缩状态 */
.sidebar.collapsed .logo { justify-content: center; padding: 0 8px; }
.sidebar.collapsed .logo span { display: none; }
.sidebar.collapsed .nav a { justify-content: center; padding: 10px 0; }
.sidebar.collapsed .nav a span { display: none; }

/* 主内容 */
.main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0;
}

/* 顶部栏 */
.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 24px;
  background: #fff;
  border-bottom: 1px solid var(--border-color-light);
  box-shadow: var(--header-shadow);
}
.topbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.topbar-left h1 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}
.topbar-left .date-text {
  font-size: 13px;
  color: var(--text-secondary);
}

/* 内容区域 */
.content-area {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
}

@media (max-width: 768px) {
  .sidebar {
    flex: 0 0 56px !important;
    padding: 12px 0;
  }
  .sidebar .logo span,
  .sidebar .nav a span,
  .sidebar .logout-btn {
    display: none;
  }
  .sidebar .nav a {
    padding: 8px;
    justify-content: center;
  }
  .sidebar .logo {
    justify-content: center;
    padding: 0;
  }
}
</style>
