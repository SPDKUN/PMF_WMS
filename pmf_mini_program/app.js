// app.js
App({
  globalData: {
    // 后端 API 地址
    // 开发环境用 localhost, 真机调试用局域网 IP
    baseUrl: 'http://127.0.0.1:8088',
    userInfo: null,
    token: ''
  },

  onLaunch() {
    // 检查登录状态
    const token = wx.getStorageSync('token')
    const userInfo = wx.getStorageSync('userInfo')
    if (token && userInfo) {
      this.globalData.token = token
      this.globalData.userInfo = JSON.parse(userInfo)
    }
  }
})
