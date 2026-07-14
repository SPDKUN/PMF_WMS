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
    </div>

    <!-- 待办卡片 -->
    <div class="card todo-card" @click="goToTasks">
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
        <div v-if="todoList.length === 0" style="text-align:center;color:#909399;padding:30px 0;">
          所有待办已完成！
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import {
  Box, List, Clock, Cpu, Promotion, DocumentCopy, Check
} from '@element-plus/icons-vue'
import { ElIcon } from 'element-plus'
import request from '@/utils/request.js'

const taskTypeMeta = {
  '质检': { icon: 'List', color: '#9254de' },
  '入库': { icon: 'ArrowRight', color: '#409EFF' },
  '出库': { icon: 'Check', color: '#f56c6c' },
  '库存调整': { icon: 'Box', color: '#e6a23c' },
  '库存盘点': { icon: 'DocumentCopy', color: '#67c23a' },
  '处理不合格货物': { icon: 'DocumentCopy', color: '#e6a23c' }
}

export default {
  name: 'WarehouseDashboard',
  components: {
    Box, List, Clock, Cpu, Promotion, DocumentCopy, Check,
    ElIcon
  },
  data() {
    return {
      stats: { totalInventory: 0, pendingTasks: 0, lastUpdate: '--' },
      aiMessage: '',
      todoList: []
    }
  },
  mounted() {
    this.fetchStats()
    this.fetchTasks()
    this.timer = setInterval(this.fetchStats, 60000)
  },
  beforeDestroy() {
    clearInterval(this.timer)
  },
  methods: {
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
      return taskTypeMeta[taskType] || { icon: 'List', color: '#409EFF' }
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
  gap: 16px;
}

/* 卡片 */
.card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 20px 24px;
}

/* 欢迎卡片 */
.welcome-card .greeting {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 6px;
}
.welcome-card .sub-greeting {
  font-size: 14px;
  color: #909399;
  margin-bottom: 14px;
}
.welcome-card .status-row {
  display: flex;
  gap: 28px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}
.welcome-card .status-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #606266;
}
.welcome-card .status-item .el-icon {
  font-size: 16px;
  color: #409EFF;
}
.welcome-card .status-item .num {
  font-weight: 600;
  color: #303133;
  font-size: 16px;
}

/* AI 输入框 */
.welcome-card .ai-input-area {
  margin-top: 4px;
  display: flex;
  align-items: center;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 4px 6px 4px 14px;
  transition: border-color 0.2s;
}
.welcome-card .ai-input-area:focus-within {
  border-color: #409EFF;
}
.welcome-card .ai-input-area input {
  flex: 1;
  border: none;
  outline: none;
  color: #303133;
  font-size: 14px;
  padding: 8px 0;
  min-width: 0;
}
.welcome-card .ai-input-area input::placeholder {
  color: #c0c4cc;
}
.welcome-card .ai-input-area .ai-icon {
  color: #409EFF;
  font-size: 16px;
  margin-right: 8px;
}
.welcome-card .ai-input-area .send-btn {
  background: #409EFF;
  border: none;
  border-radius: 4px;
  padding: 8px 18px;
  color: #fff;
  font-weight: 600;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s;
  display: flex;
  align-items: center;
  gap: 6px;
}
.welcome-card .ai-input-area .send-btn:hover:not(:disabled) {
  background: #66b1ff;
}
.welcome-card .ai-input-area .send-btn:disabled {
  background: #a0cfff;
  cursor: not-allowed;
}

.welcome-card .quick-actions {
  margin-top: 14px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.welcome-card .quick-actions .btn {
  padding: 6px 16px;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
  background: #fff;
  color: #606266;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.welcome-card .quick-actions .btn:hover {
  color: #409EFF;
  border-color: #409EFF;
}

/* 待办卡片 */
.todo-card {
  cursor: pointer;
}
.todo-card .card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}
.todo-card .card-header h3 {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
}
.todo-card .card-header h3 .el-icon {
  color: #409EFF;
}
.todo-card .card-header .count-badge {
  background: #ecf5ff;
  color: #409EFF;
  padding: 2px 10px;
  border-radius: 4px;
  font-size: 12px;
}
.todo-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.todo-item {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  border-radius: 4px;
  border: 1px solid #ebeef5;
  gap: 12px;
}
.todo-item:hover {
  background: #f5f7fa;
}
.todo-item .icon {
  width: 32px;
  height: 32px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  flex-shrink: 0;
}
.todo-item .info {
  flex: 1;
  min-width: 0;
}
.todo-item .info .title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}
.todo-item .info .desc {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}
.todo-item .priority-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 600;
  flex-shrink: 0;
}
.todo-item .priority-tag.high {
  background: #fef0f0;
  color: #f56c6c;
}
.todo-item .priority-tag.medium {
  background: #fdf6ec;
  color: #e6a23c;
}
.todo-item .priority-tag.low {
  background: #f0f9eb;
  color: #67c23a;
}
.todo-item .action-btn {
  color: #c0c4cc;
  cursor: pointer;
  transition: color 0.2s;
  font-size: 14px;
  padding: 4px;
}
.todo-item .action-btn:hover {
  color: #409EFF;
}

@media (max-width: 768px) {
  .welcome-card .greeting {
    font-size: 20px;
  }
  .welcome-card .status-row {
    gap: 14px;
  }
  .welcome-card .ai-input-area {
    flex-wrap: wrap;
    border: none;
    padding: 0;
  }
  .welcome-card .ai-input-area input {
    width: 100%;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    padding: 8px 12px;
    margin-bottom: 6px;
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
    padding: 5px 12px;
  }
}
</style>
