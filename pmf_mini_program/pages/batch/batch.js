const api = require('../../utils/api')
const util = require('../../utils/util')

Page({
  data: {
    list: [],
    filteredList: [],
    searchKeyword: '',
    showModal: false,
    detailItem: null,
    _goodsMap: {},
    // 关联数据
    batchInventory: [],
    batchQcRecords: [],
    _inventoryMap: {},
    isManager: false
  },

  onLoad() {
    wx.setNavigationBarTitle({ title: '批次管理' })
    // 权限判断：仅管理部门（主管/仓库管理员）可新增批次
    var app = getApp();
    var userInfo = app.globalData.userInfo || {};
    var position = userInfo.position || '';
    this.setData({ isManager: position === '主管' || position === '仓库管理员' });
    this.loadBaseData()
  },

  onShow() {
    if (this.data.list.length === 0) this.loadBaseData()
  },

  loadBaseData: function() {
    var self = this
    wx.showLoading({ title: '加载中...' })
    Promise.all([
      api.goodsApi.list().catch(function() { return [] }),
      api.batchApi.list().catch(function() { return [] }),
      api.inventoryApi.listWithDetails().catch(function() { return [] }),
      api.qualityDetailApi.list().catch(function() { return [] }),
      api.qualityApi.list().catch(function() { return [] })
    ]).then(function(results) {
      var goods = results[0] || []
      var batches = results[1] || []
      var invList = results[2] || []
      var qcDetails = results[3] || []
      var qcHeads = results[4] || []

      var goodsMap = {}
      for (var g = 0; g < goods.length; g++) {
        goodsMap[goods[g].goods_id] = goods[g].goods_name
      }

      // 构建批次库存映射
      var invMap = {}
      for (var i = 0; i < invList.length; i++) {
        var bid = invList[i].batch_id
        if (!invMap[bid]) invMap[bid] = []
        invMap[bid].push(invList[i])
      }

      // 构建批次质检映射
      var qcHeadMap = {}
      for (var q = 0; q < qcHeads.length; q++) {
        qcHeadMap[qcHeads[q].quality_check_no] = qcHeads[q]
      }
      var qcMap = {}
      for (var qd = 0; qd < qcDetails.length; qd++) {
        var bid2 = qcDetails[qd].batch_id
        if (!qcMap[bid2]) qcMap[bid2] = []
        var head = qcHeadMap[qcDetails[qd].quality_check_no] || {}
        qcMap[bid2].push({
          quality_check_no: qcDetails[qd].quality_check_no,
          check_type: head.check_type || '-',
          inspection_result: qcDetails[qd].inspection_result || '-'
        })
      }

      var mapped = batches.map(function(item) {
        var initial = item.initial_quantity || 0
        var remaining = item.remaining_quantity || 0
        var consumed = initial - remaining
        var progress = initial > 0 ? Math.round((consumed / initial) * 100) : 0
        var goodsName = goodsMap[item.goods_id] || ('货物#' + item.goods_id)
        return {
          batch_id: item.batch_id,
          goods_id: item.goods_id,
          goods_name: goodsName,
          production_date: item.production_date,
          expiry_date: item.expiry_date,
          initial_quantity: initial,
          remaining_quantity: remaining,
          batch_status: item.batch_status || '-',
          tagCls: util.getStatusTag(item.batch_status),
          fmtProd: item.production_date ? String(item.production_date).split('T')[0] : '-',
          fmtExp: item.expiry_date ? String(item.expiry_date).split('T')[0] : '-',
          progress: progress
        }
      })

      self.setData({
        list: mapped,
        _goodsMap: goodsMap,
        _inventoryMap: invMap,
        _qcMap: qcMap
      })
      self.filterList()
      wx.hideLoading()
    }).catch(function(e) {
      console.error(e)
      wx.hideLoading()
    })
  },

  onSearchInput(e) {
    this.setData({ searchKeyword: e.detail.value })
    this.filterList()
  },

  onSearchClear() {
    this.setData({ searchKeyword: '' })
    this.filterList()
  },

  filterList() {
    var kw = (this.data.searchKeyword || '').trim().toLowerCase()
    if (!kw) {
      this.setData({ filteredList: this.data.list })
      return
    }
    var result = this.data.list.filter(function(item) {
      var id = (item.batch_id || '').toLowerCase()
      var name = (item.goods_name || '').toLowerCase()
      return id.indexOf(kw) !== -1 || name.indexOf(kw) !== -1
    })
    this.setData({ filteredList: result })
  },

  showDetail(e) {
    var id = e.currentTarget.dataset.id
    var item = this.data.list.find(function(b) { return b.batch_id === id })
    if (!item) return

    // 获取关联的库存和质检记录
    var invRecords = this.data._inventoryMap[id] || []
    var qcRecords = this.data._qcMap[id] || []

    var invDisplay = invRecords.map(function(inv) {
      return {
        location_name: inv.location_name || '-',
        quantity: inv.quantity || 0,
        inventory_status: inv.inventory_status || '-'
      }
    })
    var qcDisplay = qcRecords.map(function(qc) {
      return {
        quality_check_no: qc.quality_check_no,
        check_type: qc.check_type,
        inspection_result: qc.inspection_result
      }
    })

    this.setData({
      detailItem: item,
      batchInventory: invDisplay,
      batchQcRecords: qcDisplay,
      showModal: true
    })
  },

  closeModal() {
    this.setData({ showModal: false, detailItem: null, batchInventory: [], batchQcRecords: [] })
  },

  stopPropagation() {},

  goCreate() {
    wx.navigateTo({ url: '/pages/batch-create/batch-create' })
  },

  onPullDownRefresh() {
    this.loadBaseData().then(function() {
      wx.stopPullDownRefresh()
    }).catch(function() {
      wx.stopPullDownRefresh()
    })
  }
})
