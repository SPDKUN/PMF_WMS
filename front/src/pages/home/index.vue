<template>
  <div class="dashboard-grid">
    <!-- 欢迎卡片 -->
    <div class="card welcome-card fade-in">
      <div class="greeting">👋 欢迎使用仓储驾驶舱</div>
      <div class="sub-greeting">系统运行正常，今日待办已为您整理如下。</div>
      <div class="status-row">
        <div class="status-item">
          <el-icon><Box /></el-icon>
          <span>总库存 <span class="num">2,847</span> 件</span>
        </div>
        <div class="status-item">
          <el-icon><List /></el-icon>
          <span>待办任务 <span class="num">{{ todoList.length }}</span> 项</span>
        </div>
        <div class="status-item">
          <el-icon><Clock /></el-icon>
          <span>最后更新 <span class="num">{{ lastUpdate }}</span></span>
        </div>
      </div>

      <!-- AI 输入框 -->
      <div class="ai-input-area">
        <el-icon class="ai-icon"><Cpu /></el-icon>
        <input type="text" v-model="aiMessage" placeholder="向 AI 助手提问，例如：查询今日入库单..." @keydown.enter="handleAiSend" />
        <button class="send-btn" @click="handleAiSend" :disabled="aiProcessing">
          <el-icon><Promotion /></el-icon> 发送
        </button>
      </div>

      <div class="quick-actions">
        <button class="btn" @click="quickAction('入库')">
          <el-icon><Plus /></el-icon> 新建入库
        </button>
        <button class="btn" @click="quickAction('出库')">
          <el-icon><ArrowRight /></el-icon> 新建出库
        </button>
        <button class="btn" @click="quickAction('盘点')">
          <el-icon><DocumentCopy /></el-icon> 创建盘点
        </button>
      </div>
    </div>

    <!-- 待办卡片 -->
    <div class="card todo-card fade-in" @click="goToTasks">
      <div class="card-header">
        <h3><el-icon><List /></el-icon> 我的待办</h3>
        <span class="count-badge">{{ todoList.length }} 项待处理</span>
      </div>
      <div class="todo-list">
        <div v-for="(item, index) in todoList" :key="index" class="todo-item" :style="{ animationDelay: 0.1 * index + 's' }">
          <div class="icon" :style="{ background: item.color + '30', color: item.color }">
            <el-icon v-if="item.icon === 'ArrowRight'"><ArrowRight /></el-icon>
            <el-icon v-else-if="item.icon === 'Check'"><Check /></el-icon>
            <el-icon v-else-if="item.icon === 'Box'"><Box /></el-icon>
            <el-icon v-else-if="item.icon === 'List'"><List /></el-icon>
            <el-icon v-else><DocumentCopy /></el-icon>
          </div>
          <div class="info">
            <div class="title">{{ item.title }}</div>
            <div class="desc">{{ item.desc }}</div>
          </div>
          <span class="priority-tag" :class="priorityClass(item.priority)">{{ item.priority }}</span>
          <div class="action-btn" @click.stop="markTodoDone(index)" title="标记完成">
            <el-icon><Check /></el-icon>
          </div>
        </div>
        <div v-if="todoList.length === 0" style="text-align:center;color:var(--text-secondary);padding:30px 0;">
          🎉 所有待办已完成！
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import {
  Box, List, Clock, Cpu, Promotion, Plus, ArrowRight, DocumentCopy, Check
} from '@element-plus/icons-vue'
import { ElIcon } from 'element-plus'

export default {
  name: 'WarehouseDashboard',
  components: {
    Box, List, Clock, Cpu, Promotion, Plus, ArrowRight, DocumentCopy, Check,
    ElIcon
  },
  data() {
    return {
      lastUpdate: '',
      aiMessage: '',
      aiProcessing: false,
      todoList: [
        { title: '采购入库单 #IN20260710-003', desc: '待质检，共 120 件', priority: '高', icon: 'ArrowRight', color: '#4facfe' },
        { title: '出库单 #OUT20260710-087', desc: '待拣货，优先级高', priority: '高', icon: 'Check', color: '#f472b6' },
        { title: '库存调整 #ADJ20260710-022', desc: '库位移库，待执行', priority: '中', icon: 'Box', color: '#fb923c' },
        { title: '质检任务 #QC20260710-015', desc: '来料检，待抽样', priority: '中', icon: 'List', color: '#a855f7' },
        { title: '盘点单 #CNT20260710-005', desc: '全盘，待复核差异', priority: '低', icon: 'DocumentCopy', color: '#34d399' },
        { title: '不合格处理 #DEF20260710-011', desc: '报废确认，待审批', priority: '低', icon: 'DocumentCopy', color: '#fb923c' }
      ]
    }
  },
  mounted() {
    this.updateTime()
    this.timer = setInterval(this.updateTime, 60000)
  },
  beforeDestroy() {
    clearInterval(this.timer)
  },
  methods: {
    updateTime() {
      this.lastUpdate = new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    },
    priorityClass(priority) {
      return { 高: 'high', 中: 'medium', 低: 'low' }[priority] || 'low'
    },
    markTodoDone(index) { this.todoList.splice(index, 1) },
    handleAiSend() {
      const msg = this.aiMessage.trim()
      if (!msg) return
      this.aiProcessing = true
      setTimeout(() => {
        alert(`[AI 演示] ${msg}`)
        this.aiMessage = ''
        this.aiProcessing = false
      }, 1200)
    },
    quickAction(type) { alert(`跳转至${type}页面`) },
    goToTasks() {
      this.$router.push({ name: 'Tasks' })
    }
  }
}
</script>

<style scoped>
.dashboard-grid {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 卡片 */
.card {
  background: var(--bg-card);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  border: 1px solid var(--border-card);
  border-radius: var(--radius-card);
  padding: 24px 28px;
  box-shadow: var(--shadow-card);
  transition: var(--transition);
  display: flex;
  flex-direction: column;
}
.card:hover {
  border-color: rgba(255, 255, 255, 0.10);
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.5);
  transform: translateY(-2px);
}

/* 欢迎卡片 */
.welcome-card {
  background: linear-gradient(145deg, rgba(79, 172, 254, 0.08), rgba(168, 85, 247, 0.05));
  border-left: 3px solid var(--glow-blue);
  position: relative;
  overflow: hidden;
}
.welcome-card::after {
  content: '';
  position: absolute;
  top: -50%;
  right: -20%;
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(79, 172, 254, 0.08) 0%, transparent 70%);
  border-radius: 50%;
  pointer-events: none;
}
.welcome-card .greeting {
  font-size: 32px;
  font-weight: 700;
  background: linear-gradient(135deg, #fff 20%, var(--glow-blue));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 8px;
  position: relative;
  z-index: 1;
}
.welcome-card .sub-greeting {
  font-size: 16px;
  color: var(--text-secondary);
  margin-bottom: 16px;
  position: relative;
  z-index: 1;
}
.welcome-card .status-row {
  display: flex;
  gap: 30px;
  flex-wrap: wrap;
  margin-bottom: 16px;
  position: relative;
  z-index: 1;
}
.welcome-card .status-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: var(--text-secondary);
}
.welcome-card .status-item .el-icon {
  font-size: 18px;
  color: var(--glow-cyan);
}
.welcome-card .status-item .num {
  font-weight: 600;
  color: #fff;
  font-size: 18px;
}

/* AI 输入框 */
.welcome-card .ai-input-area {
  position: relative;
  z-index: 1;
  margin-top: 4px;
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 60px;
  padding: 4px 6px 4px 20px;
  transition: var(--transition);
  backdrop-filter: blur(4px);
}
.welcome-card .ai-input-area:focus-within {
  border-color: var(--glow-blue);
  box-shadow: 0 0 30px rgba(79, 172, 254, 0.06);
}
.welcome-card .ai-input-area input {
  flex: 1;
  background: transparent;
  border: none;
  outline: none;
  color: var(--text-primary);
  font-size: 15px;
  padding: 12px 0;
  min-width: 0;
}
.welcome-card .ai-input-area input::placeholder {
  color: var(--text-dim);
  font-size: 14px;
}
.welcome-card .ai-input-area .ai-icon {
  color: var(--glow-purple);
  font-size: 18px;
  margin-right: 10px;
  opacity: 0.7;
}
.welcome-card .ai-input-area .send-btn {
  background: linear-gradient(135deg, var(--glow-blue), var(--glow-purple));
  border: none;
  border-radius: 40px;
  padding: 10px 22px;
  color: #fff;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  transition: var(--transition);
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}
.welcome-card .ai-input-area .send-btn:hover:not(:disabled) {
  transform: scale(1.04);
  box-shadow: 0 4px 20px rgba(79, 172, 254, 0.3);
}
.welcome-card .ai-input-area .send-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.welcome-card .ai-input-area .send-btn .el-icon {
  font-size: 16px;
}

.welcome-card .quick-actions {
  margin-top: 16px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  position: relative;
  z-index: 1;
}
.welcome-card .quick-actions .btn {
  padding: 8px 18px;
  border-radius: 40px;
  border: 1px solid rgba(255, 255, 255, 0.10);
  background: rgba(255, 255, 255, 0.04);
  color: var(--text-primary);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: var(--transition);
  backdrop-filter: blur(6px);
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
.welcome-card .quick-actions .btn:hover {
  background: rgba(79, 172, 254, 0.15);
  border-color: var(--glow-blue);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(79, 172, 254, 0.15);
}
.welcome-card .quick-actions .btn .el-icon {
  color: var(--glow-blue);
}

/* 待办卡片 */
.todo-card {
  cursor: pointer;
  overflow: hidden;
  min-height: 200px;
}
.todo-card .card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.todo-card .card-header h3 {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 10px;
}
.todo-card .card-header h3 .el-icon {
  color: var(--glow-cyan);
}
.todo-card .card-header .count-badge {
  background: rgba(79, 172, 254, 0.15);
  color: var(--glow-blue);
  padding: 2px 12px;
  border-radius: 30px;
  font-size: 12px;
  font-weight: 500;
}
.todo-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-right: 4px;
}
.todo-item {
  display: flex;
  align-items: center;
  padding: 12px 14px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.02);
  transition: var(--transition);
  border-left: 3px solid transparent;
  gap: 14px;
  opacity: 0;
  animation: fadeInUp 0.5s ease forwards;
}
.todo-item:hover {
  background: rgba(255, 255, 255, 0.04);
  border-left-color: var(--glow-blue);
}
.todo-item .icon {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  flex-shrink: 0;
  color: #fff;
}
.todo-item .icon .el-icon {
  font-size: 16px;
}
.todo-item .info {
  flex: 1;
  min-width: 0;
}
.todo-item .info .title {
  font-size: 14px;
  font-weight: 500;
}
.todo-item .info .desc {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 2px;
}
.todo-item .priority-tag {
  font-size: 10px;
  padding: 2px 10px;
  border-radius: 30px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.3px;
  flex-shrink: 0;
}
.todo-item .priority-tag.high {
  background: rgba(244, 114, 182, 0.15);
  color: var(--glow-pink);
}
.todo-item .priority-tag.medium {
  background: rgba(251, 146, 60, 0.15);
  color: var(--glow-orange);
}
.todo-item .priority-tag.low {
  background: rgba(52, 211, 153, 0.12);
  color: var(--glow-green);
}
.todo-item .action-btn {
  color: var(--text-dim);
  cursor: pointer;
  transition: var(--transition);
  font-size: 14px;
  padding: 4px 8px;
  border-radius: 8px;
}
.todo-item .action-btn:hover {
  color: var(--glow-blue);
  background: rgba(79, 172, 254, 0.08);
}
.todo-item .action-btn .el-icon {
  font-size: 18px;
}

/* 动画 */
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(18px); }
  to { opacity: 1; transform: translateY(0); }
}
.fade-in {
  animation: fadeInUp 0.7s ease forwards;
  opacity: 0;
}
.fade-in:nth-child(1) { animation-delay: 0.05s; }
.fade-in:nth-child(2) { animation-delay: 0.15s; }

/* 响应式 */
@media (max-width: 768px) {
  .welcome-card .greeting {
    font-size: 26px;
  }
  .welcome-card .status-row {
    gap: 16px;
  }
  .welcome-card .ai-input-area {
    flex-wrap: wrap;
    background: transparent;
    border: none;
    padding: 0;
  }
  .welcome-card .ai-input-area input {
    width: 100%;
    background: rgba(255, 255, 255, 0.04);
    border: 1px solid rgba(255, 255, 255, 0.08);
    border-radius: 40px;
    padding: 12px 20px;
    margin-bottom: 8px;
  }
  .welcome-card .ai-input-area .send-btn {
    width: 100%;
    justify-content: center;
  }
  .welcome-card .ai-input-area .ai-icon {
    display: none;
  }
}
@media (max-width: 480px) {
  .welcome-card .quick-actions .btn {
    font-size: 12px;
    padding: 6px 14px;
  }
}
</style>
