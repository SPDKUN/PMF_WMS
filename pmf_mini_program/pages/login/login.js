var app = getApp();
var crypto = require('../../utils/crypto');

Page({
  data: {
    username: '',
    password: '',
    canLogin: false
  },

  onUsernameInput(e) {
    var username = e.detail.value;
    this.setData({ username: username });
    this.checkCanLogin();
  },

  onPasswordInput(e) {
    var password = e.detail.value;
    this.setData({ password: password });
    this.checkCanLogin();
  },

  checkCanLogin() {
    this.setData({
      canLogin: this.data.username.length > 0 && this.data.password.length > 0
    });
  },

  async handleLogin() {
    if (!this.data.canLogin) {
      wx.showToast({ title: '请输入用户名和密码', icon: 'none' });
      return;
    }
    wx.showLoading({ title: '登录中...', mask: true });

    try {
      // AES-256-CBC 加密密码（与前端 crypto.js 一致）
      var encryptedPwd = crypto.encrypt(this.data.password);

      var res = await new Promise(function(resolve, reject) {
        wx.request({
          url: app.globalData.baseUrl + '/auth/login',
          method: 'POST',
          header: { 'Content-Type': 'application/json' },
          data: {
            username: this.data.username,
            password: encryptedPwd
          },
          success: function(r) { resolve(r); },
          fail: function(e) { reject(e); }
        });
      }.bind(this));

      wx.hideLoading();
      if (res.data && res.data.code === 200) {
        var userInfo = res.data.data;
        var token = res.data.token;

        app.globalData.userInfo = userInfo;
        app.globalData.token = token;

        wx.setStorageSync('userInfo', JSON.stringify(userInfo));
        wx.setStorageSync('token', token);
        app.globalData.justLoggedIn = true;

        wx.showToast({ title: '登录成功', icon: 'success', duration: 1000 });
        setTimeout(function() {
          wx.reLaunch({ url: '/pages/home/home' });
        }, 1000);
      } else {
        wx.showToast({ title: res.data && res.data.msg ? res.data.msg : '登录失败', icon: 'none' });
      }
    } catch (e) {
      wx.hideLoading();
      wx.showToast({ title: '网络连接失败', icon: 'none' });
    }
  }
});
