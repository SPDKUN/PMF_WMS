const api = require('../../utils/api')
const util = require('../../utils/util')

Page({
  data: {
    list: [],
    avgTemp: null,
    avgHumidity: null
  },

  onLoad() { this.loadData() },

  async loadData() {
    wx.showLoading({ title: '加载中...' })
    try {
      const list = await api.envApi.list() || []
      const mapped = list.map(item => ({
        ...item,
        fmtTime: util.formatDate(item.recorded_time)
      }))
      
      const temps = mapped.filter(i => i.temperature != null).map(i => Number(i.temperature))
      const hums = mapped.filter(i => i.humidity != null).map(i => Number(i.humidity))
      
      this.setData({
        list: mapped,
        avgTemp: temps.length > 0 ? (temps.reduce((a, b) => a + b, 0) / temps.length).toFixed(1) : null,
        avgHumidity: hums.length > 0 ? (hums.reduce((a, b) => a + b, 0) / hums.length).toFixed(0) : null
      })
    } catch (e) { console.error(e) }
    wx.hideLoading()
  }
})
