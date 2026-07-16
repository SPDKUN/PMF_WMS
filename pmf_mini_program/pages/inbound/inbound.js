var app = getApp();
var api = require('../../utils/api');
var util = require('../../utils/util');

Page({
  data: {
    currentTab: 0,
    tabs: ['全部', '待入库', '已完成'],
    filteredList: [],
    _allList: [],
    _rawList: [],
    showModal: false,
    detailItem: null,
    detailItems: [],
    isAdmin: false,
    isStaff: false,
    _goodsMap: {},
    _goodsConditionMap: {},
    _locationMap: {},
    _warehouseTypeMap: {},
    _zoneWarehouseMap: {},
    showLocationPicker: false,
    zoneList: [],
    locationSelection: [],
    selectedLocIds: [],
    pendingTaskId: null,
    locationPickerTitle: '选择库位'
  },

  onLoad: function(params) {
    this.data._pendingOrderNo = params && params.order_no ? params.order_no : null;
    var user = app.globalData.userInfo || {};
    this.setData({
      isAdmin: user.position === '系统管理员' || user.department === '管理部',
      isStaff: user.position === '仓库员工'
    });
    this.loadBaseData();
  },

  onShow: function() {
    var user = app.globalData.userInfo || {};
    this.setData({
      isAdmin: user.position === '系统管理员' || user.department === '管理部',
      isStaff: user.position === '仓库员工'
    });
    this.loadBaseData();
  },

  loadBaseData: function() {
    var self = this;
    return Promise.all([
      api.goodsApi.list().catch(function() { return []; }),
      api.locationApi.list().catch(function() { return []; }),
      api.warehouseApi.list().catch(function() { return []; }),
      api.zoneApi.list().catch(function() { return []; })
    ]).then(function(results) {
      var goods = results[0] || [];
      var locs = results[1] || [];
      var warehouses = results[2] || [];
      var zones = results[3] || [];
      var goodsMap = {};
      var goodsConditionMap = {};
      for (var i = 0; i < goods.length; i++) {
        goodsMap[goods[i].goods_id] = goods[i].goods_name || ('货物#' + goods[i].goods_id);
        goodsConditionMap[goods[i].goods_id] = goods[i].storage_condition || '常温';
      }
      var locMap = {};
      for (var j = 0; j < locs.length; j++) {
        locMap[locs[j].location_id] = locs[j].location_name || ('库位#' + locs[j].location_id);
      }
      // 储存条件 → 仓库类型映射
      var whTypeMap = {};
      for (var w = 0; w < warehouses.length; w++) {
        whTypeMap[warehouses[w].warehouse_id] = warehouses[w].warehouse_type || '一般仓库';
      }
      // 库区 → 仓库映射
      var zoneWhMap = {};
      for (var z = 0; z < zones.length; z++) {
        zoneWhMap[zones[z].zone_id] = zones[z].warehouse_id;
      }
      self.data._goodsMap = goodsMap;
      self.data._goodsConditionMap = goodsConditionMap;
      self.data._locationMap = locMap;
      self.data._warehouseTypeMap = whTypeMap;
      self.data._zoneWarehouseMap = zoneWhMap;
      self.loadData();
    }).catch(function() {
      self.loadData();
    });
  },

  loadData: function() {
    var self = this;
    wx.showLoading({ title: '加载中...' });
    Promise.all([api.inboundApi.list(), api.taskApi.list()]).then(function(results) {
      var list = results[0];
      var tasks = results[1] || [];
      if (!list) list = [];
      var taskMap = {};
      for (var t = 0; t < tasks.length; t++) {
        if (tasks[t].related_order_no) {
          taskMap[tasks[t].related_order_no] = tasks[t].task_id;
        }
      }
      var mapped = [];
      for (var i = 0; i < list.length; i++) {
        mapped.push({
          inbound_no: list[i].inbound_no,
          inbound_type: list[i].inbound_type || '-',
          order_status: list[i].order_status || '草稿',
          operator_name: list[i].operator_name || '-',
          inbound_time: list[i].inbound_time,
          formattedTime: util.formatDate(list[i].inbound_time),
          remark: list[i].remark,
          tagCls: util.getStatusTag(list[i].order_status),
          task_id: taskMap[list[i].inbound_no] || null,
          create_time: list[i].create_time,
          fmtCreate: util.formatDate(list[i].create_time)
        });
      }
      self.data._rawList = mapped;
      mapped = mapped.filter(function(m) { return m.order_status !== '草稿' });
      self.data._allList = mapped;
      self.filterList();
      wx.hideLoading();
      if (self.data._pendingOrderNo) {
        var no = self.data._pendingOrderNo;
        self.data._pendingOrderNo = null;
        var found = null;
        for (var di = 0; di < mapped.length; di++) {
          if (mapped[di].inbound_no === no) { found = mapped[di]; break; }
        }
        if (!found) {
          for (var ri = 0; ri < self.data._rawList.length; ri++) {
            if (self.data._rawList[ri].inbound_no === no) { found = self.data._rawList[ri]; break; }
          }
        }
        if (found) self.showDetailByItem(found);
      }
    }).catch(function(e) {
      console.error(e);
      wx.hideLoading();
    });
  },

  showDetailByItem: function(item) {
    var self = this;
    api.inboundDetailApi.list().then(function(allDetails) {
      var detailItems = [];
      if (allDetails) {
        for (var j = 0; j < allDetails.length; j++) {
          if (allDetails[j].inbound_no === item.inbound_no) {
            var d = allDetails[j];
            detailItems.push({
              detail_id: d.detail_id,
              goods_name: self.data._goodsMap[d.goods_id] || ('货物#' + d.goods_id),
              goods_id: d.goods_id,
              batch_no: d.batch_no || d.batch_id || '-',
              quantity: d.quantity || 0,
              location_name: self.data._locationMap[d.location_id] || (d.location_id ? '库位#' + d.location_id : '-'),
              production_date: d.production_date ? String(d.production_date).split('T')[0] : '-',
              expiry_date: d.expiry_date ? String(d.expiry_date).split('T')[0] : '-'
            });
          }
        }
      }
      self.setData({ detailItem: item, detailItems: detailItems, showModal: true });
    }).catch(function() {
      self.setData({ detailItem: item, detailItems: [], showModal: true });
    });
  },

  switchTab: function(e) {
    this.setData({ currentTab: e.currentTarget.dataset.index });
    this.filterList();
  },

  filterList: function() {
    var allList = this.data._allList;
    var currentTab = this.data.currentTab;
    if (currentTab === 0) {
      this.setData({ filteredList: allList });
    } else if (currentTab === 1) {
      this.setData({ filteredList: allList.filter(function(m) { return m.order_status === '待入库' || m.order_status === '已审核'; }) });
    } else {
      this.setData({ filteredList: allList.filter(function(m) { return m.order_status === '已完成'; }) });
    }
  },

  showDetail: function(e) {
    var no = e.currentTarget.dataset.no;
    var item = null;
    for (var i = 0; i < this.data._allList.length; i++) {
      if (this.data._allList[i].inbound_no === no) { item = this.data._allList[i]; break; }
    }
    if (item) this.showDetailByItem(item);
  },

  closeModal: function() {
    this.setData({ showModal: false, detailItem: null, detailItems: [] });
  },

  stopPropagation: function() {},

  goCreate: function() {
    wx.navigateTo({ url: '/pages/inbound-create/inbound-create' });
  },

  // 储存条件 → 仓库类型映射
  _getWarehouseTypeForCondition: function(condition) {
    var map = { '常温': '一般仓库', '冷冻': '冷冻库', '恒温': '恒温库' };
    return map[condition] || '一般仓库';
  },

  completeInbound: function() {
    var self = this;
    var taskId = self.data.detailItem ? self.data.detailItem.task_id : null;
    if (!taskId) {
      wx.showToast({ title: '未找到关联任务', icon: 'none' });
      return;
    }
    wx.showLoading({ title: '加载库位...' });
    var no = self.data.detailItem.inbound_no;
    Promise.all([api.zoneApi.list(), api.locationApi.list(), api.inboundDetailApi.list()]).then(function(results) {
      var zones = results[0] || [];
      var locs = results[1] || [];
      var details = results[2] || [];
      var batchDetail = null;
      for (var d = 0; d < details.length; d++) {
        if (details[d].inbound_no === no) { batchDetail = details[d]; break; }
      }
      var batchId = batchDetail ? (batchDetail.batch_no || batchDetail.batch_id) : '';
      var goodsId = batchDetail ? batchDetail.goods_id : null;
      // 获取货物储存条件，确定允许的仓库类型
      var storageCondition = goodsId ? (self.data._goodsConditionMap[goodsId] || '常温') : '常温';
      var allowedWhType = self._getWarehouseTypeForCondition(storageCondition);
      // 找出符合条件的库区ID（属于匹配仓库类型的库区）
      var whTypeMap = self.data._warehouseTypeMap || {};
      var zoneWhMap = self.data._zoneWarehouseMap || {};
      var allowedZoneIds = {};
      for (var z = 0; z < zones.length; z++) {
        var whId = zoneWhMap[zones[z].zone_id];
        var whType = whTypeMap[whId] || '';
        if (whType === allowedWhType) {
          allowedZoneIds[zones[z].zone_id] = true;
        }
      }
      api.batchApi.list().then(function(allBatches) {
        var initQty = 100;
        if (allBatches && batchId) {
          for (var b = 0; b < allBatches.length; b++) {
            if (allBatches[b].batch_id === batchId) { initQty = allBatches[b].initial_quantity || 100; break; }
          }
        }
        var totalNeeded = Math.ceil(initQty / 10.0);
        var zoneMap = {};
        for (var z2 = 0; z2 < zones.length; z2++) { zoneMap[zones[z2].zone_id] = zones[z2].zone_name; }
        var availableByZone = {};
        for (var l = 0; l < locs.length; l++) {
          // 只显示允许库区（匹配储存条件）的空闲库位
          if (locs[l].is_occupied === 0 && locs[l].lock_status === '未锁定' && allowedZoneIds[locs[l].zone_id]) {
            var zid = locs[l].zone_id;
            if (!availableByZone[zid]) availableByZone[zid] = [];
            availableByZone[zid].push({
              location_id: locs[l].location_id,
              location_name: locs[l].location_name,
              zone_id: zid,
              zone_name: zoneMap[zid] || '未知区'
            });
          }
        }
        var sortedZoneIds = Object.keys(availableByZone).sort();
        var locationSelection = [];
        for (var sz = 0; sz < sortedZoneIds.length; sz++) {
          var zidInt = parseInt(sortedZoneIds[sz]);
          var zoneLocs = availableByZone[zidInt];
          locationSelection.push({ isZoneHeader: true, zone_name: zoneMap[zidInt] || '未知区', zone_id: zidInt, _key: 'zh-' + zidInt });
          for (var lc = 0; lc < zoneLocs.length; lc++) {
            locationSelection.push({ isZoneHeader: false, location_id: zoneLocs[lc].location_id, location_name: zoneLocs[lc].location_name, zone_name: zoneLocs[lc].zone_name, checked: false, _key: 'loc-' + zoneLocs[lc].location_id });
          }
        }
        var title = '请选择 ' + totalNeeded + ' 个库位';
        title += '（' + storageCondition + '→' + allowedWhType + '）';
        self.setData({ showLocationPicker: true, showModal: false, zoneList: zones, locationSelection: locationSelection, selectedLocIds: [], pendingTaskId: taskId, locationPickerTitle: title });
        wx.hideLoading();
      });
    }).catch(function(e) {
      wx.hideLoading();
      wx.showToast({ title: '加载库位失败', icon: 'none' });
    });
  },

  toggleLocation: function(e) {
    var idx = e.currentTarget.dataset.idx || e.currentTarget.dataset.index;
    var loc = this.data.locationSelection[idx];
    if (!loc || loc.isZoneHeader) return;
    var selected = this.data.selectedLocIds;
    var allLocs = this.data.locationSelection;
    if (loc.checked) {
      selected = selected.filter(function(id) { return id !== loc.location_id; });
      allLocs[idx].checked = false;
    } else {
      selected.push(loc.location_id);
      allLocs[idx].checked = true;
    }
    this.setData({ selectedLocIds: selected, locationSelection: allLocs });
  },

  confirmLocations: function() {
    var self = this;
    var taskId = this.data.pendingTaskId;
    var locIds = this.data.selectedLocIds;
    if (locIds.length === 0) { wx.showToast({ title: '请至少选择一个库位', icon: 'none' }); return; }
    wx.showLoading({ title: '提交中...', mask: true });
    var userId = app.globalData.userInfo ? app.globalData.userInfo.user_id : null;
    api.inboundApi.complete(taskId, { locationIds: locIds, operatorId: userId }).then(function() {
      wx.hideLoading();
      wx.showToast({ title: '入库完成', icon: 'success' });
      self.setData({ showLocationPicker: false, pendingTaskId: null });
      self.loadBaseData();
      self.closeModal();
    }).catch(function() { wx.hideLoading(); });
  },

  cancelLocationPicker: function() {
    this.setData({ showLocationPicker: false, pendingTaskId: null, locationSelection: [], selectedLocIds: [] });
  }
});
