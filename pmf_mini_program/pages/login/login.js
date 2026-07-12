// 登录页逻辑
const api = require('../../utils/api')
const util = require('../../utils/util')

Page({
  data: {
    username: '',
    password: '',
    loading: false
  },

  onLoad() {
    // 检查是否已登录
    const token = wx.getStorageSync('token')
    if (token) {
      wx.switchTab({ url: '/pages/home/home' })
    }
  },

  onUsernameInput(e) {
    this.setData({ username: e.detail.value })
  },

  onPasswordInput(e) {
    this.setData({ password: e.detail.value })
  },

  async handleLogin() {
    const { username, password } = this.data

    if (!username.trim()) {
      util.showToast('请输入用户名')
      return
    }
    if (!password.trim()) {
      util.showToast('请输入密码')
      return
    }

    this.setData({ loading: true })

    try {
      const result = await api.login(username, password)
      const token = result.token
      const userInfo = result.data

      if (token) {
        // 保存登录信息
        wx.setStorageSync('token', token)
        wx.setStorageSync('userInfo', userInfo)

        const app = getApp()
        app.globalData.isLoggedIn = true
        app.globalData.token = token
        app.globalData.userInfo = userInfo

        util.showToast('登录成功', 'success')
        setTimeout(() => {
          wx.switchTab({ url: '/pages/home/home' })
        }, 1000)
      } else {
        util.showToast('登录失败：未获取到Token')
      }
    } catch (err) {
      console.error('登录失败', err)
      // 错误信息已在request中处理
    } finally {
      this.setData({ loading: false })
    }
  },

  goToRegister() {
    wx.navigateTo({ url: '/pages/register/register' })
  }
})
