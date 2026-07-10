<template>
  <div class="login-page">
    <canvas id="particles-canvas"></canvas>

    <div class="login-card">
      <div class="card-header">
        <div class="logo-icon">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M20 7L12 3L4 7M20 7L12 11M20 7V17L12 21M12 11L4 7M12 11V21M4 7V17L12 21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <h1 class="title">预制菜 WMS</h1>
        <p class="subtitle">智能仓储管理系统</p>
      </div>

      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label>用户名</label>
          <div class="input-wrapper">
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M20 21V19C20 17.9391 19.5786 16.9217 18.8284 16.1716C18.0783 15.4214 17.0609 15 16 15H8C6.93913 15 5.92172 15.4214 5.17157 16.1716C4.42143 16.9217 4 17.9391 4 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M12 11C14.21 11 16 9.21 16 7C16 4.79 14.21 3 12 3C9.79 3 8 4.79 8 7C8 9.21 9.79 11 12 11Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <input
              v-model="username"
              type="text"
              placeholder="请输入用户名"
              required
            />
          </div>
        </div>

        <div class="form-group">
          <label>密码</label>
          <div class="input-wrapper">
            <svg class="input-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M19 11H5C3.89543 11 3 11.8954 3 13V20C3 21.1046 3.89543 22 5 22H19C20.1046 22 21 21.1046 21 20V13C21 11.8954 20.1046 11 19 11Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M7 11V7C7 5.67392 7.52678 4.40215 8.46447 3.46447C9.40215 2.52678 10.6739 2 12 2C13.3261 2 14.5979 2.52678 15.5355 3.46447C16.4732 4.40215 17 5.67392 17 7V11" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <input
              v-model="password"
              type="password"
              placeholder="请输入密码"
              required
            />
          </div>
        </div>

        <div class="form-options">
          <label class="remember-me">
            <input type="checkbox" />
            <span>记住密码</span>
          </label>
        </div>

        <button type="submit" class="login-btn" :disabled="loading">
          <span v-if="!loading">登 录</span>
          <span v-else>登录中...</span>
          <svg v-if="!loading" class="btn-arrow" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M5 12H19M19 12L12 5M19 12L12 19" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
      </form>

      <p v-if="errorMsg" class="error-msg">
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M12 9V13M12 17H12.01M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        {{ errorMsg }}
      </p>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

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
      const maxParticles = 80

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
          vx: (Math.random() - 0.5) * 0.4,
          vy: (Math.random() - 0.5) * 0.4,
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
          ctx.fillStyle = 'rgba(79, 172, 254, 0.15)'
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
              ctx.strokeStyle = `rgba(79, 172, 254, ${0.06 * (1 - dist / 120)})`
              ctx.stroke()
            }
          })
        })
        this.animFrame = requestAnimationFrame(animate)
      }
      animate()
    },

    async handleLogin() {
      this.errorMsg = ''
      this.loading = true
      try {
        const response = await axios.get('http://localhost:8088/auth/login', {
          params: {
            username: this.username,
            password: this.password
          }
        })

        const token = response.data.token
        if (token) {
          localStorage.setItem('token', token)
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

<style lang="scss" scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.login-page {
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

.login-card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 420px;
  padding: 48px 40px;
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
  margin-bottom: 36px;

  .logo-icon {
    width: 64px;
    height: 64px;
    background: rgba(79, 172, 254, 0.12);
    border-radius: 18px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 16px;
    border: 1px solid rgba(79, 172, 254, 0.2);

    svg {
      width: 32px;
      height: 32px;
      color: #4facfe;
    }
  }

  .title {
    font-size: 28px;
    font-weight: 700;
    background: linear-gradient(135deg, #fff 20%, #4facfe);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    margin-bottom: 6px;
    letter-spacing: 2px;
  }

  .subtitle {
    font-size: 14px;
    color: rgba(255, 255, 255, 0.5);
    letter-spacing: 3px;
  }
}

form {
  .form-group {
    margin-bottom: 22px;

    label {
      display: block;
      font-size: 13px;
      font-weight: 500;
      color: rgba(255, 255, 255, 0.65);
      margin-bottom: 8px;
    }

    .input-wrapper {
      position: relative;
      display: flex;
      align-items: center;

      .input-icon {
        position: absolute;
        left: 16px;
        width: 18px;
        height: 18px;
        color: rgba(255, 255, 255, 0.35);
        pointer-events: none;
        transition: color 0.3s ease;
        z-index: 1;
      }

      input {
        width: 100%;
        padding: 14px 16px 14px 46px;
        border: 1px solid rgba(255, 255, 255, 0.1);
        border-radius: 12px;
        font-size: 14px;
        color: #f0f4ff;
        background: rgba(255, 255, 255, 0.04);
        transition: all 0.35s cubic-bezier(0.25, 0.46, 0.45, 0.94);
        outline: none;

        &::placeholder {
          color: rgba(255, 255, 255, 0.25);
        }

        &:focus {
          border-color: #4facfe;
          background: rgba(79, 172, 254, 0.06);
          box-shadow: 0 0 24px rgba(79, 172, 254, 0.08);
        }
      }

      &:focus-within .input-icon {
        color: #4facfe;
      }
    }
  }

  .form-options {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 28px;

    .remember-me {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 13px;
      color: rgba(255, 255, 255, 0.5);
      cursor: pointer;

      input[type="checkbox"] {
        width: 16px;
        height: 16px;
        accent-color: #4facfe;
        cursor: pointer;
      }
    }

    .forgot-password {
      font-size: 13px;
      color: #4facfe;
      text-decoration: none;
      transition: color 0.3s ease;

      &:hover {
        color: #a855f7;
      }
    }
  }

  .login-btn {
    width: 100%;
    padding: 15px;
    background: linear-gradient(135deg, #4facfe, #a855f7);
    color: #fff;
    border: none;
    border-radius: 12px;
    font-size: 16px;
    font-weight: 600;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12px;
    transition: all 0.35s cubic-bezier(0.25, 0.46, 0.45, 0.94);
    box-shadow: 0 4px 20px rgba(79, 172, 254, 0.25);
    letter-spacing: 4px;

    .btn-arrow {
      width: 18px;
      height: 18px;
      transition: transform 0.3s ease;
    }

    &:hover:not(:disabled) {
      transform: translateY(-2px);
      box-shadow: 0 8px 30px rgba(79, 172, 254, 0.4);

      .btn-arrow {
        transform: translateX(4px);
      }
    }

    &:active:not(:disabled) {
      transform: translateY(0);
    }

    &:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
  }
}

.error-msg {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 18px;
  padding: 12px 16px;
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

.regist-link {
  margin-top: 28px;
  text-align: center;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.45);

  a {
    color: #4facfe;
    text-decoration: none;
    font-weight: 500;
    transition: color 0.3s ease;

    &:hover {
      color: #a855f7;
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
  .login-card {
    margin: 0 20px;
    padding: 36px 24px;
  }

  .card-header .title {
    font-size: 24px;
  }
}
</style>
