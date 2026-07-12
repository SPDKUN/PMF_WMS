/**
 * 网络请求封装
 * 基于微信小程序的 wx.request API
 */

// 请求基础配置
const TIMEOUT = 15000

/**
 * 获取 Base URL（懒加载，避免模块加载时 getApp() 未就绪）
 */
function getBaseUrl() {
  try {
    const app = getApp()
    if (app && app.globalData && app.globalData.baseUrl) {
      return app.globalData.baseUrl
    }
  } catch (e) {
    // getApp() 可能在模块加载时不可用
  }
  return 'http://localhost:8088'
}

/**
 * 发起请求
 * @param {Object} options 请求配置
 * @returns {Promise}
 */
function request(options) {
  return new Promise((resolve, reject) => {
    const token = wx.getStorageSync('token')
    const header = {
      'Content-Type': 'application/json',
      ...options.header
    }

    // 自动添加 Authorization 头
    if (token) {
      header['Authorization'] = 'Bearer ' + token
    }

    wx.request({
      url: getBaseUrl() + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: header,
      timeout: options.timeout || TIMEOUT,
      success(res) {
        // HTTP 状态码检查
        if (res.statusCode === 200) {
          const result = res.data
          // 业务状态码检查
          if (result.code === 200) {
            resolve(result)
          } else if (result.code === 1002) {
            // Token 失效
            wx.removeStorageSync('token')
            wx.removeStorageSync('userInfo')
            wx.showToast({ title: '登录已过期，请重新登录', icon: 'none', duration: 2000 })
            setTimeout(() => {
              wx.redirectTo({ url: '/pages/login/login' })
            }, 2000)
            reject(result)
          } else {
            // 其他业务错误
            wx.showToast({ title: result.msg || '请求失败', icon: 'none', duration: 2000 })
            reject(result)
          }
        } else if (res.statusCode === 401) {
          wx.removeStorageSync('token')
          wx.removeStorageSync('userInfo')
          wx.showToast({ title: '请先登录', icon: 'none', duration: 2000 })
          setTimeout(() => {
            wx.redirectTo({ url: '/pages/login/login' })
          }, 2000)
          reject(res)
        } else {
          wx.showToast({ title: '服务器错误: ' + res.statusCode, icon: 'none', duration: 2000 })
          reject(res)
        }
      },
      fail(err) {
        wx.showToast({ title: '网络请求失败，请检查网络', icon: 'none', duration: 2000 })
        reject(err)
      }
    })
  })
}

/**
 * GET 请求
 */
function get(url, data = {}) {
  return request({ url, method: 'GET', data })
}

/**
 * POST 请求
 */
function post(url, data = {}) {
  return request({ url, method: 'POST', data })
}

/**
 * PUT 请求
 */
function put(url, data = {}) {
  return request({ url, method: 'PUT', data })
}

/**
 * DELETE 请求
 */
function del(url, data = {}) {
  return request({ url, method: 'DELETE', data })
}

module.exports = {
  request,
  get,
  post,
  put,
  del
}
