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
      <!-- 操作选项卡 -->
      <div class="op-tabs">
        <button
          v-for="op in operations"
          :key="op.key"
          :class="{ active: activeOp === op.key }"
          :style="activeOp === op.key
            ? { background: op.color, color: '#fff', borderColor: op.color }
            : { color: op.color, borderColor: op.color }"
          @click="switchOp(op.key)"
        >{{ op.label }}</button>
      </div>

      <!-- 新增批次 -->
      <div v-if="activeOp === 'newBatch'" class="op-content">
        <div class="form-card">
          <p class="op-desc">创建新的货物批次，生成批次号并关联货物信息。</p>
          <button class="btn btn-primary" @click="openNewBatchDialog">新增批次</button>

          <!-- 最近批次列表 -->
          <div class="table-wrapper" style="margin-top:16px;" v-if="batchList.length > 0">
            <table class="data-table">
              <thead>
                <tr>
                  <th>批次号</th>
                  <th>货物名称</th>
                  <th>生产日期</th>
                  <th>到期日期</th>
                  <th>数量</th>
                  <th>状态</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="b in batchList" :key="b.batch_id">
                  <td>{{ b.batch_id }}</td>
                  <td>{{ getGoodsName(b.goods_id) }}</td>
                  <td>{{ b.production_date }}</td>
                  <td>{{ b.expiry_date }}</td>
                  <td>{{ b.remaining_quantity }}</td>
                  <td><span class="status-tag" :class="getBatchStatusClass(b.batch_status)">{{ b.batch_status }}</span></td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- 其他操作（占位） -->
      <div v-else class="op-content">
        <div class="placeholder-card">{{ getOpDesc(activeOp) }}</div>
      </div>
    </div>

    <!-- 新增批次弹窗 -->
    <div class="dialog-overlay" v-if="newBatchDialog.visible" @click.self="newBatchDialog.visible = false">
      <div class="dialog-box">
        <div class="dialog-header">
          <h3>新增批次</h3>
          <button class="dialog-close" @click="newBatchDialog.visible = false">&times;</button>
        </div>
        <div class="dialog-body">
          <div class="form-item">
            <label>选择货物 <span class="required">*</span></label>
            <select v-model="newBatchDialog.form.goods_id">
              <option :value="null">请选择货物</option>
              <option v-for="g in goodsList" :key="g.goods_id" :value="g.goods_id">
                {{ g.goods_name }} ({{ g.goods_code }})
              </option>
            </select>
          </div>
          <div class="form-item">
            <label>生产日期 <span class="required">*</span></label>
            <input type="date" v-model="newBatchDialog.form.production_date" />
          </div>
          <div class="form-item">
            <label>保质期至 <span class="required">*</span></label>
            <input type="date" v-model="newBatchDialog.form.expiry_date" />
          </div>
          <div class="form-item">
            <label>入库初始数量 <span class="required">*</span></label>
            <input type="number" v-model.number="newBatchDialog.form.initial_quantity" min="1" placeholder="请输入入库初始数量" />
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-cancel" @click="newBatchDialog.visible = false">取消</button>
          <button class="btn btn-primary" @click="submitNewBatch">确认创建</button>
        </div>
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
import request from '@/utils/request.js'

export default {
  name: 'TasksPage',
  data() {
    return {
      activeTab: 'todo',
      activeOp: 'newBatch',
      operations: [
        { key: 'newBatch', label: '新增批次', color: '#409EFF' },
        { key: 'inbound', label: '入库', color: '#67c23a' },
        { key: 'outbound', label: '出库', color: '#e6a23c' },
        { key: 'adjust', label: '库存调整', color: '#9065db' },
        { key: 'check', label: '库存盘点', color: '#20a0ff' },
        { key: 'quality', label: '质检', color: '#f56c6c' },
        { key: 'defective', label: '处理次品', color: '#f39c12' },
      ],
      todoList: [
        { id: 1, title: '采购入库单 #IN20260710-003', type: '入库', priority: '高', deadline: '2026-07-11', status: '待处理', description: '供应商A的预制菜原料入库，共120箱，需质检后入库A区冷库。' },
        { id: 2, title: '出库单 #OUT20260710-087', type: '出库', priority: '高', deadline: '2026-07-10', status: '待处理', description: '客户B的销售出库单，预制菜A共50箱，需从A区冷库拣货出库。' },
        { id: 3, title: '库存调整 #ADJ20260710-022', type: '移库', priority: '中', deadline: '2026-07-12', status: '待处理', description: '将调味料C 100袋从B区常温库移至A区冷库暂存区。' },
        { id: 4, title: '质检任务 #QC20260710-015', type: '质检', priority: '中', deadline: '2026-07-11', status: '进行中', description: '新到冷冻食材D来料质检，抽样20件进行微生物及感官检测。' },
        { id: 5, title: '盘点单 #CNT20260710-005', type: '盘点', priority: '低', deadline: '2026-07-15', status: '待处理', description: 'A区冷库月度全盘，需复核系统数据与实际库存差异。' },
        { id: 6, title: '不合格处理 #DEF20260710-011', type: '质检', priority: '低', deadline: '2026-07-13', status: '待处理', description: '批次B20260701-005质检不合格，需确认报废并记录。' },
      ],
      detailVisible: false,
      currentDetail: {},
      nextId: 7,
      goodsList: [],
      batchList: [],
      newBatchDialog: {
        visible: false,
        form: { goods_id: null, production_date: '', expiry_date: '', initial_quantity: 0 }
      }
    }
  },
  mounted() {
    this.fetchGoods()
    this.fetchBatches()
  },
  methods: {
    switchOp(key) {
      this.activeOp = key
      if (key === 'newBatch') {
        this.fetchBatches()
      }
    },
    getOpDesc(key) {
      const map = {
        inbound: '入库功能开发中...',
        outbound: '出库功能开发中...',
        adjust: '库存调整功能开发中...',
        check: '库存盘点功能开发中...',
        quality: '质检功能开发中...',
        defective: '处理次品功能开发中...',
      }
      return map[key] || '功能开发中...'
    },
    getGoodsName(goodsId) {
      const g = this.goodsList.find(item => item.goods_id === goodsId)
      return g ? g.goods_name : '-'
    },
    getBatchStatusClass(status) {
      if (status === '正常') return 'status-normal'
      if (status === '待检') return 'status-pending'
      if (status === '锁定') return 'status-locked'
      if (status === '报废') return 'status-scrap'
      return ''
    },
    openDetail(item) {
      this.currentDetail = { ...item }
      this.detailVisible = true
    },
    completeTask(id) {
      this.todoList = this.todoList.filter(t => t.id !== id)
    },
    async fetchGoods() {
      try {
        const res = await request.get('/goods/list')
        if (res.code === 200) {
          this.goodsList = res.data || []
        }
      } catch (e) {
        // 静默失败
      }
    },
    async fetchBatches() {
      try {
        const res = await request.get('/batch/list')
        if (res.code === 200) {
          this.batchList = res.data || []
        }
      } catch (e) {
        // 静默失败
      }
    },
    openNewBatchDialog() {
      const today = new Date().toISOString().slice(0, 10)
      this.newBatchDialog.form = {
        goods_id: null,
        production_date: today,
        expiry_date: '',
        initial_quantity: 0
      }
      this.newBatchDialog.visible = true
    },
    async submitNewBatch() {
      const f = this.newBatchDialog.form
      if (!f.goods_id) { alert('请选择货物'); return }
      if (!f.production_date) { alert('请选择生产日期'); return }
      if (!f.expiry_date) { alert('请选择保质期至'); return }
      if (!f.initial_quantity || f.initial_quantity <= 0) { alert('请输入有效的入库初始数量'); return }

      try {
        const res = await request.post('/batch', {
          goods_id: f.goods_id,
          production_date: f.production_date,
          expiry_date: f.expiry_date,
          initial_quantity: f.initial_quantity
        })
        if (res.code === 200) {
          alert('批次创建成功')
          this.newBatchDialog.visible = false
          this.fetchBatches()
        } else {
          alert(res.msg || '创建失败')
        }
      } catch (e) {
        alert('创建失败')
      }
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

/* 操作选项卡 */
.op-tabs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.op-tabs button {
  padding: 6px 16px;
  border: 2px solid;
  border-radius: 4px;
  background: #fff;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}
.op-tabs button:hover {
  opacity: 0.8;
}

.op-content {
  margin-top: 4px;
}
.op-desc {
  font-size: 13px;
  color: #909399;
  margin: 0 0 12px 0;
}

.placeholder-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 40px;
  text-align: center;
  color: #c0c4cc;
  font-size: 14px;
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
  overflow-x: auto;
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
.status-tag.status-normal { background: #f0f9eb; color: #67c23a; }
.status-tag.status-pending { background: #fdf6ec; color: #e6a23c; }
.status-tag.status-locked { background: #ecf5ff; color: #409EFF; }
.status-tag.status-scrap { background: #fef0f0; color: #f56c6c; }

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
}
.form-item { display: flex; flex-direction: column; gap: 4px; margin-bottom: 12px; }
.form-item label { font-size: 13px; color: #606266; }
.form-item .required { color: #f56c6c; }
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
  padding: 16px 20px; display: flex; flex-direction: column;
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
  .op-tabs { gap: 4px; }
  .op-tabs button { padding: 5px 10px; font-size: 12px; }
}
</style>
