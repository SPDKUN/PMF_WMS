<template>
  <div class="manage-page">
    <!-- 选项卡 -->
    <div class="tab-row">
      <button :class="{ active: activeTab === 'personnel' }" @click="activeTab = 'personnel'">人员列表</button>
      <button :class="{ active: activeTab === 'warehouse' }" @click="activeTab = 'warehouse'">仓库列表</button>
    </div>

    <!-- 人员列表 -->
    <div v-if="activeTab === 'personnel'" class="tab-content">
      <div class="toolbar">
        <button class="btn btn-primary" @click="openPersonnelDialog()">
          <el-icon><Plus /></el-icon> 新增人员
        </button>
      </div>
      <div class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>姓名</th>
              <th>手机号</th>
              <th>邮箱</th>
              <th>角色</th>
              <th>状态</th>
              <th width="160">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="personnelList.length === 0">
              <td colspan="7" class="empty-cell">暂无人员数据</td>
            </tr>
            <tr v-for="p in personnelList" :key="p.id">
              <td>{{ p.id }}</td>
              <td>{{ p.name }}</td>
              <td>{{ p.phone }}</td>
              <td>{{ p.email }}</td>
              <td>{{ p.role }}</td>
              <td><span class="status-tag" :class="p.status">{{ p.status === '启用' ? '启用' : '禁用' }}</span></td>
              <td class="action-cell">
                <button class="btn-action edit" @click="openPersonnelDialog(p)">编辑</button>
                <button class="btn-action delete" @click="deletePersonnel(p.id)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 仓库列表 -->
    <div v-if="activeTab === 'warehouse'" class="tab-content">
      <div class="toolbar">
        <button class="btn btn-primary" @click="openWarehouseDialog()">
          <el-icon><Plus /></el-icon> 新增仓库
        </button>
      </div>
      <div class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>仓库名称</th>
              <th>地址</th>
              <th>面积(m²)</th>
              <th>库位数</th>
              <th>状态</th>
              <th width="160">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="warehouseList.length === 0">
              <td colspan="7" class="empty-cell">暂无仓库数据</td>
            </tr>
            <tr v-for="w in warehouseList" :key="w.id">
              <td>{{ w.id }}</td>
              <td>{{ w.name }}</td>
              <td>{{ w.address }}</td>
              <td>{{ w.area }}</td>
              <td>{{ w.slots }}</td>
              <td><span class="status-tag" :class="w.status">{{ w.status === '启用' ? '启用' : '停用' }}</span></td>
              <td class="action-cell">
                <button class="btn-action edit" @click="openWarehouseDialog(w)">编辑</button>
                <button class="btn-action delete" @click="deleteWarehouse(w.id)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 人员弹窗 -->
    <div class="dialog-overlay" v-if="personnelDialog.visible" @click.self="personnelDialog.visible = false">
      <div class="dialog-box">
        <div class="dialog-header">
          <h3>{{ personnelDialog.mode === 'create' ? '新增人员' : '编辑人员' }}</h3>
          <button class="dialog-close" @click="personnelDialog.visible = false">&times;</button>
        </div>
        <div class="dialog-body">
          <div class="form-item">
            <label>姓名</label>
            <input type="text" v-model="personnelDialog.form.name" placeholder="请输入姓名" />
          </div>
          <div class="form-item">
            <label>手机号</label>
            <input type="text" v-model="personnelDialog.form.phone" placeholder="请输入手机号" />
          </div>
          <div class="form-item">
            <label>邮箱</label>
            <input type="text" v-model="personnelDialog.form.email" placeholder="请输入邮箱" />
          </div>
          <div class="form-item">
            <label>角色</label>
            <select v-model="personnelDialog.form.role">
              <option value="管理员">管理员</option>
              <option value="仓管员">仓管员</option>
              <option value="操作员">操作员</option>
              <option value="质检员">质检员</option>
            </select>
          </div>
          <div class="form-item">
            <label>状态</label>
            <select v-model="personnelDialog.form.status">
              <option value="启用">启用</option>
              <option value="禁用">禁用</option>
            </select>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-cancel" @click="personnelDialog.visible = false">取消</button>
          <button class="btn btn-primary" @click="submitPersonnel">确认</button>
        </div>
      </div>
    </div>

    <!-- 仓库弹窗 -->
    <div class="dialog-overlay" v-if="warehouseDialog.visible" @click.self="warehouseDialog.visible = false">
      <div class="dialog-box">
        <div class="dialog-header">
          <h3>{{ warehouseDialog.mode === 'create' ? '新增仓库' : '编辑仓库' }}</h3>
          <button class="dialog-close" @click="warehouseDialog.visible = false">&times;</button>
        </div>
        <div class="dialog-body">
          <div class="form-item">
            <label>仓库名称</label>
            <input type="text" v-model="warehouseDialog.form.name" placeholder="请输入仓库名称" />
          </div>
          <div class="form-item">
            <label>地址</label>
            <input type="text" v-model="warehouseDialog.form.address" placeholder="请输入地址" />
          </div>
          <div class="form-item">
            <label>面积(m²)</label>
            <input type="number" v-model="warehouseDialog.form.area" placeholder="请输入面积" />
          </div>
          <div class="form-item">
            <label>库位数</label>
            <input type="number" v-model="warehouseDialog.form.slots" placeholder="请输入库位数" />
          </div>
          <div class="form-item">
            <label>状态</label>
            <select v-model="warehouseDialog.form.status">
              <option value="启用">启用</option>
              <option value="停用">停用</option>
            </select>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-cancel" @click="warehouseDialog.visible = false">取消</button>
          <button class="btn btn-primary" @click="submitWarehouse">确认</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { Plus } from '@element-plus/icons-vue'
import { ElIcon } from 'element-plus'

export default {
  name: 'ManagePage',
  components: { Plus, ElIcon },
  data() {
    return {
      activeTab: 'personnel',
      personnelList: [
        { id: 1, name: '张三', phone: '13800001111', email: 'zhangsan@wms.com', role: '管理员', status: '启用' },
        { id: 2, name: '李四', phone: '13800002222', email: 'lisi@wms.com', role: '仓管员', status: '启用' },
        { id: 3, name: '王五', phone: '13800003333', email: 'wangwu@wms.com', role: '操作员', status: '启用' },
        { id: 4, name: '赵六', phone: '13800004444', email: 'zhaoliu@wms.com', role: '质检员', status: '禁用' },
      ],
      warehouseList: [
        { id: 1, name: 'A区冷库', address: '园区北路88号', area: 1200, slots: 80, status: '启用' },
        { id: 2, name: 'B区常温库', address: '园区南路66号', area: 2000, slots: 150, status: '启用' },
        { id: 3, name: 'C区冷冻库', address: '园区东路33号', area: 800, slots: 50, status: '停用' },
      ],
      nextPersonnelId: 5,
      nextWarehouseId: 4,
      personnelDialog: {
        visible: false,
        mode: 'create',
        form: { id: null, name: '', phone: '', email: '', role: '仓管员', status: '启用' }
      },
      warehouseDialog: {
        visible: false,
        mode: 'create',
        form: { id: null, name: '', address: '', area: '', slots: '', status: '启用' }
      }
    }
  },
  methods: {
    // 人员 CRUD
    openPersonnelDialog(item) {
      if (item) {
        this.personnelDialog.mode = 'edit'
        this.personnelDialog.form = { ...item }
      } else {
        this.personnelDialog.mode = 'create'
        this.personnelDialog.form = { id: null, name: '', phone: '', email: '', role: '仓管员', status: '启用' }
      }
      this.personnelDialog.visible = true
    },
    submitPersonnel() {
      const f = this.personnelDialog.form
      if (!f.name) { alert('请输入姓名'); return }
      if (this.personnelDialog.mode === 'create') {
        this.personnelList.push({ ...f, id: this.nextPersonnelId++ })
      } else {
        const idx = this.personnelList.findIndex(p => p.id === f.id)
        if (idx !== -1) this.personnelList.splice(idx, 1, { ...f })
      }
      this.personnelDialog.visible = false
    },
    deletePersonnel(id) {
      if (confirm('确定删除该人员？')) {
        this.personnelList = this.personnelList.filter(p => p.id !== id)
      }
    },

    // 仓库 CRUD
    openWarehouseDialog(item) {
      if (item) {
        this.warehouseDialog.mode = 'edit'
        this.warehouseDialog.form = { ...item }
      } else {
        this.warehouseDialog.mode = 'create'
        this.warehouseDialog.form = { id: null, name: '', address: '', area: '', slots: '', status: '启用' }
      }
      this.warehouseDialog.visible = true
    },
    submitWarehouse() {
      const f = this.warehouseDialog.form
      if (!f.name) { alert('请输入仓库名称'); return }
      if (this.warehouseDialog.mode === 'create') {
        this.warehouseList.push({ ...f, id: this.nextWarehouseId++ })
      } else {
        const idx = this.warehouseList.findIndex(w => w.id === f.id)
        if (idx !== -1) this.warehouseList.splice(idx, 1, { ...f })
      }
      this.warehouseDialog.visible = false
    },
    deleteWarehouse(id) {
      if (confirm('确定删除该仓库？')) {
        this.warehouseList = this.warehouseList.filter(w => w.id !== id)
      }
    }
  }
}
</script>

<style scoped>
.manage-page { display: flex; flex-direction: column; gap: 16px; }

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

.toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
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

.status-tag {
  font-size: 11px;
  padding: 2px 10px;
  border-radius: 20px;
  font-weight: 500;
}
.status-tag.启用 { background: rgba(52,211,153,0.15); color: var(--glow-green); }
.status-tag.禁用, .status-tag.停用 { background: rgba(244,114,182,0.12); color: var(--glow-pink); }

.action-cell { display: flex; gap: 8px; }
.btn-action {
  padding: 4px 12px;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
  transition: var(--transition);
}
.btn-action.edit { background: rgba(79,172,254,0.15); color: var(--glow-blue); }
.btn-action.edit:hover { background: rgba(79,172,254,0.25); }
.btn-action.delete { background: rgba(244,114,182,0.12); color: var(--glow-pink); }
.btn-action.delete:hover { background: rgba(244,114,182,0.22); }

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
  width: 460px; max-width: 92vw;
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
.dialog-body { padding: 20px 24px; display: flex; flex-direction: column; gap: 14px; }
.dialog-footer {
  display: flex; justify-content: flex-end; gap: 10px;
  padding: 12px 24px 20px; border-top: 1px solid rgba(255,255,255,0.06);
}
.form-item { display: flex; flex-direction: column; gap: 4px; }
.form-item label { font-size: 13px; color: var(--text-secondary); font-weight: 500; }
.form-item input, .form-item select {
  height: 38px; padding: 0 12px;
  background: rgba(255,255,255,0.04);
  border: 1px solid rgba(255,255,255,0.08);
  border-radius: 8px; font-size: 13px; color: #fff;
  transition: var(--transition);
}
.form-item input:focus, .form-item select:focus {
  outline: none; border-color: var(--glow-blue);
}
.form-item select { cursor: pointer; }
.form-item select option { background: #1a1d2e; color: #fff; }

@media (max-width: 768px) {
  .data-table { font-size: 12px; }
  .data-table th, .data-table td { padding: 8px 10px; }
  .action-cell { flex-direction: column; gap: 4px; }
}
</style>
