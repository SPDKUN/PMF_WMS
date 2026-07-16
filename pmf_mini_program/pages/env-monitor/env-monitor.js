var api = require('../../utils/api');
var util = require('../../utils/util');

Page({
  data: {
    list: [],
    avgTemp: null,
    avgHumidity: null,
    // 上传表单
    warehouseList: [],
    warehouseId: null,
    warehouseIdx: -1,
    temperature: '',
    humidity: ''
  },

  onLoad() {
    wx.setNavigationBarTitle({ title: '温湿度管理' });
    this.loadData();
    this.loadWarehouses();
  },

  // 加载仓库列表
  async loadWarehouses() {
    try {
      var list = await api.warehouseApi.list() || [];
      this.setData({ warehouseList: list });
    } catch (e) { /* ignore */ }
  },

  // 加载监测记录
  async loadData() {
    wx.showLoading({ title: '加载中...' });
    try {
      var list = await api.tempHumidityApi.list() || [];
      var mapped = list.map(function(item) {
        return {
          ...item,
          fmtTime: util.formatDate(item.recorded_time)
        };
      });

      var temps = mapped.filter(function(i) { return i.temperature != null; }).map(function(i) { return Number(i.temperature); });
      var hums = mapped.filter(function(i) { return i.humidity != null; }).map(function(i) { return Number(i.humidity); });

      this.setData({
        list: mapped,
        avgTemp: temps.length > 0 ? (temps.reduce(function(a, b) { return a + b; }, 0) / temps.length).toFixed(1) : null,
        avgHumidity: hums.length > 0 ? (hums.reduce(function(a, b) { return a + b; }, 0) / hums.length).toFixed(0) : null
      });
    } catch (e) { console.error(e); }
    wx.hideLoading();
  },

  // 表单事件
  onWarehouseChange: function(e) {
    var idx = e.detail.value;
    var wh = this.data.warehouseList[idx];
    this.setData({ warehouseIdx: idx, warehouseId: wh ? wh.warehouse_id : null });
  },

  onTempInput: function(e) {
    this.setData({ temperature: e.detail.value });
  },

  onHumidityInput: function(e) {
    this.setData({ humidity: e.detail.value });
  },

  // 提交上传
  handleUpload: function() {
    var self = this;
    if (!this.data.warehouseId) {
      wx.showToast({ title: '请选择库房', icon: 'none' });
      return;
    }
    if (this.data.temperature === '' || this.data.temperature == null) {
      wx.showToast({ title: '请输入温度', icon: 'none' });
      return;
    }
    if (this.data.humidity === '' || this.data.humidity == null) {
      wx.showToast({ title: '请输入湿度', icon: 'none' });
      return;
    }

    wx.showLoading({ title: '上传中...', mask: true });
    api.tempHumidityApi.upload({
      warehouseId: this.data.warehouseId,
      temperature: parseFloat(self.data.temperature),
      humidity: parseFloat(self.data.humidity)
    }).then(function() {
      wx.hideLoading();
      wx.showToast({ title: '上传成功', icon: 'success' });
      // 重置表单
      self.setData({ temperature: '', humidity: '', warehouseIdx: -1, warehouseId: null });
      // 刷新列表
      self.loadData();
    }).catch(function(e) {
      wx.hideLoading();
      wx.showToast({ title: e && e.msg ? e.msg : '上传失败', icon: 'none' });
    });
  },

  onPullDownRefresh: function() {
    var self = this;
    this.loadData().then(function() {
      wx.stopPullDownRefresh();
    }).catch(function() {
      wx.stopPullDownRefresh();
    });
  }
});
