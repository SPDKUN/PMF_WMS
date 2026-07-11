<template>
  <div class="profile-page">
    <div class="profile-card">
      <!-- 头像区 -->
      <div class="avatar-section">
        <div class="avatar">{{ form.realName ? form.realName.charAt(0) : '管' }}</div>
        <div class="avatar-info">
          <div class="name">{{ form.realName || '管理员' }}</div>
          <div class="role">{{ form.role || '系统管理员' }}</div>
        </div>
      </div>

      <!-- 编辑/展示切换 -->
      <div class="mode-switch">
        <button v-if="!editing" class="btn btn-primary" @click="startEdit">
          <el-icon><Edit /></el-icon> 修改信息
        </button>
        <div v-else class="edit-actions">
          <button class="btn btn-primary" @click="saveProfile">保存</button>
          <button class="btn btn-cancel" @click="cancelEdit">取消</button>
        </div>
      </div>

      <!-- 信息展示/编辑 -->
      <div class="info-grid">
        <div class="info-item">
          <label>用户名</label>
          <input type="text" v-model="form.username" :disabled="!editing || true" />
          <span class="hint">用户名不可修改</span>
        </div>
        <div class="info-item">
          <label>真实姓名</label>
          <input type="text" v-model="form.realName" :disabled="!editing" />
        </div>
        <div class="info-item">
          <label>手机号</label>
          <input type="text" v-model="form.phone" :disabled="!editing" />
        </div>
        <div class="info-item">
          <label>邮箱</label>
          <input type="text" v-model="form.email" :disabled="!editing" />
        </div>
        <div class="info-item">
          <label>角色</label>
          <input type="text" v-model="form.role" disabled />
        </div>
        <div class="info-item">
          <label>部门</label>
          <input type="text" v-model="form.department" :disabled="!editing" />
        </div>
        <div class="info-item">
          <label>入职日期</label>
          <input type="text" v-model="form.joinDate" disabled />
        </div>
        <div class="info-item" v-if="editing">
          <label>新密码</label>
          <input type="password" v-model="form.password" placeholder="留空则不修改" />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { Edit } from '@element-plus/icons-vue'
import { ElIcon } from 'element-plus'

export default {
  name: 'ProfilePage',
  components: { Edit, ElIcon },
  data() {
    return {
      editing: false,
      form: {
        username: 'admin',
        realName: '管理员',
        phone: '13800009999',
        email: 'admin@wms.com',
        role: '系统管理员',
        department: '仓储管理部',
        joinDate: '2024-01-15',
        password: '',
      },
      backup: {},
    }
  },
  methods: {
    startEdit() {
      this.backup = { ...this.form }
      this.editing = true
    },
    cancelEdit() {
      this.form = { ...this.backup }
      this.editing = false
    },
    saveProfile() {
      if (!this.form.realName) { alert('请输入真实姓名'); return }
      alert('个人信息已更新！（演示模式）')
      this.editing = false
    }
  }
}
</script>

<style scoped>
.profile-page {
  display: flex;
  justify-content: flex-start;
}

.profile-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 28px;
  width: 100%;
  max-width: 640px;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}
.avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: #409EFF;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 700;
  color: #fff;
  flex-shrink: 0;
}
.avatar-info .name {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}
.avatar-info .role {
  font-size: 13px;
  color: #909399;
  margin-top: 2px;
}

.mode-switch {
  margin-bottom: 20px;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 18px;
  border: none;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-primary {
  background: #409EFF;
  color: #fff;
}
.btn-primary:hover { background: #66b1ff; }
.btn-cancel {
  background: #f4f4f5;
  color: #606266;
  margin-left: 8px;
}
.btn-cancel:hover { background: #e6e6e8; }

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.info-item label {
  font-size: 12px;
  color: #909399;
}
.info-item input {
  height: 36px;
  padding: 0 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  color: #303133;
  outline: none;
  transition: border-color 0.2s;
}
.info-item input:focus {
  border-color: #409EFF;
}
.info-item input:disabled {
  background: #f5f7fa;
  color: #909399;
  cursor: not-allowed;
}
.info-item .hint {
  font-size: 11px;
  color: #c0c4cc;
}

@media (max-width: 768px) {
  .profile-card { padding: 16px; }
  .info-grid { grid-template-columns: 1fr; }
  .avatar-section { flex-direction: column; text-align: center; }
}
</style>
