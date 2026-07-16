<template>
  <div class="profile-page">
    <div class="profile-card">
      <!-- 头像区 -->
      <div class="avatar-section">
        <div class="avatar">{{ userInfo.real_name ? userInfo.real_name.charAt(0) : '管' }}</div>
        <div class="avatar-info">
          <div class="name">{{ userInfo.real_name || '管理员' }}</div>
          <div class="role">{{ userInfo.position || '系统管理员' }}</div>
        </div>
      </div>

      <!-- 修改按钮 -->
      <div class="mode-switch">
        <button class="btn btn-primary" @click="openEditDialog">
          <el-icon><Edit /></el-icon> 修改信息
        </button>
      </div>

      <!-- 信息展示 -->
      <div class="info-grid">
        <div class="info-item">
          <label>用户名</label>
          <div class="info-value">{{ userInfo.username || '-' }}</div>
        </div>
        <div class="info-item">
          <label>真实姓名</label>
          <div class="info-value">{{ userInfo.real_name || '-' }}</div>
        </div>
        <div class="info-item">
          <label>手机号</label>
          <div class="info-value">{{ userInfo.phone || '-' }}</div>
        </div>
        <div class="info-item">
          <label>职位</label>
          <div class="info-value">{{ userInfo.position || '-' }}</div>
        </div>
        <div class="info-item">
          <label>部门</label>
          <div class="info-value">{{ userInfo.department || '-' }}</div>
        </div>
        <div class="info-item">
          <label>账号状态</label>
          <div class="info-value">
            <span class="status-tag" :class="userInfo.status === 1 ? 'active' : 'frozen'">
              {{ userInfo.status === 1 ? '激活' : '冻结' }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 修改信息弹窗 -->
    <div class="dialog-overlay" v-if="dialogVisible" @click.self="dialogVisible = false">
      <div class="dialog-box">
        <div class="dialog-header">
          <h3>修改个人信息</h3>
          <button class="dialog-close" @click="dialogVisible = false">&times;</button>
        </div>
        <div class="dialog-body">
          <div class="form-item">
            <label>用户名 <span class="required">*</span></label>
            <input type="text" v-model="editForm.username" placeholder="请输入用户名" />
          </div>
          <div class="form-item">
            <label>真实姓名 <span class="required">*</span></label>
            <input type="text" v-model="editForm.real_name" placeholder="请输入真实姓名" />
          </div>
          <div class="form-item">
            <label>手机号 <span class="required">*</span></label>
            <input type="text" v-model="editForm.phone" placeholder="请输入手机号" />
          </div>

          <!-- 密码修改区域 -->
          <div class="divider">
            <span>修改密码（选填）</span>
          </div>
          <div class="form-item">
            <label>旧密码</label>
            <input type="password" v-model="editForm.old_password" placeholder="请输入旧密码" />
          </div>
          <div class="form-item">
            <label>新密码</label>
            <input type="password" v-model="editForm.new_password" placeholder="请输入新密码" />
          </div>
          <div class="form-item">
            <label>确认新密码</label>
            <input type="password" v-model="editForm.confirm_password" placeholder="请再次输入新密码" />
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-cancel" @click="dialogVisible = false">取消</button>
          <button class="btn btn-primary" @click="submitProfile">确认</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { Edit } from '@element-plus/icons-vue'
import { ElIcon, ElMessage } from 'element-plus'
import request from '@/utils/request.js'
import { encrypt } from '@/utils/crypto.js'

export default {
  name: 'ProfilePage',
  components: { Edit, ElIcon },
  data() {
    return {
      dialogVisible: false,
      userInfo: {
        user_id: null,
        username: '',
        real_name: '',
        phone: '',
        department: '',
        position: '',
        status: 1,
      },
      editForm: {
        user_id: null,
        username: '',
        real_name: '',
        phone: '',
        department: '',
        position: '',
        old_password: '',
        new_password: '',
        confirm_password: '',
      },
    }
  },
  mounted() {
    this.loadUserInfo()
  },
  methods: {
    loadUserInfo() {
      // 优先从localStorage加载
      const stored = localStorage.getItem('userInfo')
      if (stored) {
        try {
          const data = JSON.parse(stored)
          if (data && data.user_id) {
            this.userInfo = {
              user_id: data.user_id,
              username: data.username || '',
              real_name: data.real_name || '',
              phone: data.phone || '',
              department: data.department || '',
              position: data.position || '',
              status: data.status != null ? data.status : 1,
            }
            return
          }
        } catch (e) {
          // ignore parse error
        }
      }
      // 无本地数据则尝试从后端获取
      this.fetchUserFromServer()
    },

    async fetchUserFromServer() {
      const stored = localStorage.getItem('userInfo')
      if (!stored) return
      try {
        const data = JSON.parse(stored)
        if (data.user_id) {
          const res = await request.get(`/user/${data.user_id}`)
          if (res.code === 200 && res.data) {
            this.userInfo = {
              user_id: res.data.user_id,
              username: res.data.username || '',
              real_name: res.data.real_name || '',
              phone: res.data.phone || '',
              department: res.data.department || '',
              position: res.data.position || '',
              status: res.data.status != null ? res.data.status : 1,
            }
            localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
          }
        }
      } catch (e) {
        ElMessage.error('获取用户信息失败')
      }
    },

    openEditDialog() {
      this.editForm = {
        user_id: this.userInfo.user_id,
        username: this.userInfo.username,
        real_name: this.userInfo.real_name,
        phone: this.userInfo.phone,
        department: this.userInfo.department,
        position: this.userInfo.position,
        old_password: '',
        new_password: '',
        confirm_password: '',
      }
      this.dialogVisible = true
    },

    async submitProfile() {
      if (!this.editForm.username) {
        ElMessage.warning('请输入用户名')
        return
      }
      if (!this.editForm.real_name) {
        ElMessage.warning('请输入真实姓名')
        return
      }
      if (!this.editForm.phone) {
        ElMessage.warning('请输入手机号')
        return
      }

      // 密码修改相关校验
      const hasOldPwd = this.editForm.old_password.trim() !== ''
      const hasNewPwd = this.editForm.new_password.trim() !== ''
      const hasConfirmPwd = this.editForm.confirm_password.trim() !== ''

      if (hasOldPwd || hasNewPwd || hasConfirmPwd) {
        if (!hasOldPwd) {
          ElMessage.warning('请输入旧密码')
          return
        }
        if (!hasNewPwd) {
          ElMessage.warning('请输入新密码')
          return
        }
        if (hasNewPwd && this.editForm.new_password.length < 6) {
          ElMessage.warning('新密码长度不能少于6位')
          return
        }
        if (this.editForm.new_password !== this.editForm.confirm_password) {
          ElMessage.warning('两次输入的新密码不一致')
          return
        }
        if (this.editForm.old_password === this.editForm.new_password) {
          ElMessage.warning('新密码不能与旧密码相同')
          return
        }
      }

      const params = {
        user_id: this.editForm.user_id,
        username: this.editForm.username,
        real_name: this.editForm.real_name,
        phone: this.editForm.phone,
        department: this.editForm.department,
        position: this.editForm.position,
      }
      if (hasOldPwd && hasNewPwd) {
        params.old_password = await encrypt(this.editForm.old_password)
        params.new_password = await encrypt(this.editForm.new_password)
      }

      try {
        const res = await request.put('/user/profile', params)
        if (res.code === 200) {
          ElMessage.success('个人信息更新成功')
          this.dialogVisible = false
          // 更新本地存储和展示
          if (res.data) {
            this.userInfo = {
              user_id: res.data.user_id,
              username: res.data.username || '',
              real_name: res.data.real_name || '',
              phone: res.data.phone || '',
              department: res.data.department || '',
              position: res.data.position || '',
              status: res.data.status != null ? res.data.status : 1,
            }
            localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
          }
        } else {
          ElMessage.error(res.msg || '更新失败')
        }
      } catch (e) {
        ElMessage.error('更新失败，请稍后重试')
      }
    },
  }
}
</script>

<style scoped>
.profile-page {
  display: flex;
  justify-content: flex-start;
}

.profile-card {
  background: var(--card);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-xl);
  padding: 28px;
  width: 100%;
  max-width: 640px;
  box-shadow: var(--shadow-md);
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--border-light);
}
.avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: var(--primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 700;
  color: #fff;
  flex-shrink: 0;
  box-shadow: var(--shadow-sm);
}
.avatar-info .name {
  font-size: 20px;
  font-weight: 600;
  color: var(--foreground);
  letter-spacing: -0.3px;
}
.avatar-info .role {
  font-size: 13px;
  color: var(--foreground-muted);
  margin-top: 2px;
}

.mode-switch { margin-bottom: 20px; }

.btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 7px 20px;
  border: none;
  border-radius: var(--radius-md);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}
.btn-primary {
  background: var(--primary);
  color: #fff;
}
.btn-primary:hover { background: var(--primary-hover); box-shadow: var(--shadow-sm); }
.btn-cancel {
  background: hsl(0, 0%, 96%);
  color: var(--foreground-regular);
  margin-left: 8px;
}
.btn-cancel:hover { background: hsl(0, 0%, 90%); }

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.info-item { display: flex; flex-direction: column; gap: 4px; }
.info-item label { font-size: 12px; color: var(--foreground-muted); }
.info-value {
  height: 36px;
  padding: 0 12px;
  line-height: 36px;
  font-size: 14px;
  color: var(--foreground);
  background: var(--background-deep);
  border-radius: var(--radius-md);
}

.status-tag {
  display: inline-block;
  font-size: 12px;
  padding: 4px 12px;
  border-radius: var(--radius-sm);
  line-height: 1;
}
.status-tag.active { background: var(--success-bg); color: var(--success); }
.status-tag.frozen { background: var(--danger-bg); color: var(--danger); }

.dialog-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.4); z-index: 9999;
  display: flex; align-items: center; justify-content: center;
  backdrop-filter: blur(2px);
}
.dialog-box {
  background: var(--card);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-xl);
  width: 500px; max-width: 92vw;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: var(--shadow-float);
}
.dialog-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 20px; border-bottom: 1px solid var(--border-light);
  position: sticky; top: 0; background: var(--card); z-index: 1;
}
.dialog-header h3 { font-size: 16px; font-weight: 600; color: var(--foreground); margin: 0; }
.dialog-close {
  background: none; border: none; color: var(--foreground-placeholder);
  font-size: 22px; cursor: pointer; padding: 0; line-height: 1;
  transition: color 0.2s;
}
.dialog-close:hover { color: var(--foreground); }
.dialog-body { padding: 16px 20px; display: flex; flex-direction: column; gap: 12px; }
.dialog-footer {
  display: flex; justify-content: flex-end; gap: 8px;
  padding: 10px 20px 16px; border-top: 1px solid var(--border-light);
  position: sticky; bottom: 0; background: var(--card);
}

.form-item { display: flex; flex-direction: column; gap: 4px; }
.form-item label { font-size: 13px; color: var(--foreground-regular); font-weight: 500; }
.form-item .required { color: var(--danger); }
.form-item .hint { font-size: 11px; color: var(--foreground-placeholder); }
.form-item input {
  height: 34px; padding: 0 10px;
  border: 1px solid var(--input-border);
  border-radius: var(--radius-md); font-size: 13px; color: var(--foreground);
  outline: none; transition: all 0.2s ease;
}
.form-item input:focus {
  border-color: var(--primary);
  box-shadow: 0 0 0 2px hsl(var(--primary-h), var(--primary-s), 60% / 20%);
}
.form-item input:disabled {
  background: var(--background-deep); color: var(--foreground-muted); cursor: not-allowed;
}

.divider {
  display: flex; align-items: center; margin: 8px 0;
}
.divider::before, .divider::after {
  content: ''; flex: 1; height: 1px; background: var(--border-light);
}
.divider span { padding: 0 12px; font-size: 12px; color: var(--foreground-muted); }

@media (max-width: 768px) {
  .profile-card { padding: 16px; }
  .info-grid { grid-template-columns: 1fr; }
  .avatar-section { flex-direction: column; text-align: center; }
}
</style>
