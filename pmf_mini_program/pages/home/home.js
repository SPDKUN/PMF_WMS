// 首页 - 智能仓储驾驶舱
const api = require('../../utils/api')
const util = require('../../utils/util')

Page({
  data: {
    userInfo: {},
    greeting: '',
    currentDate: '',
    aiQuestion: '',
    stats: {
      warehouseCount: 0,
      goodsCount: 0,
      userCount: 0,
      todayTasks: 0
    },
    todoList: []
  },

  onLoad() {
    this.loadUserInfo()
    this.updateGreeting()
  },

  onShow() {
    // 每次显示时刷新数据
    if (getApp().checkLogin()) {
      this.loadStats()
      this.loadTodos()
    }
  },

  loadUserInfo() {
    const userInfo = wx.getStorageSync('userInfo') || {}
    this.setData({ userInfo })
  },

  updateGreeting() {
    const hour = new Date().getHours()
    let greeting = '早上好'
    if (hour >= 12 && hour < 18) {
      greeting = '下午好'
    } else if (hour >= 18) {
      greeting = '晚上好'
    }
    const currentDate = util.formatDate(new Date())
    this.setData({ greeting, currentDate })
  },

  async loadStats() {
    try {
      // 并行加载统计数据
      const [warehouses, goods, users, tasks] = await Promise.all([
        api.getWarehouseList().catch(() => ({ data: [] })),
        api.getGoodsList().catch(() => ({ data: [] })),
        api.getUserList().catch(() => ({ data: [] })),
        api.getWorkTaskList().catch(() => ({ data: [] }))
      ])

      const now = new Date()
      const todayTasks = (tasks.data || []).filter(task => {
        return task.task_status !== 'completed' && task.deadline && new Date(task.deadline).toDateString() === now.toDateString()
      }).length

      this.setData({
        stats: {
          warehouseCount: (warehouses.data || []).length,
          goodsCount: (goods.data || []).length,
          userCount: (users.data || []).length,
          todayTasks
        }
      })
    } catch (err) {
      console.error('加载统计数据失败', err)
    }
  },

  async loadTodos() {
    try {
      const result = await api.getWorkTaskList()
      const tasks = (result.data || []).filter(t => t.task_status !== 'completed').slice(0, 5)
      // Format deadline dates
      tasks.forEach(t => {
        if (t.deadline) {
          t.deadline = util.formatDate(t.deadline)
        }
      })
      this.setData({ todoList: tasks })
    } catch (err) {
      console.error('加载待办任务失败', err)
    }
  },

  onAiInput(e) {
    this.setData({ aiQuestion: e.detail.value })
  },

  async sendAiQuestion() {
    const question = this.data.aiQuestion.trim()
    if (!question) {
      util.showToast('请输入问题')
      return
    }

    util.showToast('AI功能开发中，敬请期待...')
    this.setData({ aiQuestion: '' })

    // TODO: AI接口对接
    // try {
    //   const result = await request.post('/ai/ask', { question })
    //   处理AI回复
    // } catch (err) {
    //   console.error('AI请求失败', err)
    // }
  },

  navigateTo(e) {
    const url = e.currentTarget.dataset.url
    if (url) {
      wx.switchTab({ url })
    }
  }
})
