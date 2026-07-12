// 查询页面 - 7个只读查询Tab
const api = require('../../utils/api')
const util = require('../../utils/util')

Page({
  data: {
    activeTab: 'personnel',
    searchKeyword: '',
    searchPlaceholder: '搜索人员...',

    // 各Tab数据
    personnelList: [],
    warehouseList: [],
    goodsList: [],
    inventoryList: [],
    batchList: [],
    temperatureList: [],
    operationLogList: [],
    displayList: [],

    // 筛选器
    inventoryFilter: { goodsName: '', locationId: '' },
    logFilter: { date: '', operationType: '' }
  },

  onShow() {
    if (getApp().checkLogin()) {
      this.loadCurrentTabData()
    }
  },

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab
    const placeholders = {
      personnel: '搜索人员...',
      warehouse: '搜索仓库...',
      goods: '搜索货物...',
      inventory: '搜索库存...',
      batch: '搜索批次...',
      temperature: '',
      operationLog: '搜索日志...'
    }
    this.setData({
      activeTab: tab,
      searchKeyword: '',
      searchPlaceholder: placeholders[tab] || '搜索...'
    })
    this.loadCurrentTabData()
  },

  async loadCurrentTabData() {
    const { activeTab } = this.data
    util.showLoading('加载中...')

    try {
      switch (activeTab) {
        case 'personnel': {
          const res = await api.getUserList().catch(() => ({ data: [] }))
          this.setData({ personnelList: res.data || [] })
          break
        }
        case 'warehouse': {
          const res = await api.getWarehouseList().catch(() => ({ data: [] }))
          this.setData({ warehouseList: res.data || [] })
          break
        }
        case 'goods': {
          const res = await api.getGoodsList().catch(() => ({ data: [] }))
          this.setData({ goodsList: res.data || [] })
          break
        }
        case 'inventory':
          await this.loadInventory()
          break
        case 'batch': {
          const res = await api.getBatchList().catch(() => ({ data: [] }))
          this.setData({ batchList: res.data || [] })
          break
        }
        case 'temperature': {
          const res = await api.getTemperatureList().catch(() => ({ data: [] }))
          this.setData({ temperatureList: res.data || [] })
          break
        }
        case 'operationLog':
          await this.loadOperationLogs()
          break
      }
      this.applyFilter()
    } catch (err) {
      console.error('加载数据失败', err)
    } finally {
      util.hideLoading()
    }
  },

  onSearchInput(e) {
    this.setData({ searchKeyword: e.detail.value })
  },

  doSearch() {
    this.applyFilter()
  },

  applyFilter() {
    const { activeTab, searchKeyword } = this.data
    let list = []

    const getList = () => {
      switch (activeTab) {
        case 'personnel': return this.data.personnelList
        case 'warehouse': return this.data.warehouseList
        case 'goods': return this.data.goodsList
        case 'inventory': return this.data.inventoryList
        case 'batch': return this.data.batchList
        case 'temperature': return this.data.temperatureList
        case 'operationLog': return this.data.operationLogList
        default: return []
      }
    }

    list = getList()

    if (searchKeyword && activeTab !== 'temperature') {
      const kw = searchKeyword.toLowerCase()
      list = list.filter(item => {
        // 通用搜索：在所有字段中查找
        const str = JSON.stringify(item).toLowerCase()
        return str.includes(kw)
      })
    }

    this.setData({ displayList: list })
  },

  // 库存筛选
  onInventoryFilter(e) {
    const field = e.currentTarget.dataset.field
    this.setData({
      ['inventoryFilter.' + field]: e.detail.value
    })
  },

  async loadInventory() {
    const { goodsName, locationId } = this.data.inventoryFilter
    const res = await api.getInventoryList(goodsName, locationId).catch(() => ({ data: [] }))
    this.setData({ inventoryList: res.data || [] })
    this.applyFilter()
  },

  // 操作日志筛选
  onLogFilter(e) {
    const field = e.currentTarget.dataset.field
    this.setData({
      ['logFilter.' + field]: e.detail.value
    })
  },

  async loadOperationLogs() {
    const { date, operationType } = this.data.logFilter
    const res = await api.getOperationLogList(date, operationType).catch(() => ({ data: [] }))
    this.setData({ operationLogList: res.data || [] })
    this.applyFilter()
  }
})
