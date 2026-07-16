const app = getApp()

Page({
  data: {
    username: '',
    password: '',
    canLogin: false
  },

  onUsernameInput(e) {
    const username = e.detail.value
    this.setData({ username })
    this.checkCanLogin()
  },

  onPasswordInput(e) {
    const password = e.detail.value
    this.setData({ password })
    this.checkCanLogin()
  },

  checkCanLogin() {
    this.setData({
      canLogin: this.data.username.length > 0 && this.data.password.length > 0
    })
  },

  handleLogin() {
    if (!this.data.canLogin) {
      wx.showToast({ title: '请输入用户名和密码', icon: 'none' })
      return
    }
    wx.showLoading({ title: '登录中...', mask: true })
    
    wx.request({
      url: app.globalData.baseUrl + '/auth/login',
      method: 'GET',
      data: {
        username: this.data.username,
        password: this.data.password
      },
      success: (res) => {
        wx.hideLoading()
        if (res.data && res.data.code === 200) {
          const userInfo = res.data.data
          const token = res.data.token
          
          app.globalData.userInfo = userInfo
          app.globalData.token = token
          
          wx.setStorageSync('userInfo', JSON.stringify(userInfo))
          wx.setStorageSync('token', token)
          app.globalData.justLoggedIn = true
          
          wx.showToast({ title: '登录成功', icon: 'success', duration: 1000 })
          setTimeout(() => {
            wx.reLaunch({ url: '/pages/home/home' })
          }, 1000)
        } else {
          wx.showToast({ title: res.data?.msg || '登录失败', icon: 'none' })
        }
      },
      fail: () => {
        wx.hideLoading()
        wx.showToast({ title: '网络连接失败', icon: 'none' })
      }
    })
  },


})