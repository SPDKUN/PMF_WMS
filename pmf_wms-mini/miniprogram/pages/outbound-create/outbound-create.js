const app = getApp()
const api = require('../../utils/api')

Page({
  data: {
    priorities: ['高', '中', '低'],
    userList: [],
    goodsList: [],
    goodsDisplayList: [],
    batchList: [],
    batchDisplayList: [],
    selectedGoods: null,
    selectedBatch: null,
    selectedBatchDisplay: '',
    // 库位相关
    locationList: [],
    locationDisplayList: [],
    selectedLocation: null,
    selectedLocationDisplay: '',
    quantity: '',
    operatorId: null,
    operatorName: '',
    priority: '',
    deadline: '',
    canSubmit: false,
    _inventoryData: [],
    goodsStockInfo: ''
  },

  onLoad() { this.loadData() },

  async loadData() {
    wx.showLoading({ title: '加载中...' })
    try {
      const [users, goods, inventory] = await Promise.all([
        api.warehouseStaffApi.list(),
        api.goodsApi.list(),
        api.inventoryApi.listWithDetails()
      ])
      var goodsList = goods || []
      var goodsDisplay = goodsList.map(function(g) {
        return g.goods_name + ' (' + (g.goods_code || '') + ')'
      })
      this.setData({
        userList: users || [],
        goodsList: goodsList,
        goodsDisplayList: goodsDisplay,
        _inventoryData: inventory || []
      })
    } catch (e) {
      wx.showToast({ title: '加载数据失败', icon: 'none' })
    }
    wx.hideLoading()
  },

  onGoodsChange(e) {
    var goods = this.data.goodsList[e.detail.value]
    if (!goods) return
    var inv = this.data._inventoryData || []
    // 只显示已入库（status=正常）的库存
    var matchedInv = inv.filter(function(i) {
      return i.goods_id === goods.goods_id && i.quantity > 0 && i.inventory_status === '正常'
    })
    // 按批次聚合
    var batchQtyMap = {}
    var batchIds = []
    for (var i = 0; i < matchedInv.length; i++) {
      var bid = matchedInv[i].batch_id
      if (!bid) continue
      if (!batchQtyMap[bid]) {
        batchQtyMap[bid] = 0
        batchIds.push(bid)
      }
      batchQtyMap[bid] += (matchedInv[i].quantity || 0)
    }
    var batchList = []
    var batchDisplay = []
    for (var j = 0; j < batchIds.length; j++) {
      var bid2 = batchIds[j]
      batchList.push({ batch_id: bid2, available_qty: batchQtyMap[bid2] })
      batchDisplay.push(bid2 + ' (可出库: ' + batchQtyMap[bid2] + ')')
    }
    var totalStock = 0
    for (var k = 0; k < matchedInv.length; k++) {
      totalStock += (matchedInv[k].quantity || 0)
    }
    this.setData({
      selectedGoods: goods,
      selectedBatch: null,
      selectedBatchDisplay: '',
      selectedLocation: null,
      selectedLocationDisplay: '',
      locationList: [],
      locationDisplayList: [],
      quantity: '',
      batchList: batchList,
      batchDisplayList: batchDisplay,
      goodsStockInfo: '总库存: ' + totalStock + ' ' + (goods.unit || '件')
    })
    this.checkSubmit()
  },

  onBatchChange(e) {
    var batch = this.data.batchList[e.detail.value]
    if (!batch) return
    // 查找该批次对应已入库库存的库位
    var inv = this.data._inventoryData || []
    var matchedInv = inv.filter(function(i) {
      return i.batch_id === batch.batch_id && i.goods_id === this.data.selectedGoods.goods_id && i.inventory_status === '正常' && i.quantity > 0 && i.location_id != null
    }.bind(this))
    // 按库位分组
    var locMap = {}
    for (var i = 0; i < matchedInv.length; i++) {
      var lid = matchedInv[i].location_id
      if (!lid) continue
      if (!locMap[lid]) {
        locMap[lid] = {
          location_id: lid,
          location_name: matchedInv[i].location_name || '库位#' + lid,
          zone_id: matchedInv[i].zone_id,
          warehouse_id: matchedInv[i].warehouse_id,
          quantity: 0
        }
      }
      locMap[lid].quantity += (matchedInv[i].quantity || 0)
    }
    var locIds = Object.keys(locMap)
    var locationList = []
    var locationDisplay = []
    for (var j = 0; j < locIds.length; j++) {
      var loc = locMap[locIds[j]]
      locationList.push(loc)
      locationDisplay.push(loc.location_name + ' (库存: ' + loc.quantity + ')')
    }
    this.setData({
      selectedBatch: batch,
      selectedBatchDisplay: batch.batch_id + ' (可出库: ' + batch.available_qty + ')',
      selectedLocation: null,
      selectedLocationDisplay: '',
      locationList: locationList,
      locationDisplayList: locationDisplay,
      quantity: ''
    })
    this.checkSubmit()
  },

  onLocationChange(e) {
    var loc = this.data.locationList[e.detail.value]
    if (loc) {
      this.setData({
        selectedLocation: loc,
        selectedLocationDisplay: loc.location_name + ' (库存: ' + loc.quantity + ')'
      })
    }
    this.checkSubmit()
  },

  onQtyInput(e) {
    this.setData({ quantity: e.detail.value })
    this.checkSubmit()
  },

  onOperatorChange(e) {
    var user = this.data.userList[e.detail.value]
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
    var d = this.data
    this.setData({
      canSubmit: d.selectedGoods && d.selectedBatch && d.selectedLocation && d.operatorId && d.priority
    })
  },

  async handleSubmit() {
    var d = this.data
    if (!d.canSubmit) {
      wx.showToast({ title: '请填写完整信息', icon: 'none' })
      return
    }
    var qty = parseInt(d.quantity) || 0
    if (qty <= 0) {
      wx.showToast({ title: '请输入有效出库数量', icon: 'none' })
      return
    }
    if (qty > (d.selectedLocation.quantity || 0)) {
      wx.showToast({ title: '该库位库存不足！可出库: ' + d.selectedLocation.quantity, icon: 'none' })
      return
    }
    if (qty > (d.selectedBatch.available_qty || 0)) {
      wx.showToast({ title: '批次库存不足！可出库: ' + d.selectedBatch.available_qty, icon: 'none' })
      return
    }
    wx.showLoading({ title: '创建中...', mask: true })
    try {
      var result = await api.outboundApi.create({
        items: [{
          goodsId: d.selectedGoods.goods_id,
          batchId: d.selectedBatch.batch_id,
          quantity: qty,
          warehouseId: d.selectedLocation.warehouse_id,
          zoneId: d.selectedLocation.zone_id,
          locationId: d.selectedLocation.location_id
        }],
        assigneeId: d.operatorId,
        priority: d.priority,
        deadline: d.deadline,
        operatorId: app.globalData.userInfo ? app.globalData.userInfo.user_id : null
      })
      // 自动发布：创建后将出库单状态改为"已审核"
      var orderNo = result && result.related_order_no
      if (orderNo) {
        await api.outboundApi.update({ outbound_no: orderNo, order_status: '已审核' }).catch(function(e) { console.error('Auto-publish error:', e) })
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
