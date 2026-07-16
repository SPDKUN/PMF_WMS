function formatDate(dateStr) {
  if (!dateStr) return '-';
  var d = new Date(dateStr);
  var y = d.getFullYear();
  var m = ('0' + (d.getMonth() + 1)).slice(-2);
  var day = ('0' + d.getDate()).slice(-2);
  var h = ('0' + d.getHours()).slice(-2);
  var min = ('0' + d.getMinutes()).slice(-2);
  return y + '-' + m + '-' + day + ' ' + h + ':' + min;
}

function formatDateShort(dateStr) {
  if (!dateStr) return '-';
  var d = new Date(dateStr);
  var m = ('0' + (d.getMonth() + 1)).slice(-2);
  var day = ('0' + d.getDate()).slice(-2);
  return m + '-' + day;
}

function getStatusTag(status) {
  var map = {
    '正常': 'tag-success',
    '待检': 'tag-warning',
    '锁定': 'tag-danger',
    '报废': 'tag-danger',
    '草稿': 'tag-default',
    '已审核': 'tag-info',
    '已完成': 'tag-success',
    '待入库': 'tag-warning',
    '待出库': 'tag-info',
    '待报废': 'tag-danger',
    '进行中': 'tag-warning',
    '待处理': 'tag-warning',
    '启用': 'tag-success',
    '停用': 'tag-danger'
  };
  return map[status] || 'tag-default';
}

function showToast(title, icon) {
  if (!icon) icon = 'none';
  wx.showToast({ title: title, icon: icon, duration: 2000 });
}

function showSuccess(title) {
  wx.showToast({ title: title, icon: 'success', duration: 1500 });
}

module.exports = {
  formatDate: formatDate,
  formatDateShort: formatDateShort,
  getStatusTag: getStatusTag,
  showToast: showToast,
  showSuccess: showSuccess
};