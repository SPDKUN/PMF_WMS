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
    detailLoading: false
  },

  onLoad() { this.loadData(); },

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

  // 获取详情配置
  getDetailConfig: function(taskType) {
    var configs = {
      '入库': {
        title: '入库单详情',
        columns: [
          { key: 'goods_name', label: '货物名称' },
          { key: 'batch_id', label: '批次号' },
          { key: 'quantity', label: '数量' },
          { key: 'location_name', label: '库位' }
        ]
      },
      '出库': {
        title: '出库单详情',
        columns: [
          { key: 'goods_name', label: '货物名称' },
          { key: 'batch_id', label: '批次号' },
          { key: 'quantity', label: '数量' },
          { key: 'location_name', label: '库位' }
        ]
      },
      '质检': {
        title: '质检单详情',
        columns: [
          { key: 'batch_id', label: '批次号' },
          { key: 'goods_name', label: '货物名称' },
          { key: 'inspection_result', label: '检验结果' },
          { key: 'defect_reason', label: '不合格原因' }
        ]
      },
      '库存盘点': {
        title: '库存盘点单详情',
        columns: [
          { key: 'goods_name', label: '货物名称' },
          { key: 'location_name', label: '库位' },
          { key: 'batch_id', label: '批次号' },
          { key: 'book_quantity', label: '账面数量' },
          { key: 'actual_quantity', label: '实盘数量' }
        ]
      },
      '库存调整': {
        title: '库存调整单详情',
        columns: [
          { key: 'batch_id', label: '批次号' },
          { key: 'goods_name', label: '货物名称' },
          { key: 'quantity', label: '数量' },
          { key: 'from_location', label: '原库位' },
          { key: 'to_location', label: '新库位' }
        ]
      }
    };
    return configs[taskType] || null;
  },

  // 加载头部和明细数据
  loadDetailData: async function(taskType, orderNo, task) {
    var result = { head: {}, headFields: [], details: [] };

    if (taskType === '入库') {
      // 入库单头
      try {
        var head = await api.inboundApi.getById(orderNo);
        result.head = head;
        result.headFields = [
          { label: '入库单号', value: head.inbound_order_no || orderNo },
          { label: '入库类型', value: head.inbound_type || '-' },
          { label: '批次号', value: head.batch_id || '-' },
          { label: '状态', value: head.status || '-' },
          { label: '创建时间', value: util.formatDate(head.created_time) }
        ];
      } catch (e) { /* ignore */ }

      // 入库明细
      try {
        var details = await api.inboundDetailApi.list();
        var filtered = (details || []).filter(function(d) {
          return d.inbound_order_no === orderNo;
        });
        result.details = filtered;
      } catch (e) { /* ignore */ }
    }
    else if (taskType === '出库') {
      try {
        var head = await api.outboundApi.getById(orderNo);
        result.head = head;
        result.headFields = [
          { label: '出库单号', value: head.outbound_order_no || orderNo },
          { label: '货物名称', value: head.goods_name || '-' },
          { label: '状态', value: head.status || '-' },
          { label: '创建时间', value: util.formatDate(head.created_time) }
        ];
      } catch (e) { /* ignore */ }

      try {
        var details = await api.outboundDetailApi.list();
        var filtered = (details || []).filter(function(d) {
          return d.outbound_order_no === orderNo;
        });
        result.details = filtered;
      } catch (e) { /* ignore */ }
    }
    else if (taskType === '质检') {
      try {
        var head = await api.qualityApi.getById(orderNo);
        result.head = head;
        result.headFields = [
          { label: '质检单号', value: head.quality_check_no || orderNo },
          { label: '质检类型', value: head.check_type || '-' },
          { label: '批次号', value: head.batch_id || '-' },
          { label: '状态', value: head.status || '-' },
          { label: '创建时间', value: util.formatDate(head.created_time) }
        ];
      } catch (e) { /* ignore */ }

      try {
        var details = await api.qualityDetailApi.list();
        var filtered = (details || []).filter(function(d) {
          return d.quality_check_no === orderNo;
        });
        // enrich with goods name if possible
        result.details = filtered;
      } catch (e) { /* ignore */ }
    }
    else if (taskType === '库存盘点') {
      try {
        var head = await api.inventoryCheckHeadApi.getById(orderNo);
        result.head = head;
        result.headFields = [
          { label: '盘点单号', value: head.check_no || orderNo },
          { label: '盘点类型', value: head.check_type || '-' },
          { label: '盘点范围', value: head.scope_value || '-' },
          { label: '状态', value: head.status || '-' },
          { label: '创建时间', value: util.formatDate(head.created_time) }
        ];
      } catch (e) { /* ignore */ }

      try {
        var details = await api.inventoryCheckDetailApi.list();
        var filtered = (details || []).filter(function(d) {
          return d.check_no === orderNo;
        });
        result.details = filtered;
      } catch (e) { /* ignore */ }
    }
    else if (taskType === '库存调整') {
      try {
        var head = await api.adjustmentOrderHeadApi.getById(orderNo);
        result.head = head;
        result.headFields = [
          { label: '调整单号', value: head.adjustment_order_no || orderNo },
          { label: '状态', value: head.status || '-' },
          { label: '创建时间', value: util.formatDate(head.created_time) }
        ];
      } catch (e) { /* ignore */ }

      try {
        var details = await api.adjustmentOrderDetailApi.list();
        var filtered = (details || []).filter(function(d) {
          return d.adjustment_order_no === orderNo;
        });
        result.details = filtered;
      } catch (e) { /* ignore */ }
    }

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
