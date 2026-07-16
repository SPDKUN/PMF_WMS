const app = getApp()
const api = require('../../utils/api')

Page({
  data: {
    goodsList: [],
    goods_id: null,
    goodsName: '',
    production_date: '',
    expiry_date: '',
    initial_quantity: '',
    canSubmit: false
  },

  onLoad() {
    this.loadGoods()
  },

  async loadGoods() {
    try {
      var goods = await api.goodsApi.list() || []
      this.setData({ goodsList: goods })
    } catch (e) {
      wx.showToast({ title: '加载货物列表失败', icon: 'none' })
    }
  },

  onGoodsChange(e) {
    var g = this.data.goodsList[e.detail.value]
    this.setData({ goods_id: g.goods_id, goodsName: g.goods_name })
    this.checkSubmit()
  },

  onDateInput(e) {
    var field = e.currentTarget.dataset.field
    this.setData({ [field]: e.detail.value })
    this.checkSubmit()
  },

  onQtyInput(e) {
    this.setData({ initial_quantity: e.detail.value })
    this.checkSubmit()
  },

  checkSubmit() {
    var d = this.data
    this.setData({ canSubmit: d.goods_id && d.production_date && d.expiry_date && d.initial_quantity })
  },

  async handleSubmit() {
    if (!this.data.canSubmit) {
      wx.showToast({ title: '请填写完整信息', icon: 'none' })
      return
    }
    wx.showLoading({ title: '创建中...', mask: true })
    try {
      await api.batchApi.save({
        goods_id: this.data.goods_id,
        production_date: this.data.production_date,
        expiry_date: this.data.expiry_date,
        initial_quantity: parseInt(this.data.initial_quantity),
        batch_status: '待检'
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