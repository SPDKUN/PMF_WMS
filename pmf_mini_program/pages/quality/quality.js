var app = getApp();
var api = require('../../utils/api');
var util = require('../../utils/util');

Page({
  data: {
    currentTab: 0,
    tabs: ['全部', '待检', '已完成'],
    filteredList: [],
    _allList: [],
    _rawList: [],
    showModal: false,
    detailItem: null,
    detailItems: [],
    isAdmin: false,
    isInspector: false,
    _goodsMap: {},
    _userMap: {},
    showQcComplete: false,
    qcCompleteTaskId: null,
    qcResult: '',
    qcDefectReason: '',
    qcHandlingSuggestion: '',
    qcRemark: '',
    qcResults: ['合格', '不合格']
  },

  onLoad: function(params) {
    this.data._pendingOrderNo = params && params.order_no ? params.order_no : null;
    var user = app.globalData.userInfo || {};
    this.setData({
      isAdmin: user.position === '仓库管理员' || user.department === '管理部门',
      isInspector: user.position === '质检员'
    });
    this.loadBaseData();
  },

  onShow: function() {
    var user = app.globalData.userInfo || {};
    this.setData({
      isAdmin: user.position === '仓库管理员' || user.department === '管理部门',
      isInspector: user.position === '质检员'
    });
    this.loadBaseData();
  },

  loadBaseData: function() {
    var self = this;
    return Promise.all([
      api.goodsApi.list().catch(function() { return []; }),
      api.userApi.list().catch(function() { return []; })
    ]).then(function(results) {
      var goods = results[0] || [];
      var users = results[1] || [];
      var goodsMap = {};
      for (var i = 0; i < goods.length; i++) {
        goodsMap[goods[i].goods_id] = goods[i].goods_name || ('#' + goods[i].goods_id);
      }
      var userMap = {};
      for (var j = 0; j < users.length; j++) {
        userMap[users[j].user_id] = users[j].real_name || users[j].username;
      }
      self.data._goodsMap = goodsMap;
      self.data._userMap = userMap;
      self.loadData();
    }).catch(function() {
      self.loadData();
    });
  },

  loadData: function() {
    var self = this;
    wx.showLoading({ title: '加载中...' });
    Promise.all([api.qualityApi.list(), api.taskApi.list()]).then(function(results) {
      var list = results[0];
      var tasks = results[1] || [];
      if (!list) list = [];
      var taskMap = {};
      for (var t = 0; t < tasks.length; t++) {
        if (tasks[t].related_order_no) {
          taskMap[tasks[t].related_order_no] = tasks[t];
        }
      }
      var userMap = self.data._userMap || {};
      var mapped = [];
      for (var i = 0; i < list.length; i++) {
        var task = taskMap[list[i].quality_check_no] || {};
        var inspectorName = list[i].inspector_name || userMap[list[i].inspector_id] || '-';
        mapped.push({
          quality_check_no: list[i].quality_check_no,
          check_type: list[i].check_type || '-',
          order_status: list[i].order_status || '待检',
          inspector_name: inspectorName,
          inspector_id: list[i].inspector_id,
          inspection_time: list[i].inspection_time,
          fmtTime: list[i].inspection_time ? util.formatDate(list[i].inspection_time) : '待检验',
          remark: list[i].remark,
          tagCls: util.getStatusTag(list[i].order_status),
          task_id: task.task_id || null,
          create_time: task.created_time || list[i].create_time,
          fmtCreate: util.formatDate(task.created_time || list[i].create_time)
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
      self.data._rawList = mapped;
      self.data._allList = mapped;
      self.filterList();
      wx.hideLoading();
      if (self.data._pendingOrderNo) {
        var no = self.data._pendingOrderNo;
        self.data._pendingOrderNo = null;
        var found = null;
        for (var di = 0; di < mapped.length; di++) {
          if (mapped[di].quality_check_no === no) { found = mapped[di]; break; }
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
    api.qualityDetailApi.list().then(function(allDetails) {
      var goodsMap = self.data._goodsMap || {};
      var detailItems = [];
      if (allDetails) {
        for (var j = 0; j < allDetails.length; j++) {
          if (allDetails[j].quality_check_no === item.quality_check_no) {
            var d = allDetails[j];
            detailItems.push({
              detail_id: d.detail_id,
              batch_id: d.batch_id || '-',
              goods_id: d.goods_id,
              goods_name: goodsMap[d.goods_id] || ('货物#' + d.goods_id),
              inspection_result: d.inspection_result || '待检验',
              defect_reason: d.defect_reason || '',
              handling_suggestion: d.handling_suggestion || ''
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
    } else if (filter === '已完成') {
      result = allList.filter(function(m) { return m.order_status === '已完成' });
    } else {
      // 待检：显示所有未完成的质检任务（order_status !== '已完成'）
      result = allList.filter(function(m) { return m.order_status !== '已完成' });
    }
    this.setData({ filteredList: result });
  },

  showDetail: function(e) {
    var no = e.currentTarget.dataset.no;
    var item = null;
    for (var i = 0; i < this.data._allList.length; i++) {
      if (this.data._allList[i].quality_check_no === no) { item = this.data._allList[i]; break; }
    }
    if (item) this.showDetailByItem(item);
  },

  closeModal: function() {
    this.setData({ showModal: false, detailItem: null, detailItems: [] });
  },

  stopPropagation: function() {},

  goCreate: function() {
    wx.navigateTo({ url: '/pages/quality-create/quality-create' });
  },

  openQcComplete: function() {
    var taskId = this.data.detailItem ? this.data.detailItem.task_id : null;
    if (!taskId) {
      wx.showToast({ title: '未找到关联任务', icon: 'none' });
      return;
    }
    this.setData({
      showQcComplete: true,
      showModal: false,
      qcCompleteTaskId: taskId,
      qcResult: '',
      qcDefectReason: '',
      qcHandlingSuggestion: '',
      qcRemark: ''
    });
  },

  closeQcComplete: function() {
    this.setData({ showQcComplete: false, qcCompleteTaskId: null });
  },

  onQcResultChange: function(e) {
    this.setData({ qcResult: this.data.qcResults[e.detail.value] });
  },

  onQcInput: function(e) {
    var field = e.currentTarget.dataset.field;
    this.setData({ [field]: e.detail.value });
  },

  submitQcComplete: function() {
    var self = this;
    if (!this.data.qcResult) {
      wx.showToast({ title: '请选择质检结果', icon: 'none' });
      return;
    }
    if (this.data.qcResult === '不合格' && !this.data.qcDefectReason.trim()) {
      wx.showToast({ title: '不合格请填写原因', icon: 'none' });
      return;
    }
    wx.showLoading({ title: '提交中...', mask: true });
    var userId = app.globalData.userInfo ? app.globalData.userInfo.user_id : null;
    api.qualityApi.complete(this.data.qcCompleteTaskId, {
      inspectionResult: this.data.qcResult,
      defectReason: this.data.qcDefectReason,
      handlingSuggestion: this.data.qcHandlingSuggestion,
      remark: this.data.qcRemark,
      operatorId: userId
    }).then(function() {
      wx.hideLoading();
      wx.showToast({ title: '质检完成', icon: 'success' });
      self.setData({ showQcComplete: false, qcCompleteTaskId: null });
      self.loadBaseData();
      self.closeModal();
    }).catch(function() {
      wx.hideLoading();
    });
  }
});
