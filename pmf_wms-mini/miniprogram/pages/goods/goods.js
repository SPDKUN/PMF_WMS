const api = require('../../utils/api')
const util = require('../../utils/util')

Page({
  data: {
    currentTab: 0,
    tabs: [{ label: '全部' }, { label: '原料' }, { label: '成品' }],
    allList: [],
    filteredList: [],
    searchKeyword: '',
    summary: [
      { label: '全部', count: 0 },
      { label: '原料', count: 0 },
      { label: '成品', count: 0 }
    ],
    showModal: false,
    detailItem: null,
    _inventoryMap: {}
  },

  onLoad() {
    wx.setNavigationBarTitle({ title: '货物管理' })
    this.loadData()
  },

  onShow() {
    if (this.data.allList.length === 0) this.loadData()
  },

  async loadData() {
    wx.showLoading({ title: '加载中...' })
    try {
      const list = await api.goodsApi.list() || []
      const invList = await api.inventoryApi.listWithDetails().catch(() => [])
      var invMap = {}
      if (invList && invList.length > 0) {
        for (var i = 0; i < invList.length; i++) {
          var inv = invList[i]
          var gid = inv.goods_id
          if (!invMap[gid]) invMap[gid] = 0
          invMap[gid] += (inv.quantity || 0)
        }
      }
      var enriched = list.map(function(g) {
        var invQty = invMap[g.goods_id]
        var displayQty = (invQty !== undefined && invQty !== null) ? invQty : (g.quantity || 0)
        return {
          goods_id: g.goods_id,
          goods_code: g.goods_code || '-',
          category: g.category || '-',
          goods_name: g.goods_name || '-',
          storage_condition: g.storage_condition || '-',
          unit: g.unit || '件',
          quantity: displayQty,
          status: '正常'
        }
      })
      var raw = enriched.filter(function(g) { return g.category === '原料' }).length
      var finished = enriched.filter(function(g) { return g.category === '成品' }).length
      this.setData({
        allList: enriched,
        _inventoryMap: invMap,
        summary: [
          { label: '全部', count: enriched.length },
          { label: '原料', count: raw },
          { label: '成品', count: finished }
        ]
      })
      this.filterList()
    } catch (e) {
      console.error(e)
      wx.showToast({ title: '加载失败', icon: 'none' })
    }
    wx.hideLoading()
  },

  onSearchInput(e) {
    this.setData({ searchKeyword: e.detail.value })
    this.filterList()
  },

  onSearchClear() {
    this.setData({ searchKeyword: '' })
    this.filterList()
  },

  switchTab(e) {
    this.setData({ currentTab: e.currentTarget.dataset.index })
    this.filterList()
  },

  filterList() {
    const { allList, currentTab, searchKeyword } = this.data
    const tabLabels = ['全部', '原料', '成品']
    const filter = tabLabels[currentTab]
    var kw = (searchKeyword || '').trim().toLowerCase()
    var result = filter === '全部' ? allList : allList.filter(function(g) { return g.category === filter })
    if (kw) {
      result = result.filter(function(g) {
        var code = (g.goods_code || '').toLowerCase()
        var name = (g.goods_name || '').toLowerCase()
        return code.indexOf(kw) !== -1 || name.indexOf(kw) !== -1
      })
    }
    this.setData({ filteredList: result })
  },

  showDetail(e) {
    const id = e.currentTarget.dataset.id
    const item = this.data.allList.find(g => g.goods_id === id)
    if (item) {
      const catMap = { '原料': 'raw', '成品': 'finished' }
      item.categoryClass = catMap[item.category] || 'raw'
      item.storageClass = item.storage_condition === '冷冻' ? 'frozen' : (item.storage_condition === '恒温' ? 'constant' : 'room')
      this.setData({ detailItem: item, showModal: true })
    }
  },

  closeModal() { this.setData({ showModal: false }) },
  stopPropagation() {},

  goCreate() {
    wx.showToast({ title: '货物管理功能在Web端', icon: 'none' })
  },

  onPullDownRefresh() {
    this.loadData().then(function() {
      wx.stopPullDownRefresh()
    }).catch(function() {
      wx.stopPullDownRefresh()
    })
  }
})
