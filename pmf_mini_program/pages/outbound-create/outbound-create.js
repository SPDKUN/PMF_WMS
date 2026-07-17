const app = getApp()
const api = require('../../utils/api')

Page({
  data: {
    priorities: ['高', '中', '低'],
    userList: [],
    goodsList: [],
    goodsDisplayList: [],
    selectedGoods: null,
    selectedGoodsId: null,
    // 库存行（多行选择）
    inventoryRows: [],
    // 任务字段
    assigneeId: null,
    assigneeName: '',
    priority: '',
    deadline: '',
    canSubmit: false,
    creatorName: '',
    createTime: ''
  },

  onLoad() {
    var userInfo = app.globalData.userInfo
    if (userInfo) {
      this.setData({
        creatorName: userInfo.real_name || userInfo.username || '未知用户',
        createTime: this._fmtNow()
      })
    }
    this.loadData()
  },

  _fmtNow() {
    var now = new Date()
    var m = ('0' + (now.getMonth() + 1)).slice(-2)
    var d = ('0' + now.getDate()).slice(-2)
    var h = ('0' + now.getHours()).slice(-2)
    var min = ('0' + now.getMinutes()).slice(-2)
    return now.getFullYear() + '-' + m + '-' + d + ' ' + h + ':' + min
  },

  async loadData() {
    wx.showLoading({ title: '加载中...' })
    try {
      const [users, goods] = await Promise.all([
        api.warehouseStaffApi.list(),
        api.goodsApi.list()
      ])
      var goodsList = goods || []
      // 获取每个货物的库存总量
      var goodsDisplay = goodsList.map(function(g) {
        return g.goods_name + ' (剩余 ' + (g.quantity || 0) + ' ' + (g.unit || '件') + ')'
      })
      this.setData({
        userList: users || [],
        goodsList: goodsList,
        goodsDisplayList: goodsDisplay
      })
    } catch (e) {
      wx.showToast({ title: '加载数据失败', icon: 'none' })
    }
    wx.hideLoading()
  },

  async onGoodsChange(e) {
    var goods = this.data.goodsList[e.detail.value]
    if (!goods) return
    wx.showLoading({ title: '加载库存...' })
    try {
      var rows = await api.outboundApi.availableInventory(goods.goods_id)
      if (!rows) rows = []
      // 计算可用数量 = quantity - locked_quantity
      for (var i = 0; i < rows.length; i++) {
        rows[i].available = Math.max(0, (rows[i].quantity || 0) - (rows[i].locked_quantity || 0))
        rows[i].selectQty = 0
        rows[i].expiry_date = rows[i].expiry_date || null
        rows[i].location_name = rows[i].location_name || '-'
      }
      // FIFO: 按到期日期排序，日期靠前的排前面
      rows.sort(function(a, b) {
        if (!a.expiry_date) return 1
        if (!b.expiry_date) return -1
        return a.expiry_date < b.expiry_date ? -1 : a.expiry_date > b.expiry_date ? 1 : 0
      })
      this.setData({
        selectedGoods: goods,
        selectedGoodsId: goods.goods_id,
        inventoryRows: rows
      })
      this.calcTotal()
    } catch (e) {
      wx.showToast({ title: '加载库存失败', icon: 'none' })
    }
    wx.hideLoading()
  },

  onQtyInput(e) {
    var idx = e.currentTarget.dataset.index
    var val = parseInt(e.detail.value) || 0
    var rows = this.data.inventoryRows
    if (idx >= 0 && idx < rows.length) {
      rows[idx].selectQty = Math.max(0, Math.min(val, rows[idx].available))
      this.setData({ inventoryRows: rows })
      this.calcTotal()
    }
  },

  calcTotal() {
    var rows = this.data.inventoryRows
    var total = 0
    for (var i = 0; i < rows.length; i++) {
      total += (rows[i].selectQty || 0)
    }
    var canSubmit = total > 0 && this.data.assigneeId && this.data.priority && this.data.deadline
    this.setData({
      outboundTotalSelected: total,
      canSubmit: canSubmit
    })
  },

  onOperatorChange(e) {
    var user = this.data.userList[e.detail.value]
    if (user) {
      this.setData({ assigneeId: user.user_id, assigneeName: user.real_name })
      this.calcTotal()
    }
  },

  onPriorityChange(e) {
    this.setData({ priority: this.data.priorities[e.detail.value] })
    this.calcTotal()
  },

  onDateChange(e) {
    this.setData({ deadline: e.detail.value })
    this.calcTotal()
  },

  async handleSubmit() {
    var d = this.data
    if (d.outboundTotalSelected <= 0) {
      wx.showToast({ title: '请选择出库数量', icon: 'none' })
      return
    }
    if (!d.assigneeId) {
      wx.showToast({ title: '请选择负责人', icon: 'none' })
      return
    }
    if (!d.priority) {
      wx.showToast({ title: '请选择优先级', icon: 'none' })
      return
    }
    if (!d.deadline) {
      wx.showToast({ title: '请选择截至日期', icon: 'none' })
      return
    }

    wx.showModal({
      title: '确认创建',
      content: '确定要创建出库任务吗？将锁定所有涉及的库位。',
      success: async (res) => {
        if (!res.confirm) return
        wx.showLoading({ title: '创建中...', mask: true })
        try {
          var rows = d.inventoryRows.filter(function(r) { return r.selectQty > 0 })
          var items = rows.map(function(r) {
            return {
              goodsId: r.goods_id,
              batchId: r.batch_id,
              quantity: r.selectQty,
              warehouseId: r.warehouse_id,
              zoneId: r.zone_id,
              locationId: r.location_id
            }
          })
          var uid = app.globalData.userInfo ? app.globalData.userInfo.user_id : null
          await api.outboundApi.create({
            items: items,
            assigneeId: d.assigneeId,
            priority: d.priority,
            deadline: d.deadline,
            operatorId: uid
          })
          wx.hideLoading()
          wx.showToast({ title: '出库任务创建成功', icon: 'success' })
          setTimeout(function() { wx.navigateBack() }, 1500)
        } catch (e) {
          wx.hideLoading()
          wx.showToast({ title: e && e.msg ? e.msg : '创建失败', icon: 'none' })
        }
      }
    })
  }
})
