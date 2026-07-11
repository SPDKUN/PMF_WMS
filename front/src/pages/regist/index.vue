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
import axios from 'axios'

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
        const response = await axios.post('http://localhost:8088/user', {
          username: this.username,
          real_name: this.realName,
          phone: this.phone,
          password: this.password
        })
        if (response.data.code === 200) {
          this.$router.push({ name: 'Login' })
        } else {
          this.errorMsg = response.data.msg || '注册失败'
        }
      } catch (error) {
        console.error('注册请求出错：', error)
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
  background: #f5f7fa;
}

.regist-card {
  width: 100%;
  max-width: 420px;
  padding: 36px 36px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.card-header {
  text-align: center;
  margin-bottom: 28px;
}

.card-header .title {
  font-size: 22px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 6px 0;
}

.card-header .subtitle {
  font-size: 13px;
  color: #909399;
  margin: 0;
}

form .form-group {
  margin-bottom: 16px;
}

form .form-group label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: #606266;
  margin-bottom: 4px;
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

form .regist-btn {
  width: 100%;
  margin-top: 8px;
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

form .regist-btn:hover {
  background: #66b1ff;
}

.error-msg {
  margin-top: 14px;
  padding: 10px 14px;
  background: #fef0f0;
  border: 1px solid #fde2e2;
  border-radius: 4px;
  color: #f56c6c;
  font-size: 13px;
}

.login-link {
  margin-top: 20px;
  text-align: center;
  font-size: 13px;
  color: #909399;
}

.login-link a {
  color: #409EFF;
  text-decoration: none;
}

.login-link a:hover {
  color: #66b1ff;
}

@media (max-width: 480px) {
  .regist-card {
    margin: 0 20px;
    padding: 28px 20px;
  }

  .card-header .title {
    font-size: 20px;
  }
}
</style>
