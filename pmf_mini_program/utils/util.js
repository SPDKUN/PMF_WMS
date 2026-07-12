/**
 * 工具函数
 */

/**
 * 格式化时间
 * @param {Date|string|number} date 日期
 * @param {string} format 格式（默认 'YYYY-MM-DD HH:mm:ss'）
 */
function formatTime(date, format) {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = d.getMonth() + 1
  const day = d.getDate()
  const hour = d.getHours()
  const minute = d.getMinutes()
  const second = d.getSeconds()

  format = format || 'YYYY-MM-DD HH:mm:ss'

  return format
    .replace('YYYY', year)
    .replace('MM', padZero(month))
    .replace('DD', padZero(day))
    .replace('HH', padZero(hour))
    .replace('mm', padZero(minute))
    .replace('ss', padZero(second))
}

/**
 * 格式化日期
 */
function formatDate(date) {
  return formatTime(date, 'YYYY-MM-DD')
}

/**
 * 格式化数字（保留旧版兼容）
 */
function formatNumber(n) {
  n = n.toString()
  return n[1] ? n : '0' + n
}

/**
 * 补零
 */
function padZero(n) {
  n = n.toString()
  return n[1] ? n : '0' + n
}

/**
 * 显示加载提示
 */
function showLoading(title) {
  wx.showLoading({
    title: title || '加载中...',
    mask: true
  })
}

/**
 * 隐藏加载提示
 */
function hideLoading() {
  wx.hideLoading()
}

/**
 * 显示消息提示
 */
function showToast(title, icon) {
  wx.showToast({
    title: title || '',
    icon: icon || 'none',
    duration: 2000
  })
}

/**
 * 显示确认对话框
 */
function showConfirm(title, content) {
  return new Promise((resolve) => {
    wx.showModal({
      title: title || '提示',
      content: content || '',
      success(res) {
        resolve(res.confirm)
      }
    })
  })
}

module.exports = {
  formatTime,
  formatDate,
  formatNumber,
  padZero,
  showLoading,
  hideLoading,
  showToast,
  showConfirm
}
