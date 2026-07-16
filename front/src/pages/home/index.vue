<template>
  <div class="dashboard-grid">
    <!-- 欢迎卡片 -->
    <div class="card welcome-card">
      <div class="greeting">欢迎使用仓储驾驶舱</div>
      <div class="sub-greeting">系统运行正常，今日待办已为您整理如下。</div>
      <div class="status-row">
        <div class="status-item">
          <el-icon><Box /></el-icon>
          <span>总库存 <span class="num">{{ stats.totalInventory.toLocaleString() }}</span> 件</span>
        </div>
        <div class="status-item">
          <el-icon><List /></el-icon>
          <span>待办任务 <span class="num">{{ stats.pendingTasks }}</span> 项</span>
        </div>
        <div class="status-item">
          <el-icon><Clock /></el-icon>
          <span>最后更新 <span class="num">{{ stats.lastUpdate }}</span></span>
        </div>
      </div>

      <!-- AI 输入框 -->
      <div class="ai-input-area">
        <el-icon class="ai-icon"><Cpu /></el-icon>
        <input type="text" v-model="aiMessage" placeholder="向 AI 助手提问，例如：查询今日入库单..." @keydown.enter="handleAiSend" />
        <button class="send-btn" @click="handleAiSend">
          <el-icon><Promotion /></el-icon> 发送
        </button>
      </div>

      <!-- 快捷操作 -->
      <div class="quick-actions">
        <button class="quick-btn" @click="$router.push({ name: 'Tasks', query: { tab: 'assign', op: 'inbound' } })">
          <el-icon><ArrowRight /></el-icon> 新建入库单
        </button>
        <button class="quick-btn" @click="$router.push({ name: 'Tasks', query: { tab: 'assign', op: 'outbound' } })">
          <el-icon><Check /></el-icon> 新建出库单
        </button>
        <button class="quick-btn" @click="$router.push({ name: 'Tasks', query: { tab: 'assign', op: 'quality' } })">
          <el-icon><List /></el-icon> 发起质检
        </button>
        <button class="quick-btn" @click="$router.push({ name: 'Dashboard' })">
          <el-icon><DataAnalysis /></el-icon> 数据看板
        </button>
      </div>
    </div>

    <!-- 大盘统计卡片行 -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon" style="background: hsl(212, 80%, 96%); color: hsl(212, 100%, 48%);">
          <el-icon><ArrowDown /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-value">{{ quickStats.todayInbound }}</div>
          <div class="stat-label">今日入库（单）</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: hsl(36, 80%, 96%); color: hsl(36, 90%, 48%);">
          <el-icon><Top /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-value">{{ quickStats.todayOutbound }}</div>
          <div class="stat-label">今日出库（单）</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: hsl(270, 60%, 96%); color: hsl(270, 65%, 55%);">
          <el-icon><List /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-value">{{ quickStats.pendingQC }}</div>
          <div class="stat-label">待质检任务</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: hsl(5, 80%, 96%); color: hsl(5, 85%, 58%);">
          <el-icon><WarningFilled /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-value">{{ quickStats.alertCount }}</div>
          <div class="stat-label">库存预警</div>
        </div>
      </div>
    </div>

    <!-- 待办卡片 -->
    <div class="card todo-card card-clickable" @click="goToTasks">
      <div class="card-header">
        <h3><el-icon><List /></el-icon> 我的待办</h3>
        <span class="count-badge">{{ todoList.length }} 项待处理</span>
      </div>
      <div class="todo-list">
        <div v-for="(item, index) in todoList" :key="index" class="todo-item">
          <div class="icon" :style="{ background: item.color, color: '#fff' }">
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
        <div v-if="todoList.length === 0" style="text-align:center;color:var(--foreground-muted);padding:30px 0;">
          所有待办已完成！
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import {
  Box, List, Clock, Cpu, Promotion, DocumentCopy, Check, ArrowRight, DataAnalysis, ArrowDown, Top, WarningFilled
} from '@element-plus/icons-vue'
import { ElIcon } from 'element-plus'
import request from '@/utils/request.js'

const taskTypeMeta = {
  '质检': { icon: 'List', color: '#9254de' },
  '入库': { icon: 'ArrowRight', color: 'var(--primary-color)' },
  '出库': { icon: 'Check', color: 'var(--danger)' },
  '库存调整': { icon: 'Box', color: 'var(--warning)' },
  '库存盘点': { icon: 'DocumentCopy', color: 'var(--success)' },
  '处理不合格货物': { icon: 'DocumentCopy', color: 'var(--warning)' }
}

export default {
  name: 'WarehouseDashboard',
  components: {
    Box, List, Clock, Cpu, Promotion, DocumentCopy, Check, ArrowRight, DataAnalysis, ArrowDown, Top, WarningFilled,
    ElIcon
  },
  data() {
    return {
      stats: { totalInventory: 0, pendingTasks: 0, lastUpdate: '--' },
      quickStats: { todayInbound: 0, todayOutbound: 0, pendingQC: 0, alertCount: 0 },
      aiMessage: '',
      todoList: []
    }
  },
  mounted() {
    this.fetchStats()
    this.fetchQuickStats()
    this.fetchTasks()
    this.timer = setInterval(this.fetchStats, 60000)
  },
  beforeDestroy() {
    clearInterval(this.timer)
  },
  methods: {
    async fetchQuickStats() {
      try {
        const [qcRes] = await Promise.all([
          request.get('/qualityCheck/myTasks', { params: { assigneeId: JSON.parse(localStorage.getItem('userInfo') || '{}').user_id || '' } })
        ])
        if (qcRes.code === 200) {
          this.quickStats.pendingQC = (qcRes.data || []).length
        }
      } catch (e) { /* ignore */ }
      // 入库/出库/预警暂用默认值，后端有对应接口后可替换
      try {
        const [inRes, outRes] = await Promise.all([
          request.get('/inboundOrderHead/list'),
          request.get('/outboundOrderHead/list')
        ])
        const today = new Date().toISOString().slice(0, 10)
        if (inRes.code === 200) {
          this.quickStats.todayInbound = (inRes.data || []).filter(i => (i.created_at || '').startsWith(today)).length
        }
        if (outRes.code === 200) {
          this.quickStats.todayOutbound = (outRes.data || []).filter(o => (o.created_at || '').startsWith(today)).length
        }
      } catch (e) { /* ignore */ }
    },
    async fetchStats() {
      const stored = localStorage.getItem('userInfo')
      let uid = null
      if (stored) {
        try { uid = JSON.parse(stored).user_id } catch (e) { /* ignore */ }
      }
      try {
        const params = {}
        if (uid) params.assigneeId = uid
        const res = await request.get('/ai/homeStats', params)
        if (res.code === 200) {
          this.stats = res.data
        }
      } catch (e) { /* ignore */ }
    },
    priorityClass(priority) {
      return { 高: 'high', 中: 'medium', 低: 'low' }[priority] || 'low'
    },
    getTaskMeta(taskType) {
      return taskTypeMeta[taskType] || { icon: 'List', color: 'var(--primary-color)' }
    },
    async fetchTasks() {
      const stored = localStorage.getItem('userInfo')
      if (!stored) return
      try {
        const data = JSON.parse(stored)
        const uid = data.user_id
        if (!uid) return
        // 合并质检和盘点任务，按 task_id 去重
        const taskMap = new Map()
        const addTasks = (list) => {
          (list || []).forEach(t => {
            if (!taskMap.has(t.task_id)) taskMap.set(t.task_id, t)
          })
        }
        const qcRes = await request.get('/qualityCheck/myTasks', { assigneeId: uid })
        if (qcRes.code === 200 && qcRes.data) addTasks(qcRes.data)
        const invRes = await request.get('/inventoryCheck/myTasks', { assigneeId: uid })
        if (invRes.code === 200 && invRes.data) addTasks(invRes.data)
        this.todoList = Array.from(taskMap.values()).map(t => ({
          title: t.related_order_no || '-',
          desc: t.task_type,
          priority: t.priority,
          ...this.getTaskMeta(t.task_type)
        }))
      } catch (e) { /* ignore */ }
    },
    markTodoDone(index) { this.todoList.splice(index, 1) },
    handleAiSend() {
      const msg = this.aiMessage.trim()
      if (!msg) return
      // 跳转到AI聊天页面，并通过 query 传递初始问题
      this.$router.push({ name: 'AiChat', query: { q: msg } })
    },
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

/* 欢迎卡片 */
.welcome-card .greeting {
  font-size: 26px;
  font-weight: 700;
  color: var(--foreground);
  margin-bottom: 6px;
  letter-spacing: -0.3px;
}
.welcome-card .sub-greeting {
  font-size: 14px;
  color: var(--foreground-muted);
  margin-bottom: 18px;
}
.welcome-card .status-row {
  display: flex;
  gap: 32px;
  flex-wrap: wrap;
  margin-bottom: 18px;
}
.welcome-card .status-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--foreground-regular);
}
.welcome-card .status-item .el-icon {
  font-size: 18px;
  color: var(--primary);
}
.welcome-card .status-item .num {
  font-weight: 700;
  color: var(--foreground);
  font-size: 18px;
}

/* AI 输入框 */
.welcome-card .ai-input-area {
  display: flex;
  align-items: center;
  border: 1px solid var(--input-border);
  border-radius: var(--radius-lg);
  padding: 4px 6px 4px 16px;
  transition: all 0.2s ease;
  background: var(--background-deep);
}
.welcome-card .ai-input-area:focus-within {
  border-color: var(--primary);
  box-shadow: 0 0 0 2px hsl(var(--primary-h), var(--primary-s), 60% / 20%);
  background: var(--card);
}
.welcome-card .ai-input-area input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  color: var(--foreground);
  font-size: 14px;
  padding: 8px 0;
  min-width: 0;
}
.welcome-card .ai-input-area input::placeholder { color: var(--foreground-placeholder); }
.welcome-card .ai-input-area .ai-icon {
  color: var(--primary);
  font-size: 18px;
  margin-right: 8px;
}
.welcome-card .ai-input-area .send-btn {
  background: var(--primary);
  border: none;
  border-radius: var(--radius-md);
  padding: 8px 20px;
  color: var(--text-on-primary);
  font-weight: 600;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 6px;
}
.welcome-card .ai-input-area .send-btn:hover:not(:disabled) {
  background: var(--primary-hover);
  box-shadow: var(--shadow-sm);
}
.welcome-card .ai-input-area .send-btn:disabled {
  background: hsl(var(--primary-h), 50%, 70%);
  cursor: not-allowed;
}

/* 快捷操作 */
.welcome-card .quick-actions {
  margin-top: 16px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.quick-btn {
  padding: 8px 18px;
  border-radius: var(--radius-md);
  border: 1px solid var(--border);
  background: var(--card);
  color: var(--foreground-regular);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.quick-btn:hover {
  color: var(--primary);
  border-color: var(--primary);
  background: var(--primary-bg);
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

/* 大盘统计卡片行 */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
.stat-card {
  background: var(--card);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-xl);
  padding: 18px 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: var(--shadow-sm);
  transition: all 0.2s ease;
}
.stat-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}
.stat-icon {
  width: 44px; height: 44px;
  border-radius: var(--radius-lg);
  display: flex; align-items: center; justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
}
.stat-body { display: flex; flex-direction: column; gap: 2px; }
.stat-value {
  font-size: 24px; font-weight: 700;
  color: var(--foreground);
  line-height: 1.2;
}
.stat-label {
  font-size: 12px;
  color: var(--foreground-muted);
}

/* 待办卡片 */
.todo-card .card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--border-light);
}
.todo-card .card-header h3 {
  font-size: 15px;
  font-weight: 600;
  color: var(--foreground);
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
}
.todo-card .card-header h3 .el-icon { color: var(--primary); font-size: 18px; }
.todo-card .card-header .count-badge {
  background: var(--primary-bg);
  color: var(--primary);
  padding: 3px 12px;
  border-radius: var(--radius-sm);
  font-size: 12px;
  font-weight: 600;
}
.todo-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.todo-item {
  display: flex;
  align-items: center;
  padding: 12px 14px;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
  gap: 12px;
  transition: all 0.2s ease;
}
.todo-item:hover {
  background: var(--primary-bg);
  border-color: hsl(var(--primary-h), 30%, 85%);
  transform: translateX(4px);
}
.todo-item .icon {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
}
.todo-item .info { flex: 1; min-width: 0; }
.todo-item .info .title {
  font-size: 14px;
  font-weight: 500;
  color: var(--foreground);
}
.todo-item .info .desc {
  font-size: 12px;
  color: var(--foreground-muted);
  margin-top: 2px;
}
.todo-item .priority-tag {
  font-size: 11px;
  padding: 2px 10px;
  border-radius: var(--radius-sm);
  font-weight: 600;
  flex-shrink: 0;
}
.todo-item .priority-tag.high { background: var(--danger-bg); color: var(--danger); }
.todo-item .priority-tag.medium { background: var(--warning-bg); color: var(--warning); }
.todo-item .priority-tag.low { background: var(--success-bg); color: var(--success); }
.todo-item .action-btn {
  color: var(--foreground-placeholder);
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 16px;
  padding: 4px;
  border-radius: var(--radius-sm);
}
.todo-item .action-btn:hover {
  color: var(--primary);
  background: hsl(var(--primary-h), 50%, 90%);
}

@media (max-width: 768px) {
  .stats-row { grid-template-columns: repeat(2, 1fr); }
  .welcome-card .greeting { font-size: 20px; }
  .welcome-card .status-row { gap: 16px; }
  .welcome-card .ai-input-area {
    flex-wrap: wrap; border: 1px solid var(--input-border); padding: 6px;
    border-radius: var(--radius-lg);
  }
  .welcome-card .ai-input-area input {
    width: 100%; padding: 8px 0; margin-bottom: 6px;
  }
  .welcome-card .ai-input-area .send-btn { width: 100%; justify-content: center; }
  .welcome-card .ai-input-area .ai-icon { display: none; }
}
</style>
