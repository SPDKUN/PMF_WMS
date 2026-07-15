<template>
  <div class="login-page">
    <!-- 左侧：装饰图 -->
    <div class="login-image-side">
      <span class="brand-tag">WMS预制菜仓储管理系统</span>
      <img src="/login_image.png" alt="warehouse" />
    </div>

    <!-- 左右交界渐变过渡 -->
    <div class="gradient-divider"></div>

    <!-- 右侧：登录表单区域 -->
    <div class="login-form-side">
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
          const userData = response.data
          if (userData && userData.status === 0) {
            this.errorMsg = '该账号已被停用，请联系管理员'
            this.loading = false
            return
          }
          localStorage.setItem('token', token)
          if (userData) {
            localStorage.setItem('userInfo', JSON.stringify(userData))
          }
          // 每次登录生成新会话ID，AI聊天记录按会话隔离
          const sessionId = Date.now().toString()
          localStorage.setItem('ai_session_id', sessionId)
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
  height: 100vh;
  overflow: hidden;
  position: relative;
}

/* ========== 左侧：装饰图 ========== */
.login-image-side {
  flex: 9;
  overflow: hidden;
  position: relative;
}

.login-image-side img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

/* 左上角品牌标识 */
.brand-tag {
  position: absolute;
  top: 28px;
  left: 36px;
  font-size: 15px;
  color: #10b981;
  font-weight: 600;
  letter-spacing: 1px;
  user-select: none;
  z-index: 3;
}

/* ========== 右侧：登录表单区域 ========== */
.login-form-side {
  flex: 7;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--primary-bg, #ecfdf5);
  min-width: 0;
}

/* ========== 登录卡片 ========== */
.login-card {
  width: 100%;
  max-width: 380px;
  padding: 44px 40px;
  background: transparent;
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
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  font-size: 14px;
  color: #303133;
  outline: none;
  box-sizing: border-box;
  transition: border-color 0.2s;
}

form .form-group input:focus {
  border-color: var(--primary-color, #409EFF);
}

form .form-group input::placeholder {
  color: #c0c4cc;
}

form .login-btn {
  width: 100%;
  padding: 12px;
  background: var(--primary-color, #10b981);
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
  letter-spacing: 4px;
  margin-top: 4px;
}

form .login-btn:hover:not(:disabled) {
  background: var(--primary-hover, #059669);
}

form .login-btn:disabled {
  background: #6ee7b7;
  cursor: not-allowed;
}

.error-msg {
  margin-top: 16px;
  padding: 10px 14px;
  background: #fef0f0;
  border: 1px solid #fde2e2;
  border-radius: 6px;
  color: #f56c6c;
  font-size: 13px;
  text-align: center;
}

/* ========== 交界渐变过渡 ========== */
.gradient-divider {
  position: absolute;
  top: 0;
  left: calc(56.25% - 60px);
  width: 60px;
  height: 100%;
  background: linear-gradient(to left, var(--primary-bg, #ecfdf5) 0%, transparent 100%);
  z-index: 2;
  pointer-events: none;
}

/* ========== 移动端适配 ========== */
@media (max-width: 768px) {
  .login-image-side {
    display: none;
  }

  .gradient-divider {
    display: none;
  }

  .login-form-side {
    flex: 1;
    padding: 0 24px;
  }

  .login-card {
    padding: 32px 24px;
  }
}
</style>
