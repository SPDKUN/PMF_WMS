const app = getApp()

Page({
  data: {
    userInfo: {},
    avatarText: 'U',
    showModal: false
  },

  onShow() {
    const info = app.globalData.userInfo || {}
    this.setData({
      userInfo: info,
      avatarText: (info.real_name || 'U').charAt(0)
    });
    // 从首页"个人信息"入口跳转过来时，自动打开弹窗
    if (app.globalData.showProfileInfo) {
      app.globalData.showProfileInfo = false;
      this.setData({ showModal: true });
    }
  },

  showUserInfo() {
    this.setData({ showModal: true })
  },

  closeModal() {
    this.setData({ showModal: false })
  },

  stopPropagation() {},

  goMyTasks() {
    wx.switchTab({ url: '/pages/tasks/tasks' })
  },

  goAbout() {
    wx.showModal({
      title: '关于系统',
      content: '预制菜WMS仓储管理系统 v2.0\n基于PMF-WMS后端构建',
      showCancel: false
    })
  },

  handleLogout() {
    wx.showModal({
      title: '确认退出',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          wx.clearStorageSync()
          app.globalData.token = ''
          app.globalData.userInfo = null
          wx.redirectTo({ url: '/pages/login/login' })
        }
      }
    })
  }
})