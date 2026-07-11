<template>
  <div class="user-list-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2>人员列表</h2>
        <div class="breadcrumb">
          <span class="breadcrumb-item" @click="$router.push({ name: 'View' })">查看</span>
          <span class="breadcrumb-separator">/</span>
          <span class="breadcrumb-item active">人员列表</span>
        </div>
      </div>
      <div class="header-right">
        <button class="btn btn-export" @click="handleExport">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M21 15V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M7 10L12 15L17 10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M12 15V3" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          导出Excel
        </button>
        <button class="btn btn-primary" @click="openCreateDialog">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 5V19M5 12H19" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          新增用户
        </button>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <div class="search-item">
        <label>用户名</label>
        <input type="text" v-model="searchForm.userName" placeholder="请输入用户名" @keyup.enter="handleSearch" />
      </div>
      <div class="search-item">
        <label>真实姓名</label>
        <input type="text" v-model="searchForm.realName" placeholder="请输入真实姓名" @keyup.enter="handleSearch" />
      </div>
      <div class="search-actions">
        <button class="btn btn-search" @click="handleSearch">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M21 21L15 15M17 9C17 13.4183 13.4183 17 9 17C4.58172 17 1 13.4183 1 9C1 4.58172 4.58172 1 9 1C13.4183 1 17 4.58172 17 9Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          搜索
        </button>
        <button class="btn btn-reset" @click="resetSearch">重置</button>
      </div>
    </div>

    <!-- 表格 -->
    <div class="table-wrapper">
      <div v-if="loading" class="loading-mask">
        <div class="loading-spinner"></div>
        <span>加载中...</span>
      </div>
      <table class="data-table" v-else>
        <thead>
          <tr>
            <th width="60">ID</th>
            <th>用户名</th>
            <th>真实姓名</th>
            <th>手机号</th>
            <th>邮箱</th>
            <th width="180">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="filteredData.length === 0">
            <td colspan="6" class="empty-cell">
              <span>暂无数据</span>
            </td>
          </tr>
          <tr v-for="user in pagedData" :key="user.user_id">
            <td>{{ user.user_id }}</td>
            <td>{{ user.user_name }}</td>
            <td>{{ user.real_name || '-' }}</td>
            <td>{{ user.phone || '-' }}</td>
            <td>{{ user.email || '-' }}</td>
            <td class="action-cell">
              <button class="btn-action edit" @click="openEditDialog(user)">编辑</button>
              <button class="btn-action delete" @click="confirmDelete(user)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrapper">
      <span class="total-info">共 {{ filteredData.length }} 条</span>
      <div class="pagination">
        <button class="page-btn" :disabled="currentPage <= 1" @click="currentPage--">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M15 18L9 12L15 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
        <span class="page-info">{{ currentPage }} / {{ totalPages }}</span>
        <button class="page-btn" :disabled="currentPage >= totalPages" @click="currentPage++">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M9 18L15 12L9 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
      </div>
      <div class="page-size-select">
        <span>每页</span>
        <select v-model="pageSize">
          <option :value="5">5</option>
          <option :value="10">10</option>
          <option :value="20">20</option>
          <option :value="50">50</option>
        </select>
        <span>条</span>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <div class="dialog-overlay" v-if="dialogVisible" @click.self="closeDialog">
      <div class="dialog-box">
        <div class="dialog-header">
          <h3>{{ dialogMode === 'create' ? '新增用户' : '编辑用户' }}</h3>
          <button class="dialog-close" @click="closeDialog">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>
        <div class="dialog-body">
          <div class="form-item">
            <label><span class="required">*</span>用户名</label>
            <input type="text" v-model="form.user_name" placeholder="请输入用户名" :disabled="dialogMode === 'edit'" />
            <span class="form-hint" v-if="dialogMode === 'edit'">用户名不可修改</span>
          </div>
          <div class="form-item">
            <label><span class="required" v-if="dialogMode === 'create'">*</span>密码</label>
            <input type="password" v-model="form.password" :placeholder="dialogMode === 'create' ? '请输入密码' : '留空则不修改'" />
            <span class="form-hint" v-if="dialogMode === 'edit'">留空表示不修改密码</span>
          </div>
          <div class="form-item">
            <label><span class="required">*</span>真实姓名</label>
            <input type="text" v-model="form.real_name" placeholder="请输入真实姓名" />
          </div>
          <div class="form-item">
            <label>手机号</label>
            <input type="text" v-model="form.phone" placeholder="请输入手机号" />
          </div>
          <div class="form-item">
            <label>邮箱</label>
            <input type="text" v-model="form.email" placeholder="请输入邮箱" />
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-cancel" @click="closeDialog">取消</button>
          <button class="btn btn-primary" @click="submitForm" :disabled="submitting">
            {{ submitting ? '提交中...' : '确认' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 删除确认弹窗 -->
    <div class="dialog-overlay" v-if="deleteDialogVisible" @click.self="deleteDialogVisible = false">
      <div class="dialog-box dialog-confirm">
        <div class="dialog-header">
          <h3>确认删除</h3>
          <button class="dialog-close" @click="deleteDialogVisible = false">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>
        <div class="dialog-body">
          <p>确定要删除用户 <strong>{{ deleteTarget?.real_name || deleteTarget?.user_name }}</strong> 吗？此操作不可恢复。</p>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-cancel" @click="deleteDialogVisible = false">取消</button>
          <button class="btn btn-danger" @click="handleDelete" :disabled="deleting">
            {{ deleting ? '删除中...' : '确认删除' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Toast 提示 -->
    <transition name="slide-down">
      <div v-if="toast.show" class="toast-bar" :class="toast.type">
        <span class="toast-message">{{ toast.message }}</span>
        <button class="toast-close" @click="toast.show = false">&times;</button>
      </div>
    </transition>
  </div>
</template>

<script>
const API_BASE = 'http://localhost:8088'

export default {
  name: 'UserList',
  data() {
    return {
      userList: [],
      loading: false,
      searchForm: { userName: '', realName: '' },
      currentPage: 1,
      pageSize: 10,
      dialogVisible: false,
      dialogMode: 'create',
      submitting: false,
      form: { user_id: null, user_name: '', password: '', real_name: '', phone: '', email: '' },
      deleteDialogVisible: false,
      deleteTarget: null,
      deleting: false,
      toast: { show: false, message: '', type: 'info' },
      toastTimer: null,
    }
  },
  computed: {
    filteredData() {
      const { userName, realName } = this.searchForm
      return this.userList.filter(user => {
        const matchUserName = userName ? user.user_name.includes(userName) : true
        const matchRealName = realName ? (user.real_name || '').includes(realName) : true
        return matchUserName && matchRealName
      })
    },
    pagedData() {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return this.filteredData.slice(start, end)
    },
    totalPages() {
      return Math.ceil(this.filteredData.length / this.pageSize) || 1
    },
  },
  watch: {
    filteredData() {
      if (this.currentPage > this.totalPages) { this.currentPage = 1 }
    },
    pageSize() { this.currentPage = 1 },
  },
  mounted() {
    this.fetchUserList()
  },
  beforeDestroy() {
    if (this.toastTimer) { clearTimeout(this.toastTimer) }
  },
  methods: {
    getAuthHeaders() {
      const token = localStorage.getItem('token')
      return {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : '',
      }
    },
    async fetchUserList() {
      this.loading = true
      try {
        const response = await fetch(`${API_BASE}/user/personnel`, {
          headers: this.getAuthHeaders(),
        })
        const result = await response.json()
        if (result.code === 200) {
          this.userList = result.data || []
        } else if (result.code === 401) {
          this.showToast('令牌已过期，请重新登录', 'error')
          localStorage.removeItem('token')
          this.$router.push({ name: 'Login' })
        } else {
          this.showToast(result.msg || '获取用户列表失败', 'error')
        }
      } catch (error) {
        console.error('获取用户列表失败:', error)
        this.showToast('网络异常，请稍后重试', 'error')
      } finally {
        this.loading = false
      }
    },
    async createUser(data) {
      try {
        const response = await fetch(`${API_BASE}/user`, {
          method: 'POST', headers: this.getAuthHeaders(), body: JSON.stringify(data),
        })
        const result = await response.json()
        if (result.code === 200) {
          this.showToast('新增用户成功', 'success')
          await this.fetchUserList()
          return true
        } else if (result.code === 401) {
          this.showToast('令牌已过期，请重新登录', 'error')
          localStorage.removeItem('token')
          this.$router.push({ name: 'Login' })
          return false
        } else {
          this.showToast(result.msg || '新增用户失败', 'error')
          return false
        }
      } catch (error) {
        console.error('新增用户失败:', error)
        this.showToast('网络异常，请稍后重试', 'error')
        return false
      }
    },
    async updateUser(data) {
      try {
        const response = await fetch(`${API_BASE}/user`, {
          method: 'PUT', headers: this.getAuthHeaders(), body: JSON.stringify(data),
        })
        const result = await response.json()
        if (result.code === 200) {
          this.showToast('修改用户成功', 'success')
          await this.fetchUserList()
          return true
        } else if (result.code === 401) {
          this.showToast('令牌已过期，请重新登录', 'error')
          localStorage.removeItem('token')
          this.$router.push({ name: 'Login' })
          return false
        } else {
          this.showToast(result.msg || '修改用户失败', 'error')
          return false
        }
      } catch (error) {
        console.error('修改用户失败:', error)
        this.showToast('网络异常，请稍后重试', 'error')
        return false
      }
    },
    async deleteUser(userId) {
      try {
        const response = await fetch(`${API_BASE}/user/${userId}`, {
          method: 'DELETE', headers: this.getAuthHeaders(),
        })
        const result = await response.json()
        if (result.code === 200) {
          this.showToast('删除用户成功', 'success')
          await this.fetchUserList()
          return true
        } else if (result.code === 401) {
          this.showToast('令牌已过期，请重新登录', 'error')
          localStorage.removeItem('token')
          this.$router.push({ name: 'Login' })
          return false
        } else {
          this.showToast(result.msg || '删除用户失败', 'error')
          return false
        }
      } catch (error) {
        console.error('删除用户失败:', error)
        this.showToast('网络异常，请稍后重试', 'error')
        return false
      }
    },
    async handleExport() {
      try {
        window.open(`${API_BASE}/user/excel`, '_blank')
        this.showToast('导出任务已提交，文件将自动下载', 'success')
      } catch (error) {
        console.error('导出失败:', error)
        this.showToast('导出失败，请稍后重试', 'error')
      }
    },
    handleSearch() { this.currentPage = 1 },
    resetSearch() {
      this.searchForm.userName = ''
      this.searchForm.realName = ''
      this.currentPage = 1
    },
    openCreateDialog() {
      this.dialogMode = 'create'
      this.form = { user_id: null, user_name: '', password: '', real_name: '', phone: '', email: '' }
      this.dialogVisible = true
    },
    openEditDialog(user) {
      this.dialogMode = 'edit'
      this.form = {
        user_id: user.user_id, user_name: user.user_name, password: '',
        real_name: user.real_name || '', phone: user.phone || '', email: user.email || '',
      }
      this.dialogVisible = true
    },
    closeDialog() { this.dialogVisible = false; this.submitting = false },
    async submitForm() {
      if (!this.form.user_name) { this.showToast('请输入用户名', 'error'); return }
      if (this.dialogMode === 'create' && !this.form.password) { this.showToast('请输入密码', 'error'); return }
      if (!this.form.real_name) { this.showToast('请输入真实姓名', 'error'); return }
      this.submitting = true
      try {
        let success = false
        if (this.dialogMode === 'create') {
          success = await this.createUser({
            user_name: this.form.user_name, password: this.form.password,
            real_name: this.form.real_name, phone: this.form.phone, email: this.form.email,
          })
        } else {
          const payload = {
            user_id: this.form.user_id, user_name: this.form.user_name,
            real_name: this.form.real_name, phone: this.form.phone, email: this.form.email,
          }
          if (this.form.password) { payload.password = this.form.password }
          success = await this.updateUser(payload)
        }
        if (success) { this.closeDialog() }
      } finally { this.submitting = false }
    },
    confirmDelete(user) { this.deleteTarget = user; this.deleteDialogVisible = true },
    async handleDelete() {
      if (!this.deleteTarget) return
      this.deleting = true
      try {
        const success = await this.deleteUser(this.deleteTarget.user_id)
        if (success) { this.deleteDialogVisible = false; this.deleteTarget = null }
      } finally { this.deleting = false }
    },
    showToast(message, type = 'info') {
      if (this.toastTimer) { clearTimeout(this.toastTimer); this.toastTimer = null }
      this.toast.message = message
      this.toast.type = type
      this.toast.show = true
      this.toastTimer = setTimeout(() => { this.toast.show = false; this.toastTimer = null }, 3000)
    },
  },
}
</script>

<style scoped>
.user-list-page { padding: 0; }

/* 页面头部 */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  padding: 14px 18px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  margin-bottom: 12px;
}
.page-header .header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.page-header .header-left h2 {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}
.page-header .header-left .breadcrumb {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #909399;
}
.page-header .header-left .breadcrumb .breadcrumb-item {
  cursor: pointer;
  transition: color 0.2s;
}
.page-header .header-left .breadcrumb .breadcrumb-item:hover { color: #409EFF; }
.page-header .header-left .breadcrumb .breadcrumb-item.active { color: #409EFF; cursor: default; }
.page-header .header-left .breadcrumb .breadcrumb-separator { color: #c0c4cc; }
.page-header .header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 按钮 */
.btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  border: none;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}
.btn svg { width: 15px; height: 15px; }
.btn.btn-primary { background: #409EFF; color: #fff; }
.btn.btn-primary:hover { background: #66b1ff; }
.btn.btn-primary:disabled { background: #a0cfff; cursor: not-allowed; }
.btn.btn-export { background: #67C23A; color: #fff; }
.btn.btn-export:hover { background: #85ce61; }
.btn.btn-search { background: #409EFF; color: #fff; }
.btn.btn-search:hover { background: #66b1ff; }
.btn.btn-reset { background: #f4f4f5; color: #606266; }
.btn.btn-reset:hover { background: #e6e6e8; }
.btn.btn-cancel { background: #f4f4f5; color: #606266; }
.btn.btn-cancel:hover { background: #e6e6e8; }
.btn.btn-danger { background: #f56c6c; color: #fff; }
.btn.btn-danger:hover { background: #f78989; }
.btn.btn-danger:disabled { background: #f8a0a0; cursor: not-allowed; }

/* 搜索栏 */
.search-bar {
  display: flex;
  align-items: flex-end;
  gap: 14px;
  background: #fff;
  padding: 14px 18px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}
.search-bar .search-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.search-bar .search-item label {
  font-size: 13px;
  color: #606266;
}
.search-bar .search-item input {
  width: 160px;
  height: 32px;
  padding: 0 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 13px;
  color: #303133;
  outline: none;
  transition: border-color 0.2s;
}
.search-bar .search-item input:focus { border-color: #409EFF; }
.search-bar .search-item input::placeholder { color: #c0c4cc; }
.search-bar .search-actions {
  display: flex;
  align-items: center;
  gap: 6px;
  padding-bottom: 0;
}

/* 表格 */
.table-wrapper {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  overflow: hidden;
  position: relative;
  min-height: 200px;
}
.loading-mask {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  gap: 10px;
  color: #909399;
}
.loading-mask .loading-spinner {
  width: 28px;
  height: 28px;
  border: 2px solid #ebeef5;
  border-top-color: #409EFF;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}
.data-table thead { background: #f5f7fa; }
.data-table thead th {
  padding: 10px 14px;
  text-align: left;
  font-weight: 600;
  color: #303133;
  border-bottom: 1px solid #ebeef5;
  white-space: nowrap;
}
.data-table tbody tr { transition: background 0.15s; }
.data-table tbody tr:hover { background: #f5f7fa; }
.data-table tbody td {
  padding: 10px 14px;
  border-bottom: 1px solid #ebeef5;
  color: #606266;
  vertical-align: middle;
}
.data-table .empty-cell { text-align: center; padding: 40px 0; color: #c0c4cc; }
.data-table .action-cell { display: flex; gap: 6px; align-items: center; }
.btn-action {
  padding: 3px 10px;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-action.edit { background: #ecf5ff; color: #409EFF; }
.btn-action.edit:hover { background: #d9ecff; }
.btn-action.delete { background: #fef0f0; color: #f56c6c; }
.btn-action.delete:hover { background: #fde2e2; }

/* 分页 */
.pagination-wrapper {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 18px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  margin-top: 12px;
}
.pagination-wrapper .total-info { font-size: 13px; color: #606266; }
.pagination-wrapper .pagination { display: flex; align-items: center; gap: 6px; }
.pagination-wrapper .pagination .page-btn {
  width: 30px; height: 30px;
  display: flex; align-items: center; justify-content: center;
  border: 1px solid #dcdfe6; border-radius: 4px;
  background: #fff; cursor: pointer; color: #606266;
}
.pagination-wrapper .pagination .page-btn svg { width: 13px; height: 13px; }
.pagination-wrapper .pagination .page-btn:hover:not(:disabled) { border-color: #409EFF; color: #409EFF; }
.pagination-wrapper .pagination .page-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.pagination-wrapper .pagination .page-info { font-size: 13px; color: #606266; min-width: 50px; text-align: center; }
.pagination-wrapper .page-size-select { display: flex; align-items: center; gap: 4px; font-size: 13px; color: #606266; }
.pagination-wrapper .page-size-select select {
  padding: 3px 6px; border: 1px solid #dcdfe6; border-radius: 4px;
  font-size: 13px; color: #303133; background: #fff; cursor: pointer; outline: none;
}
.pagination-wrapper .page-size-select select:focus { border-color: #409EFF; }

/* 弹窗 */
.dialog-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.35); z-index: 9999;
  display: flex; align-items: center; justify-content: center;
  animation: fadeIn 0.2s ease;
}
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
.dialog-box {
  background: #fff; border-radius: 4px;
  width: 500px; max-width: 92vw; max-height: 90vh;
  display: flex; flex-direction: column; overflow: hidden;
  border: 1px solid #ebeef5;
}
.dialog-box.dialog-confirm { width: 400px; }
.dialog-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 20px; border-bottom: 1px solid #ebeef5;
}
.dialog-header h3 { font-size: 15px; font-weight: 600; color: #303133; margin: 0; }
.dialog-header .dialog-close {
  width: 28px; height: 28px;
  display: flex; align-items: center; justify-content: center;
  border: none; background: none; cursor: pointer; border-radius: 4px;
  color: #909399;
}
.dialog-header .dialog-close svg { width: 16px; height: 16px; }
.dialog-header .dialog-close:hover { background: #f0f2f5; color: #303133; }
.dialog-body { padding: 16px 20px; overflow-y: auto; flex: 1; }
.dialog-body p { font-size: 14px; color: #606266; line-height: 1.6; margin: 0; }
.dialog-body p strong { color: #303133; }
.form-item { display: flex; flex-direction: column; gap: 4px; margin-bottom: 14px; }
.form-item:last-child { margin-bottom: 0; }
.form-item label { font-size: 13px; color: #606266; }
.form-item label .required { color: #f56c6c; }
.form-item input {
  height: 34px; padding: 0 10px;
  border: 1px solid #dcdfe6; border-radius: 4px;
  font-size: 13px; color: #303133; outline: none;
  transition: border-color 0.2s;
}
.form-item input:focus { border-color: #409EFF; }
.form-item input:disabled { background: #f5f7fa; color: #909399; cursor: not-allowed; }
.form-item .form-hint { font-size: 12px; color: #909399; }
.dialog-footer {
  display: flex; justify-content: flex-end; gap: 8px;
  padding: 10px 20px 16px; border-top: 1px solid #ebeef5;
}

/* Toast */
.toast-bar {
  position: fixed; top: 80px; left: 50%; transform: translateX(-50%);
  z-index: 99999; display: flex; align-items: center; gap: 10px;
  padding: 10px 18px; background: #fff; border-radius: 4px;
  border: 1px solid #ebeef5; border-left: 3px solid #409EFF;
  min-width: 260px; max-width: 480px;
}
.toast-bar.success { border-left-color: #67C23A; }
.toast-bar.error { border-left-color: #f56c6c; }
.toast-bar.info { border-left-color: #409EFF; }
.toast-bar .toast-message { font-size: 14px; color: #303133; flex: 1; }
.toast-bar .toast-close {
  background: none; border: none; cursor: pointer;
  color: #909399; font-size: 18px; padding: 0 4px;
}
.toast-bar .toast-close:hover { color: #606266; }
.slide-down-enter-active, .slide-down-leave-active { transition: all 0.3s ease; }
.slide-down-enter-from, .slide-down-leave-to { opacity: 0; transform: translateX(-50%) translateY(-16px); }

/* 响应式 */
@media (max-width: 768px) {
  .page-header { flex-direction: column; align-items: flex-start; gap: 10px; }
  .page-header .header-right { width: 100%; flex-wrap: wrap; }
  .search-bar { flex-direction: column; align-items: stretch; }
  .search-bar .search-item input { width: 100%; }
  .data-table { font-size: 12px; }
  .data-table thead th, .data-table tbody td { padding: 8px; }
  .data-table .action-cell { flex-direction: column; gap: 4px; }
  .pagination-wrapper { flex-direction: column; gap: 10px; align-items: stretch; text-align: center; }
  .pagination-wrapper .pagination { justify-content: center; }
  .pagination-wrapper .page-size-select { justify-content: center; }
  .dialog-box { width: 94vw; max-height: 80vh; }
}
@media (max-width: 480px) {
  .btn { font-size: 12px; padding: 5px 10px; }
  .btn svg { width: 13px; height: 13px; }
}
</style>
