const app = getApp()
const api = require('../../utils/api')

Page({
  data: {
    checkTypes: ['来料检', '日常抽检'],
    priorities: ['高', '中', '低'],
    inspectorList: [],
    batchList: [],
    batchDisplayList: [],
    checkType: '',
    batchId: '',
    batchDisplay: '',
    inspectorId: null,
    inspectorName: '',
    priority: '',
    deadline: '',
    remark: '',
    canSubmit: false,
    _goodsMap: {},
    _allBatches: [],
    _allInventory: []
  },

  onLoad() {
    this.loadData()
  },

  async loadData() {
    wx.showLoading({ title: '加载中...' })
    try {
      const [inspectors, goods, batches, inventory] = await Promise.all([
        api.userApi.list().then(function(users) {
          return (users || []).filter(function(u) { return u.position === '质检员' })
        }),
        api.goodsApi.list(),
        api.batchApi.list(),
        api.inventoryApi.listWithDetails()
      ])
      var goodsMap = {}
      if (goods) {
        for (var g = 0; g < goods.length; g++) {
          goodsMap[goods[g].goods_id] = goods[g].goods_name
        }
      }
      this.setData({
        inspectorList: inspectors || [],
        _goodsMap: goodsMap,
        _allBatches: batches || [],
        _allInventory: inventory || []
      })
    } catch (e) {
      wx.showToast({ title: '加载数据失败', icon: 'none' })
    }
    wx.hideLoading()
  },

  onTypeChange(e) {
    var type = this.data.checkTypes[e.detail.value]
    this.setData({ checkType: type, batchId: '', batchDisplay: '' })
    this.loadBatchesByType(type)
    this.checkSubmit()
  },

  loadBatchesByType: function(type) {
    var self = this
    wx.showLoading({ title: '加载批次...' })
    // 从库存记录中找出对应状态的批次
    var inv = this.data._allInventory || []
    var batchStatusFilter = ''
    if (type === '来料检') {
      // 未入库的批次：库存状态为"待入库"
      batchStatusFilter = '待入库'
    } else if (type === '日常抽检') {
      // 已入库的批次：库存状态为"正常"
      batchStatusFilter = '正常'
    }
    var matchedBatchIds = {}
    for (var i = 0; i < inv.length; i++) {
      if (inv[i].inventory_status === batchStatusFilter && inv[i].batch_id) {
        matchedBatchIds[inv[i].batch_id] = true
      }
    }
    // 用批次ID过滤全部批次列表
    var allBatches = this.data._allBatches || []
    var goodsMap = this.data._goodsMap || {}
    var displayList = []
    for (var j = 0; j < allBatches.length; j++) {
      var b = allBatches[j]
      if (matchedBatchIds[b.batch_id]) {
        var name = goodsMap[b.goods_id] || ''
        displayList.push({
          batch_id: b.batch_id,
          goods_name: name,
          goods_id: b.goods_id,
          display: b.batch_id + (name ? ' - ' + name : '')
        })
      }
    }
    this.setData({ batchList: displayList })
    wx.hideLoading()
  },

  onBatchChange(e) {
    var batch = this.data.batchList[e.detail.value]
    if (batch) {
      this.setData({ batchId: batch.batch_id, batchDisplay: batch.display })
    }
    this.checkSubmit()
  },

  onInspectorChange(e) {
    var user = this.data.inspectorList[e.detail.value]
    this.setData({ inspectorId: user.user_id, inspectorName: user.real_name })
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

  onInput(e) {
    var field = e.currentTarget.dataset.field
    this.setData({ [field]: e.detail.value })
    this.checkSubmit()
  },

  checkSubmit() {
    var d = this.data
    this.setData({ canSubmit: d.checkType && d.batchId && d.inspectorId && d.priority })
  },

  async handleSubmit() {
    if (!this.data.canSubmit) {
      wx.showToast({ title: '请填写完整信息', icon: 'none' })
      return
    }
    wx.showLoading({ title: '创建中...', mask: true })
    try {
      await api.qualityApi.create({
        checkType: this.data.checkType,
        batchId: this.data.batchId,
        inspectorId: this.data.inspectorId,
        priority: this.data.priority,
        deadline: this.data.deadline,
        remark: this.data.remark,
        operatorId: app.globalData.userInfo ? app.globalData.userInfo.user_id : null
      })
      wx.hideLoading()
      wx.showToast({ title: '创建成功', icon: 'success' })
      setTimeout(function() { wx.navigateBack() }, 1500)
    } catch (e) {
      wx.hideLoading()
      wx.showToast({ title: e && e.msg ? e.msg : '操作失败', icon: 'none' })
    }
  }
})
