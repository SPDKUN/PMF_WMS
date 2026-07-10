<template>
  <div class="regist-page">
    <canvas id="particles-canvas"></canvas>

    <div class="regist-card">
      <div class="card-header">
        <div class="logo-icon">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M20 7L12 3L4 7M20 7L12 11M20 7V17L12 21M12 11L4 7M12 11V21M4 7V17L12 21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <h1 class="title">创建账号</h1>
        <p class="subtitle">注册加入预制菜WMS系统</p>
      </div>

      <form @submit.prevent="handleRegist">
        <div class="form-group">
          <label>用户名</label>
          <div class="input-wrapper">
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M20 21V19C20 17.9391 19.5786 16.9217 18.8284 16.1716C18.0783 15.4214 17.0609 15 16 15H8C6.93913 15 5.92172 15.4214 5.17157 16.1716C4.42143 16.9217 4 17.9391 4 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M12 11C14.21 11 16 9.21 16 7C16 4.79 14.21 3 12 3C9.79 3 8 4.79 8 7C8 9.21 9.79 11 12 11Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <input v-model="username" type="text" placeholder="请输入用户名" required />
          </div>
        </div>

        <div class="form-group">
          <label>姓名</label>
          <div class="input-wrapper">
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M14 2H6C4.89543 2 4 2.89543 4 4V20C4 21.1046 4.89543 22 6 22H18C19.1046 22 20 21.1046 20 20V8L14 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M14 2V8H20" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <input v-model="realName" type="text" placeholder="请输入真实姓名" required />
          </div>
        </div>

        <div class="form-group">
          <label>手机号</label>
          <div class="input-wrapper">
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M22 16.92V19.92C22.001 20.5533 21.7737 21.1653 21.3635 21.6433C20.9533 22.1213 20.3899 22.4307 19.78 22.51C15.58 22.93 12.42 22.93 8.22 22.51C7.61009 22.4307 7.04673 22.1213 6.63655 21.6433C6.22638 21.1653 5.99904 20.5533 6 19.92V16.92M16 7C16 9.20914 14.2091 11 12 11C9.79086 11 8 9.20914 8 7C8 4.79086 9.79086 3 12 3C14.2091 3 16 4.79086 16 7Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <input v-model="phone" type="text" placeholder="请输入手机号" required />
          </div>
        </div>

        <div class="form-group">
          <label>密码</label>
          <div class="input-wrapper">
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M19 11H5C3.89543 11 3 11.8954 3 13V20C3 21.1046 3.89543 22 5 22H19C20.1046 22 21 21.1046 21 20V13C21 11.8954 20.1046 11 19 11Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M7 11V7C7 5.67392 7.52678 4.40215 8.46447 3.46447C9.40215 2.52678 10.6739 2 12 2C13.3261 2 14.5979 2.52678 15.5355 3.46447C16.4732 4.40215 17 5.67392 17 7V11" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <input v-model="password" type="password" placeholder="请输入密码" required />
          </div>
        </div>

        <div class="form-group">
          <label>确认密码</label>
          <div class="input-wrapper">
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M9 12L11 14L15 10M19 11H5C3.89543 11 3 11.8954 3 13V20C3 21.1046 3.89543 22 5 22H19C20.1046 22 21 21.1046 21 20V13C21 11.8954 20.1046 11 19 11Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <input v-model="confirmPassword" type="password" placeholder="请再次输入密码" required />
          </div>
        </div>

        <button type="submit" class="regist-btn">注 册</button>
      </form>

      <p v-if="errorMsg" class="error-msg">
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M12 9V13M12 17H12.01M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        {{ errorMsg }}
      </p>

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
  mounted() {
    this.initParticles()
  },
  beforeDestroy() {
    if (this.animFrame) cancelAnimationFrame(this.animFrame)
  },
  methods: {
    initParticles() {
      const canvas = document.getElementById('particles-canvas')
      if (!canvas) return
      const ctx = canvas.getContext('2d')
      let particles = []
      const maxParticles = 60

      const resize = () => {
        canvas.width = window.innerWidth
        canvas.height = window.innerHeight
      }
      resize()
      window.addEventListener('resize', resize)

      for (let i = 0; i < maxParticles; i++) {
        particles.push({
          x: Math.random() * canvas.width,
          y: Math.random() * canvas.height,
          vx: (Math.random() - 0.5) * 0.3,
          vy: (Math.random() - 0.5) * 0.3,
          r: Math.random() * 1.5 + 0.5
        })
      }

      const animate = () => {
        ctx.clearRect(0, 0, canvas.width, canvas.height)
        particles.forEach((p, i) => {
          p.x += p.vx
          p.y += p.vy
          if (p.x < 0 || p.x > canvas.width) p.vx *= -1
          if (p.y < 0 || p.y > canvas.height) p.vy *= -1
          ctx.beginPath()
          ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2)
          ctx.fillStyle = 'rgba(168, 85, 247, 0.12)'
          ctx.fill()
          particles.forEach((p2, j) => {
            if (i === j) return
            const dx = p.x - p2.x
            const dy = p.y - p2.y
            const dist = Math.sqrt(dx * dx + dy * dy)
            if (dist < 120) {
              ctx.beginPath()
              ctx.moveTo(p.x, p.y)
              ctx.lineTo(p2.x, p2.y)
              ctx.strokeStyle = `rgba(168, 85, 247, ${0.05 * (1 - dist / 120)})`
              ctx.stroke()
            }
          })
        })
        this.animFrame = requestAnimationFrame(animate)
      }
      animate()
    },

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

<style lang="scss" scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.regist-page {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: #0b0e1a;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
  overflow: hidden;
}

#particles-canvas {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
}

.regist-card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 440px;
  padding: 44px 40px;
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 24px;
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.5);
  animation: fadeInUp 0.7s ease forwards;
  opacity: 0;
}

.card-header {
  text-align: center;
  margin-bottom: 32px;

  .logo-icon {
    width: 56px;
    height: 56px;
    background: rgba(168, 85, 247, 0.12);
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 14px;
    border: 1px solid rgba(168, 85, 247, 0.2);

    svg {
      width: 28px;
      height: 28px;
      color: #a855f7;
    }
  }

  .title {
    font-size: 26px;
    font-weight: 700;
    background: linear-gradient(135deg, #fff 20%, #a855f7);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    margin-bottom: 6px;
    letter-spacing: 2px;
  }

  .subtitle {
    font-size: 13px;
    color: rgba(255, 255, 255, 0.45);
    letter-spacing: 2px;
  }
}

form {
  .form-group {
    margin-bottom: 18px;

    label {
      display: block;
      font-size: 13px;
      font-weight: 500;
      color: rgba(255, 255, 255, 0.65);
      margin-bottom: 6px;
    }

    .input-wrapper {
      position: relative;
      display: flex;
      align-items: center;

      .input-icon {
        position: absolute;
        left: 14px;
        width: 16px;
        height: 16px;
        color: rgba(255, 255, 255, 0.3);
        pointer-events: none;
        transition: color 0.3s ease;
        z-index: 1;
      }

      input {
        width: 100%;
        padding: 12px 14px 12px 42px;
        border: 1px solid rgba(255, 255, 255, 0.1);
        border-radius: 10px;
        font-size: 14px;
        color: #f0f4ff;
        background: rgba(255, 255, 255, 0.04);
        transition: all 0.35s cubic-bezier(0.25, 0.46, 0.45, 0.94);
        outline: none;

        &::placeholder {
          color: rgba(255, 255, 255, 0.25);
        }

        &:focus {
          border-color: #a855f7;
          background: rgba(168, 85, 247, 0.06);
          box-shadow: 0 0 24px rgba(168, 85, 247, 0.08);
        }
      }

      &:focus-within .input-icon {
        color: #a855f7;
      }
    }
  }

  .regist-btn {
    width: 100%;
    margin-top: 8px;
    padding: 14px;
    background: linear-gradient(135deg, #a855f7, #4facfe);
    color: #fff;
    border: none;
    border-radius: 12px;
    font-size: 16px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.35s cubic-bezier(0.25, 0.46, 0.45, 0.94);
    box-shadow: 0 4px 20px rgba(168, 85, 247, 0.25);
    letter-spacing: 4px;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 30px rgba(168, 85, 247, 0.4);
    }

    &:active {
      transform: translateY(0);
    }
  }
}

.error-msg {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 16px;
  padding: 10px 14px;
  background: rgba(244, 114, 182, 0.1);
  border: 1px solid rgba(244, 114, 182, 0.25);
  border-radius: 10px;
  color: #f472b6;
  font-size: 13px;
  animation: shake 0.5s ease;

  svg {
    width: 18px;
    height: 18px;
    flex-shrink: 0;
  }
}

.login-link {
  margin-top: 24px;
  text-align: center;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.45);

  a {
    color: #a855f7;
    text-decoration: none;
    font-weight: 500;
    transition: color 0.3s ease;

    &:hover {
      color: #4facfe;
    }
  }
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(24px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-5px); }
  75% { transform: translateX(5px); }
}

@media (max-width: 480px) {
  .regist-card {
    margin: 0 20px;
    padding: 32px 22px;
  }

  .card-header .title {
    font-size: 22px;
  }
}
</style>
