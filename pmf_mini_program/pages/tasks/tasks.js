var api = require('../../utils/api');
var util = require('../../utils/util');
var app = getApp();

Page({
  data: {
    filterType: 'mine',
    taskList: [],
    _allTasks: [],
    // 详情弹窗
    showDetail: false,
    detailTitle: '',
    detailHead: null,
    detailHeadFields: [],
    detailList: [],
    detailColumns: [],
    detailLoading: false,
    detailTask: null,
    // 名称缓存
    _goodsMap: null,
    _warehouseMap: null,
    // 完成弹窗
    showComplete: false,
    completeType: '',
    completeTitle: '',
    completeLoading: false,
    completeDetail: [],
    completeTask: null,
    // 质检专用
    qcResult: '',
    qcDefectReason: '',
    qcHandlingSuggestion: '',
    qcRemark: '',
    qcResults: ['合格', '不合格'],
    // 盘点专用
    invCheckRows: []
  },

  onLoad() {
    this.loadData();
    this.loadNameMaps();
  },

  onShow() { this.loadData(); },

  async loadData() {
    wx.showLoading({ title: '加载中...' });
    try {
      var list = (await api.taskApi.list()) || [];
      this.data._allTasks = list.map(function(item) {
        var isCompleted = item.completed_time !== null && item.completed_time !== undefined && item.completed_time !== '';
        return {
          ...item,
          tagCls: isCompleted ? 'tag-success' : 'tag-warning',
          taskIcon: item.task_type === '入库' ? '📥' : item.task_type === '出库' ? '📤' : item.task_type === '质检' ? '🔍' : item.task_type === '库存盘点' ? '📋' : item.task_type === '库存调整' ? '🔧' : item.task_type === '处理不合格货物' ? '🗑' : '📋',
          fmtCreated: util.formatDate(item.created_time),
          displayStatus: isCompleted ? '已完成' : '待处理'
        };
      });
      this.filterList();
    } catch (e) { console.error(e); }
    wx.hideLoading();
  },

  onTaskTap(e) {
    var task = this.data.taskList[e.currentTarget.dataset.index];
    if (!task) return;
    this.openDetailPopup(task);
  },

  openDetailPopup: function(task) {
    var self = this;
    var taskType = task.task_type || '';
    var orderNo = task.related_order_no || '';

    var config = this.getDetailConfig(taskType);
    if (!config) {
      wx.showToast({ title: '暂不支持查看此类型详情', icon: 'none' });
      return;
    }

    this.setData({
      showDetail: true,
      detailTitle: config.title,
      detailHead: null,
      detailHeadFields: [],
      detailList: [],
      detailColumns: config.columns,
      detailLoading: true,
      detailTask: task
    });

    this.loadDetailData(taskType, orderNo, task).then(function(result) {
      self.setData({
        detailHead: result.head,
        detailHeadFields: result.headFields,
        detailList: result.details,
        detailLoading: false
      });
    }).catch(function() {
      self.setData({ detailLoading: false });
      wx.showToast({ title: '加载详情失败', icon: 'none' });
    });
  },

  loadNameMaps: function() {
    var self = this;
    api.goodsApi.list().then(function(goods) {
      var map = {};
      (goods || []).forEach(function(g) { map[g.goods_id] = g.goods_name || g.goodsCode || ('货物' + g.goods_id); });
      self.data._goodsMap = map;
    }).catch(function() {});
    api.warehouseApi.list().then(function(whs) {
      var map = {};
      (whs || []).forEach(function(w) { map[w.warehouse_id] = w.warehouse_name || ('仓库' + w.warehouse_id); });
      self.data._warehouseMap = map;
    }).catch(function() {});
  },

  enrichDetails: function(details, taskType) {
    var gMap = this.data._goodsMap || {};
    return (details || []).map(function(d) {
      var row = {};
      for (var k in d) { row[k] = d[k]; }
      var gid = d.goods_id;
      if (gid != null) row._goodsName = gMap[gid] || ('货物#' + gid);
      if (taskType === '库存盘点' || taskType === '入库' || taskType === '出库') {
        var wid = d.warehouse_id, zid = d.zone_id, lid = d.location_id;
        if (wid != null || zid != null || lid != null) {
          row._locationName = (wid != null ? wid : '-') + '-' + (zid != null ? zid : '-') + '-' + (lid != null ? lid : '-');
        }
      }
      if (taskType === '库存调整') {
        var fwid = d.from_warehouse_id, fzid = d.from_zone_id, flid = d.from_location_id;
        var twid = d.to_warehouse_id, tzid = d.to_zone_id, tlid = d.to_location_id;
        if (fwid != null || fzid != null || flid != null) {
          row._fromLocation = (fwid != null ? fwid : '-') + '-' + (fzid != null ? fzid : '-') + '-' + (flid != null ? flid : '-');
        }
        if (twid != null || tzid != null || tlid != null) {
          row._toLocation = (twid != null ? twid : '-') + '-' + (tzid != null ? tzid : '-') + '-' + (tlid != null ? tlid : '-');
        }
      }
      return row;
    });
  },

  getDetailConfig: function(taskType) {
    var configs = {
      '入库': {
        title: '入库单详情',
        columns: [
          { key: '_goodsName', label: '货物名称' },
          { key: 'batch_no', label: '批次号' },
          { key: 'quantity', label: '数量' },
          { key: '_locationName', label: '库位' }
        ]
      },
      '出库': {
        title: '出库单详情',
        columns: [
          { key: '_goodsName', label: '货物名称' },
          { key: 'batch_id', label: '批次号' },
          { key: 'quantity', label: '数量' },
          { key: '_locationName', label: '库位' }
        ]
      },
      '质检': {
        title: '质检单详情',
        columns: [
          { key: '_goodsName', label: '货物名称' },
          { key: 'batch_id', label: '批次号' },
          { key: 'inspection_result', label: '检验结果' },
          { key: 'defect_reason', label: '不合格原因' },
          { key: 'handling_suggestion', label: '处理建议' }
        ]
      },
      '库存盘点': {
        title: '库存盘点单详情',
        columns: [
          { key: '_goodsName', label: '货物名称' },
          { key: '_locationName', label: '库位' },
          { key: 'batch_id', label: '批次号' },
          { key: 'book_quantity', label: '账面数量' },
          { key: 'actual_quantity', label: '实盘数量' },
          { key: 'difference', label: '差异' },
          { key: 'detail_status', label: '状态' }
        ]
      },
      '库存调整': {
        title: '库存调整单详情',
        columns: [
          { key: '_goodsName', label: '货物名称' },
          { key: 'batch_id', label: '批次号' },
          { key: 'quantity', label: '数量' },
          { key: '_fromLocation', label: '原库位' },
          { key: '_toLocation', label: '新库位' }
        ]
      },
      '处理不合格货物': {
        title: '次品处理详情',
        columns: [
          { key: '_goodsName', label: '货物名称' },
          { key: 'batch_id', label: '批次号' },
          { key: 'quantity', label: '数量' },
          { key: '_locationName', label: '库位' }
        ]
      }
    };
    return configs[taskType] || null;
  },

  loadDetailData: async function(taskType, orderNo, task) {
    var result = { head: {}, headFields: [], details: [] };
    function val(rec, keys) {
      if (rec == null) return '-';
      if (typeof keys === 'string') keys = [keys];
      for (var i = 0; i < keys.length; i++) {
        if (rec[keys[i]] != null && rec[keys[i]] !== '') return rec[keys[i]];
      }
      return '-';
    }

    if (taskType === '入库') {
      try {
        var heads = await api.inboundApi.list();
        var head = (heads || []).find(function(h) { return h.inbound_no === orderNo; }) || {};
        result.head = head;
        result.headFields = [
          { label: '入库单号', value: val(head, 'inbound_no') || orderNo },
          { label: '入库类型', value: val(head, 'inbound_type') },
          { label: '状态', value: val(head, 'order_status') },
          { label: '入库时间', value: util.formatDate(val(head, 'inbound_time')) },
          { label: '备注', value: val(head, 'remark') }
        ];
      } catch (e) {}
      try {
        var details = await api.inboundDetailApi.list();
        result.details = (details || []).filter(function(d) { return d.inbound_no === orderNo; });
      } catch (e) {}
    }
    else if (taskType === '出库') {
      try {
        var heads = await api.outboundApi.list();
        var head = (heads || []).find(function(h) { return h.outbound_no === orderNo; }) || {};
        result.head = head;
        result.headFields = [
          { label: '出库单号', value: val(head, 'outbound_no') || orderNo },
          { label: '出库类型', value: val(head, 'outbound_type') },
          { label: '优先级', value: val(head, 'priority') },
          { label: '状态', value: val(head, 'order_status') },
          { label: '创建时间', value: util.formatDate(val(head, 'create_time')) },
          { label: '备注', value: val(head, 'remark') }
        ];
      } catch (e) {}
      try {
        var details = await api.outboundDetailApi.list();
        result.details = (details || []).filter(function(d) { return d.outbound_no === orderNo; });
      } catch (e) {}
    }
    else if (taskType === '质检') {
      try {
        var heads = await api.qualityApi.list();
        var head = (heads || []).find(function(h) { return h.quality_check_no === orderNo; }) || {};
        result.head = head;
        result.headFields = [
          { label: '质检单号', value: val(head, 'quality_check_no') || orderNo },
          { label: '质检类型', value: val(head, 'check_type') },
          { label: '状态', value: val(head, 'order_status') },
          { label: '质检员ID', value: val(head, 'inspector_id') },
          { label: '质检时间', value: util.formatDate(val(head, 'inspection_time')) },
          { label: '备注', value: val(head, 'remark') }
        ];
      } catch (e) {}
      try {
        var details = await api.qualityDetailApi.list();
        result.details = (details || []).filter(function(d) { return d.quality_check_no === orderNo; });
      } catch (e) {}
    }
    else if (taskType === '库存盘点') {
      try {
        var heads = await api.inventoryCheckHeadApi.list();
        var head = (heads || []).find(function(h) { return h.check_no === orderNo; }) || {};
        result.head = head;
        result.headFields = [
          { label: '盘点单号', value: val(head, 'check_no') || orderNo },
          { label: '盘点类型', value: val(head, 'check_type') },
          { label: '状态', value: val(head, 'order_status') },
          { label: '操作员ID', value: val(head, 'operator_id') },
          { label: '创建时间', value: util.formatDate(val(head, 'created_time')) },
          { label: '完成时间', value: util.formatDate(val(head, 'completed_time')) },
          { label: '备注', value: val(head, 'remark') }
        ];
      } catch (e) {}
      try {
        var details = await api.inventoryCheckDetailApi.list();
        result.details = (details || []).filter(function(d) { return d.check_no === orderNo; });
      } catch (e) {}
    }
    else if (taskType === '库存调整') {
      try {
        var heads = await api.adjustmentOrderHeadApi.list();
        var head = (heads || []).find(function(h) { return h.adjustment_no === orderNo; }) || {};
        result.head = head;
        result.headFields = [
          { label: '调整单号', value: val(head, 'adjustment_no') || orderNo },
          { label: '调整类型', value: val(head, 'adjustment_type') },
          { label: '状态', value: val(head, 'order_status') },
          { label: '操作员ID', value: val(head, 'operator_id') },
          { label: '调整时间', value: util.formatDate(val(head, 'adjustment_time')) }
        ];
      } catch (e) {}
      try {
        var details = await api.adjustmentOrderDetailApi.list();
        result.details = (details || []).filter(function(d) { return d.adjustment_no === orderNo; });
      } catch (e) {}
    }
    else if (taskType === '处理不合格货物') {
      result.head = task;
      result.headFields = [
        { label: '任务单号', value: val(task, 'related_order_no') },
        { label: '优先级', value: val(task, 'priority') },
        { label: '截至日期', value: util.formatDate(val(task, 'deadline')) },
        { label: '备注', value: val(task, 'remark') }
      ];
      try {
        var details = await api.defectiveApi.detail(task.task_id);
        result.details = details || [];
      } catch (e) {}
    }

    result.details = this.enrichDetails(result.details, taskType);
    return result;
  },

  closeDetail: function() {
    this.setData({ showDetail: false, detailHead: null, detailList: [], detailLoading: false, detailTask: null });
  },

  stopPropagation: function() {},

  // ==================== 去完成 ====================
  goComplete: function() {
    var task = this.data.detailTask;
    if (!task) return;
    var taskType = task.task_type || '';

    if (taskType === '入库') {
      // 跳转到入库管理页面完成
      var orderNo = task.related_order_no || '';
      this.closeDetail();
      wx.navigateTo({ url: '/pages/inbound/inbound?order_no=' + orderNo });
    } else if (taskType === '出库') {
      this.openOutboundComplete(task);
    } else if (taskType === '质检') {
      this.openQcComplete(task);
    } else if (taskType === '库存盘点') {
      this.openInvCheckComplete(task);
    } else if (taskType === '库存调整') {
      this.openAdjustmentComplete(task);
    } else if (taskType === '处理不合格货物') {
      this.openDefectiveComplete(task);
    } else {
      wx.showToast({ title: '该类型暂不支持完成', icon: 'none' });
    }
  },

  // --- 出库完成 ---
  async openOutboundComplete(task) {
    this.setData({
      showComplete: true,
      completeType: '出库',
      completeTitle: '完成出库',
      completeTask: task,
      completeLoading: true,
      completeDetail: []
    });
    try {
      var details = await api.outboundApi.detail(task.task_id);
      this.setData({ completeDetail: details || [], completeLoading: false });
    } catch (e) {
      this.setData({ completeLoading: false });
    }
  },

  // --- 质检完成 ---
  openQcComplete(task) {
    this.setData({
      showComplete: true,
      completeType: '质检',
      completeTitle: '完成质检',
      completeTask: task,
      completeDetail: [],
      qcResult: '',
      qcDefectReason: '',
      qcHandlingSuggestion: '',
      qcRemark: task.remark || ''
    });
  },

  onQcResultChange(e) {
    this.setData({ qcResult: this.data.qcResults[e.detail.value] });
  },

  onQcInput(e) {
    var field = e.currentTarget.dataset.field;
    var data = {};
    data[field] = e.detail.value;
    this.setData(data);
  },

  // --- 库存盘点完成 ---
  async openInvCheckComplete(task) {
    this.setData({
      showComplete: true,
      completeType: '库存盘点',
      completeTitle: '完成盘点',
      completeTask: task,
      completeLoading: true,
      invCheckRows: []
    });
    try {
      // 并行加载明细和名称映射
      var [rows, goods, locs] = await Promise.all([
        api.inventoryCheckApi.details(task.related_order_no),
        api.goodsApi.list().catch(function() { return []; }),
        api.locationApi.list().catch(function() { return []; })
      ]);
      // 构建名称映射
      var goodsMap = {};
      for (var g = 0; g < (goods || []).length; g++) {
        goodsMap[goods[g].goods_id] = goods[g].goods_name || ('货物#' + goods[g].goods_id);
      }
      var locMap = {};
      for (var l = 0; l < (locs || []).length; l++) {
        locMap[locs[l].location_id] = locs[l].location_name || ('库位#' + locs[l].location_id);
      }
      // 丰富行数据
      for (var i = 0; i < (rows || []).length; i++) {
        rows[i]._actualInput = rows[i].actual_quantity != null ? String(rows[i].actual_quantity) : '';
        rows[i]._goodsName = goodsMap[rows[i].goods_id] || '-';
        rows[i]._locationName = locMap[rows[i].location_id] || '-';
      }
      this.setData({ invCheckRows: rows || [], completeLoading: false });
    } catch (e) {
      this.setData({ completeLoading: false });
      wx.showToast({ title: '获取盘点明细失败', icon: 'none' });
    }
  },

  onInvCheckQtyInput(e) {
    var idx = e.currentTarget.dataset.index;
    var rows = this.data.invCheckRows;
    if (idx >= 0 && idx < rows.length) {
      rows[idx]._actualInput = e.detail.value;
      this.setData({ invCheckRows: rows });
    }
  },

  // --- 库存调整完成 ---
  async openAdjustmentComplete(task) {
    this.setData({
      showComplete: true,
      completeType: '库存调整',
      completeTitle: '完成调整',
      completeTask: task,
      completeLoading: true,
      completeDetail: []
    });
    try {
      var details = await api.adjustmentApi.detail(task.task_id);
      this.setData({ completeDetail: details || [], completeLoading: false });
    } catch (e) {
      this.setData({ completeLoading: false });
    }
  },

  // --- 次品处理完成 ---
  async openDefectiveComplete(task) {
    this.setData({
      showComplete: true,
      completeType: '处理不合格货物',
      completeTitle: '完成次品处理',
      completeTask: task,
      completeLoading: true,
      completeDetail: []
    });
    try {
      var details = await api.defectiveApi.detail(task.task_id);
      this.setData({ completeDetail: details || [], completeLoading: false });
    } catch (e) {
      this.setData({ completeLoading: false });
    }
  },

  // ==================== 确认完成 ====================
  confirmComplete: function() {
    var self = this;
    var task = this.data.completeTask;
    if (!task) return;
    var type = this.data.completeType;

    if (type === '出库') {
      wx.showModal({
        title: '确认出库',
        content: '确定要完成出库吗？出库后库存数量将相应减少。',
        success: function(res) { if (res.confirm) self.submitComplete(); }
      });
    } else if (type === '质检') {
      if (!this.data.qcResult) { wx.showToast({ title: '请选择质检结果', icon: 'none' }); return; }
      wx.showModal({
        title: '确认提交',
        content: '确定要提交质检结果吗？提交后将无法撤回。',
        success: function(res) { if (res.confirm) self.submitComplete(); }
      });
    } else if (type === '库存盘点') {
      var rows = this.data.invCheckRows;
      for (var i = 0; i < rows.length; i++) {
        var qty = parseInt(rows[i]._actualInput);
        if (isNaN(qty) || qty < 0) {
          wx.showToast({ title: '请为第' + (i + 1) + '行填写有效的实盘数量', icon: 'none' });
          return;
        }
        if (qty > 10) {
          wx.showToast({ title: '每个库位货物上限不能超过10（第' + (i + 1) + '行）', icon: 'none' });
          return;
        }
      }
      wx.showModal({
        title: '确认提交',
        content: '确定要提交盘点结果吗？提交后将更新库存数据。',
        success: function(res) { if (res.confirm) self.submitComplete(); }
      });
    } else if (type === '库存调整') {
      wx.showModal({
        title: '确认完成',
        content: '确定要完成调整吗？货物将转移到新库位，原库位将被释放。',
        success: function(res) { if (res.confirm) self.submitComplete(); }
      });
    } else if (type === '处理不合格货物') {
      wx.showModal({
        title: '确认处理',
        content: '确定要完成次品处理吗？相关库存和批次将被删除。',
        success: function(res) { if (res.confirm) self.submitComplete(); }
      });
    }
  },

  submitComplete: async function() {
    var task = this.data.completeTask;
    var type = this.data.completeType;
    var uid = app.globalData.userInfo ? app.globalData.userInfo.user_id : null;
    wx.showLoading({ title: '提交中...', mask: true });
    try {
      var res;
      if (type === '出库') {
        res = await api.outboundApi.complete(task.task_id, { operatorId: uid });
      } else if (type === '质检') {
        res = await api.qualityApi.complete(task.task_id, {
          inspectionResult: this.data.qcResult,
          defectReason: this.data.qcDefectReason,
          handlingSuggestion: this.data.qcHandlingSuggestion,
          remark: this.data.qcRemark,
          operatorId: uid
        });
      } else if (type === '库存盘点') {
        var details = this.data.invCheckRows.map(function(d) {
          return { detailId: d.detail_id, actualQuantity: parseInt(d._actualInput) || 0 };
        });
        res = await api.inventoryCheckApi.complete(task.task_id, {
          details: details,
          remark: task.remark || '',
          operatorId: uid
        });
      } else if (type === '库存调整') {
        res = await api.adjustmentApi.complete(task.task_id, { operatorId: uid });
      } else if (type === '处理不合格货物') {
        res = await api.defectiveApi.complete(task.task_id, { operatorId: uid });
      }
      wx.hideLoading();
      wx.showToast({ title: '操作完成', icon: 'success' });
      this.setData({ showComplete: false, showDetail: false });
      this.loadData();
    } catch (e) {
      wx.hideLoading();
      wx.showToast({ title: e && e.msg ? e.msg : '操作失败', icon: 'none' });
    }
  },

  closeComplete: function() {
    this.setData({ showComplete: false, completeTask: null, completeDetail: [], invCheckRows: [] });
  },

  // ==================== 列表过滤 ====================
  filterList() {
    var _allTasks = this.data._allTasks;
    var uid = app.globalData.userInfo ? app.globalData.userInfo.user_id : null;
    var list = _allTasks.filter(function(t) {
      return t.assignee_id === uid && !t.completed_time;
    });
    this.setData({ taskList: list });
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
