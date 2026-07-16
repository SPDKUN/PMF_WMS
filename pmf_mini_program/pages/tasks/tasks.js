const api = require('../../utils/api')
const util = require('../../utils/util')

Page({
  data: {
    filterType: 'all',
    taskList: [],
    _allTasks: []
  },

  onLoad() { this.loadData() },

  // 切换 tab 时刷新数据
  onShow() { this.loadData() },

  async loadData() {
    wx.showLoading({ title: '加载中...' })
    try {
      var list = (await api.taskApi.list()) || []
      this.data._allTasks = list.map(function(item) {
        return {
          ...item,
          tagCls: item.completed_time ? 'tag-success' : 'tag-warning',
          taskIcon: item.task_type === '入库' ? '📥' : item.task_type === '出库' ? '📤' : '📋',
          fmtCreated: util.formatDate(item.created_time)
        };
      })
      this.filterList()
    } catch (e) { console.error(e) }
    wx.hideLoading()
  },

  setFilter(e) {
    this.setData({ filterType: e.currentTarget.dataset.type })
    this.filterList()
  },

  onTaskTap(e) {
    var task = this.data.taskList[e.currentTarget.dataset.index];
    if (!task) return;
    var page = '';
    var orderNo = task.related_order_no || '';
    if (task.task_type === '入库') page = '/pages/inbound/inbound?order_no=' + orderNo;
    else if (task.task_type === '出库') page = '/pages/outbound/outbound?order_no=' + orderNo;
    else if (task.task_type === '质检') page = '/pages/quality/quality?order_no=' + orderNo;
    if (page) wx.navigateTo({ url: page });
  },

  filterList() {
    const { _allTasks, filterType } = this.data
    const app = getApp()

    if (filterType === 'mine') {
      // "我的任务": 只显示未完成的任务
      var uid = app.globalData.userInfo ? app.globalData.userInfo.user_id : null
      var list = _allTasks.filter(function(t) {
        return t.assignee_id === uid && !t.completed_time
      })
      this.setData({ taskList: list })
    } else {
      // "全部": 未完成的排最上方，已完成排下方
      var pending = []
      var done = []
      for (var i = 0; i < _allTasks.length; i++) {
        var t = _allTasks[i]
        if (t.completed_time) {
          done.push(t)
        } else {
          pending.push(t)
        }
      }
      this.setData({ taskList: pending.concat(done) })
    }
  }
})
