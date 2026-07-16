<template>
  <div class="app-wrapper">
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ collapsed: sidebarCollapsed }" :style="{ flex: `0 0 ${sidebarCollapsed ? 60 : 224}px` }">
      <div class="logo">
        <el-icon><Box /></el-icon>
        <span>WMS</span>
      </div>
      <nav class="nav">
        <template v-for="item in visibleMenuItems" :key="item.label">
          <!-- 无子菜单：普通链接 -->
          <a
            v-if="!item.children"
            href="#"
            :class="{ active: currentRoute === item.route }"
            @click.prevent="navigate(item.route)"
          >
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.label }}</span>
          </a>
          <!-- 有子菜单：可展开父级 -->
          <div v-else class="nav-group" :class="{ expanded: expandedMenus[item.label] }">
            <a
              href="#"
              class="nav-parent"
              :class="{ active: isMenuActive(item) }"
              @click.prevent="toggleMenu(item.label)"
            >
              <el-icon><component :is="item.icon" /></el-icon>
              <span>{{ item.label }}</span>
              <el-icon class="arrow"><ArrowDown /></el-icon>
            </a>
            <div class="submenu" v-show="expandedMenus[item.label]">
              <a
                v-for="child in item.children"
                :key="child.label"
                href="#"
                :class="{ active: isChildActive(item, child) }"
                @click.prevent="navigate(child.route, child.query)"
              >
                <span>{{ child.label }}</span>
              </a>
            </div>
          </div>
        </template>
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
          <div class="breadcrumb">
            <el-icon><HomeFilled /></el-icon>
            <span class="breadcrumb-sep">/</span>
            <span>{{ pageTitle }}</span>
          </div>
        </div>
        <div class="topbar-right">
          <div class="theme-toggle" @click="toggleTheme" :title="theme === 'dark' ? '切换亮色模式' : '切换暗色模式'">
            <el-icon v-if="theme === 'dark'"><Sunny /></el-icon>
            <el-icon v-else><Moon /></el-icon>
          </div>
          <span class="header-date">{{ currentDate }}</span>
          <div class="user-badge" @click="navigate('Profile')" title="个人中心">
            <span class="user-avatar">{{ (userInfo.real_name || userInfo.username || 'U').charAt(0) }}</span>
            <span class="user-name">{{ userInfo.real_name || userInfo.username || '用户' }}</span>
          </div>
        </div>
      </header>

      <div class="content-area" :class="{ 'loading-active': routeLoading }">
        <div class="route-loading-overlay">
          <div class="route-loader"></div>
        </div>
        <router-view></router-view>
      </div>
      <footer class="layout-footer">
        <span>PMF WMS 智能仓储管理系统</span>
        <span class="footer-divider">|</span>
        <span>© 2026 Prepared-Meal-Factory</span>
      </footer>
    </main>
  </div>
</template>

<script>
import {
  Box, HomeFilled, Setting, Search, List, ChatDotRound, UserFilled, SwitchButton, Calendar, DataAnalysis, DArrowLeft, DArrowRight, Sunny, Moon, ArrowDown
} from '@element-plus/icons-vue'
import { ElIcon } from 'element-plus'
import { useTheme } from '@/composables/useTheme.js'

export default {
  name: 'MainLayout',
  components: {
    Box, HomeFilled, Setting, Search, List, ChatDotRound, UserFilled, SwitchButton, Calendar, DataAnalysis, DArrowLeft, DArrowRight, Sunny, Moon, ArrowDown, ElIcon
  },
  setup() {
    const { theme, toggleTheme } = useTheme()
    return { theme, toggleTheme }
  },
  data() {
    return {
      currentDate: '',
      sidebarCollapsed: false,
      userPosition: '',
      userInfo: {},
      routeLoading: false,
      expandedMenus: {},
      menuItems: [
        { label: '主页', icon: 'HomeFilled', route: 'Home' },
        {
          label: '我的管理', icon: 'Setting', requirePosition: '主管',
          children: [
            { label: '人员列表', route: 'Manage', query: { tab: 'personnel' } },
            { label: '仓库列表', route: 'Manage', query: { tab: 'warehouse' } },
            { label: '货物列表', route: 'Manage', query: { tab: 'goods' } },
          ]
        },
        { label: '数据看板', icon: 'DataAnalysis', route: 'Dashboard' },
        {
          label: '明细查询', icon: 'Search',
          children: [
            { label: '人员查询', route: 'Query', query: { tab: 'personnel' } },
            { label: '仓库查询', route: 'Query', query: { tab: 'warehouse' } },
            { label: '货物查询', route: 'Query', query: { tab: 'goods' } },
            { label: '库存明细', route: 'Query', query: { tab: 'inventory' } },
            { label: '批次查询', route: 'Query', query: { tab: 'batch' } },
            { label: '操作日志', route: 'Query', query: { tab: 'log' } },
          ]
        },
        {
          label: '工作任务', icon: 'List',
          children: [
            { label: '我的待办', route: 'Tasks', query: { tab: 'todo' } },
            { label: '操作中心', route: 'Tasks', query: { tab: 'assign' } },
          ]
        },
        { label: 'AI助手', icon: 'ChatDotRound', route: 'AiChat' },
        { label: '个人中心', icon: 'UserFilled', route: 'Profile' },
      ],
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
    },
    visibleMenuItems() {
      return this.menuItems.filter(item => {
        if (item.requirePosition && this.userPosition !== item.requirePosition) return false
        return true
      })
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
      try {
        const parsed = JSON.parse(stored)
        this.userPosition = parsed.position || ''
        this.userInfo = parsed
      } catch (e) {}
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
    navigate(name, query) {
      if (query) {
        this.$router.push({ name, query })
      } else {
        this.$router.push({ name })
      }
    },
    toggleMenu(label) {
      this.expandedMenus[label] = !this.expandedMenus[label]
      this.expandedMenus = { ...this.expandedMenus }  // 触发响应式更新
    },
    isMenuActive(item) {
      return item.children.some(c => c.route === this.currentRoute)
    },
    isChildActive(parent, child) {
      return this.currentRoute === child.route &&
        this.$route.query.tab === child.query.tab
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
  color: var(--text-on-primary);
  flex-shrink: 0;
}
.sidebar .logo span {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-on-primary);
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
  min-height: 0;
  margin-top: 32px;
  overflow-y: auto;
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
.sidebar .nav a.active,
.submenu a.active {
  background: var(--sidebar-item-active-bg);
  color: var(--sidebar-text-hover);
  font-weight: 500;
  box-shadow: 0 2px 8px hsl(0 0% 0% / 12%);
}

/* 下拉菜单组 */
.nav-group .arrow {
  font-size: 12px;
  margin-left: auto;
  transition: transform 0.25s ease;
}
.nav-group.expanded .arrow {
  transform: rotate(180deg);
}

/* 子菜单容器 */
.submenu {
  padding-left: 24px;
  overflow: hidden;
  margin-bottom: 2px;
}
.submenu a {
  display: flex;
  align-items: center;
  padding: 8px 14px;
  border-radius: var(--radius-md);
  color: var(--sidebar-text);
  text-decoration: none;
  font-size: 13px;
  transition: all 0.2s ease;
  white-space: nowrap;
  opacity: 0.9;
}
.submenu a:hover {
  background: var(--sidebar-item-hover-bg);
  color: var(--sidebar-text-hover);
  opacity: 1;
}
.submenu a::before {
  content: '';
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: var(--sidebar-text);
  margin-right: 8px;
  opacity: 0.5;
  flex-shrink: 0;
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
  color: var(--text-on-primary);
}

/* 收缩状态 */
.sidebar.collapsed .logo { justify-content: center; padding: 0 8px; }
.sidebar.collapsed .logo span { display: none; }
.sidebar.collapsed .nav a { justify-content: center; padding: 10px 0; }
.sidebar.collapsed .nav a span { display: none; }
.sidebar.collapsed .nav-group .arrow { display: none; }
.sidebar.collapsed .submenu { display: none; }

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
  padding: 0 24px;
  height: 48px;
  background: var(--topbar-bg);
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

/* 面包屑 */
.breadcrumb {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--foreground-muted);
  margin-left: 4px;
}
.breadcrumb .el-icon { font-size: 14px; color: var(--foreground-muted); }
.breadcrumb-sep { color: var(--foreground-placeholder); margin: 0 2px; }
.breadcrumb span:last-child { color: var(--foreground); font-weight: 500; }

/* 右侧区域 */
.topbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
.header-date {
  font-size: 12px;
  color: var(--foreground-muted);
}

.theme-toggle {
  width: 32px; height: 32px;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  color: var(--foreground-muted);
  font-size: 18px;
}
.theme-toggle:hover {
  background: var(--border-light);
  color: var(--foreground);
}

/* 用户徽章 */
.user-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px 4px 4px;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.2s ease;
}
.user-badge:hover { background: var(--border-light); }
.user-avatar {
  width: 32px; height: 32px;
  border-radius: 50%;
  background: var(--primary);
  color: var(--text-on-primary);
  display: flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: 600;
  flex-shrink: 0;
}
.user-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--foreground);
  white-space: nowrap;
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
  background: var(--page-bg);
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

/* ========== 页脚 ========== */
.layout-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 6px 24px;
  font-size: 12px;
  color: var(--foreground-muted);
  background: var(--card);
  border-top: 1px solid var(--border-light);
  flex-shrink: 0;
}
.footer-divider { color: var(--foreground-placeholder); }

/* ========== 响应式 ========== */
@media (max-width: 768px) {
  .sidebar {
    flex: 0 0 56px !important;
    padding: 12px 0;
  }
  .sidebar .logo span,
  .sidebar .nav a span,
  .sidebar .logout-btn,
  .sidebar .nav-group .arrow,
  .sidebar .submenu { display: none; }
  .sidebar .nav a { padding: 8px; justify-content: center; }
  .sidebar .logo { justify-content: center; padding: 0; }
  .content-area { padding: 14px 16px; }
}
</style>
