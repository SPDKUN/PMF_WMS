<template>
  <div class="app-wrapper">
    <canvas id="particles-canvas"></canvas>

    <!-- 侧边栏 -->
    <aside class="sidebar">
      <div class="logo">
        <el-icon><Box /></el-icon>
        <span>WMS·智仓</span>
      </div>
      <nav class="nav">
        <a href="#" :class="{ active: currentRoute === 'Home' }" @click.prevent="navigate('Home')">
          <el-icon><HomeFilled /></el-icon>
          <span>主页</span>
        </a>
        <a href="#" :class="{ active: currentRoute === 'Manage' }" @click.prevent="navigate('Manage')">
          <el-icon><Setting /></el-icon>
          <span>我的管理</span>
        </a>
        <a href="#" :class="{ active: currentRoute === 'Query' }" @click.prevent="navigate('Query')">
          <el-icon><Search /></el-icon>
          <span>明细查询</span>
        </a>
        <a href="#" :class="{ active: currentRoute === 'Tasks' || currentRoute === 'TaskForm' }" @click.prevent="navigate('Tasks')">
          <el-icon><List /></el-icon>
          <span>工作任务</span>
        </a>
        <a href="#" :class="{ active: currentRoute === 'Profile' }" @click.prevent="navigate('Profile')">
          <el-icon><UserFilled /></el-icon>
          <span>个人中心</span>
        </a>
      </nav>
      <div class="logout-btn" @click="handleLogout" title="退出登录">
        <el-icon><SwitchButton /></el-icon>
      </div>
    </aside>

    <!-- 主内容 -->
    <main class="main">
      <header class="topbar">
        <div class="topbar-left">
          <h1>{{ pageTitle }}</h1>
          <div class="date-badge">
            <el-icon><Calendar /></el-icon>
            <span>{{ currentDate }}</span>
          </div>
        </div>
      </header>

      <div class="content-area">
        <router-view></router-view>
      </div>
    </main>
  </div>
</template>

<script>
import {
  Box, HomeFilled, Setting, Search, List, UserFilled, SwitchButton, Calendar
} from '@element-plus/icons-vue'
import { ElIcon } from 'element-plus'

export default {
  name: 'MainLayout',
  components: {
    Box, HomeFilled, Setting, Search, List, UserFilled, SwitchButton, Calendar,
    ElIcon
  },
  data() {
    return {
      currentDate: '',
    }
  },
  computed: {
    currentRoute() {
      return this.$route.name || ''
    },
    pageTitle() {
      const titles = {
        Home: '智能仓储驾驶舱',
        Manage: '我的管理',
        Query: '明细查询',
        Tasks: '工作任务',
        Profile: '个人中心',
      }
      return titles[this.$route.name] || 'WMS·智仓'
    }
  },
  mounted() {
    this.updateDateTime()
    this.timer = setInterval(this.updateDateTime, 60000)
    this.initParticles()
    window.addEventListener('resize', this.resizeParticles)
  },
  beforeDestroy() {
    clearInterval(this.timer)
    window.removeEventListener('resize', this.resizeParticles)
    if (this.animationId) cancelAnimationFrame(this.animationId)
  },
  methods: {
    navigate(name) {
      this.$router.push({ name })
    },
    handleLogout() {
      localStorage.removeItem('token')
      this.$router.push({ name: 'Login' })
    },
    updateDateTime() {
      const now = new Date()
      this.currentDate = now.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'short' })
    },
    initParticles() {
      const canvas = document.getElementById('particles-canvas')
      if (!canvas) return
      const ctx = canvas.getContext('2d')
      let w, h, particles = []
      const resize = () => {
        w = canvas.width = window.innerWidth
        h = canvas.height = window.innerHeight
      }
      this.resizeParticles = resize
      resize()
      class Particle {
        constructor() { this.reset() }
        reset() {
          this.x = Math.random() * w
          this.y = Math.random() * h
          this.r = Math.random() * 1.8 + 0.6
          this.dx = (Math.random() - 0.5) * 0.35
          this.dy = (Math.random() - 0.5) * 0.35
          this.alpha = Math.random() * 0.5 + 0.15
        }
        update() {
          this.x += this.dx; this.y += this.dy
          if (this.x < 0 || this.x > w) this.dx *= -1
          if (this.y < 0 || this.y > h) this.dy *= -1
        }
        draw() {
          ctx.beginPath()
          ctx.arc(this.x, this.y, this.r, 0, Math.PI * 2)
          ctx.fillStyle = `rgba(79,172,254,${this.alpha})`
          ctx.fill()
        }
      }
      for (let i = 0; i < 60; i++) particles.push(new Particle())
      const draw = () => {
        ctx.clearRect(0, 0, w, h)
        particles.forEach(p => { p.update(); p.draw() })
        for (let i = 0; i < particles.length; i++) {
          for (let j = i + 1; j < particles.length; j++) {
            const dx = particles[i].x - particles[j].x
            const dy = particles[i].y - particles[j].y
            if (Math.sqrt(dx*dx+dy*dy) < 140) {
              ctx.beginPath()
              ctx.moveTo(particles[i].x, particles[i].y)
              ctx.lineTo(particles[j].x, particles[j].y)
              ctx.strokeStyle = `rgba(79,172,254,${0.08*(1-Math.sqrt(dx*dx+dy*dy)/140)})`
              ctx.lineWidth = 0.6
              ctx.stroke()
            }
          }
        }
        this.animationId = requestAnimationFrame(draw)
      }
      draw()
    }
  }
}
</script>

<style>
* { margin:0; padding:0; box-sizing:border-box; }
:root {
  --bg-primary: #0b0e1a;
  --bg-card: rgba(255,255,255,0.04);
  --border-card: rgba(255,255,255,0.06);
  --text-primary: #f0f4ff;
  --text-secondary: rgba(255,255,255,0.65);
  --text-dim: rgba(255,255,255,0.35);
  --glow-blue: #4facfe;
  --glow-purple: #a855f7;
  --glow-pink: #f472b6;
  --glow-cyan: #22d3ee;
  --glow-green: #34d399;
  --glow-orange: #fb923c;
  --shadow-card: 0 8px 32px rgba(0,0,0,0.5);
  --radius-card: 20px;
  --transition: 0.35s cubic-bezier(0.25,0.46,0.45,0.94);
}
</style>

<style scoped>
.app-wrapper {
  position: relative;
  display: flex;
  height: 100vh;
  padding: 16px 20px 20px 20px;
  gap: 20px;
  background: var(--bg-primary);
  color: var(--text-primary);
  font-family: 'Inter', 'Segoe UI', system-ui, -apple-system, sans-serif;
  overflow: hidden;
}

#particles-canvas {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  pointer-events: none;
}

/* 侧边栏 */
.sidebar {
  flex: 0 0 72px;
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 28px;
  padding: 24px 0 20px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.4);
  transition: var(--transition);
  overflow: hidden;
  z-index: 1;
}
.sidebar:hover {
  flex: 0 0 200px;
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(255, 255, 255, 0.10);
}
.sidebar .logo {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 16px;
  width: 100%;
  white-space: nowrap;
}
.sidebar .logo .el-icon {
  font-size: 28px;
  background: linear-gradient(135deg, var(--glow-blue), var(--glow-purple));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  flex-shrink: 0;
}
.sidebar .logo span {
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.5px;
  opacity: 0;
  transition: var(--transition);
  background: linear-gradient(135deg, #f0f4ff, var(--glow-blue));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
.sidebar:hover .logo span {
  opacity: 1;
}

.sidebar .nav {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
  padding: 0 12px;
}
.sidebar .nav a {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 14px;
  border-radius: 16px;
  color: var(--text-secondary);
  text-decoration: none;
  font-size: 15px;
  font-weight: 500;
  transition: var(--transition);
  white-space: nowrap;
  position: relative;
}
.sidebar .nav a .el-icon {
  font-size: 20px;
  width: 24px;
  text-align: center;
  flex-shrink: 0;
  transition: var(--transition);
}
.sidebar .nav a span {
  opacity: 0;
  transition: var(--transition);
}
.sidebar:hover .nav a span {
  opacity: 1;
}
.sidebar .nav a:hover {
  background: rgba(79, 172, 254, 0.12);
  color: #fff;
  box-shadow: 0 0 24px rgba(79, 172, 254, 0.08);
}
.sidebar .nav a.active {
  background: rgba(79, 172, 254, 0.15);
  color: #fff;
  box-shadow: inset 0 0 0 1px rgba(79, 172, 254, 0.2);
}
.sidebar .nav a.active .el-icon {
  color: var(--glow-blue);
}

/* 登出按钮 */
.logout-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(244, 114, 182, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: var(--transition);
  border: 1px solid rgba(244, 114, 182, 0.2);
  flex-shrink: 0;
}
.logout-btn .el-icon {
  font-size: 18px;
  color: var(--glow-pink);
  transition: var(--transition);
}
.logout-btn:hover {
  background: rgba(244, 114, 182, 0.25);
  border-color: var(--glow-pink);
  transform: scale(1.08);
  box-shadow: 0 0 24px rgba(244, 114, 182, 0.2);
}

/* 主内容 */
.main {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
  overflow: hidden;
  min-width: 0;
  z-index: 1;
}

/* 顶部栏 */
.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0 4px 0;
  flex-shrink: 0;
}
.topbar-left {
  display: flex;
  align-items: center;
  gap: 20px;
}
.topbar-left h1 {
  font-size: 22px;
  font-weight: 600;
  background: linear-gradient(135deg, #fff 30%, var(--glow-blue));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: -0.3px;
}
.topbar-left .date-badge {
  font-size: 13px;
  color: var(--text-secondary);
  background: rgba(255, 255, 255, 0.04);
  padding: 6px 16px;
  border-radius: 40px;
  border: 1px solid rgba(255, 255, 255, 0.06);
  backdrop-filter: blur(6px);
  display: flex;
  align-items: center;
  gap: 8px;
}
.topbar-left .date-badge .el-icon {
  color: var(--glow-cyan);
  font-size: 16px;
}

/* 内容区域 */
.content-area {
  flex: 1;
  overflow-y: auto;
  padding-right: 4px;
}

/* 响应式 */
@media (max-width: 768px) {
  .app-wrapper {
    padding: 10px;
    flex-direction: column;
  }
  .sidebar {
    flex: 0 0 56px;
    flex-direction: row;
    padding: 0 16px;
    border-radius: 18px;
    height: 56px;
  }
  .sidebar:hover {
    flex: 0 0 56px;
  }
  .sidebar .logo span,
  .sidebar .nav a span {
    display: none;
  }
  .sidebar .nav {
    flex-direction: row;
    padding: 0 4px;
    gap: 4px;
  }
  .sidebar .nav a {
    padding: 8px 12px;
  }
  .sidebar .nav a .el-icon {
    font-size: 16px;
  }
  .logout-btn {
    width: 32px;
    height: 32px;
  }
  .topbar-left .date-badge {
    display: none;
  }
}
@media (max-width: 480px) {
  .topbar-left h1 {
    font-size: 17px;
  }
}
</style>
