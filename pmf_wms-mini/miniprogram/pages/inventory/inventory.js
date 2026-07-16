var api = require('../../utils/api');
var util = require('../../utils/util');

Page({
  data: {
    currentTab: 0,
    searchText: '',
    tabs: [
      { id: 0, label: '全部', count: 0, filter: '' },
      { id: 1, label: '正常', count: 0, filter: '正常' },
      { id: 2, label: '待入库', count: 0, filter: '待入库' },
      { id: 3, label: '待出库', count: 0, filter: '待出库' }
    ],
    allList: [],
    filteredList: []
  },

  onLoad: function() {
    this.loadData();
  },

  onShow: function() {
    if (this.data.allList.length === 0) this.loadData();
  },

  loadData: function() {
    var self = this;
    wx.showLoading({ title: '加载中...' });
    api.inventoryApi.listWithDetails().then(function(list) {
      if (!list) list = [];
      var mapped = [];
      for (var i = 0; i < list.length; i++) {
        var item = list[i];
        mapped.push({
          inventory_id: item.inventory_id,
          goods_name: item.goods_name,
          batch_id: item.batch_id,
          quantity: item.quantity,
          locked_quantity: item.locked_quantity,
          unit: item.unit || '件',
          inventory_status: item.inventory_status,
          location_name: item.location_name || '未分配',
          tagCls: util.getStatusTag(item.inventory_status),
          statusColor: item.inventory_status === '正常' ? 'normal' : (item.inventory_status === '待入库' ? 'pending' : 'danger')
        });
      }
      var tabs = self.data.tabs;
      tabs[0].count = mapped.length;
      var normalCount = 0, pendingIn = 0, pendingOut = 0;
      for (var j = 0; j < mapped.length; j++) {
        if (mapped[j].inventory_status === '正常') normalCount++;
        else if (mapped[j].inventory_status === '待入库') pendingIn++;
        else if (mapped[j].inventory_status === '待出库') pendingOut++;
      }
      tabs[1].count = normalCount;
      tabs[2].count = pendingIn;
      tabs[3].count = pendingOut;
      self.setData({ allList: mapped, tabs: tabs });
      self.filterList();
      wx.hideLoading();
    })["catch"](function(e) {
      console.error(e);
      wx.hideLoading();
    });
  },

  switchTab: function(e) {
    this.setData({ currentTab: e.currentTarget.dataset.index });
    this.filterList();
  },

  onSearch: function(e) {
    this.setData({ searchText: e.detail.value });
    this.filterList();
  },

  filterList: function() {
    var allList = this.data.allList;
    var currentTab = this.data.currentTab;
    var tabs = this.data.tabs;
    var searchText = this.data.searchText;
    var filterVal = tabs[currentTab].filter;
    var result = [];
    for (var i = 0; i < allList.length; i++) {
      var item = allList[i];
      if (filterVal && item.inventory_status !== filterVal) continue;
      if (searchText) {
        var name = (item.goods_name || '').toLowerCase();
        if (name.indexOf(searchText.toLowerCase()) === -1) continue;
      }
      result.push(item);
    }
    this.setData({ filteredList: result });
  },

  goDetail: function(e) {
    var id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: '/pages/inventory/inventory?id=' + id });
  }
});