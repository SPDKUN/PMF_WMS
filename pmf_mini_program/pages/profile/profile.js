// 个人中心页面
const api = require('../../utils/api')
const util = require('../../utils/util')

Page({
  data: {
    userInfo: {},
    showEditModal: false,
    editForm: {
      username: '',
      real_name: '',
      phone: '',
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    }
  },

  onLoad() {
    this.loadUserInfo()
  },

  onShow() {
    this.loadUserInfo()
  },

  loadUserInfo() {
    const userInfo = wx.getStorageSync('userInfo') || {}
    this.setData({ userInfo })
  },

  // 打开编辑弹窗
  openEditModal() {
    const { userInfo } = this.data
    this.setData({
      showEditModal: true,
      editForm: {
        username: userInfo.username || '',
        real_name: userInfo.real_name || '',
        phone: userInfo.phone || '',
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
    })
  },

  closeEditModal() {
    this.setData({ showEditModal: false })
  },

  onEditInput(e) {
    const field = e.currentTarget.dataset.field
    this.setData({
      ['editForm.' + field]: e.detail.value
    })
  },

  async submitProfile() {
    const { editForm, userInfo } = this.data

    // 验证
    if (!editForm.username || !editForm.real_name) {
      util.showToast('用户名和真实姓名不能为空')
      return
    }

    // 如果填写了密码相关字段，需要验证
    const hasPasswordChange = editForm.oldPassword || editForm.newPassword || editForm.confirmPassword
    if (hasPasswordChange) {
      if (!editForm.oldPassword) {
        util.showToast('请输入旧密码')
        return
      }
      if (!editForm.newPassword || editForm.newPassword.length < 6) {
        util.showToast('新密码至少6位')
        return
      }
      if (editForm.newPassword !== editForm.confirmPassword) {
        util.showToast('两次新密码输入不一致')
        return
      }
    }

    try {
      util.showLoading('保存中...')
      const updateData = {
        user_id: userInfo.user_id,
        username: editForm.username,
        real_name: editForm.real_name,
        phone: editForm.phone
      }

      if (hasPasswordChange) {
        updateData.oldPassword = editForm.oldPassword
        updateData.newPassword = editForm.newPassword
      }

      // 使用 profile 接口
      await api.updateProfile(updateData)

      // 更新本地存储的用户信息
      const newUserInfo = {
        ...userInfo,
        username: editForm.username,
        real_name: editForm.real_name,
        phone: editForm.phone
      }
      wx.setStorageSync('userInfo', newUserInfo)
      getApp().globalData.userInfo = newUserInfo

      util.hideLoading()
      util.showToast('保存成功', 'success')
      this.closeEditModal()
      this.loadUserInfo()
    } catch (err) {
      util.hideLoading()
      console.error('保存失败', err)
    }
  },

  // 退出登录
  async handleLogout() {
    const confirmed = await util.showConfirm('退出登录', '确定要退出登录吗？')
    if (confirmed) {
      getApp().logout()
    }
  },

  noop() {}
})
