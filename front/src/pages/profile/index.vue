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
  background: var(--bg-card);
  backdrop-filter: blur(14px);
  border: 1px solid var(--border-card);
  border-radius: var(--radius-card);
  padding: 32px;
  box-shadow: var(--shadow-card);
  width: 100%;
  max-width: 680px;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid rgba(255,255,255,0.06);
}
.avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--glow-blue), var(--glow-purple));
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 700;
  color: #fff;
  flex-shrink: 0;
}
.avatar-info .name {
  font-size: 22px;
  font-weight: 600;
  color: #fff;
}
.avatar-info .role {
  font-size: 14px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.mode-switch {
  margin-bottom: 24px;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 20px;
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
  margin-left: 10px;
}
.btn-cancel:hover { background: rgba(255,255,255,0.1); }

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}
.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.info-item label {
  font-size: 12px;
  color: var(--text-dim);
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.info-item input {
  height: 40px;
  padding: 0 14px;
  background: rgba(255,255,255,0.03);
  border: 1px solid rgba(255,255,255,0.08);
  border-radius: 8px;
  font-size: 14px;
  color: #fff;
  transition: var(--transition);
}
.info-item input:focus {
  outline: none;
  border-color: var(--glow-blue);
}
.info-item input:disabled {
  background: rgba(255,255,255,0.01);
  color: var(--text-secondary);
  cursor: not-allowed;
}
.info-item .hint {
  font-size: 11px;
  color: var(--text-dim);
}

@media (max-width: 768px) {
  .profile-card { padding: 20px; }
  .info-grid { grid-template-columns: 1fr; }
  .avatar-section { flex-direction: column; text-align: center; }
}
</style>
