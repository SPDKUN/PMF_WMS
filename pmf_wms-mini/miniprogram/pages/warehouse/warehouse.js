const api = require('../../utils/api')
const util = require('../../utils/util')

Page({
  data: {
    list: []
  },

  onLoad() {
    this.loadData()
  },

  async loadData() {
    wx.showLoading({ title: '加载中...' })
    try {
      const list = await api.warehouseApi.list() || []
      this.setData({
        list: list.map(item => ({
          ...item,
          tagCls: util.getStatusTag(item.status),
          typeBg: item.warehouse_type === '冷冻库' ? '#DBEAFE' : 
                  item.warehouse_type === '恒温库' ? '#FCE7F3' : '#F0FDF4',
          typeIcon: item.warehouse_type === '冷冻库' ? '❄️' : 
                    item.warehouse_type === '恒温库' ? '🌡️' : '🏠'
        }))
      })
    } catch (e) { console.error(e) }
    wx.hideLoading()
  },

  goDetail(e) {
    wx.showToast({ title: '库房详情', icon: 'none' })
  }
})
