const app = getApp()

var request = function(url, method, data) {
  if (!method) method = 'GET';
  if (!data) data = {};
  // Auto cache-bust for GET requests
  if (method === 'GET') {
    var sep = url.indexOf('?') >= 0 ? '&' : '?';
    url += sep + '_t=' + Date.now();
  }
  return new Promise(function(resolve, reject) {
    wx.request({
      url: app.globalData.baseUrl + url,
      method: method,
      data: data,
      header: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + app.globalData.token
      },
      success: function(res) {
        if (res.data.code === 200) {
          resolve(res.data.data);
        } else {
          wx.showToast({ title: res.data.msg || '请求失败', icon: 'none' });
          reject(res.data);
        }
      },
      fail: function(err) {
        wx.showToast({ title: '网络错误', icon: 'none' });
        reject(err);
      }
    });
  });
};

var login = function(username, password) {
  return request('/auth/login?username=' + username + '&password=' + password);
};

var goodsApi = {
  list: function() { return request('/goods/list'); },
  getById: function(id) { return request('/goods/' + id); },
  save: function(data) { return request('/goods', 'POST', data); },
  update: function(data) { return request('/goods', 'PUT', data); },
  del: function(id) { return request('/goods/' + id, 'DELETE'); }
};

var inventoryApi = {
  list: function() { return request('/inventory/list'); },
  listWithDetails: function(batchId) {
    return request('/inventory/listWithDetails' + (batchId ? '?batchId=' + batchId : ''));
  },
  getById: function(id) { return request('/inventory/' + id); }
};

var inboundDetailApi = {
  list: function() { return request('/inboundOrderDetail/list'); },
  getById: function(id) { return request('/inboundOrderDetail/' + id); },
  save: function(data) { return request('/inboundOrderDetail', 'POST', data); },
  update: function(data) { return request('/inboundOrderDetail', 'PUT', data); }
};

var warehouseApi = {
  list: function() { return request('/warehouse/list'); },
  getById: function(id) { return request('/warehouse/' + id); }
};

var zoneApi = {
  list: function() { return request('/zone/list'); }
};

var locationApi = {
  list: function() { return request('/location/list'); }
};

var inboundApi = {
  list: function() { return request('/inboundOrderHead/list'); },
  getById: function(no) { return request('/inboundOrderHead/' + no); },
  save: function(data) { return request('/inboundOrderHead', 'POST', data); },
  update: function(data) { return request('/inboundOrderHead', 'PUT', data); },
  del: function(no) { return request('/inboundOrderHead/' + no, 'DELETE'); },
  availableBatches: function() { return request('/inbound/availableBatches'); },
  create: function(data) { return request('/inbound/create', 'POST', data); },
  complete: function(taskId, data) { return request('/inbound/complete/' + taskId, 'PUT', data); },
  reject: function(taskId, data) { return request('/inbound/reject/' + taskId, 'PUT', data); }
};

var outboundDetailApi = {
  list: function() { return request('/outboundOrderDetail/list'); },
  getById: function(id) { return request('/outboundOrderDetail/' + id); }
};

var outboundApi = {
  list: function() { return request('/outboundOrderHead/list'); },
  getById: function(no) { return request('/outboundOrderHead/' + no); },
  create: function(data) { return request('/outbound/create', 'POST', data); },
  update: function(data) { return request('/outboundOrderHead', 'PUT', data); },
  complete: function(taskId, data) { return request('/outbound/complete/' + taskId, 'PUT', data); },
  reject: function(taskId, data) { return request('/outbound/reject/' + taskId, 'PUT', data); }
};

var taskApi = {
  list: function() { return request('/workTask/list'); },
  myTasks: function(userId) { return request('/workTask/myTasks?assigneeId=' + userId); }
};

var batchApi = {
  list: function() { return request('/batch/list'); },
  save: function(data) { return request('/batch', 'POST', data); }
};

var qualityDetailApi = {
  list: function() { return request('/qualityCheckDetail/list'); }
};

var qualityApi = {
  list: function() { return request('/qualityCheckHead/list'); },
  getById: function(no) { return request('/qualityCheckHead/' + no); },
  create: function(data) { return request('/qualityCheck/create', 'POST', data); },
  complete: function(taskId, data) { return request('/qualityCheck/complete/' + taskId, 'PUT', data); },
  myTasks: function(userId) { return request('/qualityCheck/myTasks?assigneeId=' + userId); }
};

var envApi = {
  list: function() { return request('/temperatureHumidityRecord/list'); }
};

var userApi = {
  list: function() { return request('/user/personnel'); }
};

var warehouseStaffApi = {
  list: function() { return request('/user/warehouseStaff'); }
};


var aiApi = {
  chat: function(q, h) { return request('/ai/chat', 'POST', {question:q, history:h||[]}); },
  homeStats: function(a) { return request('/ai/homeStats' + (a ? '?assigneeId='+a : '')); }
};

module.exports = {
  request: request,
  login: login,
  goodsApi: goodsApi,
  inventoryApi: inventoryApi,
  warehouseApi: warehouseApi,
  zoneApi: zoneApi,
  locationApi: locationApi,
  inboundApi: inboundApi,
  inboundDetailApi: inboundDetailApi,
  outboundDetailApi: outboundDetailApi,
  outboundApi: outboundApi,
  taskApi: taskApi,
  batchApi: batchApi,
  qualityDetailApi: qualityDetailApi,
  qualityApi: qualityApi,
  envApi: envApi,
  userApi: userApi,
  warehouseStaffApi: warehouseStaffApi,
  aiApi: aiApi
};