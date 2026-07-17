var app = getApp();
var api = require('../../utils/api');
var util = require('../../utils/util');

Page({
  data: {
    navBarHeight: 44,
    userInfo: {},
    currentDate: '',
    stats: [
      { id: 1, icon: '📦', label: '货物总数', count: 0, bgColor: '#EFF6FF' },
      { id: 2, icon: '🏭', label: '库房数', count: 0, bgColor: '#F0FDF4' },
      { id: 3, icon: '📋', label: '库存量', count: 0, bgColor: '#FFF7ED' },
      { id: 4, icon: '📌', label: '待办任务', count: 0, bgColor: '#FEF2F2' }
    ],
    quickActions: [
      { id: 1, icon: '🏷️', label: '批次管理', page: '/pages/batch/batch', bgColor: '#E0E7FF' },
      { id: 2, icon: '📥', label: '入库管理', page: '/pages/inbound/inbound', bgColor: '#F0FDF4' },
      { id: 3, icon: '📤', label: '出库管理', page: '/pages/outbound/outbound', bgColor: '#FFF7ED' },
      { id: 4, icon: '📦', label: '货物管理', page: '/pages/goods/goods', bgColor: '#EFF6FF' },
      { id: 5, icon: '✅', label: '质检管理', page: '/pages/quality/quality', bgColor: '#FEF9C3' },
      { id: 6, icon: '📊', label: '数据看板', page: '/pages/dashboard/dashboard', bgColor: '#F3E8FF' },
      { id: 7, icon: '🌡️', label: '温湿度上传', page: '/pages/env-monitor/env-monitor', bgColor: '#E0F2FE', needPerm: true },
      { id: 8, icon: '👤', label: '个人信息', page: '/pages/profile/profile', bgColor: '#FCE7F3', isTab: true }
    ],
    recentTasks: [],
    showNotifications: false,
    notifications: [],
    // 权限弹窗
    showPermModal: false,
    permModalMsg: ''
  },

  onLoad: function() {
    var sysInfo = wx.getSystemInfoSync();
    this.setData({ navBarHeight: sysInfo.statusBarHeight, currentDate: this.getCurrentDate() });
  },

  onShow: function() {
    if (!app.globalData.token) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }
    this.setData({ userInfo: app.globalData.userInfo || {} });
    var self = this;
    this.loadData(function() {
      if (app.globalData.justLoggedIn) {
        app.globalData.justLoggedIn = false;
        var tasks = self.data.recentTasks || [];
        if (tasks.length > 0) self.showNotifications();
      }
    });
  },

  onPullDownRefresh: function() { this.loadData(); },

  getCurrentDate: function() {
    var now = new Date();
    var weekDays = ['日', '一', '二', '三', '四', '五', '六'];
    var m = ('0' + (now.getMonth() + 1)).slice(-2);
    var d = ('0' + now.getDate()).slice(-2);
    return now.getFullYear() + '-' + m + '-' + d + ' 星期' + weekDays[now.getDay()];
  },

  loadData: function(callback) {
    var self = this;
    wx.showNavigationBarLoading();
    Promise.all([
      api.goodsApi.list().catch(function() { return []; }),
      api.warehouseApi.list().catch(function() { return []; }),
      api.inventoryApi.list().catch(function() { return []; }),
      api.taskApi.list().catch(function() { return []; })
    ]).then(function(results) {
      var goods = results[0] || [];
      var warehouses = results[1] || [];
      var inventory = results[2] || [];
      var tasks = results[3] || [];

      // 🔴 修复: 先获取 stats 引用，再修改
      var stats = self.data.stats;
      stats[0].count = goods.length || 0;
      stats[1].count = warehouses.length || 0;
      var totalQty = 0;
      for (var i = 0; i < inventory.length; i++) {
        totalQty += (inventory[i].quantity || 0);
      }
      stats[2].count = totalQty || 0;

      var uid = app.globalData.userInfo ? app.globalData.userInfo.user_id : null;
      var pendingCount = 0;
      var recent = [];
      for (var j = 0; j < tasks.length; j++) {
        var t = tasks[j];
        if (t.assignee_id !== uid) continue;
        var isCompleted = t.completed_time !== null && t.completed_time !== undefined && t.completed_time !== '';
        var statusField = t.task_status || t.status || '';
        var isPending = !isCompleted && statusField !== '已完成' && statusField !== '已关闭';
        if (isPending) {
          pendingCount++;
          if (recent.length < 5) {
            recent.push({
              task_id: t.task_id,
              task_type: t.task_type || '-',
              priority: t.priority || '中',
              related_order_no: t.related_order_no || '-',
              deadline: t.deadline ? util.formatDate(t.deadline) : '-',
              displayStatus: '待处理'
            });
          }
        }
      }
      stats[3].count = pendingCount;

      self.setData({ stats: stats, recentTasks: recent });
      wx.hideNavigationBarLoading();
      wx.stopPullDownRefresh();
      if (callback) callback();
    }).catch(function(e) {
      console.error(e);
      wx.hideNavigationBarLoading();
      wx.stopPullDownRefresh();
      if (callback) callback();
    });
  },

  toggleNotifications: function() {
    this.setData({ showNotifications: !this.data.showNotifications });
    if (this.data.showNotifications) this.showNotificationsContent();
  },

  showNotifications: function() {
    this.setData({ showNotifications: true });
    this.showNotificationsContent();
  },

  showNotificationsContent: function() {
    var tasks = this.data.recentTasks || [];
    var notifs = [];
    for (var i = 0; i < tasks.length; i++) {
      notifs.push({
        icon: tasks[i].task_type === '入库' ? '📥' : tasks[i].task_type === '出库' ? '📤' : '📋',
        title: tasks[i].task_type + '任务',
        desc: '单号: ' + (tasks[i].related_order_no || '-') + ' | 截止: ' + (tasks[i].deadline || '-'),
        time: tasks[i].deadline || '-'
      });
    }
    if (notifs.length === 0) notifs.push({ icon: '✅', title: '暂无通知', desc: '所有待办任务已完成', time: '' });
    this.setData({ notifications: notifs });
  },

  openAiAssistant: function() {
    var saved = getApp().globalData.chatHistory || [];
    if (saved.length === 0) saved = [{ role: 'ai', text: '你好！我是仓储管理AI助手，有什么可以帮您的吗？' }];
    this.setData({ showAiChat: true, aiMessages: saved, aiInput: '', aiLoading: false });
  },
  closeAiChat: function() {
    var msgs = this.data.aiMessages;
    if (msgs.length > 50) msgs = msgs.slice(-50);
    getApp().globalData.chatHistory = msgs;
    this.setData({ showAiChat: false });
  },
  onAiInput: function(e) { this.setData({ aiInput: e.detail.value }); },

  sendAiMessage: function() {
    var self = this;
    var question = this.data.aiInput.trim();
    if (!question || this.data.aiLoading) return;
    var msgs = this.data.aiMessages;
    msgs.push({ role: 'user', text: question });
    this.setData({ aiMessages: msgs, aiInput: '', aiLoading: true });
    api.aiApi.chat(question, []).then(function(reply) {
      msgs.push({ role: 'ai', text: reply || '暂无回复' });
      self.setData({ aiMessages: msgs, aiLoading: false });
      if (msgs.length > 50) msgs = msgs.slice(-50);
      getApp().globalData.chatHistory = msgs;
    }).catch(function() {
      msgs.push({ role: 'ai', text: '网络异常，请稍后重试' });
      self.setData({ aiMessages: msgs, aiLoading: false });
      if (msgs.length > 50) msgs = msgs.slice(-50);
      getApp().globalData.chatHistory = msgs;
    });
  },

  stopPropagation: function() {},
  onQuickAction: function(e) {
    var page = e.currentTarget.dataset.page;
    var needPerm = e.currentTarget.dataset.needPerm;
    var isTab = e.currentTarget.dataset.isTab;
    if (!page) return;
    // 温湿度上传 - 权限检查：仅质检员和温度检测员可访问
    if (needPerm) {
      var userInfo = app.globalData.userInfo || {};
      var position = userInfo.position || '';
      if (position !== '质检员' && position !== '温度检测员') {
        this.setData({ showPermModal: true, permModalMsg: '仅质检员和温度检测员可上传温湿度' });
        return;
      }
    }
    // tabBar 页面必须用 switchTab，且 switchTab 不支持 URL 参数
    if (isTab) {
      // 个人信息：设置全局标记让 profile 页自动打开弹窗
      if (page === '/pages/profile/profile') {
        app.globalData.showProfileInfo = true;
      }
      wx.switchTab({ url: page });
    } else {
      wx.navigateTo({ url: page });
    }
  },
  closePermModal: function() {
    this.setData({ showPermModal: false });
  },
  goTasks: function() { wx.switchTab({ url: '/pages/tasks/tasks' }); },
  onStatTap: function(e) {
    var index = e.currentTarget.dataset.index;
    var stat = this.data.stats[index];
    wx.showToast({ title: stat.label + ': ' + stat.count, icon: 'none' });
  }
});
