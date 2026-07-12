// app.js
App({
  onLaunch() {
    // Check login status
    const token = wx.getStorageSync('token')
    if (!token) {
      // No token, redirect to login
      this.globalData.isLoggedIn = false
    } else {
      this.globalData.isLoggedIn = true
      this.globalData.token = token
      const userInfo = wx.getStorageSync('userInfo')
      if (userInfo) {
        this.globalData.userInfo = userInfo
      }
    }
  },

  // Global login check method
  checkLogin() {
    const token = wx.getStorageSync('token')
    if (!token) {
      wx.redirectTo({ url: '/pages/login/login' })
      return false
    }
    return true
  },

  // Clear login state
  logout() {
    wx.removeStorageSync('token')
    wx.removeStorageSync('userInfo')
    this.globalData.isLoggedIn = false
    this.globalData.token = ''
    this.globalData.userInfo = null
    wx.redirectTo({ url: '/pages/login/login' })
  },

  globalData: {
    isLoggedIn: false,
    token: '',
    userInfo: null,
    // Server base URL - change this to your actual backend server address
    baseUrl: 'http://localhost:8088'
  }
})
