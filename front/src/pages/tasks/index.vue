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
              <td colspan="6" class="empty-cell">所有待办已完成！</td>
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
.tasks-page { display: flex; flex-direction: column; gap: 12px; }

.tab-row {
  display: flex;
  gap: 0;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 4px;
  width: fit-content;
}
.tab-row button {
  padding: 6px 20px;
  border: none;
  border-radius: 4px;
  background: transparent;
  color: #606266;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}
.tab-row button.active {
  background: #409EFF;
  color: #fff;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 16px;
  border: none;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-primary {
  background: #409EFF;
  color: #fff;
  align-self: flex-start;
}
.btn-primary:hover { background: #66b1ff; }
.btn-cancel {
  background: #f4f4f5;
  color: #606266;
}
.btn-cancel:hover { background: #e6e6e8; }

.table-wrapper {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  overflow: hidden;
}
.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}
.data-table thead {
  background: #f5f7fa;
}
.data-table th {
  padding: 10px 14px;
  text-align: left;
  font-weight: 600;
  color: #303133;
  border-bottom: 1px solid #ebeef5;
  white-space: nowrap;
}
.data-table td {
  padding: 10px 14px;
  border-bottom: 1px solid #ebeef5;
  color: #606266;
}
.data-table tbody tr:hover { background: #f5f7fa; }
.empty-cell { text-align: center; padding: 40px 0; color: #c0c4cc; }

.priority-tag {
  font-size: 11px; padding: 2px 8px; border-radius: 4px;
}
.priority-tag.高 { background: #fef0f0; color: #f56c6c; }
.priority-tag.中 { background: #fdf6ec; color: #e6a23c; }
.priority-tag.低 { background: #f0f9eb; color: #67c23a; }

.status-tag {
  font-size: 11px; padding: 2px 8px; border-radius: 4px;
}
.status-tag.待处理 { background: #fdf6ec; color: #e6a23c; }
.status-tag.进行中 { background: #ecf5ff; color: #409EFF; }
.status-tag.已完成 { background: #f0f9eb; color: #67c23a; }

.action-cell { display: flex; gap: 6px; }
.btn-action {
  padding: 4px 12px; border: none; border-radius: 4px; font-size: 12px; cursor: pointer; transition: all 0.2s;
}
.btn-action.done { background: #f0f9eb; color: #67c23a; }
.btn-action.done:hover { background: #e1f3d8; }

/* 表单卡片 */
.form-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  max-width: 560px;
}
.form-item { display: flex; flex-direction: column; gap: 4px; }
.form-item label { font-size: 13px; color: #606266; }
.form-item input, .form-item select, .form-item textarea {
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 13px;
  color: #303133;
  outline: none;
  transition: border-color 0.2s;
  font-family: inherit;
}
.form-item input:focus, .form-item select:focus, .form-item textarea:focus {
  border-color: #409EFF;
}
.form-item select { background: #fff; cursor: pointer; }
.form-item textarea { resize: vertical; min-height: 80px; }

/* 弹窗 */
.dialog-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.35); z-index: 9999;
  display: flex; align-items: center; justify-content: center;
}
.dialog-box {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  width: 480px; max-width: 92vw;
}
.dialog-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 20px; border-bottom: 1px solid #ebeef5;
}
.dialog-header h3 { font-size: 15px; font-weight: 600; color: #303133; margin: 0; }
.dialog-close {
  background: none; border: none; color: #c0c4cc;
  font-size: 20px; cursor: pointer; padding: 0; line-height: 1;
}
.dialog-close:hover { color: #303133; }
.dialog-body {
  padding: 16px 20px; display: flex; flex-direction: column; gap: 10px;
}
.detail-row { font-size: 14px; color: #303133; line-height: 1.6; }
.detail-row .label { color: #909399; }
.dialog-footer {
  display: flex; justify-content: flex-end; gap: 8px;
  padding: 10px 20px 16px; border-top: 1px solid #ebeef5;
}

@media (max-width: 768px) {
  .form-card { max-width: 100%; }
  .data-table { font-size: 12px; }
  .data-table th, .data-table td { padding: 8px; }
}
</style>
