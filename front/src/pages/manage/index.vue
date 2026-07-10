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
        <div class="search-bar">
          <select v-model="searchType" class="search-type">
            <option value="user_id">按ID</option>
            <option value="real_name">按姓名</option>
          </select>
          <input
            type="text"
            v-model="searchKeyword"
            placeholder="请输入搜索关键词"
            @keyup.enter="searchPersonnel"
            class="search-input"
          />
          <button class="btn btn-search" @click="searchPersonnel">
            <el-icon><Search /></el-icon> 搜索
          </button>
          <button class="btn btn-cancel" @click="resetSearch">重置</button>
        </div>
      </div>
      <div class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>用户名</th>
              <th>姓名</th>
              <th>手机号</th>
              <th>所属部门</th>
              <th>职位</th>
              <th>账号状态</th>
              <th width="200">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="personnelList.length === 0">
              <td colspan="8" class="empty-cell">暂无人员数据</td>
            </tr>
            <tr v-for="p in personnelList" :key="p.user_id">
              <td>{{ p.user_id }}</td>
              <td>{{ p.username }}</td>
              <td>{{ p.real_name }}</td>
              <td>{{ p.phone }}</td>
              <td>{{ p.department }}</td>
              <td>{{ p.position }}</td>
              <td><span class="status-tag" :class="p.status === 1 ? '启用' : '禁用'">{{ p.status === 1 ? '启用' : '禁用' }}</span></td>
              <td class="action-cell">
                <button class="btn-action edit" @click="openPersonnelDialog(p)">编辑</button>
                <button class="btn-action delete" @click="deletePersonnel(p.user_id)">删除</button>
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
            <label>用户名 <span class="required">*</span></label>
            <input type="text" v-model="personnelDialog.form.username" placeholder="请输入用户名" />
          </div>
          <div class="form-item">
            <label>姓名 <span class="required">*</span></label>
            <input type="text" v-model="personnelDialog.form.real_name" placeholder="请输入姓名" />
          </div>
          <div class="form-item">
            <label>手机号 <span class="required">*</span></label>
            <input type="text" v-model="personnelDialog.form.phone" placeholder="请输入手机号" />
          </div>
          <div class="form-item">
            <label>所属部门 <span class="required">*</span></label>
            <input type="text" v-model="personnelDialog.form.department" placeholder="请输入所属部门" />
          </div>
          <div class="form-item">
            <label>职位 <span class="required">*</span></label>
            <input type="text" v-model="personnelDialog.form.position" placeholder="请输入职位" />
          </div>
          <div class="form-item" v-if="personnelDialog.mode === 'edit'">
            <button class="btn btn-reset-pwd" @click="resetPassword">重置密码</button>
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

    <!-- 重置密码确认弹窗 -->
    <div class="dialog-overlay" v-if="confirmDialog.visible" @click.self="confirmDialog.visible = false">
      <div class="dialog-box confirm-box">
        <div class="dialog-header">
          <h3>提示</h3>
          <button class="dialog-close" @click="confirmDialog.visible = false">&times;</button>
        </div>
        <div class="dialog-body" style="text-align:center;padding:24px;">
          <p style="color:#fff;font-size:14px;margin:0;">确定将该用户的密码重置为 123456 吗？</p>
        </div>
        <div class="dialog-footer" style="justify-content:center;">
          <button class="btn btn-cancel" @click="confirmDialog.visible = false">取消</button>
          <button class="btn btn-primary" @click="doResetPassword">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import { Plus, Search } from '@element-plus/icons-vue'
import { ElIcon, ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: 'http://localhost:8088'
})

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export default {
  name: 'ManagePage',
  components: { Plus, Search, ElIcon },
  data() {
    return {
      activeTab: 'personnel',
      allPersonnel: [],
      personnelList: [],
      searchType: 'user_id',
      searchKeyword: '',
      warehouseList: [
        { id: 1, name: 'A区冷库', address: '园区北路88号', area: 1200, slots: 80, status: '启用' },
        { id: 2, name: 'B区常温库', address: '园区南路66号', area: 2000, slots: 150, status: '启用' },
        { id: 3, name: 'C区冷冻库', address: '园区东路33号', area: 800, slots: 50, status: '停用' },
      ],
      nextWarehouseId: 4,
      personnelDialog: {
        visible: false,
        mode: 'create',
        form: { user_id: null, username: '', real_name: '', phone: '', department: '', position: '' }
      },
      confirmDialog: {
        visible: false
      },
      warehouseDialog: {
        visible: false,
        mode: 'create',
        form: { id: null, name: '', address: '', area: '', slots: '', status: '启用' }
      }
    }
  },
  mounted() {
    this.fetchPersonnel()
  },
  methods: {
    async fetchPersonnel() {
      try {
        const res = await api.get('/user/personnel')
        if (res.data.code === 200) {
          this.allPersonnel = res.data.data || []
          this.personnelList = this.allPersonnel
        }
      } catch (e) {
        ElMessage.error('获取人员列表失败')
      }
    },

    searchPersonnel() {
      const keyword = this.searchKeyword.trim()
      if (!keyword) {
        this.personnelList = this.allPersonnel
        return
      }
      if (this.searchType === 'user_id') {
        const id = parseInt(keyword)
        if (isNaN(id)) {
          ElMessage.warning('请输入有效的数字ID')
          return
        }
        this.personnelList = this.allPersonnel.filter(p => p.user_id === id)
      } else {
        this.personnelList = this.allPersonnel.filter(p =>
          p.real_name && p.real_name.includes(keyword)
        )
      }
    },

    resetSearch() {
      this.searchKeyword = ''
      this.personnelList = this.allPersonnel
    },

    openPersonnelDialog(item) {
      if (item) {
        this.personnelDialog.mode = 'edit'
        this.personnelDialog.form = {
          user_id: item.user_id,
          username: item.username,
          real_name: item.real_name,
          phone: item.phone,
          department: item.department,
          position: item.position
        }
      } else {
        this.personnelDialog.mode = 'create'
        this.personnelDialog.form = { user_id: null, username: '', real_name: '', phone: '', department: '', position: '' }
      }
      this.personnelDialog.visible = true
    },

    async submitPersonnel() {
      const f = this.personnelDialog.form
      if (!f.username) { ElMessage.warning('请输入用户名'); return }
      if (!f.real_name) { ElMessage.warning('请输入姓名'); return }
      if (!f.phone) { ElMessage.warning('请输入手机号'); return }
      if (!f.department) { ElMessage.warning('请输入所属部门'); return }
      if (!f.position) { ElMessage.warning('请输入职位'); return }

      if (this.personnelDialog.mode === 'create') {
        const maxId = this.allPersonnel.length > 0
          ? Math.max(...this.allPersonnel.map(p => p.user_id))
          : 0
        try {
          const res = await api.post('/user', {
            user_id: maxId + 1,
            username: f.username,
            real_name: f.real_name,
            phone: f.phone,
            department: f.department,
            position: f.position,
            password: '123456',
            status: 1
          })
          if (res.data.code === 200) {
            ElMessage.success('新增成功')
            this.personnelDialog.visible = false
            this.fetchPersonnel()
          } else {
            ElMessage.error(res.data.msg || '新增失败')
          }
        } catch (e) {
          ElMessage.error('新增失败')
        }
      } else {
        try {
          const res = await api.put('/user', {
            user_id: f.user_id,
            username: f.username,
            real_name: f.real_name,
            phone: f.phone,
            department: f.department,
            position: f.position
          })
          if (res.data.code === 200) {
            ElMessage.success('修改成功')
            this.personnelDialog.visible = false
            this.fetchPersonnel()
          } else {
            ElMessage.error(res.data.msg || '修改失败')
          }
        } catch (e) {
          ElMessage.error('修改失败')
        }
      }
    },

    deletePersonnel(id) {
      if (!confirm('确定删除该人员？')) return
      api.delete(`/user/${id}`).then(res => {
        if (res.data.code === 200) {
          ElMessage.success('删除成功')
          this.fetchPersonnel()
        } else {
          ElMessage.error(res.data.msg || '删除失败')
        }
      }).catch(() => {
        ElMessage.error('删除失败')
      })
    },

    resetPassword() {
      this.confirmDialog.visible = true
    },
    async doResetPassword() {
      try {
        const res = await api.put('/user', {
          user_id: this.personnelDialog.form.user_id,
          password: '123456'
        })
        if (res.data.code === 200) {
          ElMessage.success('密码重置成功')
          this.confirmDialog.visible = false
        } else {
          ElMessage.error(res.data.msg || '密码重置失败')
        }
      } catch (e) {
        ElMessage.error('密码重置失败')
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
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 8px;
}
.search-type {
  height: 34px;
  padding: 0 10px;
  background: rgba(255,255,255,0.04);
  border: 1px solid rgba(255,255,255,0.08);
  border-radius: 8px;
  font-size: 13px;
  color: #fff;
  cursor: pointer;
}
.search-type option { background: #1a1d2e; color: #fff; }
.search-input {
  height: 34px;
  width: 200px;
  padding: 0 12px;
  background: rgba(255,255,255,0.04);
  border: 1px solid rgba(255,255,255,0.08);
  border-radius: 8px;
  font-size: 13px;
  color: #fff;
  transition: var(--transition);
}
.search-input:focus { outline: none; border-color: var(--glow-blue); }
.search-input::placeholder { color: var(--text-dim); }

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
.btn-search {
  background: rgba(79,172,254,0.15);
  color: var(--glow-blue);
}
.btn-search:hover { background: rgba(79,172,254,0.25); }
.btn-reset-pwd {
  background: rgba(244,114,182,0.12);
  color: var(--glow-pink);
  width: 100%;
}
.btn-reset-pwd:hover { background: rgba(244,114,182,0.22); }

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
.confirm-box {
  width: 360px;
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
.form-item .required { color: var(--glow-pink); }
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
  .toolbar { flex-direction: column; align-items: flex-start; }
  .search-input { width: 140px; }
}
</style>
