<template>
  <div class="view-layout">
    <!-- ====== 页面头部 ====== -->
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
        <button class="refresh-btn" @click="handleRefresh">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M1 4V10H7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M3.51 15C4.15812 17.0081 5.38714 18.7542 7.02023 20.0407C8.65331 21.3272 10.6146 22.0951 12.6623 22.2454C14.71 22.3957 16.7514 21.9218 18.5353 20.8896C20.3191 19.8574 21.7608 18.319 20.706 16.476" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M23 20V14H17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M20.49 9C19.8419 6.99188 18.6129 5.24578 16.9798 3.9593C15.3467 2.67282 13.3854 1.90493 11.3377 1.75462C9.28999 1.6043 7.2486 2.07819 5.46474 3.1104C3.68089 4.1426 2.2392 5.68102 1 7.524" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          刷新
        </button>
      </div>
    </div>

    <!-- ====== 统计概览 ====== -->
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

    <!-- ====== 查看选项区域 ====== -->
    <div class="section-title">
      <h3>查看选项</h3>
      <span class="section-line"></span>
      <span class="section-tip">选择下方入口，查看对应数据</span>
    </div>

    <div class="view-grid">
      <!-- 1. 查看人员列表（已对接 · 突出） -->
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
            <span class="meta-arrow">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M5 12H19M19 12L12 5M19 12L12 19" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </span>
          </div>
        </div>
      </div>

      <!-- 2. 查看仓库信息 -->
      <div class="view-card" @click="handleComingSoon('仓库信息')">
        <div class="card-badge dev">开发中</div>
        <div class="card-icon warehouse">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M20 7L12 3L4 7M20 7L12 11M20 7V17L12 21M12 11L4 7M12 11V21M4 7V17L12 21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <div class="card-content">
          <h4>查看仓库信息</h4>
          <p>仓库基础信息与布局</p>
          <div class="card-meta">
            <span class="meta-tag dev">开发中</span>
          </div>
        </div>
      </div>

      <!-- 3. 查看商品档案 -->
      <div class="view-card" @click="handleComingSoon('商品档案')">
        <div class="card-badge dev">开发中</div>
        <div class="card-icon product">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M20 7H4C2.89543 7 2 7.89543 2 9V19C2 20.1046 2.89543 21 4 21H20C21.1046 21 22 20.1046 22 19V9C22 7.89543 21.1046 7 20 7Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M16 21V5C16 4.46957 15.7893 3.96086 15.4142 3.58579C15.0391 3.21071 14.5304 3 14 3H10C9.46957 3 8.96086 3.21071 8.58579 3.58579C8.21071 3.96086 8 4.46957 8 5V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <div class="card-content">
          <h4>查看商品档案</h4>
          <p>商品详细资料与分类</p>
          <div class="card-meta">
            <span class="meta-tag dev">开发中</span>
          </div>
        </div>
      </div>

      <!-- 4. 查看出入库流水 -->
      <div class="view-card" @click="handleComingSoon('出入库流水')">
        <div class="card-badge dev">开发中</div>
        <div class="card-icon flow">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M7 13L12 18L22 8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M3 13L8 18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <div class="card-content">
          <h4>查看出入库流水</h4>
          <p>出入库记录与明细</p>
          <div class="card-meta">
            <span class="meta-tag dev">开发中</span>
          </div>
        </div>
      </div>

      <!-- 5. 查看操作日志 -->
      <div class="view-card" @click="handleComingSoon('操作日志')">
        <div class="card-badge dev">开发中</div>
        <div class="card-icon log">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M14 2H6C5.46957 2 4.96086 2.21071 4.58579 2.58579C4.21071 2.96086 4 3.46957 4 4V20C4 20.5304 4.21071 21.0391 4.58579 21.4142C4.96086 21.7893 5.46957 22 6 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V8L14 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M14 2V8H20" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M16 13H8M16 17H8M10 9H8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <div class="card-content">
          <h4>查看操作日志</h4>
          <p>系统操作记录与追踪</p>
          <div class="card-meta">
            <span class="meta-tag dev">开发中</span>
          </div>
        </div>
      </div>

      <!-- 6. 查看库存报表 -->
      <div class="view-card" @click="handleComingSoon('库存报表')">
        <div class="card-badge dev">开发中</div>
        <div class="card-icon report">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M18 20V10M12 20V4M6 20V14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <div class="card-content">
          <h4>查看库存报表</h4>
          <p>库存统计与数据分析</p>
          <div class="card-meta">
            <span class="meta-tag dev">开发中</span>
          </div>
        </div>
      </div>

      <!-- 7. 查看任务看板 -->
      <div class="view-card" @click="handleComingSoon('任务看板')">
        <div class="card-badge dev">开发中</div>
        <div class="card-icon task">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M9 11L12 14L22 4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M21 12V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H16" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <div class="card-content">
          <h4>查看任务看板</h4>
          <p>任务进度与状态概览</p>
          <div class="card-meta">
            <span class="meta-tag dev">开发中</span>
          </div>
        </div>
      </div>

      <!-- 8. 查看系统配置 -->
      <div class="view-card" @click="handleComingSoon('系统配置')">
        <div class="card-badge dev">开发中</div>
        <div class="card-icon config">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 15C13.6569 15 15 13.6569 15 12C15 10.3431 13.6569 9 12 9C10.3431 9 9 10.3431 9 12C9 13.6569 10.3431 15 12 15Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M19.4 15C19.2669 15.3016 19.2272 15.6362 19.286 15.9606C19.3448 16.285 19.4995 16.5843 19.73 16.82L19.79 16.88C19.976 17.0657 20.1235 17.2863 20.2241 17.5291C20.3248 17.7719 20.3766 18.0322 20.3766 18.295C20.3766 18.5578 20.3248 18.8181 20.2241 19.0609C20.1235 19.3037 19.976 19.5243 19.79 19.71C19.6043 19.896 19.3837 20.0435 19.1409 20.1441C18.8981 20.2448 18.6378 20.2966 18.375 20.2966C18.1122 20.2966 17.8519 20.2448 17.6091 20.1441C17.3663 20.0435 17.1457 19.896 16.96 19.71L16.9 19.65C16.6643 19.4195 16.365 19.2648 16.0406 19.206C15.7162 19.1472 15.3816 19.1869 15.08 19.32C14.7842 19.4468 14.532 19.6572 14.3543 19.9255C14.1766 20.1938 14.0813 20.5082 14.08 20.83V21C14.08 21.5304 13.8693 22.0391 13.4942 22.4142C13.1191 22.7893 12.6104 23 12.08 23C11.5496 23 11.0409 22.7893 10.6658 22.4142C10.2907 22.0391 10.08 21.5304 10.08 21V20.91C10.0723 20.579 9.96512 20.258 9.77251 19.9887C9.5799 19.7194 9.31074 19.5143 9 19.4C8.69838 19.2669 8.36381 19.2272 8.03941 19.286C7.71502 19.3448 7.41568 19.4995 7.18 19.73L7.12 19.79C6.93425 19.976 6.71366 20.1235 6.47088 20.2241C6.2281 20.3248 5.96778 20.3766 5.705 20.3766C5.44222 20.3766 5.1819 20.3248 4.93912 20.2241C4.69634 20.1235 4.47575 19.976 4.29 19.79C4.10405 19.6043 3.95653 19.3837 3.85588 19.1409C3.75523 18.8981 3.70343 18.6378 3.70343 18.375C3.70343 18.1122 3.75523 17.8519 3.85588 17.6091C3.95653 17.3663 4.10405 17.1457 4.29 16.96L4.35 16.9C4.58054 16.6643 4.73523 16.365 4.79403 16.0406C4.85282 15.7162 4.81312 15.3816 4.68 15.08C4.55324 14.7842 4.34284 14.532 4.07454 14.3543C3.80624 14.1766 3.49179 14.0813 3.17 14.08H3C2.46957 14.08 1.96086 13.8693 1.58579 13.4942C1.21071 13.1191 1 12.6104 1 12.08C1 11.5496 1.21071 11.0409 1.58579 10.6658C1.96086 10.2907 2.46957 10.08 3 10.08H3.09C3.42099 10.0723 3.742 9.96512 4.0113 9.77251C4.28059 9.5799 4.48572 9.31074 4.6 9C4.73312 8.69838 4.77282 8.36381 4.71403 8.03941C4.65523 7.71502 4.50054 7.41568 4.27 7.18L4.21 7.12C4.02405 6.93425 3.87653 6.71366 3.77588 6.47088C3.67523 6.2281 3.62343 5.96778 3.62343 5.705C3.62343 5.44222 3.67523 5.1819 3.77588 4.93912C3.87653 4.69634 4.02405 4.47575 4.21 4.29C4.39575 4.10405 4.61634 3.95653 4.85912 3.85588C5.1019 3.75523 5.36222 3.70343 5.625 3.70343C5.88778 3.70343 6.1481 3.75523 6.39088 3.85588C6.63366 3.95653 6.85425 4.10405 7.04 4.29L7.1 4.35C7.33568 4.58054 7.63502 4.73523 7.95941 4.79403C8.28381 4.85282 8.61838 4.81312 8.92 4.68H9C9.29577 4.55324 9.54802 4.34284 9.72572 4.07454C9.90341 3.80624 9.99872 3.49179 10 3.17V3C10 2.46957 10.2107 1.96086 10.5858 1.58579C10.9609 1.21071 11.4696 1 12 1C12.5304 1 13.0391 1.21071 13.4142 1.58579C13.7893 1.96086 14 2.46957 14 3V3.09C14.0013 3.41179 14.0966 3.72624 14.2743 3.99454C14.452 4.26284 14.7042 4.47324 15 4.6C15.3016 4.73312 15.6362 4.77282 15.9606 4.71403C16.285 4.65523 16.5843 4.50054 16.82 4.27L16.88 4.21C17.0657 4.02405 17.2863 3.87653 17.5291 3.77588C17.7719 3.67523 18.0322 3.62343 18.295 3.62343C18.5578 3.62343 18.8181 3.67523 19.0609 3.77588C19.3037 3.87653 19.5243 4.02405 19.71 4.21C19.896 4.39575 20.0435 4.61634 20.1441 4.85912C20.2448 5.1019 20.2966 5.36222 20.2966 5.625C20.2966 5.88778 20.2448 6.1481 20.1441 6.39088C20.0435 6.63366 19.896 6.85425 19.71 7.04L19.65 7.1C19.4195 7.33568 19.2648 7.63502 19.206 7.95941C19.1472 8.28381 19.1869 8.61838 19.32 8.92V9C19.4468 9.29577 19.6572 9.54802 19.9255 9.72572C20.1938 9.90341 20.5082 9.99872 20.83 10H21C21.5304 10 22.0391 10.2107 22.4142 10.5858C22.7893 10.9609 23 11.4696 23 12C23 12.5304 22.7893 13.0391 22.4142 13.4142C22.0391 13.7893 21.5304 14 21 14H20.91C20.5882 14.0013 20.2738 14.0966 20.0055 14.2743C19.7372 14.452 19.5268 14.7042 19.4 15Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <div class="card-content">
          <h4>查看系统配置</h4>
          <p>系统参数与设置信息</p>
          <div class="card-meta">
            <span class="meta-tag dev">开发中</span>
          </div>
        </div>
      </div>
    </div>

    <!-- ====== 提示条 ====== -->
    <transition name="slide-down">
      <div v-if="showToast" class="toast-bar" :class="toastType">
        <div class="toast-icon">
          <svg v-if="toastType === 'info'" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M12 8V12M12 16H12.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <svg v-else viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M22 11.08V12C21.9988 14.1564 21.3005 16.2547 20.0093 17.9818C18.7182 19.709 16.9033 20.9725 14.8354 21.5839C12.7674 22.1953 10.5573 22.1219 8.53447 21.3746C6.51168 20.6273 4.78465 19.2461 3.61096 17.4371C2.43727 15.628 1.87979 13.4881 2.02168 11.3363C2.16356 9.18455 2.99721 7.13631 4.39828 5.49706C5.79935 3.85781 7.69279 2.71537 9.79619 2.24013C11.8996 1.7649 14.1003 1.98232 16.07 2.85999" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M22 4L12 14.01L9 11.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <span class="toast-message">{{ toastMessage }}</span>
        <button class="toast-close" @click="showToast = false">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
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
      toastType: 'info', // 'info' | 'success'
      timer: null,
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
      // 直接跳转到人员列表页面
      this.$router.push({ name: 'Personnel' })
    },

    // 处理"开发中"点击
    handleComingSoon(moduleName) {
      this.showToastMessage(`「${moduleName}」功能正在开发中，敬请期待！`, 'info')
    },

    // 显示提示条
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
      }, 3500)
    },

    // 刷新页面
    handleRefresh() {
      this.showToastMessage('数据已刷新', 'success')
      // 实际项目中可以调用刷新数据的API
    },
  },
}
</script>

<style lang="scss" scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.view-layout {
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC',
    'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
  background: #f0f2f5;
  min-height: 100vh;
}

/* ====== 页面头部 ====== */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  padding: 16px 24px;
  border-radius: 4px;
  margin-bottom: 16px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);

  .header-left {
    display: flex;
    align-items: center;
    gap: 20px;

    h2 {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
      margin: 0;
      letter-spacing: 0.5px;
    }

    .breadcrumb {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;
      color: #909399;

      .breadcrumb-item {
        &.active {
          color: #409EFF;
        }
      }
      .breadcrumb-separator {
        color: #c0c4cc;
      }
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 16px;

    .header-time {
      font-size: 13px;
      color: #606266;
      font-variant-numeric: tabular-nums;
      background: #f5f7fa;
      padding: 4px 12px;
      border-radius: 4px;
      letter-spacing: 0.5px;
    }

    .refresh-btn {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 6px 16px;
      border: 1px solid #dcdfe6;
      border-radius: 4px;
      background: #fff;
      color: #606266;
      font-size: 13px;
      cursor: pointer;
      transition: all 0.2s ease;

      svg {
        width: 16px;
        height: 16px;
      }

      &:hover {
        border-color: #409EFF;
        color: #409EFF;
        background: #ecf5ff;
      }

      &:active {
        transform: scale(0.97);
      }
    }
  }
}

/* ====== 统计概览 ====== */
.stat-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;

  .stat-card {
    background: #fff;
    border-radius: 4px;
    padding: 18px 20px;
    display: flex;
    align-items: center;
    gap: 16px;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
    transition: box-shadow 0.3s ease;

    &:hover {
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    }

    .stat-icon {
      width: 44px;
      height: 44px;
      border-radius: 4px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;

      svg {
        width: 22px;
        height: 22px;
        color: #fff;
      }

      &.blue {
        background: #409EFF;
      }
      &.green {
        background: #67C23A;
      }
      &.orange {
        background: #E6A23C;
      }
      &.purple {
        background: #9254de;
      }
    }

    .stat-info {
      display: flex;
      flex-direction: column;
      flex: 1;

      .stat-label {
        font-size: 13px;
        color: #909399;
        margin-bottom: 2px;
      }

      .stat-value {
        font-size: 22px;
        font-weight: 600;
        color: #303133;
        line-height: 1.2;
      }
    }
  }
}

/* ====== 区域标题 ====== */
.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;

  h3 {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    white-space: nowrap;
    margin: 0;
  }

  .section-line {
    flex: 1;
    height: 1px;
    background: #ebeef5;
  }

  .section-tip {
    font-size: 13px;
    color: #909399;
    white-space: nowrap;
  }
}

/* ====== 查看选项网格 ====== */
.view-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.view-card {
  background: #fff;
  border-radius: 4px;
  padding: 20px 20px 16px;
  cursor: pointer;
  border: 1px solid #ebeef5;
  transition: all 0.3s ease;
  position: relative;
  display: flex;
  flex-direction: column;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.02);

  &:hover {
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    transform: translateY(-3px);
    border-color: #d0d7e0;

    .card-meta .meta-arrow svg {
      transform: translateX(4px);
    }
  }

  .card-badge {
    position: absolute;
    top: 12px;
    right: 12px;
    font-size: 11px;
    padding: 2px 10px;
    border-radius: 10px;
    background: #67C23A;
    color: #fff;
    font-weight: 500;
    letter-spacing: 0.3px;

    &.dev {
      background: #e6a23c;
      color: #fff;
    }
  }

  .card-icon {
    width: 48px;
    height: 48px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 12px;
    flex-shrink: 0;

    svg {
      width: 24px;
      height: 24px;
      color: #fff;
    }

    &.user {
      background: #409EFF;
    }
    &.warehouse {
      background: #67C23A;
    }
    &.product {
      background: #E6A23C;
    }
    &.flow {
      background: #9254de;
    }
    &.log {
      background: #f56c6c;
    }
    &.report {
      background: #13c2c2;
    }
    &.task {
      background: #fa8c16;
    }
    &.config {
      background: #597ef7;
    }
  }

  .card-content {
    flex: 1;
    display: flex;
    flex-direction: column;

    h4 {
      font-size: 15px;
      font-weight: 600;
      color: #303133;
      margin: 0 0 4px 0;
    }

    p {
      font-size: 13px;
      color: #909399;
      margin: 0 0 12px 0;
      line-height: 1.4;
    }

    .card-meta {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-top: auto;

      .meta-tag {
        font-size: 11px;
        padding: 2px 10px;
        border-radius: 10px;
        background: #ecf5ff;
        color: #409EFF;
        font-weight: 500;

        &.dev {
          background: #fdf6ec;
          color: #e6a23c;
        }
      }

      .meta-arrow {
        display: flex;
        align-items: center;
        color: #c0c4cc;
        transition: color 0.3s;

        svg {
          width: 18px;
          height: 18px;
          transition: transform 0.3s ease;
        }
      }
    }
  }

  // 突出样式（人员列表）
  &.featured {
    border-color: #409EFF;
    border-width: 2px;
    background: linear-gradient(135deg, #f0f7ff 0%, #ffffff 60%);

    .card-badge {
      background: #409EFF;
      color: #fff;
      box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
    }

    .card-icon.user {
      background: #409EFF;
      box-shadow: 0 4px 12px rgba(64, 158, 255, 0.25);
    }

    .card-meta .meta-tag {
      background: #409EFF;
      color: #fff;
    }

    &:hover {
      border-color: #409EFF;
      box-shadow: 0 4px 24px rgba(64, 158, 255, 0.15);
      transform: translateY(-3px);
    }
  }
}

/* ====== 提示条 ====== */
.toast-bar {
  position: fixed;
  top: 80px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 9999;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 24px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.12);
  border-left: 4px solid #409EFF;
  min-width: 300px;
  max-width: 520px;

  &.info {
    border-left-color: #409EFF;
    .toast-icon {
      color: #409EFF;
    }
  }
  &.success {
    border-left-color: #67C23A;
    .toast-icon {
      color: #67C23A;
    }
  }

  .toast-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    svg {
      width: 20px;
      height: 20px;
    }
  }

  .toast-message {
    font-size: 14px;
    color: #303133;
    flex: 1;
    line-height: 1.5;
  }

  .toast-close {
    display: flex;
    align-items: center;
    justify-content: center;
    background: none;
    border: none;
    cursor: pointer;
    color: #909399;
    padding: 4px;
    border-radius: 4px;
    transition: all 0.2s;

    svg {
      width: 16px;
      height: 16px;
    }

    &:hover {
      background: #f0f2f5;
      color: #606266;
    }
  }
}

/* 提示条动画 */
.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}
.slide-down-enter-from {
  opacity: 0;
  transform: translateX(-50%) translateY(-20px);
}
.slide-down-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(-20px);
}

/* ====== 响应式 ====== */
@media (max-width: 1200px) {
  .view-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  .stat-row {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 992px) {
  .view-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;

    .header-right {
      width: 100%;
      justify-content: flex-start;
    }
  }
  .section-title .section-tip {
    display: none;
  }
}

@media (max-width: 768px) {
  .view-layout {
    padding: 12px;
  }
  .view-grid {
    grid-template-columns: 1fr;
  }
  .stat-row {
    grid-template-columns: 1fr 1fr;
    gap: 10px;
  }
  .stat-row .stat-card {
    padding: 14px 16px;

    .stat-info .stat-value {
      font-size: 18px;
    }
    .stat-icon {
      width: 38px;
      height: 38px;
      svg {
        width: 18px;
        height: 18px;
      }
    }
  }
  .page-header {
    padding: 14px 16px;

    .header-left h2 {
      font-size: 16px;
    }
    .header-right .header-time {
      font-size: 12px;
      padding: 2px 10px;
    }
    .header-right .refresh-btn {
      font-size: 12px;
      padding: 4px 12px;
    }
  }
  .view-card {
    padding: 16px 16px 12px;
  }
  .toast-bar {
    min-width: unset;
    width: 90%;
    padding: 10px 16px;
    top: 70px;
    .toast-message {
      font-size: 13px;
    }
  }
}

@media (max-width: 480px) {
  .stat-row {
    grid-template-columns: 1fr 1fr;
    gap: 8px;
  }
  .stat-row .stat-card {
    padding: 12px 14px;
    gap: 12px;
    .stat-info .stat-value {
      font-size: 16px;
    }
    .stat-icon {
      width: 34px;
      height: 34px;
      svg {
        width: 16px;
        height: 16px;
      }
    }
    .stat-info .stat-label {
      font-size: 11px;
    }
  }
  .page-header .header-left .breadcrumb {
    font-size: 12px;
  }
}
</style>