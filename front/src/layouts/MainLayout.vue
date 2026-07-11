<template>
  <div class="app-wrapper">
    <!-- 侧边栏 -->
    <aside class="sidebar">
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
        <a href="#" :class="{ active: currentRoute === 'Query' }" @click.prevent="navigate('Query')">
          <el-icon><Search /></el-icon>
          <span>明细查询</span>
        </a>
        <a href="#" :class="{ active: currentRoute === 'Tasks' || currentRoute === 'TaskForm' }" @click.prevent="navigate('Tasks')">
          <el-icon><List /></el-icon>
          <span>工作任务</span>
        </a>
        <a href="#" :class="{ active: currentRoute === 'Profile' }" @click.prevent="navigate('Profile')">
          <el-icon><UserFilled /></el-icon>
          <span>个人中心</span>
        </a>
      </nav>
      <div class="logout-btn" @click="handleLogout" title="退出登录">
        <el-icon><SwitchButton /></el-icon>
      </div>
    </aside>

    <!-- 主内容 -->
    <main class="main">
      <header class="topbar">
        <div class="topbar-left">
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
  Box, HomeFilled, Setting, Search, List, UserFilled, SwitchButton, Calendar
} from '@element-plus/icons-vue'
import { ElIcon } from 'element-plus'

export default {
  name: 'MainLayout',
  components: {
    Box, HomeFilled, Setting, Search, List, UserFilled, SwitchButton, Calendar,
    ElIcon
  },
  data() {
    return {
      currentDate: '',
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
        Query: '明细查询',
        Tasks: '工作任务',
        Profile: '个人中心',
      }
      return titles[this.$route.name] || 'WMS'
    }
  },
  mounted() {
    this.updateDateTime()
    this.timer = setInterval(this.updateDateTime, 60000)
  },
  beforeDestroy() {
    clearInterval(this.timer)
  },
  methods: {
    navigate(name) {
      this.$router.push({ name })
    },
    handleLogout() {
      localStorage.removeItem('token')
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
  background: #f5f7fa;
  color: #303133;
  overflow: hidden;
}

/* 侧边栏 */
.sidebar {
  flex: 0 0 200px;
  background: #fff;
  border-right: 1px solid #ebeef5;
  padding: 20px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
}
.sidebar .logo {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 16px;
  width: 100%;
}
.sidebar .logo .el-icon {
  font-size: 24px;
  color: #409EFF;
}
.sidebar .logo span {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
}

.sidebar .nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
  width: 100%;
  padding: 0 12px;
}
.sidebar .nav a {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: 4px;
  color: #606266;
  text-decoration: none;
  font-size: 14px;
  transition: background 0.2s;
}
.sidebar .nav a .el-icon {
  font-size: 18px;
}
.sidebar .nav a:hover {
  background: #ecf5ff;
  color: #409EFF;
}
.sidebar .nav a.active {
  background: #ecf5ff;
  color: #409EFF;
}

/* 登出按钮 */
.logout-btn {
  width: 36px;
  height: 36px;
  border-radius: 4px;
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
  padding: 12px 20px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
}
.topbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.topbar-left h1 {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}
.topbar-left .date-text {
  font-size: 13px;
  color: #909399;
}

/* 内容区域 */
.content-area {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
}

@media (max-width: 768px) {
  .sidebar {
    flex: 0 0 56px;
    padding: 12px 0;
  }
  .sidebar .logo span,
  .sidebar .nav a span {
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
