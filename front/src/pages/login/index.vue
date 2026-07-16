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
            <!-- 表单双向绑定 -->
            <input
              v-model="username" 
              type="text"
              placeholder="请输入用户名"
              required
            />
          </div>

          <div class="form-group">
            <label>密码</label>
            <!-- 表单双向绑定 -->
            <input
              v-model="password"
              type="password"
              placeholder="请输入密码"
              required
            />
          </div>

          <!-- 点击登录改为登录中...，增加反馈 -->
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
import request from '@/utils/request.js'
import { encrypt } from '@/utils/crypto.js'

export default {
  name: 'LoginView',
  data() {
    return {
      username: '',
      password: '',
      errorMsg: '',
      loading: false
    }
  },
  methods: {
    async handleLogin() {
      this.errorMsg = ''
      this.loading = true
      try {
        // AES 加密密码后通过 POST 发送
        const encryptedPwd = await encrypt(this.password)
        const response = await request.post('/auth/login', {
          username: this.username,
          password: encryptedPwd
        })

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
          const sessionId = Date.now().toString()
          localStorage.setItem('ai_session_id', sessionId)
          this.$router.push({ name: 'Home' })
        } else {
          this.errorMsg = '用户名密码错误'
        }
      } catch (error) {
        console.error('登录失败:', error)
        const msg = error?.response?.data?.msg || error?.message || '网络问题，请检查网络'
        this.errorMsg = msg
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

/* ========== 左侧：装饰区 ========== */
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
.brand-tag {
  position: absolute;
  top: 28px;
  left: 36px;
  font-size: 20px;
  color: var(--primary);
  font-weight: 700;
  letter-spacing: 1px;
  user-select: none;
  z-index: 3;
}

/* ========== 右侧：表单区域 ========== */
.login-form-side {
  flex: 7;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--primary-bg);
  min-width: 0;
}

.login-card {
  width: 100%;
  max-width: 400px;
  padding: 48px 44px;
  background: var(--card);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-float);
}

.card-header {
  text-align: center;
  margin-bottom: 36px;
}
.card-header .title {
  font-size: 26px;
  font-weight: 700;
  color: var(--foreground);
  margin: 0 0 8px 0;
  letter-spacing: -0.3px;
}
.card-header .subtitle {
  font-size: 14px;
  color: var(--foreground-muted);
  margin: 0;
}

form .form-group {
  margin-bottom: 20px;
}
form .form-group label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: var(--foreground-regular);
  margin-bottom: 6px;
}
form .form-group input {
  width: 100%;
  padding: 11px 14px;
  border: 1px solid var(--input-border);
  border-radius: var(--radius-md);
  font-size: 14px;
  color: var(--foreground);
  outline: none;
  box-sizing: border-box;
  transition: all 0.2s ease;
  background: var(--card);
}
form .form-group input:focus {
  border-color: var(--primary);
  box-shadow: 0 0 0 2px hsl(var(--primary-h), var(--primary-s), 60% / 20%);
}
form .form-group input::placeholder { color: var(--foreground-placeholder); }

form .login-btn {
  width: 100%;
  padding: 12px;
  background: var(--primary);
  color: var(--text-on-primary);
  border: none;
  border-radius: var(--radius-md);
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  letter-spacing: 4px;
  margin-top: 8px;
}
form .login-btn:hover:not(:disabled) {
  background: var(--primary-hover);
  box-shadow: var(--shadow-md);
  transform: translateY(-1px);
}
form .login-btn:disabled {
  background: var(--primary-disabled);
  cursor: not-allowed;
  transform: none;
}

.error-msg {
  margin-top: 16px;
  padding: 10px 14px;
  background: var(--danger-bg);
  border: 1px solid var(--danger-bg);
  border-radius: var(--radius-md);
  color: var(--danger);
  font-size: 13px;
  text-align: center;
}

/* 交界渐变 */
.gradient-divider {
  position: absolute;
  top: 0;
  left: calc(56.25% - 80px);
  width: 80px;
  height: 100%;
  background: linear-gradient(to left, var(--primary-bg) 0%, transparent 100%);
  z-index: 2;
  pointer-events: none;
}

@media (max-width: 768px) {
  .login-image-side, .gradient-divider { display: none; }
  .login-form-side { flex: 1; padding: 0 24px; }
  .login-card { padding: 36px 28px; box-shadow: none; }
}
</style>
