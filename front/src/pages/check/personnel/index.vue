<template>
  <div class="user-list-page">
    <!-- ====== 页面头部 ====== -->
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

    <!-- ====== 搜索栏 ====== -->
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

    <!-- ====== 表格 ====== -->
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
              <div class="empty-state">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M20 7L12 3L4 7M20 7L12 11M20 7V17L12 21M12 11L4 7M12 11V21M4 7V17L12 21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <span>暂无数据</span>
              </div>
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

    <!-- ====== 分页 ====== -->
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

    <!-- ====== 新增/编辑弹窗 ====== -->
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

    <!-- ====== 删除确认弹窗 ====== -->
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

    <!-- ====== Toast 提示 ====== -->
    <transition name="slide-down">
      <div v-if="toast.show" class="toast-bar" :class="toast.type">
        <div class="toast-icon">
          <svg v-if="toast.type === 'success'" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M22 11.08V12C21.9988 14.1564 21.3005 16.2547 20.0093 17.9818C18.7182 19.709 16.9033 20.9725 14.8354 21.5839C12.7674 22.1953 10.5573 22.1219 8.53447 21.3746C6.51168 20.6273 4.78465 19.2461 3.61096 17.4371C2.43727 15.628 1.87979 13.4881 2.02168 11.3363C2.16356 9.18455 2.99721 7.13631 4.39828 5.49706C5.79935 3.85781 7.69279 2.71537 9.79619 2.24013C11.8996 1.7649 14.1003 1.98232 16.07 2.85999" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M22 4L12 14.01L9 11.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <svg v-else-if="toast.type === 'error'" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M12 8V12M12 16H12.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <svg v-else viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M12 8V12M12 16H12.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <span class="toast-message">{{ toast.message }}</span>
        <button class="toast-close" @click="toast.show = false">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
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
      // 原始数据
      userList: [],
      loading: false,

      // 搜索表单
      searchForm: {
        userName: '',
        realName: '',
      },

      // 分页
      currentPage: 1,
      pageSize: 10,

      // 弹窗
      dialogVisible: false,
      dialogMode: 'create', // 'create' | 'edit'
      submitting: false,
      form: {
        user_id: null,
        user_name: '',
        password: '',
        real_name: '',
        phone: '',
        email: '',
      },

      // 删除
      deleteDialogVisible: false,
      deleteTarget: null,
      deleting: false,

      // Toast
      toast: {
        show: false,
        message: '',
        type: 'info', // 'success' | 'error' | 'info'
      },
      toastTimer: null,
    }
  },

  computed: {
    // 搜索过滤后的数据
    filteredData() {
      const { userName, realName } = this.searchForm
      return this.userList.filter(user => {
        const matchUserName = userName ? user.user_name.includes(userName) : true
        const matchRealName = realName ? (user.real_name || '').includes(realName) : true
        return matchUserName && matchRealName
      })
    },

    // 分页后的数据
    pagedData() {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return this.filteredData.slice(start, end)
    },

    // 总页数
    totalPages() {
      return Math.ceil(this.filteredData.length / this.pageSize) || 1
    },
  },

  watch: {
    // 当数据变化或每页条数变化时，重置到第一页
    filteredData() {
      if (this.currentPage > this.totalPages) {
        this.currentPage = 1
      }
    },
    pageSize() {
      this.currentPage = 1
    },
  },

  mounted() {
    this.fetchUserList()
  },

  beforeDestroy() {
    if (this.toastTimer) {
      clearTimeout(this.toastTimer)
    }
  },

  methods: {
    // ========== API 调用 ==========

    // 获取认证请求头
    getAuthHeaders() {
      const token = localStorage.getItem('token')
      return {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : '',
      }
    },

    // 获取用户列表
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

    // 新增用户
    async createUser(data) {
      try {
        const response = await fetch(`${API_BASE}/user`, {
          method: 'POST',
          headers: this.getAuthHeaders(),
          body: JSON.stringify(data),
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

    // 修改用户
    async updateUser(data) {
      try {
        const response = await fetch(`${API_BASE}/user`, {
          method: 'PUT',
          headers: this.getAuthHeaders(),
          body: JSON.stringify(data),
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

    // 删除用户
    async deleteUser(userId) {
      try {
        const response = await fetch(`${API_BASE}/user/${userId}`, {
          method: 'DELETE',
          headers: this.getAuthHeaders(),
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

    // 导出Excel
    async handleExport() {
      try {
        // 直接打开下载链接
        window.open(`${API_BASE}/user/excel`, '_blank')
        this.showToast('导出任务已提交，文件将自动下载', 'success')
      } catch (error) {
        console.error('导出失败:', error)
        this.showToast('导出失败，请稍后重试', 'error')
      }
    },

    // ========== 搜索 ==========

    handleSearch() {
      this.currentPage = 1
    },

    resetSearch() {
      this.searchForm.userName = ''
      this.searchForm.realName = ''
      this.currentPage = 1
    },

    // ========== 弹窗控制 ==========

    openCreateDialog() {
      this.dialogMode = 'create'
      this.form = {
        user_id: null,
        user_name: '',
        password: '',
        real_name: '',
        phone: '',
        email: '',
      }
      this.dialogVisible = true
    },

    openEditDialog(user) {
      this.dialogMode = 'edit'
      this.form = {
        user_id: user.user_id,
        user_name: user.user_name,
        password: '', // 留空表示不修改
        real_name: user.real_name || '',
        phone: user.phone || '',
        email: user.email || '',
      }
      this.dialogVisible = true
    },

    closeDialog() {
      this.dialogVisible = false
      this.submitting = false
    },

    // ========== 表单提交 ==========

    async submitForm() {
      // 表单校验
      if (!this.form.user_name) {
        this.showToast('请输入用户名', 'error')
        return
      }
      if (this.dialogMode === 'create' && !this.form.password) {
        this.showToast('请输入密码', 'error')
        return
      }
      if (!this.form.real_name) {
        this.showToast('请输入真实姓名', 'error')
        return
      }

      this.submitting = true
      try {
        let success = false
        if (this.dialogMode === 'create') {
          success = await this.createUser({
            user_name: this.form.user_name,
            password: this.form.password,
            real_name: this.form.real_name,
            phone: this.form.phone,
            email: this.form.email,
          })
        } else {
          // 编辑模式：如果密码为空，则不传递密码字段
          const payload = {
            user_id: this.form.user_id,
            user_name: this.form.user_name,
            real_name: this.form.real_name,
            phone: this.form.phone,
            email: this.form.email,
          }
          if (this.form.password) {
            payload.password = this.form.password
          }
          success = await this.updateUser(payload)
        }
        if (success) {
          this.closeDialog()
        }
      } finally {
        this.submitting = false
      }
    },

    // ========== 删除 ==========

    confirmDelete(user) {
      this.deleteTarget = user
      this.deleteDialogVisible = true
    },

    async handleDelete() {
      if (!this.deleteTarget) return
      this.deleting = true
      try {
        const success = await this.deleteUser(this.deleteTarget.user_id)
        if (success) {
          this.deleteDialogVisible = false
          this.deleteTarget = null
        }
      } finally {
        this.deleting = false
      }
    },

    // ========== Toast ==========

    showToast(message, type = 'info') {
      if (this.toastTimer) {
        clearTimeout(this.toastTimer)
        this.toastTimer = null
      }
      this.toast.message = message
      this.toast.type = type
      this.toast.show = true
      this.toastTimer = setTimeout(() => {
        this.toast.show = false
        this.toastTimer = null
      }, 3000)
    },
  },
}
</script>

<style lang="scss" scoped>
.user-list-page {
  padding: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC',
    'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

/* ====== 页面头部 ====== */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  padding: 16px 20px;
  border-radius: 4px;
  margin-bottom: 16px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);

  .header-left {
    display: flex;
    align-items: center;
    gap: 20px;

    h2 {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
      margin: 0;
    }

    .breadcrumb {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;
      color: #909399;

      .breadcrumb-item {
        cursor: pointer;
        transition: color 0.2s;
        &:hover {
          color: #409EFF;
        }
        &.active {
          color: #409EFF;
          cursor: default;
        }
      }
      .breadcrumb-separator {
        color: #c0c4cc;
      }
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 10px;
  }
}

/* ====== 按钮 ====== */
.btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;

  svg {
    width: 16px;
    height: 16px;
  }

  &:active {
    transform: scale(0.97);
  }

  &.btn-primary {
    background: #409EFF;
    color: #fff;
    &:hover {
      background: #66b1ff;
    }
    &:disabled {
      background: #a0cfff;
      cursor: not-allowed;
    }
  }

  &.btn-export {
    background: #67C23A;
    color: #fff;
    &:hover {
      background: #85ce61;
    }
  }

  &.btn-search {
    background: #409EFF;
    color: #fff;
    &:hover {
      background: #66b1ff;
    }
  }

  &.btn-reset {
    background: #f4f4f5;
    color: #606266;
    &:hover {
      background: #e6e6e8;
    }
  }

  &.btn-cancel {
    background: #f4f4f5;
    color: #606266;
    &:hover {
      background: #e6e6e8;
    }
  }

  &.btn-danger {
    background: #f56c6c;
    color: #fff;
    &:hover {
      background: #f78989;
    }
    &:disabled {
      background: #f8a0a0;
      cursor: not-allowed;
    }
  }
}

/* ====== 搜索栏 ====== */
.search-bar {
  display: flex;
  align-items: flex-end;
  gap: 16px;
  background: #fff;
  padding: 16px 20px;
  border-radius: 4px;
  margin-bottom: 16px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  flex-wrap: wrap;

  .search-item {
    display: flex;
    flex-direction: column;
    gap: 4px;

    label {
      font-size: 13px;
      color: #606266;
      font-weight: 500;
    }

    input {
      width: 180px;
      height: 34px;
      padding: 0 12px;
      border: 1px solid #dcdfe6;
      border-radius: 4px;
      font-size: 13px;
      color: #303133;
      transition: border-color 0.2s ease;

      &:focus {
        outline: none;
        border-color: #409EFF;
        box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.15);
      }

      &::placeholder {
        color: #c0c4cc;
      }
    }
  }

  .search-actions {
    display: flex;
    align-items: center;
    gap: 8px;
    padding-bottom: 0;
  }
}

/* ====== 表格 ====== */
.table-wrapper {
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  overflow: hidden;
  position: relative;
  min-height: 280px;
}

.loading-mask {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 280px;
  gap: 12px;
  color: #909399;

  .loading-spinner {
    width: 32px;
    height: 32px;
    border: 3px solid #ebeef5;
    border-top-color: #409EFF;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
  }

  span {
    font-size: 14px;
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;

  thead {
    background: #f5f7fa;

    th {
      padding: 12px 16px;
      text-align: left;
      font-weight: 600;
      color: #303133;
      border-bottom: 1px solid #ebeef5;
      white-space: nowrap;
    }
  }

  tbody {
    tr {
      transition: background 0.15s ease;
      &:hover {
        background: #f5f7fa;
      }

      td {
        padding: 12px 16px;
        border-bottom: 1px solid #ebeef5;
        color: #606266;
        vertical-align: middle;
      }
    }
  }

  .empty-cell {
    text-align: center;
    padding: 40px 0;
  }

  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    color: #c0c4cc;

    svg {
      width: 48px;
      height: 48px;
    }
    span {
      font-size: 14px;
    }
  }

  .action-cell {
    display: flex;
    gap: 8px;
    align-items: center;
  }

  .btn-action {
    padding: 4px 12px;
    border: none;
    border-radius: 3px;
    font-size: 12px;
    cursor: pointer;
    transition: all 0.2s ease;

    &.edit {
      background: #ecf5ff;
      color: #409EFF;
      &:hover {
        background: #d9ecff;
      }
    }

    &.delete {
      background: #fef0f0;
      color: #f56c6c;
      &:hover {
        background: #fde2e2;
      }
    }
  }
}

/* ====== 分页 ====== */
.pagination-wrapper {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  background: #fff;
  border-radius: 4px;
  margin-top: 16px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);

  .total-info {
    font-size: 13px;
    color: #606266;
  }

  .pagination {
    display: flex;
    align-items: center;
    gap: 8px;

    .page-btn {
      width: 32px;
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      border: 1px solid #dcdfe6;
      border-radius: 4px;
      background: #fff;
      cursor: pointer;
      transition: all 0.2s ease;
      color: #606266;

      svg {
        width: 14px;
        height: 14px;
      }

      &:hover:not(:disabled) {
        border-color: #409EFF;
        color: #409EFF;
      }

      &:disabled {
        opacity: 0.4;
        cursor: not-allowed;
      }
    }

    .page-info {
      font-size: 13px;
      color: #606266;
      min-width: 60px;
      text-align: center;
    }
  }

  .page-size-select {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    color: #606266;

    select {
      padding: 4px 8px;
      border: 1px solid #dcdfe6;
      border-radius: 4px;
      font-size: 13px;
      color: #303133;
      background: #fff;
      cursor: pointer;
      outline: none;
      &:focus {
        border-color: #409EFF;
      }
    }
  }
}

/* ====== 弹窗 ====== */
.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.35);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: fadeIn 0.25s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.dialog-box {
  background: #fff;
  border-radius: 8px;
  width: 520px;
  max-width: 92vw;
  max-height: 90vh;
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  animation: slideUp 0.3s ease;
  overflow: hidden;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.96);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.dialog-box.dialog-confirm {
  width: 420px;
}

.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  border-bottom: 1px solid #ebeef5;

  h3 {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin: 0;
  }

  .dialog-close {
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    border: none;
    background: none;
    cursor: pointer;
    border-radius: 4px;
    color: #909399;
    transition: all 0.2s ease;

    svg {
      width: 18px;
      height: 18px;
    }

    &:hover {
      background: #f0f2f5;
      color: #303133;
    }
  }
}

.dialog-body {
  padding: 20px 24px;
  overflow-y: auto;
  flex: 1;

  p {
    font-size: 14px;
    color: #606266;
    line-height: 1.6;
    margin: 0;

    strong {
      color: #303133;
    }
  }
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 16px;

  &:last-child {
    margin-bottom: 0;
  }

  label {
    font-size: 13px;
    font-weight: 500;
    color: #606266;

    .required {
      color: #f56c6c;
      margin-right: 2px;
    }
  }

  input {
    height: 36px;
    padding: 0 12px;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    font-size: 13px;
    color: #303133;
    transition: border-color 0.2s ease;

    &:focus {
      outline: none;
      border-color: #409EFF;
      box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.15);
    }

    &:disabled {
      background: #f5f7fa;
      color: #909399;
      cursor: not-allowed;
    }
  }

  .form-hint {
    font-size: 12px;
    color: #909399;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 12px 24px 20px;
  border-top: 1px solid #ebeef5;
}

/* ====== Toast ====== */
.toast-bar {
  position: fixed;
  top: 80px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 99999;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.12);
  border-left: 4px solid #409EFF;
  min-width: 280px;
  max-width: 500px;

  &.success {
    border-left-color: #67C23A;
    .toast-icon {
      color: #67C23A;
    }
  }
  &.error {
    border-left-color: #f56c6c;
    .toast-icon {
      color: #f56c6c;
    }
  }
  &.info {
    border-left-color: #409EFF;
    .toast-icon {
      color: #409EFF;
    }
  }

  .toast-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    svg {
      width: 20px;
      height: 20px;
    }
  }

  .toast-message {
    font-size: 14px;
    color: #303133;
    flex: 1;
    line-height: 1.5;
  }

  .toast-close {
    display: flex;
    align-items: center;
    justify-content: center;
    background: none;
    border: none;
    cursor: pointer;
    color: #909399;
    padding: 4px;
    border-radius: 4px;
    transition: all 0.2s;

    svg {
      width: 16px;
      height: 16px;
    }

    &:hover {
      background: #f0f2f5;
      color: #606266;
    }
  }
}

.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}
.slide-down-enter-from {
  opacity: 0;
  transform: translateX(-50%) translateY(-20px);
}
.slide-down-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(-20px);
}

/* ====== 响应式 ====== */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;

    .header-right {
      width: 100%;
      flex-wrap: wrap;
    }
  }

  .search-bar {
    flex-direction: column;
    align-items: stretch;

    .search-item {
      input {
        width: 100%;
      }
    }

    .search-actions {
      justify-content: flex-start;
    }
  }

  .data-table {
    font-size: 12px;

    thead th,
    tbody td {
      padding: 8px 10px;
    }

    .action-cell {
      flex-direction: column;
      gap: 4px;
    }
  }

  .pagination-wrapper {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
    text-align: center;

    .pagination {
      justify-content: center;
    }
    .page-size-select {
      justify-content: center;
    }
  }

  .dialog-box {
    width: 94vw;
    max-height: 80vh;

    .dialog-header {
      padding: 12px 16px;
      h3 {
        font-size: 15px;
      }
    }
    .dialog-body {
      padding: 16px;
    }
    .dialog-footer {
      padding: 10px 16px 16px;
    }
  }

  .form-item input {
    height: 34px;
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  .page-header .header-left h2 {
    font-size: 16px;
  }
  .page-header .header-left .breadcrumb {
    font-size: 12px;
  }
  .btn {
    font-size: 12px;
    padding: 6px 12px;
    svg {
      width: 14px;
      height: 14px;
    }
  }
  .search-bar .search-item label {
    font-size: 12px;
  }
}
</style>