<template>
  <div class="tasks-page">
    <!-- 选项卡 -->
    <div class="tab-row">
      <button :class="{ active: activeTab === 'todo' }" @click="activeTab = 'todo'">我的待办</button>
      <button :class="{ active: activeTab === 'assign' }" @click="activeTab = 'assign'">布置任务</button>
    </div>

    <!-- 我的待办 -->
    <div v-if="activeTab === 'todo'" class="tab-content">
      <div class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>任务名称</th>
              <th>类型</th>
              <th>优先级</th>
              <th>截止日期</th>
              <th>状态</th>
              <th width="120">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="todoList.length === 0">
              <td colspan="6" class="empty-cell">🎉 所有待办已完成！</td>
            </tr>
            <tr v-for="item in todoList" :key="item.id" @click="openDetail(item)" style="cursor:pointer;">
              <td>{{ item.title }}</td>
              <td>{{ item.type }}</td>
              <td><span class="priority-tag" :class="item.priority">{{ item.priority }}</span></td>
              <td>{{ item.deadline }}</td>
              <td><span class="status-tag" :class="item.status">{{ item.status }}</span></td>
              <td class="action-cell" @click.stop>
                <button class="btn-action done" @click="completeTask(item.id)">完成</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 布置任务 -->
    <div v-if="activeTab === 'assign'" class="tab-content">
      <div class="form-card">
        <div class="form-item">
          <label>任务名称</label>
          <input type="text" v-model="taskForm.title" placeholder="请输入任务名称" />
        </div>
        <div class="form-item">
          <label>任务类型</label>
          <select v-model="taskForm.type">
            <option value="入库">入库</option>
            <option value="出库">出库</option>
            <option value="盘点">盘点</option>
            <option value="质检">质检</option>
            <option value="移库">移库</option>
          </select>
        </div>
        <div class="form-item">
          <label>优先级</label>
          <select v-model="taskForm.priority">
            <option value="高">高</option>
            <option value="中">中</option>
            <option value="低">低</option>
          </select>
        </div>
        <div class="form-item">
          <label>截止日期</label>
          <input type="date" v-model="taskForm.deadline" />
        </div>
        <div class="form-item">
          <label>指派给</label>
          <input type="text" v-model="taskForm.assignee" placeholder="请输入指派人" />
        </div>
        <div class="form-item">
          <label>任务描述</label>
          <textarea v-model="taskForm.description" placeholder="请输入任务描述..." rows="4"></textarea>
        </div>
        <button class="btn btn-primary" @click="publishTask">发布任务</button>
      </div>
    </div>

    <!-- 待办详情弹窗 -->
    <div class="dialog-overlay" v-if="detailVisible" @click.self="detailVisible = false">
      <div class="dialog-box">
        <div class="dialog-header">
          <h3>任务详情</h3>
          <button class="dialog-close" @click="detailVisible = false">&times;</button>
        </div>
        <div class="dialog-body">
          <div class="detail-row"><span class="label">任务名称：</span><span>{{ currentDetail.title }}</span></div>
          <div class="detail-row"><span class="label">任务类型：</span><span>{{ currentDetail.type }}</span></div>
          <div class="detail-row"><span class="label">优先级：</span><span class="priority-tag" :class="currentDetail.priority">{{ currentDetail.priority }}</span></div>
          <div class="detail-row"><span class="label">截止日期：</span><span>{{ currentDetail.deadline }}</span></div>
          <div class="detail-row"><span class="label">状态：</span><span class="status-tag" :class="currentDetail.status">{{ currentDetail.status }}</span></div>
          <div class="detail-row"><span class="label">描述：</span><span>{{ currentDetail.description || '暂无描述' }}</span></div>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-cancel" @click="detailVisible = false">关闭</button>
          <button class="btn btn-primary" @click="completeTask(currentDetail.id); detailVisible = false;">完成任务</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'TasksPage',
  data() {
    return {
      activeTab: 'todo',
      todoList: [
        { id: 1, title: '采购入库单 #IN20260710-003', type: '入库', priority: '高', deadline: '2026-07-11', status: '待处理', description: '供应商A的预制菜原料入库，共120箱，需质检后入库A区冷库。' },
        { id: 2, title: '出库单 #OUT20260710-087', type: '出库', priority: '高', deadline: '2026-07-10', status: '待处理', description: '客户B的销售出库单，预制菜A共50箱，需从A区冷库拣货出库。' },
        { id: 3, title: '库存调整 #ADJ20260710-022', type: '移库', priority: '中', deadline: '2026-07-12', status: '待处理', description: '将调味料C 100袋从B区常温库移至A区冷库暂存区。' },
        { id: 4, title: '质检任务 #QC20260710-015', type: '质检', priority: '中', deadline: '2026-07-11', status: '进行中', description: '新到冷冻食材D来料质检，抽样20件进行微生物及感官检测。' },
        { id: 5, title: '盘点单 #CNT20260710-005', type: '盘点', priority: '低', deadline: '2026-07-15', status: '待处理', description: 'A区冷库月度全盘，需复核系统数据与实际库存差异。' },
        { id: 6, title: '不合格处理 #DEF20260710-011', type: '质检', priority: '低', deadline: '2026-07-13', status: '待处理', description: '批次B20260701-005质检不合格，需确认报废并记录。' },
      ],
      taskForm: {
        title: '', type: '入库', priority: '中', deadline: '', assignee: '', description: ''
      },
      detailVisible: false,
      currentDetail: {},
      nextId: 7,
    }
  },
  methods: {
    openDetail(item) {
      this.currentDetail = { ...item }
      this.detailVisible = true
    },
    completeTask(id) {
      this.todoList = this.todoList.filter(t => t.id !== id)
    },
    publishTask() {
      if (!this.taskForm.title) { alert('请输入任务名称'); return }
      this.todoList.push({
        id: this.nextId++,
        title: this.taskForm.title,
        type: this.taskForm.type,
        priority: this.taskForm.priority,
        deadline: this.taskForm.deadline || '未设定',
        status: '待处理',
        description: this.taskForm.description,
      })
      this.taskForm = { title: '', type: '入库', priority: '中', deadline: '', assignee: '', description: '' }
      this.activeTab = 'todo'
    }
  }
}
</script>

<style scoped>
.tasks-page { display: flex; flex-direction: column; gap: 16px; }

.tab-row {
  display: flex;
  gap: 0;
  background: rgba(255,255,255,0.03);
  border-radius: 14px;
  padding: 4px;
  width: fit-content;
  border: 1px solid rgba(255,255,255,0.05);
}
.tab-row button {
  padding: 8px 24px;
  border: none;
  border-radius: 12px;
  background: transparent;
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: var(--transition);
}
.tab-row button.active {
  background: rgba(79,172,254,0.2);
  color: #fff;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 18px;
  border: none;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: var(--transition);
}
.btn-primary {
  background: linear-gradient(135deg, var(--glow-blue), var(--glow-purple));
  color: #fff;
  align-self: flex-start;
}
.btn-primary:hover { transform: translateY(-1px); box-shadow: 0 4px 16px rgba(79,172,254,0.25); }
.btn-cancel {
  background: rgba(255,255,255,0.06);
  color: var(--text-secondary);
}
.btn-cancel:hover { background: rgba(255,255,255,0.1); }

.table-wrapper {
  background: var(--bg-card);
  backdrop-filter: blur(14px);
  border: 1px solid var(--border-card);
  border-radius: var(--radius-card);
  overflow: hidden;
}
.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}
.data-table thead {
  background: rgba(255,255,255,0.03);
}
.data-table th {
  padding: 12px 16px;
  text-align: left;
  font-weight: 600;
  color: var(--text-secondary);
  border-bottom: 1px solid rgba(255,255,255,0.06);
  white-space: nowrap;
}
.data-table td {
  padding: 12px 16px;
  border-bottom: 1px solid rgba(255,255,255,0.04);
  color: var(--text-primary);
}
.data-table tbody tr:hover { background: rgba(255,255,255,0.02); }
.empty-cell { text-align: center; padding: 40px 0; color: var(--text-dim); }

.priority-tag {
  font-size: 11px; padding: 2px 10px; border-radius: 20px; font-weight: 600;
}
.priority-tag.高 { background: rgba(244,114,182,0.15); color: var(--glow-pink); }
.priority-tag.中 { background: rgba(251,146,60,0.15); color: var(--glow-orange); }
.priority-tag.低 { background: rgba(52,211,153,0.12); color: var(--glow-green); }

.status-tag {
  font-size: 11px; padding: 2px 10px; border-radius: 20px; font-weight: 500;
}
.status-tag.待处理 { background: rgba(251,146,60,0.12); color: var(--glow-orange); }
.status-tag.进行中 { background: rgba(79,172,254,0.15); color: var(--glow-blue); }
.status-tag.已完成 { background: rgba(52,211,153,0.12); color: var(--glow-green); }

.action-cell { display: flex; gap: 8px; }
.btn-action {
  padding: 4px 14px; border: none; border-radius: 6px; font-size: 12px; cursor: pointer; transition: var(--transition);
}
.btn-action.done { background: rgba(52,211,153,0.15); color: var(--glow-green); }
.btn-action.done:hover { background: rgba(52,211,153,0.25); }

/* 表单卡片 */
.form-card {
  background: var(--bg-card);
  backdrop-filter: blur(14px);
  border: 1px solid var(--border-card);
  border-radius: var(--radius-card);
  padding: 24px 28px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-width: 600px;
}
.form-item { display: flex; flex-direction: column; gap: 4px; }
.form-item label { font-size: 13px; color: var(--text-secondary); font-weight: 500; }
.form-item input, .form-item select, .form-item textarea {
  padding: 10px 14px;
  background: rgba(255,255,255,0.04);
  border: 1px solid rgba(255,255,255,0.08);
  border-radius: 8px;
  font-size: 13px;
  color: #fff;
  transition: var(--transition);
  font-family: inherit;
}
.form-item input:focus, .form-item select:focus, .form-item textarea:focus {
  outline: none;
  border-color: var(--glow-blue);
}
.form-item select { cursor: pointer; }
.form-item select option { background: #1a1d2e; color: #fff; }
.form-item textarea { resize: vertical; min-height: 80px; }

/* 弹窗 */
.dialog-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5); z-index: 9999;
  display: flex; align-items: center; justify-content: center;
}
.dialog-box {
  background: #1a1d2e;
  border: 1px solid rgba(255,255,255,0.1);
  border-radius: 16px;
  width: 500px; max-width: 92vw;
  box-shadow: 0 20px 60px rgba(0,0,0,0.5);
}
.dialog-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 16px 24px; border-bottom: 1px solid rgba(255,255,255,0.06);
}
.dialog-header h3 { font-size: 16px; font-weight: 600; color: #fff; }
.dialog-close {
  background: none; border: none; color: var(--text-dim);
  font-size: 22px; cursor: pointer; padding: 0; line-height: 1;
}
.dialog-close:hover { color: #fff; }
.dialog-body {
  padding: 20px 24px; display: flex; flex-direction: column; gap: 12px;
}
.detail-row { font-size: 14px; color: var(--text-primary); line-height: 1.6; }
.detail-row .label { color: var(--text-secondary); }
.dialog-footer {
  display: flex; justify-content: flex-end; gap: 10px;
  padding: 12px 24px 20px; border-top: 1px solid rgba(255,255,255,0.06);
}

@media (max-width: 768px) {
  .form-card { max-width: 100%; }
  .data-table { font-size: 12px; }
  .data-table th, .data-table td { padding: 8px 10px; }
}
</style>
