<template>
  <div class="view-layout">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2>查看中心</h2>
        <div class="breadcrumb">
          <span class="breadcrumb-item">首页</span>
          <span class="breadcrumb-separator">/</span>
          <span class="breadcrumb-item active">查看</span>
        </div>
      </div>
      <div class="header-right">
        <div class="header-time">{{ currentTime }}</div>
        <button class="refresh-btn" @click="handleRefresh">刷新</button>
      </div>
    </div>

    <!-- 统计概览 -->
    <div class="stat-row">
      <div class="stat-card">
        <div class="stat-icon blue">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M22 12H18L15 21L9 3L6 12H2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-label">总数据量</span>
          <span class="stat-value">1,284</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon green">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M17 21V19C17 17.9391 16.5786 16.9217 15.8284 16.1716C15.0783 15.4214 14.0609 15 13 15H5C3.93913 15 2.92172 15.4214 2.17157 16.1716C1.42143 16.9217 1 17.9391 1 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M9 11C11.21 11 13 9.21 13 7C13 4.79 11.21 3 9 3C6.79 3 5 4.79 5 7C5 9.21 6.79 11 9 11Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M23 21V19C22.9993 18.1137 22.6452 17.2641 22.018 16.639C21.3908 16.0139 20.5444 15.6523 19.66 15.639" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M16 3.13C16.8864 3.13779 17.7346 3.49352 18.3597 4.11864C18.9848 4.74376 19.3362 5.59086 19.3362 6.475C19.3362 7.35914 18.9848 8.20624 18.3597 8.83136C17.7346 9.45648 16.8864 9.81221 16 9.82" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-label">人员总数</span>
          <span class="stat-value">56</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon orange">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M20 7L12 3L4 7M20 7L12 11M20 7V17L12 21M12 11L4 7M12 11V21M4 7V17L12 21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-label">仓库总数</span>
          <span class="stat-value">12</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon purple">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 8V12L15 15M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-label">今日更新</span>
          <span class="stat-value">23</span>
        </div>
      </div>
    </div>

    <!-- 查看选项区域 -->
    <div class="section-title">
      <h3>查看选项</h3>
      <span class="section-line"></span>
    </div>

    <div class="view-grid">
      <!-- 查看人员列表（已对接） -->
      <div class="view-card featured" @click="navigateToUserList">
        <div class="card-badge">已对接</div>
        <div class="card-icon user">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M17 21V19C17 17.9391 16.5786 16.9217 15.8284 16.1716C15.0783 15.4214 14.0609 15 13 15H5C3.93913 15 2.92172 15.4214 2.17157 16.1716C1.42143 16.9217 1 17.9391 1 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M9 11C11.21 11 13 9.21 13 7C13 4.79 11.21 3 9 3C6.79 3 5 4.79 5 7C5 9.21 6.79 11 9 11Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M23 21V19C22.9993 18.1137 22.6452 17.2641 22.018 16.639C21.3908 16.0139 20.5444 15.6523 19.66 15.639" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M16 3.13C16.8864 3.13779 17.7346 3.49352 18.3597 4.11864C18.9848 4.74376 19.3362 5.59086 19.3362 6.475C19.3362 7.35914 18.9848 8.20624 18.3597 8.83136C17.7346 9.45648 16.8864 9.81221 16 9.82" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <div class="card-content">
          <h4>查看人员列表</h4>
          <p>查看系统所有用户信息</p>
          <div class="card-meta">
            <span class="meta-tag">可用</span>
          </div>
        </div>
      </div>

      <!-- 其他卡片（开发中） -->
      <div class="view-card" v-for="item in devCards" :key="item.name" @click="handleComingSoon(item.name)">
        <div class="card-badge dev">开发中</div>
        <div class="card-icon" :class="item.iconClass">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" v-html="item.iconPath"></svg>
        </div>
        <div class="card-content">
          <h4>{{ item.title }}</h4>
          <p>{{ item.desc }}</p>
          <div class="card-meta">
            <span class="meta-tag dev">开发中</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 提示条 -->
    <transition name="slide-down">
      <div v-if="showToast" class="toast-bar" :class="toastType">
        <span class="toast-message">{{ toastMessage }}</span>
        <button class="toast-close" @click="showToast = false">&times;</button>
      </div>
    </transition>
  </div>
</template>

<script>
export default {
  name: 'ViewPage',
  data() {
    return {
      currentTime: '',
      showToast: false,
      toastMessage: '',
      toastType: 'info',
      timer: null,
      devCards: [
        { name: '仓库信息', title: '查看仓库信息', desc: '仓库基础信息与布局', iconClass: 'warehouse', iconPath: '<path d="M20 7L12 3L4 7M20 7L12 11M20 7V17L12 21M12 11L4 7M12 11V21M4 7V17L12 21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>' },
        { name: '商品档案', title: '查看商品档案', desc: '商品详细资料与分类', iconClass: 'product', iconPath: '<path d="M20 7H4C2.89543 7 2 7.89543 2 9V19C2 20.1046 2.89543 21 4 21H20C21.1046 21 22 20.1046 22 19V9C22 7.89543 21.1046 7 20 7Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M16 21V5C16 4.46957 15.7893 3.96086 15.4142 3.58579C15.0391 3.21071 14.5304 3 14 3H10C9.46957 3 8.96086 3.21071 8.58579 3.58579C8.21071 3.96086 8 4.46957 8 5V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>' },
        { name: '出入库流水', title: '查看出入库流水', desc: '出入库记录与明细', iconClass: 'flow', iconPath: '<path d="M7 13L12 18L22 8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M3 13L8 18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>' },
        { name: '操作日志', title: '查看操作日志', desc: '系统操作记录与追踪', iconClass: 'log', iconPath: '<path d="M14 2H6C5.46957 2 4.96086 2.21071 4.58579 2.58579C4.21071 2.96086 4 3.46957 4 4V20C4 20.5304 4.21071 21.0391 4.58579 21.4142C4.96086 21.7893 5.46957 22 6 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V8L14 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M14 2V8H20" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M16 13H8M16 17H8M10 9H8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>' },
        { name: '库存报表', title: '查看库存报表', desc: '库存统计与数据分析', iconClass: 'report', iconPath: '<path d="M18 20V10M12 20V4M6 20V14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>' },
        { name: '任务看板', title: '查看任务看板', desc: '任务进度与状态概览', iconClass: 'task', iconPath: '<path d="M9 11L12 14L22 4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M21 12V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H16" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>' },
        { name: '系统配置', title: '查看系统配置', desc: '系统参数与设置信息', iconClass: 'config', iconPath: '<path d="M12 15C13.6569 15 15 13.6569 15 12C15 10.3431 13.6569 9 12 9C10.3431 9 9 10.3431 9 12C9 13.6569 10.3431 15 12 15Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M19.4 15C19.2669 15.3016 19.2272 15.6362 19.286 15.9606C19.3448 16.285 19.4995 16.5843 19.73 16.82L19.79 16.88C19.976 17.0657 20.1235 17.2863 20.2241 17.5291C20.3248 17.7719 20.3766 18.0322 20.3766 18.295C20.3766 18.5578 20.3248 18.8181 20.2241 19.0609C20.1235 19.3037 19.976 19.5243 19.79 19.71C19.6043 19.896 19.3837 20.0435 19.1409 20.1441C18.8981 20.2448 18.6378 20.2966 18.375 20.2966C18.1122 20.2966 17.8519 20.2448 17.6091 20.1441C17.3663 20.0435 17.1457 19.896 16.96 19.71L16.9 19.65C16.6643 19.4195 16.365 19.2648 16.0406 19.206C15.7162 19.1472 15.3816 19.1869 15.08 19.32C14.7842 19.4468 14.532 19.6572 14.3543 19.9255C14.1766 20.1938 14.0813 20.5082 14.08 20.83V21C14.08 21.5304 13.8693 22.0391 13.4942 22.4142C13.1191 22.7893 12.6104 23 12.08 23C11.5496 23 11.0409 22.7893 10.6658 22.4142C10.2907 22.0391 10.08 21.5304 10.08 21V20.91C10.0723 20.579 9.96512 20.258 9.77251 19.9887C9.5799 19.7194 9.31074 19.5143 9 19.4C8.69838 19.2669 8.36381 19.2272 8.03941 19.286C7.71502 19.3448 7.41568 19.4995 7.18 19.73L7.12 19.79C6.93425 19.976 6.71366 20.1235 6.47088 20.2241C6.2281 20.3248 5.96778 20.3766 5.705 20.3766C5.44222 20.3766 5.1819 20.3248 4.93912 20.2241C4.69634 20.1235 4.47575 19.976 4.29 19.79C4.10405 19.6043 3.95653 19.3837 3.85588 19.1409C3.75523 18.8981 3.70343 18.6378 3.70343 18.375C3.70343 18.1122 3.75523 17.8519 3.85588 17.6091C3.95653 17.3663 4.10405 17.1457 4.29 16.96L4.35 16.9C4.58054 16.6643 4.73523 16.365 4.79403 16.0406C4.85282 15.7162 4.81312 15.3816 4.68 15.08C4.55324 14.7842 4.34284 14.532 4.07454 14.3543C3.80624 14.1766 3.49179 14.0813 3.17 14.08H3C2.46957 14.08 1.96086 13.8693 1.58579 13.4942C1.21071 13.1191 1 12.6104 1 12.08C1 11.5496 1.21071 11.0409 1.58579 10.6658C1.96086 10.2907 2.46957 10.08 3 10.08H3.09C3.42099 10.0723 3.742 9.96512 4.0113 9.77251C4.28059 9.5799 4.48572 9.31074 4.6 9C4.73312 8.69838 4.77282 8.36381 4.71403 8.03941C4.65523 7.71502 4.50054 7.41568 4.27 7.18L4.21 7.12C4.02405 6.93425 3.87653 6.71366 3.77588 6.47088C3.67523 6.2281 3.62343 5.96778 3.62343 5.705C3.62343 5.44222 3.67523 5.1819 3.77588 4.93912C3.87653 4.69634 4.02405 4.47575 4.21 4.29C4.39575 4.10405 4.61634 3.95653 4.85912 3.85588C5.1019 3.75523 5.36222 3.70343 5.625 3.70343C5.88778 3.70343 6.1481 3.75523 6.39088 3.85588C6.63366 3.95653 6.85425 4.10405 7.04 4.29L7.1 4.35C7.33568 4.58054 7.63502 4.73523 7.95941 4.79403C8.28381 4.85282 8.61838 4.81312 8.92 4.68H9C9.29577 4.55324 9.54802 4.34284 9.72572 4.07454C9.90341 3.80624 9.99872 3.49179 10 3.17V3C10 2.46957 10.2107 1.96086 10.5858 1.58579C10.9609 1.21071 11.4696 1 12 1C12.5304 1 13.0391 1.21071 13.4142 1.58579C13.7893 1.96086 14 2.46957 14 3V3.09C14.0013 3.41179 14.0966 3.72624 14.2743 3.99454C14.452 4.26284 14.7042 4.47324 15 4.6C15.3016 4.73312 15.6362 4.77282 15.9606 4.71403C16.285 4.65523 16.5843 4.50054 16.82 4.27L16.88 4.21C17.0657 4.02405 17.2863 3.87653 17.5291 3.77588C17.7719 3.67523 18.0322 3.62343 18.295 3.62343C18.5578 3.62343 18.8181 3.67523 19.0609 3.77588C19.3037 3.87653 19.5243 4.02405 19.71 4.21C19.896 4.39575 20.0435 4.61634 20.1441 4.85912C20.2448 5.1019 20.2966 5.36222 20.2966 5.625C20.2966 5.88778 20.2448 6.1481 20.1441 6.39088C20.0435 6.63366 19.896 6.85425 19.71 7.04L19.65 7.1C19.4195 7.33568 19.2648 7.63502 19.206 7.95941C19.1472 8.28381 19.1869 8.61838 19.32 8.92V9C19.4468 9.29577 19.6572 9.54802 19.9255 9.72572C20.1938 9.90341 20.5082 9.99872 20.83 10H21C21.5304 10 22.0391 10.2107 22.4142 10.5858C22.7893 10.9609 23 11.4696 23 12C23 12.5304 22.7893 13.0391 22.4142 13.4142C22.0391 13.7893 21.5304 14 21 14H20.91C20.5882 14.0013 20.2738 14.0966 20.0055 14.2743C19.7372 14.452 19.5268 14.7042 19.4 15Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>' },
      ]
    }
  },
  mounted() {
    this.updateTime()
    this.timeInterval = setInterval(this.updateTime, 1000)
  },
  beforeDestroy() {
    if (this.timeInterval) {
      clearInterval(this.timeInterval)
    }
    if (this.timer) {
      clearTimeout(this.timer)
    }
  },
  methods: {
    navigateToUserList() {
      this.$router.push({ name: 'Personnel' })
    },
    handleComingSoon(moduleName) {
      this.showToastMessage(`「${moduleName}」功能正在开发中，敬请期待！`, 'info')
    },
    showToastMessage(message, type = 'info') {
      if (this.timer) {
        clearTimeout(this.timer)
        this.timer = null
      }
      this.toastMessage = message
      this.toastType = type
      this.showToast = true
      this.timer = setTimeout(() => {
        this.showToast = false
        this.timer = null
      }, 3000)
    },
    handleRefresh() {
      this.showToastMessage('数据已刷新', 'success')
    },
    updateTime() {
      const now = new Date()
      this.currentTime = now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
    },
  },
}
</script>

<style scoped>
.view-layout {
  padding: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* 页面头部 */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--card);
  padding: 14px 20px;
  border: 1px solid var(--border-light);
  border-radius: 4px;
  margin-bottom: 12px;
}

.page-header .header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-header .header-left h2 {
  font-size: 16px;
  font-weight: 600;
  color: var(--foreground);
  margin: 0;
}

.page-header .header-left .breadcrumb {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--foreground-muted);
}

.page-header .header-left .breadcrumb .breadcrumb-item.active {
  color: var(--primary-color);
}

.page-header .header-left .breadcrumb .breadcrumb-separator {
  color: var(--foreground-placeholder);
}

.page-header .header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-header .header-right .header-time {
  font-size: 13px;
  color: var(--foreground-regular);
  background: var(--page-bg);
  padding: 4px 10px;
  border-radius: 4px;
}

.page-header .header-right .refresh-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 5px 14px;
  border: 1px solid var(--border);
  border-radius: 4px;
  background: var(--card);
  color: var(--foreground-regular);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.page-header .header-right .refresh-btn:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

/* 统计概览 */
.stat-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}

.stat-row .stat-card {
  background: var(--card);
  border: 1px solid var(--border-light);
  border-radius: 4px;
  padding: 16px 18px;
  display: flex;
  align-items: center;
  gap: 14px;
}

.stat-row .stat-card .stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-row .stat-card .stat-icon svg {
  width: 20px;
  height: 20px;
  color: var(--text-on-primary);
}

.stat-row .stat-card .stat-icon.blue { background: var(--primary-color); }
.stat-row .stat-card .stat-icon.green { background: var(--success); }
.stat-row .stat-card .stat-icon.orange { background: var(--warning); }
.stat-row .stat-card .stat-icon.purple { background: #9254de; }

.stat-row .stat-card .stat-info {
  display: flex;
  flex-direction: column;
}

.stat-row .stat-card .stat-info .stat-label {
  font-size: 13px;
  color: var(--foreground-muted);
}

.stat-row .stat-card .stat-info .stat-value {
  font-size: 22px;
  font-weight: 600;
  color: var(--foreground);
}

/* 区域标题 */
.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.section-title h3 {
  font-size: 15px;
  font-weight: 600;
  color: var(--foreground);
  white-space: nowrap;
  margin: 0;
}

.section-title .section-line {
  flex: 1;
  height: 1px;
  background: var(--border-light);
}

/* 查看选项网格 */
.view-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.view-card {
  background: var(--card);
  border: 1px solid var(--border-light);
  border-radius: 4px;
  padding: 18px 18px 14px;
  cursor: pointer;
  position: relative;
  display: flex;
  flex-direction: column;
  transition: border-color 0.2s;
}

.view-card:hover {
  border-color: var(--primary-color);
}

.view-card .card-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  background: var(--success);
  color: var(--text-on-primary);
}

.view-card .card-badge.dev {
  background: var(--warning);
  color: var(--text-on-primary);
}

.view-card .card-icon {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 10px;
  flex-shrink: 0;
}

.view-card .card-icon svg {
  width: 22px;
  height: 22px;
  color: var(--text-on-primary);
}

.view-card .card-icon.user { background: var(--primary-color); }
.view-card .card-icon.warehouse { background: var(--success); }
.view-card .card-icon.product { background: var(--warning); }
.view-card .card-icon.flow { background: #9254de; }
.view-card .card-icon.log { background: var(--danger); }
.view-card .card-icon.report { background: #13c2c2; }
.view-card .card-icon.task { background: #fa8c16; }
.view-card .card-icon.config { background: #597ef7; }

.view-card .card-content h4 {
  font-size: 14px;
  font-weight: 600;
  color: var(--foreground);
  margin: 0 0 4px 0;
}

.view-card .card-content p {
  font-size: 13px;
  color: var(--foreground-muted);
  margin: 0 0 10px 0;
}

.view-card .card-meta .meta-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  background: var(--primary-bg);
  color: var(--primary-color);
}

.view-card .card-meta .meta-tag.dev {
  background: var(--warning-bg);
  color: var(--warning);
}

/* 突出样式（人员列表） */
.view-card.featured {
  border-color: var(--primary-color);
}

.view-card.featured .card-meta .meta-tag {
  background: var(--primary-color);
  color: var(--text-on-primary);
}

/* 提示条 */
.toast-bar {
  position: fixed;
  top: 80px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 9999;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 20px;
  background: var(--card);
  border: 1px solid var(--border-light);
  border-radius: 4px;
  border-left: 3px solid var(--primary-color);
  min-width: 280px;
  max-width: 500px;
}

.toast-bar.success { border-left-color: var(--success); }
.toast-bar.info { border-left-color: var(--primary-color); }

.toast-bar .toast-message {
  font-size: 14px;
  color: var(--foreground);
  flex: 1;
}

.toast-bar .toast-close {
  background: none;
  border: none;
  cursor: pointer;
  color: var(--foreground-placeholder);
  font-size: 18px;
  padding: 0 4px;
}

.toast-bar .toast-close:hover {
  color: var(--foreground-regular);
}

/* 提示条动画 */
.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.3s ease;
}
.slide-down-enter-from,
.slide-down-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(-16px);
}

/* 响应式 */
@media (max-width: 1200px) {
  .view-grid { grid-template-columns: repeat(3, 1fr); }
  .stat-row { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 992px) {
  .view-grid { grid-template-columns: repeat(2, 1fr); }
  .page-header { flex-direction: column; align-items: flex-start; gap: 10px; }
}
@media (max-width: 768px) {
  .view-layout { padding: 0; }
  .view-grid { grid-template-columns: 1fr; }
  .stat-row { grid-template-columns: 1fr 1fr; gap: 8px; }
  .toast-bar { min-width: unset; width: 90%; }
}
</style>
