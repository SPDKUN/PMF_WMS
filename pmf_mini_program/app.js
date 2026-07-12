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
    // Server base URL
    // 当前：电脑和手机连同一个WiFi → 用WLAN适配器IPv4地址
    // 热点方案：Windows开启移动热点后 → 用热点适配器IP（通常是192.168.137.1）
    baseUrl: 'http://10.242.194.76:8088'
  }
})
