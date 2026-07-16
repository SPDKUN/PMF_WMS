var api = require('../../utils/api');
var util = require('../../utils/util');

Page({
  data: {
    filterType: 'all',
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
    // 名称缓存
    _goodsMap: null,
    _warehouseMap: null
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
          taskIcon: item.task_type === '入库' ? '📥' : item.task_type === '出库' ? '📤' : item.task_type === '质检' ? '🔍' : item.task_type === '库存盘点' ? '📋' : item.task_type === '库存调整' ? '🔧' : '📋',
          fmtCreated: util.formatDate(item.created_time),
          displayStatus: isCompleted ? '已完成' : '待处理'
        };
      });
      this.filterList();
    } catch (e) { console.error(e); }
    wx.hideLoading();
  },

  setFilter(e) {
    this.setData({ filterType: e.currentTarget.dataset.type });
    this.filterList();
  },

  onTaskTap(e) {
    var task = this.data.taskList[e.currentTarget.dataset.index];
    if (!task) return;
    this.openDetailPopup(task);
  },

  // 打开详情弹窗
  openDetailPopup: function(task) {
    var self = this;
    var taskType = task.task_type || '';
    var orderNo = task.related_order_no || '';

    // 根据任务类型设置弹窗标题和列
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
      detailLoading: true
    });

    // 加载头部和明细数据
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

  // 预加载货物和仓库名称映射
  loadNameMaps: function() {
    var self = this;
    // 货物名映射
    api.goodsApi.list().then(function(goods) {
      var map = {};
      (goods || []).forEach(function(g) { map[g.goods_id] = g.goods_name || g.goodsCode || ('货物' + g.goods_id); });
      self.data._goodsMap = map;
    }).catch(function() {});
    // 仓库名映射
    api.warehouseApi.list().then(function(whs) {
      var map = {};
      (whs || []).forEach(function(w) { map[w.warehouse_id] = w.warehouse_name || ('仓库' + w.warehouse_id); });
      self.data._warehouseMap = map;
    }).catch(function() {});
  },

  // 用名称丰富明细记录
  enrichDetails: function(details, taskType) {
    var gMap = this.data._goodsMap || {};
    var wMap = this.data._warehouseMap || {};
    return (details || []).map(function(d) {
      var row = {};
      // 复制原始字段
      for (var k in d) { row[k] = d[k]; }

      // 货物名
      var gid = d.goods_id;
      if (gid != null) row._goodsName = gMap[gid] || ('货物#' + gid);

      // 库位名（仓库-库区-库位）
      if (taskType === '库存盘点' || taskType === '入库' || taskType === '出库') {
        var wid = d.warehouse_id;
        var zid = d.zone_id;
        var lid = d.location_id;
        if (wid != null || zid != null || lid != null) {
          row._locationName = (wid != null ? wid : '-') + '-' + (zid != null ? zid : '-') + '-' + (lid != null ? lid : '-');
        }
      }
      // 库存调整：原库位 / 新库位
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

  // 获取详情配置（优先展示名称列）
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
      }
    };
    return configs[taskType] || null;
  },

  // 加载头部和明细数据（字段名匹配后端实体）
  loadDetailData: async function(taskType, orderNo, task) {
    var result = { head: {}, headFields: [], details: [] };
    // 辅助：取值，依次尝试多个候选键
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
        var head = (heads || []).find(function(h) {
          return (h.inbound_no) === orderNo;
        }) || {};
        result.head = head;
        result.headFields = [
          { label: '入库单号', value: val(head, 'inbound_no') || orderNo },
          { label: '入库类型', value: val(head, 'inbound_type') },
          { label: '状态', value: val(head, 'order_status') },
          { label: '入库时间', value: util.formatDate(val(head, 'inbound_time')) },
          { label: '备注', value: val(head, 'remark') }
        ];
      } catch (e) { console.error('load inbound head:', e); }
      try {
        var details = await api.inboundDetailApi.list();
        result.details = (details || []).filter(function(d) {
          return d.inbound_no === orderNo;
        });
      } catch (e) { console.error('load inbound detail:', e); }
    }
    else if (taskType === '出库') {
      try {
        var heads = await api.outboundApi.list();
        var head = (heads || []).find(function(h) {
          return h.outbound_no === orderNo;
        }) || {};
        result.head = head;
        result.headFields = [
          { label: '出库单号', value: val(head, 'outbound_no') || orderNo },
          { label: '出库类型', value: val(head, 'outbound_type') },
          { label: '优先级', value: val(head, 'priority') },
          { label: '状态', value: val(head, 'order_status') },
          { label: '创建时间', value: util.formatDate(val(head, 'create_time')) },
          { label: '备注', value: val(head, 'remark') }
        ];
      } catch (e) { console.error('load outbound head:', e); }
      try {
        var details = await api.outboundDetailApi.list();
        result.details = (details || []).filter(function(d) {
          return d.outbound_no === orderNo;
        });
      } catch (e) { console.error('load outbound detail:', e); }
    }
    else if (taskType === '质检') {
      try {
        var heads = await api.qualityApi.list();
        var head = (heads || []).find(function(h) {
          return h.quality_check_no === orderNo;
        }) || {};
        result.head = head;
        result.headFields = [
          { label: '质检单号', value: val(head, 'quality_check_no') || orderNo },
          { label: '质检类型', value: val(head, 'check_type') },
          { label: '状态', value: val(head, 'order_status') },
          { label: '质检员ID', value: val(head, 'inspector_id') },
          { label: '质检时间', value: util.formatDate(val(head, 'inspection_time')) },
          { label: '备注', value: val(head, 'remark') }
        ];
      } catch (e) { console.error('load qc head:', e); }
      try {
        var details = await api.qualityDetailApi.list();
        result.details = (details || []).filter(function(d) {
          return d.quality_check_no === orderNo;
        });
      } catch (e) { console.error('load qc detail:', e); }
    }
    else if (taskType === '库存盘点') {
      try {
        var heads = await api.inventoryCheckHeadApi.list();
        var head = (heads || []).find(function(h) {
          return h.check_no === orderNo;
        }) || {};
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
      } catch (e) { console.error('load check head:', e); }
      try {
        var details = await api.inventoryCheckDetailApi.list();
        result.details = (details || []).filter(function(d) {
          return d.check_no === orderNo;
        });
      } catch (e) { console.error('load check detail:', e); }
    }
    else if (taskType === '库存调整') {
      try {
        var heads = await api.adjustmentOrderHeadApi.list();
        var head = (heads || []).find(function(h) {
          return h.adjustment_no === orderNo;
        }) || {};
        result.head = head;
        result.headFields = [
          { label: '调整单号', value: val(head, 'adjustment_no') || orderNo },
          { label: '调整类型', value: val(head, 'adjustment_type') },
          { label: '状态', value: val(head, 'order_status') },
          { label: '操作员ID', value: val(head, 'operator_id') },
          { label: '调整时间', value: util.formatDate(val(head, 'adjustment_time')) }
        ];
      } catch (e) { console.error('load adjust head:', e); }
      try {
        var details = await api.adjustmentOrderDetailApi.list();
        result.details = (details || []).filter(function(d) {
          return d.adjustment_no === orderNo;
        });
      } catch (e) { console.error('load adjust detail:', e); }
    }

    // 用货物名称和库位名称丰富明细
    result.details = this.enrichDetails(result.details, taskType);
    return result;
  },

  closeDetail: function() {
    this.setData({ showDetail: false, detailHead: null, detailList: [], detailLoading: false });
  },

  stopPropagation: function() {},

  filterList() {
    var _allTasks = this.data._allTasks;
    var filterType = this.data.filterType;
    var app = getApp();

    if (filterType === 'mine') {
      var uid = app.globalData.userInfo ? app.globalData.userInfo.user_id : null;
      var list = _allTasks.filter(function(t) {
        return t.assignee_id === uid && !t.completed_time;
      });
      this.setData({ taskList: list });
    } else {
      var pending = [];
      var done = [];
      for (var i = 0; i < _allTasks.length; i++) {
        var t = _allTasks[i];
        if (t.completed_time) {
          done.push(t);
        } else {
          pending.push(t);
        }
      }
      this.setData({ taskList: pending.concat(done) });
    }
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
