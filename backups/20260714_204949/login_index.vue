<template>
  <div class="login-page">
    <div class="login-card">
      <div class="card-header">
        <h1 class="title">预制菜 WMS</h1>
        <p class="subtitle">智能仓储管理系统</p>
      </div>

      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label>用户名</label>
          <input
            v-model="username"
            type="text"
            placeholder="请输入用户名"
            required
          />
        </div>

        <div class="form-group">
          <label>密码</label>
          <input
            v-model="password"
            type="password"
            placeholder="请输入密码"
            required
          />
        </div>

        <button type="submit" class="login-btn" :disabled="loading">
          <span v-if="!loading">登 录</span>
          <span v-else>登录中...</span>
        </button>
      </form>

      <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

import request from '@/utils/request.js'

export default {
  name: 'LoginView',
  // 定义数据
  data() {
    return {
      username: '',
      password: '',
      errorMsg: '',
      loading: false
    }
  },

  //生命周期函数，不用调用，时间一到自动执行
  //例如在页面加载之前先从后台访问数据
  // mounted() {

  // },

  // 定义方法
  methods: {
    async handleLogin() {
      this.errorMsg = ''
      this.loading = true
      try {

        // 1. 先打印 request 对象，确保导入正确
    console.log('request 对象：', request)

        const response = await request.get('/auth/login', {
          username: this.username,
          password: this.password
        })

        // 3. 打印 response 实际值
    console.log('response 实际值：', response)

        const token = response.token
        if (token) {
          localStorage.setItem('token', token)
          // 存储用户基本信息
          const userData = response.data
          if (userData) {
            localStorage.setItem('userInfo', JSON.stringify(userData))
          }
          this.$router.push({ name: 'Home' })
        } else {
          this.errorMsg = '登录失败，未获取到令牌'
        }
      } catch (error) {
        console.error('登录请求出错：', error)
        this.errorMsg = '网络问题或者用户名密码错误'
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: #f5f7fa;
}

.login-card {
  width: 100%;
  max-width: 400px;
  padding: 40px 36px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.card-header {
  text-align: center;
  margin-bottom: 32px;
}

.card-header .title {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 6px 0;
}

.card-header .subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

form .form-group {
  margin-bottom: 18px;
}

form .form-group label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: #606266;
  margin-bottom: 6px;
}

form .form-group input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  color: #303133;
  outline: none;
  box-sizing: border-box;
  transition: border-color 0.2s;
}

form .form-group input:focus {
  border-color: #409EFF;
}

form .form-group input::placeholder {
  color: #c0c4cc;
}

form .form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

form .form-options .remember-me {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #909399;
  cursor: pointer;
}

form .form-options .remember-me input[type="checkbox"] {
  width: 16px;
  height: 16px;
  cursor: pointer;
}

form .login-btn {
  width: 100%;
  padding: 12px;
  background: #409EFF;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
  letter-spacing: 4px;
}

form .login-btn:hover:not(:disabled) {
  background: #66b1ff;
}

form .login-btn:disabled {
  background: #a0cfff;
  cursor: not-allowed;
}

.error-msg {
  margin-top: 16px;
  padding: 10px 14px;
  background: #fef0f0;
  border: 1px solid #fde2e2;
  border-radius: 4px;
  color: #f56c6c;
  font-size: 13px;
}

@media (max-width: 480px) {
  .login-card {
    margin: 0 20px;
    padding: 28px 20px;
  }

  .card-header .title {
    font-size: 22px;
  }
}
</style>
