// 注册页逻辑
const api = require('../../utils/api')
const util = require('../../utils/util')

Page({
  data: {
    form: {
      username: '',
      real_name: '',
      phone: '',
      password: ''
    },
    confirmPassword: '',
    loading: false
  },

  onInput(e) {
    const field = e.currentTarget.dataset.field
    const value = e.detail.value
    this.setData({
      ['form.' + field]: value
    })
  },

  onConfirmPasswordInput(e) {
    this.setData({ confirmPassword: e.detail.value })
  },

  async handleRegister() {
    const { form, confirmPassword } = this.data

    // 表单验证
    if (!form.username.trim()) {
      util.showToast('请输入用户名')
      return
    }
    if (!form.real_name.trim()) {
      util.showToast('请输入真实姓名')
      return
    }
    if (!form.phone.trim()) {
      util.showToast('请输入手机号')
      return
    }
    if (!/^1[3-9]\d{9}$/.test(form.phone.trim())) {
      util.showToast('请输入正确的手机号')
      return
    }
    if (!form.password) {
      util.showToast('请输入密码')
      return
    }
    if (form.password.length < 6) {
      util.showToast('密码至少6位')
      return
    }
    if (form.password !== confirmPassword) {
      util.showToast('两次密码输入不一致')
      return
    }

    this.setData({ loading: true })

    try {
      await api.createUser({
        username: form.username.trim(),
        real_name: form.real_name.trim(),
        phone: form.phone.trim(),
        password: form.password
      })

      util.showToast('注册成功，请登录', 'success')
      setTimeout(() => {
        wx.navigateBack()
      }, 1500)
    } catch (err) {
      console.error('注册失败', err)
    } finally {
      this.setData({ loading: false })
    }
  },

  goToLogin() {
    wx.navigateBack()
  }
})
