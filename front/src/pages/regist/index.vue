<template>
  <div class="regist-page">
    <div class="regist-card">
      <div class="card-header">
        <h1 class="title">创建账号</h1>
        <p class="subtitle">注册加入预制菜WMS系统</p>
      </div>

      <form @submit.prevent="handleRegist">
        <div class="form-group">
          <label>用户名</label>
          <input v-model="username" type="text" placeholder="请输入用户名" required />
        </div>

        <div class="form-group">
          <label>姓名</label>
          <input v-model="realName" type="text" placeholder="请输入真实姓名" required />
        </div>

        <div class="form-group">
          <label>手机号</label>
          <input v-model="phone" type="text" placeholder="请输入手机号" required />
        </div>

        <div class="form-group">
          <label>密码</label>
          <input v-model="password" type="password" placeholder="请输入密码" required />
        </div>

        <div class="form-group">
          <label>确认密码</label>
          <input v-model="confirmPassword" type="password" placeholder="请再次输入密码" required />
        </div>

        <button type="submit" class="regist-btn">注 册</button>
      </form>

      <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>

      <p class="login-link">
        已有账号？<router-link to="/login">立即登录</router-link>
      </p>
    </div>
  </div>
</template>

<script>
import request from '@/utils/request.js'

export default {
  name: 'RegistView',
  data() {
    return {
      username: '',
      realName: '',
      phone: '',
      password: '',
      confirmPassword: '',
      errorMsg: ''
    }
  },
  methods: {
    async handleRegist() {
      this.errorMsg = ''
      if (this.password !== this.confirmPassword) {
        this.errorMsg = '两次密码输入不一致'
        return
      }
      try {
        const response = await request.post('/user', {
          username: this.username,
          real_name: this.realName,
          phone: this.phone,
          password: this.password
        })
        if (response.code === 200) {
          this.$router.push({ name: 'Login' })
        } else {
          this.errorMsg = response.msg || '注册失败'
        }
      } catch (error) {
        this.errorMsg = '网络异常，请稍后重试'
      }
    }
  }
}
</script>

<style scoped>
.regist-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: var(--page-bg);
}

.regist-card {
  width: 100%;
  max-width: 440px;
  padding: 40px 40px;
  background: var(--card);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-float);
}

.card-header {
  text-align: center;
  margin-bottom: 28px;
}
.card-header .title {
  font-size: 24px;
  font-weight: 700;
  color: var(--foreground);
  margin: 0 0 6px 0;
  letter-spacing: -0.3px;
}
.card-header .subtitle {
  font-size: 14px;
  color: var(--foreground-muted);
  margin: 0;
}

form .form-group { margin-bottom: 18px; }
form .form-group label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: var(--foreground-regular);
  margin-bottom: 6px;
}
form .form-group input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--input-border);
  border-radius: var(--radius-md);
  font-size: 14px;
  color: var(--foreground);
  outline: none;
  box-sizing: border-box;
  transition: all 0.2s ease;
}
form .form-group input:focus {
  border-color: var(--primary);
  box-shadow: 0 0 0 2px hsl(var(--primary-h), var(--primary-s), 60% / 20%);
}
form .form-group input::placeholder { color: var(--foreground-placeholder); }

.regist-btn {
  width: 100%;
  padding: 12px;
  background: var(--primary);
  color: #fff;
  border: none;
  border-radius: var(--radius-md);
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  letter-spacing: 4px;
  margin-top: 4px;
}
.regist-btn:hover {
  background: var(--primary-hover);
  box-shadow: var(--shadow-md);
}

.error-msg {
  margin-top: 16px;
  padding: 10px 14px;
  background: var(--danger-bg);
  border: 1px solid hsl(5, 80%, 85%);
  border-radius: var(--radius-md);
  color: var(--danger);
  font-size: 13px;
  text-align: center;
}

.login-link {
  margin-top: 16px;
  margin-bottom: 0;
  text-align: center;
  font-size: 13px;
  color: var(--foreground-muted);
}
.login-link a {
  color: var(--primary);
  text-decoration: none;
  font-weight: 500;
}
.login-link a:hover { text-decoration: underline; }
</style>
