// 任务页面 - 我的待办 + 任务分配
const api = require('../../utils/api')
const util = require('../../utils/util')

Page({
  data: {
    activeTab: 'mytodos',
    todoList: [],

    // 批次表单
    showBatchModal: false,
    batchForm: {
      batch_id: '',
      goods_id: '',
      production_date: '',
      expiry_date: '',
      initial_quantity: ''
    }
  },

  onShow() {
    if (getApp().checkLogin()) {
      this.loadTodos()
    }
  },

  async loadTodos() {
    util.showLoading('加载中...')
    try {
      const res = await api.getWorkTaskList().catch(() => ({ data: [] }))
      const tasks = (res.data || []).map(t => {
        if (t.deadline) t.deadline = util.formatDate(t.deadline)
        return t
      })
      this.setData({ todoList: tasks })
    } catch (err) {
      console.error('加载任务失败', err)
    } finally {
      util.hideLoading()
    }
  },

  switchTab(e) {
    this.setData({ activeTab: e.currentTarget.dataset.tab })
  },

  // 完成任务
  async completeTask(e) {
    const taskId = e.currentTarget.dataset.id
    const confirmed = await util.showConfirm('确认完成', '确定该任务已完成？')
    if (!confirmed) return

    try {
      const task = this.data.todoList.find(t => t.task_id === taskId)
      if (task) {
        await api.updateWorkTask({
          task_id: taskId,
          task_status: 'completed',
          completed_time: new Date().toISOString()
        })
        util.showToast('任务已完成', 'success')
        this.loadTodos()
      }
    } catch (err) {
      console.error('更新任务失败', err)
    }
  },

  // 打开新增批次弹窗
  openTaskModal(e) {
    const type = e.currentTarget.dataset.type
    if (type === 'inbound') {
      this.setData({
        showBatchModal: true,
        batchForm: {
          batch_id: '',
          goods_id: '',
          production_date: '',
          expiry_date: '',
          initial_quantity: ''
        }
      })
    } else {
      this.showComingSoon()
    }
  },

  closeBatchModal() {
    this.setData({ showBatchModal: false })
  },

  // 批次表单
  onBatchInput(e) {
    const field = e.currentTarget.dataset.field
    this.setData({ ['batchForm.' + field]: e.detail.value })
  },

  onProdDateChange(e) {
    this.setData({ 'batchForm.production_date': e.detail.value })
  },

  onExpDateChange(e) {
    this.setData({ 'batchForm.expiry_date': e.detail.value })
  },

  async submitBatch() {
    const { batchForm } = this.data

    if (!batchForm.batch_id) {
      util.showToast('请输入批次号')
      return
    }
    if (!batchForm.goods_id) {
      util.showToast('请输入货物ID')
      return
    }
    if (!batchForm.initial_quantity) {
      util.showToast('请输入初始数量')
      return
    }

    try {
      util.showLoading('创建中...')
      await api.createBatch({
        batch_id: batchForm.batch_id,
        goods_id: parseInt(batchForm.goods_id),
        production_date: batchForm.production_date || null,
        expiry_date: batchForm.expiry_date || null,
        initial_quantity: parseInt(batchForm.initial_quantity),
        remaining_quantity: parseInt(batchForm.initial_quantity),
        batch_status: 'active'
      })

      util.hideLoading()
      util.showToast('批次创建成功', 'success')
      this.closeBatchModal()
    } catch (err) {
      util.hideLoading()
      console.error('创建批次失败', err)
    }
  },

  showComingSoon() {
    util.showToast('功能开发中，敬请期待...')
  },

  noop() {}
})
