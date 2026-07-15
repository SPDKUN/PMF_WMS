// 登录页逻辑
const api = require('../../utils/api')
const util = require('../../utils/util')

Page({
  data: {
    username: '',
    password: '',
    loading: false,
    errorMsg: ''
  },

  onLoad() {
    // 检查是否已登录
    const token = wx.getStorageSync('token')
    if (token) {
      wx.switchTab({ url: '/pages/home/home' })
    }
  },

  onUsernameInput(e) {
    this.setData({ username: e.detail.value, errorMsg: '' })
  },

  onPasswordInput(e) {
    this.setData({ password: e.detail.value, errorMsg: '' })
  },

  async handleLogin() {
    const { username, password } = this.data

    if (!username.trim()) {
      this.setData({ errorMsg: '请输入用户名' })
      return
    }
    if (!password.trim()) {
      this.setData({ errorMsg: '请输入密码' })
      return
    }

    this.setData({ loading: true, errorMsg: '' })

    try {
      const result = await api.login(username, password)
      const token = result.token
      const userInfo = result.data

      // 检查账号是否被停用（与Vue前端一致）
      if (userInfo && userInfo.status === 0) {
        this.setData({ errorMsg: '该账号已被停用，请联系管理员', loading: false })
        return
      }

      if (token) {
        // 保存登录信息
        wx.setStorageSync('token', token)
        wx.setStorageSync('userInfo', userInfo)
        // 生成会话ID（AI聊天记录按登录会话隔离）
        wx.setStorageSync('ai_session_id', Date.now().toString())

        const app = getApp()
        app.globalData.isLoggedIn = true
        app.globalData.token = token
        app.globalData.userInfo = userInfo

        util.showToast('登录成功', 'success')
        setTimeout(() => {
          wx.switchTab({ url: '/pages/home/home' })
        }, 1000)
      } else {
        this.setData({ errorMsg: '登录失败：未获取到Token' })
      }
    } catch (err) {
      console.error('登录失败', err)
      // 显示后端返回的具体错误信息
      const msg = (err && err.msg) ? err.msg : '网络问题或者用户名密码错误'
      this.setData({ errorMsg: msg })
    } finally {
      this.setData({ loading: false })
    }
  },

  goToRegister() {
    wx.navigateTo({ url: '/pages/register/register' })
  }
})
