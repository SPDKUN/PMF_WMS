const app = getApp()
const api = require('../../utils/api')
const util = require('../../utils/util')

Page({
  data: {
    inboundTypes: ['采购入库', '退货入库', '生产入库'],
    priorities: ['高', '中', '低'],
    userList: [],
    batchList: [],
    inboundType: '',
    batchId: '',
    batchDisplay: '',
    operatorId: null,
    operatorName: '',
    priority: '',
    deadline: '',
    remark: '',
    canSubmit: false,
    _goodsMap: {},
    // 来自本地数据库的创建人和创建时间
    creatorName: '',
    creatorId: null,
    createTime: ''
  },

  onLoad() {
    // 从本地数据库读取当前登录用户信息作为创建人
    var userInfo = app.globalData.userInfo
    if (userInfo) {
      this.setData({
        creatorName: userInfo.real_name || userInfo.username || '未知用户',
        creatorId: userInfo.user_id || null,
        createTime: util.formatDate(new Date().toISOString())
      })
    }
    this.loadData()
  },

  async loadData() {
    wx.showLoading({ title: '加载中...' })
    try {
      const [users, batches, goods] = await Promise.all([
        api.warehouseStaffApi.list(),
        api.inboundApi.availableBatches(),
        api.goodsApi.list()
      ])
      var goodsMap = {}
      if (goods) {
        for (var g = 0; g < goods.length; g++) {
          goodsMap[goods[g].goods_id] = goods[g].goods_name
        }
      }
      // 直接使用后端返回的批次，不再额外过滤
      var enrichedBatches = (batches || []).map(function(b) {
        var name = goodsMap[b.goods_id] || ''
        return {
          batch_id: b.batch_id,
          goods_name: name,
          goods_id: b.goods_id,
          batchDisplay: b.batch_id + (name ? ' - ' + name : '')
        }
      })
      // 如果本地已有创建人信息，默认选中当前用户为执行人
      var defaultOperatorId = this.data.operatorId
      var defaultOperatorName = this.data.operatorName
      if (!defaultOperatorId && this.data.creatorId && users && users.length > 0) {
        for (var u = 0; u < users.length; u++) {
          if (users[u].user_id === this.data.creatorId) {
            defaultOperatorId = users[u].user_id
            defaultOperatorName = users[u].real_name
            break
          }
        }
      }
      this.setData({
        userList: users || [],
        batchList: enrichedBatches || [],
        _goodsMap: goodsMap,
        operatorId: defaultOperatorId,
        operatorName: defaultOperatorName
      })
    } catch (e) {
      wx.showToast({ title: '加载数据失败', icon: 'none' })
    }
    wx.hideLoading()
  },

  onInput(e) {
    const field = e.currentTarget.dataset.field
    this.setData({ [field]: e.detail.value })
    this.checkSubmit()
  },

  onTypeChange(e) {
    this.setData({ inboundType: this.data.inboundTypes[e.detail.value] })
    this.checkSubmit()
  },

  onBatchChange(e) {
    const idx = e.detail.value
    const batch = this.data.batchList[idx]
    if (batch) {
      this.setData({ batchId: batch.batch_id, batchDisplay: batch.batchDisplay })
    }
    this.checkSubmit()
  },

  onOperatorChange(e) {
    const idx = e.detail.value
    const user = this.data.userList[idx]
    this.setData({ operatorId: user.user_id, operatorName: user.real_name })
    this.checkSubmit()
  },

  onPriorityChange(e) {
    this.setData({ priority: this.data.priorities[e.detail.value] })
    this.checkSubmit()
  },

  onDateChange(e) {
    this.setData({ deadline: e.detail.value })
    this.checkSubmit()
  },

  checkSubmit() {
    const d = this.data
    this.setData({ canSubmit: d.inboundType && d.batchId && d.operatorId && d.priority })
  },

  async handleSubmit() {
    if (!this.data.canSubmit) {
      wx.showToast({ title: '请填写完整信息', icon: 'none' })
      return
    }
    wx.showLoading({ title: '创建中...', mask: true })
    try {
      var result = await api.inboundApi.create({
        inboundType: this.data.inboundType,
        batchId: this.data.batchId,
        operatorId: this.data.operatorId,
        // 将本地数据库中的创建人ID传给后端，与入库单关联
        creatorId: this.data.creatorId,
        priority: this.data.priority,
        deadline: this.data.deadline,
        remark: this.data.remark
      })
      // 自动发布：创建后将入库单状态改为"已审核"，仓库员工即可看到任务
      var orderNo = result && result.related_order_no
      if (orderNo) {
        await api.inboundApi.update({ inbound_no: orderNo, order_status: '已审核' }).catch(function(e) { console.error('Auto-publish error:', e) })
      }
      wx.hideLoading()
      wx.showToast({ title: '创建成功', icon: 'success' })
      setTimeout(function() { wx.navigateBack() }, 1500)
    } catch (e) {
      wx.hideLoading()
      wx.showToast({ title: e && e.msg ? e.msg : '操作失败', icon: 'none' })
    }
  }
})
