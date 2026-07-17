var app = getApp();
var api = require('../../utils/api');
var util = require('../../utils/util');

Page({
  data: {
    currentTab: 0,
    tabs: ['全部', '待出库', '已完成'],
    filteredList: [],
    _allList: [],
    _rawList: [],
    showModal: false,
    detailItem: null,
    detailItems: [],
    isAdmin: false,
    isStaff: false,
    _goodsMap: {},
    _locationMap: {}
  },

  onLoad: function(params) {
    this.data._pendingOrderNo = params && params.order_no ? params.order_no : null;
    var user = app.globalData.userInfo || {};
    this.setData({
      isAdmin: user.position === '仓库管理员' || user.department === '管理部门',
      isStaff: user.position === '仓库员工'
    });
    this.loadBaseData();
  },

  onShow: function() {
    var user = app.globalData.userInfo || {};
    this.setData({
      isAdmin: user.position === '仓库管理员' || user.department === '管理部门',
      isStaff: user.position === '仓库员工'
    });
    this.loadBaseData();
  },

  loadBaseData: function() {
    var self = this;
    return Promise.all([
      api.goodsApi.list().catch(function() { return []; }),
      api.locationApi.list().catch(function() { return []; })
    ]).then(function(results) {
      var goods = results[0] || [];
      var locs = results[1] || [];
      var goodsMap = {};
      for (var i = 0; i < goods.length; i++) {
        goodsMap[goods[i].goods_id] = goods[i].goods_name || ('#' + goods[i].goods_id);
      }
      var locMap = {};
      for (var j = 0; j < locs.length; j++) {
        locMap[locs[j].location_id] = locs[j].location_name || ('库位#' + locs[j].location_id);
      }
      self.data._goodsMap = goodsMap;
      self.data._locationMap = locMap;
      self.loadData();
    }).catch(function() {
      self.loadData();
    });
  },

  loadData: function() {
    var self = this;
    wx.showLoading({ title: '加载中...' });
    Promise.all([api.outboundApi.list(), api.taskApi.list(), api.outboundDetailApi.list(), api.userApi.list()]).then(function(results) {
      var list = results[0];
      var tasks = results[1] || [];
      var allDetails = results[2] || [];
      var users = results[3] || [];
      if (!list) list = [];
      // 构建用户ID → 真实姓名映射
      var userMap = {};
      for (var u = 0; u < users.length; u++) {
        userMap[users[u].user_id] = users[u].real_name || users[u].username || '';
      }
      var taskMap = {};
      for (var t = 0; t < tasks.length; t++) {
        if (tasks[t].related_order_no) {
          taskMap[tasks[t].related_order_no] = tasks[t];
        }
      }
      var detailLocMap = {};
      for (var dt = 0; dt < allDetails.length; dt++) {
        var d = allDetails[dt];
        if (!detailLocMap[d.outbound_no]) detailLocMap[d.outbound_no] = [];
        detailLocMap[d.outbound_no].push({
          location_id: d.location_id,
          location_name: self.data._locationMap[d.location_id] || (d.location_id ? '库位#' + d.location_id : '-'),
          quantity: d.quantity || 0
        });
      }
      var mapped = [];
      for (var i = 0; i < list.length; i++) {
        var task = taskMap[list[i].outbound_no] || {};
        var opName = task.operator_name || task.assignee_name || '';
        if (!opName && task.assignee_id) {
          opName = userMap[task.assignee_id] || '';
        }
        if (!opName) {
          opName = list[i].operator_name || '-';
        }
        mapped.push({
          outbound_no: list[i].outbound_no,
          outbound_type: list[i].outbound_type || '-',
          order_status: list[i].order_status || '草稿',
          operator_name: opName,
          outbound_time: list[i].outbound_time,
          formattedTime: util.formatDate(list[i].outbound_time),
          remark: list[i].remark,
          tagCls: util.getStatusTag(list[i].order_status),
          task_id: task.task_id || null,
          create_time: task.created_time || list[i].create_time,
          fmtCreate: util.formatDate(task.created_time || list[i].create_time),
          locations: detailLocMap[list[i].outbound_no] || []
        });
      }
      // 按创建时间倒序排列（最新在前）
      mapped.sort(function(a, b) {
        var ta = a.create_time || '';
        var tb = b.create_time || '';
        if (ta > tb) return -1;
        if (ta < tb) return 1;
        return 0;
      });
      // 保存原始列表，用于从任务跳转时查找
      self.data._rawList = mapped;
      // 列表显示过滤掉草稿
      mapped = mapped.filter(function(m) { return m.order_status !== '草稿' });
      self.data._allList = mapped;
      self.filterList();
      wx.hideLoading();
      // 从任务跳转时，先在过滤列表找，找不到再到原始列表找
      if (self.data._pendingOrderNo) {
        var no = self.data._pendingOrderNo;
        self.data._pendingOrderNo = null;
        var found = null;
        for (var di = 0; di < mapped.length; di++) {
          if (mapped[di].outbound_no === no) { found = mapped[di]; break; }
        }
        if (!found) {
          for (var ri = 0; ri < self.data._rawList.length; ri++) {
            if (self.data._rawList[ri].outbound_no === no) { found = self.data._rawList[ri]; break; }
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
    api.outboundDetailApi.list().then(function(allDetails) {
      var detailItems = [];
      if (allDetails) {
        for (var j = 0; j < allDetails.length; j++) {
          if (allDetails[j].outbound_no === item.outbound_no) {
            var d = allDetails[j];
            detailItems.push({
              detail_id: d.detail_id,
              goods_name: self.data._goodsMap[d.goods_id] || ('货物#' + d.goods_id),
              goods_id: d.goods_id,
              batch_id: d.batch_id || '-',
              quantity: d.quantity || 0,
              location_name: self.data._locationMap[d.location_id] || (d.location_id ? '库位#' + d.location_id : '-')
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
    var tabs = this.data.tabs;
    var filter = tabs[currentTab];
    var result = [];
    if (filter === '全部') {
      result = allList;
    } else {
      for (var i = 0; i < allList.length; i++) {
        if (allList[i].order_status === filter) {
          result.push(allList[i]);
        }
      }
    }
    this.setData({ filteredList: result });
  },

  showDetail: function(e) {
    var no = e.currentTarget.dataset.no;
    var item = null;
    for (var i = 0; i < this.data._allList.length; i++) {
      if (this.data._allList[i].outbound_no === no) {
        item = this.data._allList[i];
        break;
      }
    }
    if (item) this.showDetailByItem(item);
  },

  closeModal: function() {
    this.setData({ showModal: false, detailItem: null, detailItems: [] });
  },

  stopPropagation: function() {},

  goCreate: function() {
    wx.navigateTo({ url: '/pages/outbound-create/outbound-create' });
  },

  completeOutbound: function() {
    var self = this;
    var taskId = self.data.detailItem ? self.data.detailItem.task_id : null;
    if (!taskId) {
      wx.showToast({ title: '未找到关联任务', icon: 'none' });
      return;
    }
    wx.showActionSheet({
      itemList: ['确认执行出库'],
      success: function() {
        wx.showLoading({ title: '提交中...' });
        var userId = app.globalData.userInfo ? app.globalData.userInfo.user_id : null;
        api.outboundApi.complete(taskId, { operatorId: userId }).then(function() {
          wx.hideLoading();
          wx.showToast({ title: '出库完成', icon: 'success' });
          self.loadBaseData();
          self.closeModal();
        }).catch(function() {
          wx.hideLoading();
        });
      }
    });
  }
});
