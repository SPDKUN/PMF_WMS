<template>
  <div class="manage-page">
    <!-- 选项卡 -->
    <div class="tab-row">
      <button :class="{ active: activeTab === 'personnel' }" @click="activeTab = 'personnel'">人员列表</button>
      <button :class="{ active: activeTab === 'warehouse' }" @click="activeTab = 'warehouse'">仓库列表</button>
      <button :class="{ active: activeTab === 'goods' }" @click="activeTab = 'goods'">货物列表</button>
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
            <tr v-for="p in pagedPersonnel" :key="p.user_id">
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
      <div class="pagination-row">
        <span class="total-info">共 {{ personnelList.length }} 条</span>
        <div class="pagination-controls">
          <button :disabled="page.personnel <= 1" @click="page.personnel = 1">首页</button>
          <button :disabled="page.personnel <= 1" @click="page.personnel--">上一页</button>
          <template v-for="p in getPageNumbers(personnelTotalPages, page.personnel)" :key="p">
            <span v-if="p === '...'" class="page-ellipsis">...</span>
            <button v-else :class="{ active: page.personnel === p }" @click="page.personnel = p">{{ p }}</button>
          </template>
          <button :disabled="page.personnel >= personnelTotalPages" @click="page.personnel++">下一页</button>
          <button :disabled="page.personnel >= personnelTotalPages" @click="page.personnel = personnelTotalPages">末页</button>
        </div>
      </div>
    </div>

    <!-- 仓库列表 -->
    <div v-if="activeTab === 'warehouse'" class="tab-content">
      <div class="toolbar">
        <button class="btn btn-primary" @click="openWarehouseDialog()">
          <el-icon><Plus /></el-icon> 新增仓库
        </button>
        <div class="search-bar">
          <select v-model="warehouseSearchType" class="search-type">
            <option value="warehouse_id">按ID</option>
            <option value="warehouse_name">按名称</option>
          </select>
          <input
            type="text"
            v-model="warehouseSearchKeyword"
            placeholder="请输入搜索关键词"
            @keyup.enter="searchWarehouse"
            class="search-input"
          />
          <button class="btn btn-search" @click="searchWarehouse">
            <el-icon><Search /></el-icon> 搜索
          </button>
          <button class="btn btn-cancel" @click="resetWarehouseSearch">重置</button>
        </div>
      </div>
      <div class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>仓库名称</th>
              <th>仓库类型</th>
              <th>地址</th>
              <th>仓库尺寸</th>
              <th>可用库位数</th>
              <th>状态</th>
              <th width="220">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="warehouseList.length === 0">
              <td colspan="8" class="empty-cell">暂无仓库数据</td>
            </tr>
            <tr v-for="w in pagedWarehouses" :key="w.warehouse_id">
              <td>{{ w.warehouse_id }}</td>
              <td>{{ w.warehouse_name }}</td>
              <td>{{ w.warehouse_type }}</td>
              <td>{{ w.location }}</td>
              <td>{{ w.warehouse_size }}</td>
              <td>{{ w.available_slots }} / {{ w.total_slots }}</td>
              <td><span class="status-tag" :class="w.status === '启用' ? '启用' : '禁用'">{{ w.status === '启用' ? '启用' : '停用' }}</span></td>
              <td class="action-cell">
                <button class="btn-action view" @click="openViewDialog(w)">查看</button>
                <button class="btn-action edit" @click="openWarehouseDialog(w)">编辑</button>
                <button class="btn-action delete" @click="deleteWarehouse(w.warehouse_id)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="pagination-row">
        <span class="total-info">共 {{ warehouseList.length }} 条</span>
        <div class="pagination-controls">
          <button :disabled="page.warehouse <= 1" @click="page.warehouse = 1">首页</button>
          <button :disabled="page.warehouse <= 1" @click="page.warehouse--">上一页</button>
          <template v-for="p in getPageNumbers(warehouseTotalPages, page.warehouse)" :key="p">
            <span v-if="p === '...'" class="page-ellipsis">...</span>
            <button v-else :class="{ active: page.warehouse === p }" @click="page.warehouse = p">{{ p }}</button>
          </template>
          <button :disabled="page.warehouse >= warehouseTotalPages" @click="page.warehouse++">下一页</button>
          <button :disabled="page.warehouse >= warehouseTotalPages" @click="page.warehouse = warehouseTotalPages">末页</button>
        </div>
      </div>
    </div>

    <!-- 货物列表 -->
    <div v-if="activeTab === 'goods'" class="tab-content">
      <div class="toolbar">
        <button class="btn btn-primary" @click="openGoodsDialog()">
          <el-icon><Plus /></el-icon> 新增货物
        </button>
        <div class="search-bar">
          <select v-model="goodsSearchType" class="search-type">
            <option value="goods_code">按货物编码</option>
            <option value="goods_name">按货物名称</option>
          </select>
          <input
            type="text"
            v-model="goodsSearchKeyword"
            placeholder="请输入搜索关键词"
            @keyup.enter="searchGoods"
            class="search-input"
          />
          <button class="btn btn-search" @click="searchGoods">
            <el-icon><Search /></el-icon> 搜索
          </button>
          <button class="btn btn-cancel" @click="resetGoodsSearch">重置</button>
        </div>
      </div>
      <div class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>货物ID</th>
              <th>货物编码</th>
              <th>种类</th>
              <th>名称</th>
              <th>储存条件</th>
              <th>数量</th>
              <th>单位</th>
              <th width="100">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="goodsList.length === 0">
              <td colspan="8" class="empty-cell">暂无货物数据</td>
            </tr>
            <tr v-for="g in pagedGoods" :key="g.goods_id">
              <td>{{ g.goods_id }}</td>
              <td>{{ g.goods_code }}</td>
              <td>{{ g.category }}</td>
              <td>{{ g.goods_name }}</td>
              <td>{{ g.storage_condition }}</td>
              <td>{{ g.quantity }}</td>
              <td>{{ g.unit }}</td>
              <td>
                <button class="btn-action delete" @click="deleteGoods(g.goods_id)" :disabled="g.quantity > 0" :title="g.quantity > 0 ? '数量不为0，不可删除' : ''">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="pagination-row">
        <span class="total-info">共 {{ goodsList.length }} 条</span>
        <div class="pagination-controls">
          <button :disabled="page.goods <= 1" @click="page.goods = 1">首页</button>
          <button :disabled="page.goods <= 1" @click="page.goods--">上一页</button>
          <template v-for="p in getPageNumbers(goodsTotalPages, page.goods)" :key="p">
            <span v-if="p === '...'" class="page-ellipsis">...</span>
            <button v-else :class="{ active: page.goods === p }" @click="page.goods = p">{{ p }}</button>
          </template>
          <button :disabled="page.goods >= goodsTotalPages" @click="page.goods++">下一页</button>
          <button :disabled="page.goods >= goodsTotalPages" @click="page.goods = goodsTotalPages">末页</button>
        </div>
      </div>
    </div>

    <!-- 货物新增弹窗 -->
    <div class="dialog-overlay" v-if="goodsDialog.visible" @click.self="goodsDialog.visible = false">
      <div class="dialog-box">
        <div class="dialog-header">
          <h3>新增货物</h3>
          <button class="dialog-close" @click="goodsDialog.visible = false">&times;</button>
        </div>
        <div class="dialog-body">
          <div class="form-item">
            <label>货物编码 <span class="required">*</span></label>
            <input type="text" v-model="goodsDialog.form.goods_code" placeholder="请输入货物编码" />
          </div>
          <div class="form-item">
            <label>种类 <span class="required">*</span></label>
            <select v-model="goodsDialog.form.category">
              <option value="">请选择种类</option>
              <option value="原料">原料</option>
              <option value="成品">成品</option>
            </select>
          </div>
          <div class="form-item">
            <label>名称 <span class="required">*</span></label>
            <input type="text" v-model="goodsDialog.form.goods_name" placeholder="请输入名称" />
          </div>
          <div class="form-item">
            <label>储存条件 <span class="required">*</span></label>
            <select v-model="goodsDialog.form.storage_condition">
              <option value="">请选择储存条件</option>
              <option value="常温">常温</option>
              <option value="冷冻">冷冻</option>
              <option value="恒温">恒温</option>
            </select>
          </div>
          <div class="form-item">
            <label>单位 <span class="required">*</span></label>
            <input type="text" v-model="goodsDialog.form.unit" placeholder="请输入单位（如：箱、袋、kg）" />
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-cancel" @click="goodsDialog.visible = false">取消</button>
          <button class="btn btn-primary" @click="submitGoods">确认</button>
        </div>
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
            <select v-model="personnelDialog.form.department" @change="onDepartmentChange">
              <option value="">请选择所属部门</option>
              <option value="管理部门">管理部门</option>
              <option value="仓管部门">仓管部门</option>
              <option value="质检部门">质检部门</option>
            </select>
          </div>
          <div class="form-item">
            <label>职位 <span class="required">*</span></label>
            <select v-model="personnelDialog.form.position" :disabled="!personnelDialog.form.department">
              <option value="">请选择职位</option>
              <option v-for="pos in availablePositions" :key="pos" :value="pos">{{ pos }}</option>
            </select>
          </div>
          <div class="form-item" v-if="personnelDialog.mode === 'edit'">
            <label>账号状态</label>
            <select v-model="personnelDialog.form.status">
              <option :value="1">启用</option>
              <option :value="0">停用</option>
            </select>
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

    <!-- 仓库新增/编辑弹窗 -->
    <div class="dialog-overlay" v-if="warehouseDialog.visible" @click.self="warehouseDialog.visible = false">
      <div class="dialog-box">
        <div class="dialog-header">
          <h3>{{ warehouseDialog.mode === 'create' ? '新增仓库' : '编辑仓库' }}</h3>
          <button class="dialog-close" @click="warehouseDialog.visible = false">&times;</button>
        </div>
        <div class="dialog-body">
          <div class="form-item">
            <label>仓库名称 <span class="required">*</span></label>
            <input type="text" v-model="warehouseDialog.form.warehouse_name" placeholder="请输入仓库名称" />
          </div>
          <div class="form-item">
            <label>仓库类型 <span class="required">*</span></label>
            <select v-model="warehouseDialog.form.warehouse_type">
              <option value="">请选择仓库类型</option>
              <option value="一般仓库">一般仓库</option>
              <option value="冷冻库">冷冻库</option>
              <option value="恒温库">恒温库</option>
            </select>
          </div>
          <div class="form-item">
            <label>地址 <span class="required">*</span></label>
            <input type="text" v-model="warehouseDialog.form.location" placeholder="请输入地址" />
          </div>
          <div class="form-item" v-if="warehouseDialog.mode === 'create'">
            <label>仓库尺寸 <span class="required">*</span></label>
            <select v-model="warehouseDialog.form.warehouse_size">
              <option value="">请选择仓库尺寸</option>
              <option value="大">大（16库区/128库位）</option>
              <option value="中">中（8库区/64库位）</option>
              <option value="小">小（4库区/32库位）</option>
            </select>
          </div>
          <div class="form-item" v-else>
            <label>仓库尺寸</label>
            <input type="text" :value="warehouseDialog.form.warehouse_size" disabled />
          </div>
          <div class="form-item" v-if="warehouseDialog.mode === 'edit'">
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
          <p style="color:#303133;font-size:14px;margin:0;">确定将该用户的密码重置为 123456 吗？</p>
        </div>
        <div class="dialog-footer" style="justify-content:center;">
          <button class="btn btn-cancel" @click="confirmDialog.visible = false">取消</button>
          <button class="btn btn-primary" @click="doResetPassword">确定</button>
        </div>
      </div>
    </div>

    <!-- 仓库查看弹窗（可视化） -->
    <div class="dialog-overlay" v-if="viewDialog.visible" @click.self="viewDialog.visible = false">
      <div class="dialog-box view-dialog-box">
        <div class="dialog-header">
          <h3>{{ viewDialog.warehouse.warehouse_name }} - 库区库位可视化</h3>
          <button class="dialog-close" @click="viewDialog.visible = false">&times;</button>
        </div>
        <div class="view-dialog-body">
          <!-- 侧边栏：库区列表 -->
          <div class="zone-sidebar">
            <div class="zone-sidebar-title">库区列表</div>
            <div
              v-for="zone in viewDialog.zones"
              :key="zone.zone_id"
              class="zone-item"
              :class="{ active: viewDialog.selectedZoneId === zone.zone_id }"
              @click="selectZone(zone)"
            >
              <span class="zone-dot" :class="zone.available_slots > 0 ? 'dot-green' : 'dot-red'"></span>
              <span class="zone-name">{{ zone.zone_name }}</span>
              <span class="zone-available">可用: {{ zone.available_slots }}/{{ zone.total_slots }}</span>
            </div>
            <div v-if="viewDialog.zones.length === 0" class="zone-empty">暂无库区数据</div>
          </div>
          <!-- 主视图区：库位网格 -->
          <div class="location-main">
            <div class="location-main-title" v-if="viewDialog.selectedZone">
              {{ viewDialog.selectedZone.zone_name }} - 库位一览
            </div>
            <div class="location-grid" v-if="viewDialog.selectedZone">
              <div
                v-for="loc in viewDialog.locations"
                :key="loc.location_id"
                class="location-card"
                :class="loc.is_occupied === 1 ? 'loc-occupied' : (loc.lock_status === '锁定' ? 'loc-locked' : 'loc-free')"
              >
                <span class="loc-name">{{ loc.location_name }}</span>
              </div>
              <div v-if="viewDialog.locations.length === 0" class="zone-empty">暂无库位数据</div>
            </div>
            <div class="location-main-placeholder" v-else>
              请在左侧选择一个库区查看库位详情
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import request from '@/utils/request.js'
import { Plus, Search } from '@element-plus/icons-vue'
import { ElIcon, ElMessage } from 'element-plus'

export default {
  name: 'ManagePage',
  components: { Plus, Search, ElIcon },
  data() {
    return {
      activeTab: 'personnel',
      pageSize: 10,
      page: { personnel: 1, warehouse: 1, goods: 1 },
      allPersonnel: [],
      personnelList: [],
      searchType: 'user_id',
      searchKeyword: '',
      warehouseList: [],
      allWarehouses: [],
      warehouseSearchType: 'warehouse_id',
      warehouseSearchKeyword: '',
      goodsList: [],
      allGoods: [],
      goodsSearchType: 'goods_code',
      goodsSearchKeyword: '',
      goodsDialog: {
        visible: false,
        form: { goods_id: null, goods_code: '', category: '', goods_name: '', storage_condition: '', unit: '' }
      },
      personnelDialog: {
        visible: false,
        mode: 'create',
        form: { user_id: null, username: '', real_name: '', phone: '', department: '', position: '', status: 1 }
      },
      confirmDialog: {
        visible: false
      },
      warehouseDialog: {
        visible: false,
        mode: 'create',
        form: { warehouse_id: null, warehouse_name: '', warehouse_type: '', location: '', warehouse_size: '', status: '启用' }
      },
      viewDialog: {
        visible: false,
        warehouse: {},
        zones: [],
        selectedZoneId: null,
        selectedZone: null,
        locations: []
      }
    }
  },
  computed: {
    // --- 分页 ---
    pagedPersonnel() {
      const s = (this.page.personnel - 1) * this.pageSize
      return this.personnelList.slice(s, s + this.pageSize)
    },
    personnelTotalPages() { return Math.ceil(this.personnelList.length / this.pageSize) || 1 },

    pagedWarehouses() {
      const s = (this.page.warehouse - 1) * this.pageSize
      return this.warehouseList.slice(s, s + this.pageSize)
    },
    warehouseTotalPages() { return Math.ceil(this.warehouseList.length / this.pageSize) || 1 },

    pagedGoods() {
      const s = (this.page.goods - 1) * this.pageSize
      return this.goodsList.slice(s, s + this.pageSize)
    },
    goodsTotalPages() { return Math.ceil(this.goodsList.length / this.pageSize) || 1 },

    availablePositions() {
      const map = {
        '管理部门': ['主管'],
        '仓管部门': ['仓库管理员', '仓库员工'],
        '质检部门': ['质检员', '温度检测员'],
      }
      return map[this.personnelDialog.form.department] || []
    }
  },
  mounted() {
    this.fetchPersonnel()
    this.fetchWarehouses()
    this.fetchGoods()
  },
  methods: {
    // --- 分页页码生成 ---
    getPageNumbers(totalPages, current) {
      const pages = []
      if (totalPages <= 7) {
        for (let i = 1; i <= totalPages; i++) pages.push(i)
      } else {
        pages.push(1)
        if (current > 3) pages.push('...')
        const start = Math.max(2, current - 1)
        const end = Math.min(totalPages - 1, current + 1)
        for (let i = start; i <= end; i++) pages.push(i)
        if (current < totalPages - 2) pages.push('...')
        pages.push(totalPages)
      }
      return pages
    },

    async fetchPersonnel() {
      try {
        const res = await request.get('/user/personnel')
        if (res.code === 200) {
          this.allPersonnel = res.data || []
          this.personnelList = this.allPersonnel
        }
      } catch (e) {
        ElMessage.error('获取人员列表失败')
      }
    },

    searchPersonnel() {
      this.page.personnel = 1
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
      this.page.personnel = 1
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
          position: item.position,
          status: item.status != null ? item.status : 1
        }
      } else {
        this.personnelDialog.mode = 'create'
        this.personnelDialog.form = { user_id: null, username: '', real_name: '', phone: '', department: '', position: '', status: 1 }
      }
      this.personnelDialog.visible = true
    },

    onDepartmentChange() {
      this.personnelDialog.form.position = ''
    },

    async submitPersonnel() {
      const f = this.personnelDialog.form
      if (!f.username) { ElMessage.warning('请输入用户名'); return }
      if (!f.real_name) { ElMessage.warning('请输入姓名'); return }
      if (!f.phone) { ElMessage.warning('请输入手机号'); return }
      if (!f.department) { ElMessage.warning('请选择所属部门'); return }
      if (!f.position) { ElMessage.warning('请选择职位'); return }

      if (this.personnelDialog.mode === 'create') {
        const maxId = this.allPersonnel.length > 0
          ? Math.max(...this.allPersonnel.map(p => p.user_id))
          : 0
        try {
          const res = await request.post('/user', {
            user_id: maxId + 1,
            username: f.username,
            real_name: f.real_name,
            phone: f.phone,
            department: f.department,
            position: f.position,
            password: '123456',
            status: 1
          })
          if (res.code === 200) {
            ElMessage.success('新增成功')
            this.personnelDialog.visible = false
            this.fetchPersonnel()
          } else {
            ElMessage.error(res.msg || '新增失败')
          }
        } catch (e) {
          ElMessage.error('新增失败')
        }
      } else {
        try {
          const res = await request.put('/user', {
            user_id: f.user_id,
            username: f.username,
            real_name: f.real_name,
            phone: f.phone,
            department: f.department,
            position: f.position,
            status: f.status
          })
          if (res.code === 200) {
            ElMessage.success('修改成功')
            this.personnelDialog.visible = false
            this.fetchPersonnel()
          } else {
            ElMessage.error(res.msg || '修改失败')
          }
        } catch (e) {
          ElMessage.error('修改失败')
        }
      }
    },

    deletePersonnel(id) {
      if (!confirm('确定删除该人员？')) return
      request.delete(`/user/${id}`).then(res => {
        if (res.code === 200) {
          ElMessage.success('删除成功')
          this.fetchPersonnel()
        } else {
          ElMessage.error(res.msg || '删除失败')
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
        const res = await request.put('/user', {
          user_id: this.personnelDialog.form.user_id,
          password: '123456'
        })
        if (res.code === 200) {
          ElMessage.success('密码重置成功')
          this.confirmDialog.visible = false
        } else {
          ElMessage.error(res.msg || '密码重置失败')
        }
      } catch (e) {
        ElMessage.error('密码重置失败')
      }
    },

    // 仓库 CRUD
    async fetchWarehouses() {
      try {
        const res = await request.get('/warehouse/list')
        if (res.code === 200) {
          this.allWarehouses = res.data || []
          this.warehouseList = this.allWarehouses
        }
      } catch (e) {
        ElMessage.error('获取仓库列表失败')
      }
    },

    searchWarehouse() {
      this.page.warehouse = 1
      const keyword = this.warehouseSearchKeyword.trim()
      if (!keyword) {
        this.warehouseList = this.allWarehouses
        return
      }
      if (this.warehouseSearchType === 'warehouse_id') {
        const id = parseInt(keyword)
        if (isNaN(id)) {
          ElMessage.warning('请输入有效的数字ID')
          return
        }
        this.warehouseList = this.allWarehouses.filter(w => w.warehouse_id === id)
      } else {
        this.warehouseList = this.allWarehouses.filter(w =>
          w.warehouse_name && w.warehouse_name.includes(keyword)
        )
      }
    },

    resetWarehouseSearch() {
      this.page.warehouse = 1
      this.warehouseSearchKeyword = ''
      this.warehouseList = this.allWarehouses
    },

    openWarehouseDialog(item) {
      if (item) {
        this.warehouseDialog.mode = 'edit'
        this.warehouseDialog.form = {
          warehouse_id: item.warehouse_id,
          warehouse_name: item.warehouse_name,
          warehouse_type: item.warehouse_type,
          location: item.location,
          warehouse_size: item.warehouse_size,
          status: item.status
        }
      } else {
        this.warehouseDialog.mode = 'create'
        this.warehouseDialog.form = { warehouse_id: null, warehouse_name: '', warehouse_type: '', location: '', warehouse_size: '', status: '启用' }
      }
      this.warehouseDialog.visible = true
    },

    async submitWarehouse() {
      const f = this.warehouseDialog.form
      if (!f.warehouse_name) { ElMessage.warning('请输入仓库名称'); return }
      if (!f.warehouse_type) { ElMessage.warning('请选择仓库类型'); return }
      if (!f.location) { ElMessage.warning('请输入地址'); return }

      if (this.warehouseDialog.mode === 'create') {
        if (!f.warehouse_size) { ElMessage.warning('请选择仓库尺寸'); return }
        try {
          const res = await request.post('/warehouse', {
            warehouse_name: f.warehouse_name,
            warehouse_type: f.warehouse_type,
            location: f.location,
            warehouse_size: f.warehouse_size
          })
          if (res.code === 200) {
            ElMessage.success('新增成功，已自动创建库区和库位')
            this.warehouseDialog.visible = false
            this.fetchWarehouses()
          } else {
            ElMessage.error(res.msg || '新增失败')
          }
        } catch (e) {
          ElMessage.error('新增失败')
        }
      } else {
        try {
          const res = await request.put('/warehouse', {
            warehouse_id: f.warehouse_id,
            warehouse_name: f.warehouse_name,
            warehouse_type: f.warehouse_type,
            location: f.location,
            status: f.status
          })
          if (res.code === 200) {
            ElMessage.success('修改成功')
            this.warehouseDialog.visible = false
            this.fetchWarehouses()
          } else {
            ElMessage.error(res.msg || '修改失败')
          }
        } catch (e) {
          ElMessage.error('修改失败')
        }
      }
    },

    async deleteWarehouse(id) {
      if (!confirm('确定删除该仓库？将同时删除关联的库区和库位！')) return
      try {
        const res = await request.delete(`/warehouse/${id}`)
        if (res.code === 200) {
          ElMessage.success('删除成功')
          this.fetchWarehouses()
        } else {
          ElMessage.error(res.msg || '删除失败')
        }
      } catch (e) {
        ElMessage.error('删除失败')
      }
    },

    // 仓库查看（可视化）
    async openViewDialog(warehouse) {
      this.viewDialog.warehouse = warehouse
      this.viewDialog.selectedZoneId = null
      this.viewDialog.selectedZone = null
      this.viewDialog.locations = []
      this.viewDialog.visible = true

      try {
        const res = await request.get('/zone/list', { warehouseId: warehouse.warehouse_id })
        if (res.code === 200) {
          this.viewDialog.zones = res.data || []
        }
      } catch (e) {
        ElMessage.error('获取库区列表失败')
      }
    },

    async selectZone(zone) {
      this.viewDialog.selectedZoneId = zone.zone_id
      this.viewDialog.selectedZone = zone
      try {
        const res = await request.get('/location/list', { zoneId: zone.zone_id })
        if (res.code === 200) {
          this.viewDialog.locations = res.data || []
        }
      } catch (e) {
        ElMessage.error('获取库位列表失败')
      }
    },

    // 货物 CRUD
    async fetchGoods() {
      try {
        const res = await request.get('/goods/list')
        if (res.code === 200) {
          this.allGoods = res.data || []
          this.goodsList = this.allGoods
        }
      } catch (e) {
        ElMessage.error('获取货物列表失败')
      }
    },

    searchGoods() {
      this.page.goods = 1
      const keyword = this.goodsSearchKeyword.trim()
      if (!keyword) {
        this.goodsList = this.allGoods
        return
      }
      if (this.goodsSearchType === 'goods_code') {
        this.goodsList = this.allGoods.filter(g =>
          g.goods_code && g.goods_code.includes(keyword)
        )
      } else {
        this.goodsList = this.allGoods.filter(g =>
          g.goods_name && g.goods_name.includes(keyword)
        )
      }
    },

    resetGoodsSearch() {
      this.page.goods = 1
      this.goodsSearchKeyword = ''
      this.goodsList = this.allGoods
    },

    openGoodsDialog() {
      this.goodsDialog.form = { goods_code: '', category: '', goods_name: '', storage_condition: '', unit: '' }
      this.goodsDialog.visible = true
    },

    async submitGoods() {
      const f = this.goodsDialog.form
      if (!f.goods_code) { ElMessage.warning('请输入货物编码'); return }
      if (!f.category) { ElMessage.warning('请选择种类'); return }
      if (!f.goods_name) { ElMessage.warning('请输入名称'); return }
      if (!f.storage_condition) { ElMessage.warning('请选择储存条件'); return }
      if (!f.unit) { ElMessage.warning('请输入单位'); return }

      try {
        const res = await request.post('/goods', {
          goods_code: f.goods_code,
          category: f.category,
          goods_name: f.goods_name,
          storage_condition: f.storage_condition,
          unit: f.unit
        })
        if (res.code === 200) {
          ElMessage.success('新增成功')
          this.goodsDialog.visible = false
          this.fetchGoods()
        } else {
          ElMessage.error(res.msg || '新增失败')
        }
      } catch (e) {
        ElMessage.error('新增失败')
      }
    },

    async deleteGoods(id) {
      if (!confirm('确定删除该货物？')) return
      try {
        const res = await request.delete(`/goods/${id}`)
        if (res.code === 200) {
          ElMessage.success('删除成功')
          this.fetchGoods()
        } else {
          ElMessage.error(res.msg || '删除失败')
        }
      } catch (e) {
        ElMessage.error('删除失败')
      }
    }
  }
}
</script>

<style scoped>
.manage-page { display: flex; flex-direction: column; gap: 12px; }

.tab-row {
  display: flex;
  gap: 0;
  background: #fff;
  border: 1px solid var(--border-color-light);
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
  background: var(--primary-color);
  color: #fff;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 8px;
}
.search-type {
  height: 32px;
  padding: 0 10px;
  border: 1px solid var(--border-color-light);
  border-radius: 4px;
  font-size: 13px;
  color: #303133;
  background: #fff;
}
.search-input {
  height: 32px;
  width: 180px;
  padding: 0 10px;
  border: 1px solid var(--border-color-light);
  border-radius: 4px;
  font-size: 13px;
  color: #303133;
  outline: none;
  transition: border-color 0.2s;
}
.search-input:focus { border-color: var(--primary-color); }
.search-input::placeholder { color: #c0c4cc; }

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
  background: var(--primary-color);
  color: #fff;
}
.btn-primary:hover { background: var(--primary-hover); }
.btn-cancel {
  background: #f4f4f5;
  color: #606266;
}
.btn-cancel:hover { background: #e6e6e8; }
.btn-search {
  background: #ecf5ff;
  color: #409eff;
}
.btn-search:hover { background: #d9ecff; }
.btn-reset-pwd {
  background: #fef0f0;
  color: #f56c6c;
  width: 100%;
}
.btn-reset-pwd:hover { background: #fde2e2; }

.table-wrapper {
  background: #fff;
  border: 1px solid var(--border-color-light);
  border-radius: 4px;
  overflow: hidden;
}
.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}
.data-table thead {
  background: var(--page-bg);
}
.data-table th {
  padding: 10px 14px;
  text-align: left;
  font-weight: 600;
  color: #303133;
  border-bottom: 1px solid var(--border-color-light);
  white-space: nowrap;
}
.data-table td {
  padding: 10px 14px;
  border-bottom: 1px solid var(--border-color-light);
  color: #606266;
}
.data-table tbody tr:hover { background: var(--page-bg); }
.empty-cell { text-align: center; padding: 40px 0; color: #c0c4cc; }

.status-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
}
.status-tag.启用 { background: #f0f9eb; color: #67c23a; }
.status-tag.禁用, .status-tag.停用 { background: #fef0f0; color: #f56c6c; }

.action-cell { display: flex; gap: 6px; }
.btn-action {
  padding: 4px 10px;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-action.edit { background: var(--primary-bg); color: var(--primary-color); }
.btn-action.edit:hover { background: var(--primary-bg-hover); }
.btn-action.view { background: #f0f9eb; color: #67c23a; }
.btn-action.view:hover { background: #e1f3d8; }
.btn-action.delete { background: #fef0f0; color: #f56c6c; }
.btn-action.delete:hover { background: #fde2e2; }
.btn-action.delete:disabled { background: #f5f5f5; color: #c0c4cc; cursor: not-allowed; }

/* 弹窗 */
.dialog-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.35); z-index: 9999;
  display: flex; align-items: center; justify-content: center;
}
.dialog-box {
  background: #fff;
  border: 1px solid var(--border-color-light);
  border-radius: 4px;
  width: 460px; max-width: 92vw;
}
.confirm-box {
  width: 360px;
}
.dialog-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 20px; border-bottom: 1px solid var(--border-color-light);
}
.dialog-header h3 { font-size: 15px; font-weight: 600; color: #303133; margin: 0; }
.dialog-close {
  background: none; border: none; color: #c0c4cc;
  font-size: 20px; cursor: pointer; padding: 0; line-height: 1;
}
.dialog-close:hover { color: #303133; }
.dialog-body { padding: 16px 20px; display: flex; flex-direction: column; gap: 12px; }
.dialog-footer {
  display: flex; justify-content: flex-end; gap: 8px;
  padding: 10px 20px 16px; border-top: 1px solid var(--border-color-light);
}
.form-item { display: flex; flex-direction: column; gap: 4px; }
.form-item label { font-size: 13px; color: #606266; }
.form-item .required { color: #f56c6c; }
.form-item input, .form-item select {
  height: 34px; padding: 0 10px;
  border: 1px solid var(--border-color-light);
  border-radius: 4px; font-size: 13px; color: #303133;
  outline: none; transition: border-color 0.2s;
}
.form-item input:focus, .form-item select:focus { border-color: var(--primary-color); }
.form-item select { background: #fff; cursor: pointer; }

/* 仓库查看弹窗 */
.view-dialog-box {
  width: 900px; max-width: 95vw;
}
.view-dialog-body {
  display: flex;
  height: 460px;
}
.zone-sidebar {
  width: 240px; min-width: 240px;
  border-right: 1px solid var(--border-color-light);
  overflow-y: auto;
  background: #fafafa;
}
.zone-sidebar-title {
  padding: 12px 14px;
  font-size: 13px; font-weight: 600; color: #303133;
  border-bottom: 1px solid var(--border-color-light);
  background: #fff;
  position: sticky; top: 0;
}
.zone-item {
  display: flex; align-items: center; gap: 8px;
  padding: 10px 14px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.15s;
}
.zone-item:hover { background: var(--primary-bg); }
.zone-item.active { background: var(--primary-bg-hover); }
.zone-dot {
  width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0;
}
.dot-green { background: #67c23a; }
.dot-red { background: #f56c6c; }
.zone-name {
  flex: 1; font-size: 13px; color: #303133;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.zone-available {
  font-size: 11px; color: #909399; flex-shrink: 0;
}
.zone-empty {
  padding: 20px; text-align: center; color: #c0c4cc; font-size: 13px;
}

.location-main {
  flex: 1; display: flex; flex-direction: column;
  overflow-y: auto;
}
.location-main-title {
  padding: 12px 16px;
  font-size: 13px; font-weight: 600; color: #303133;
  border-bottom: 1px solid var(--border-color-light);
  background: #fff;
  position: sticky; top: 0; z-index: 1;
}
.location-main-placeholder {
  flex: 1; display: flex; align-items: center; justify-content: center;
  color: #c0c4cc; font-size: 14px;
}
.location-grid {
  display: flex; flex-wrap: wrap; gap: 12px;
  padding: 16px;
  align-content: flex-start;
}
.location-card {
  width: 90px; height: 56px;
  border-radius: 4px;
  display: flex; align-items: center; justify-content: center;
  cursor: default;
  transition: transform 0.15s;
}
.location-card:hover { transform: scale(1.05); }
.loc-free {
  background: #f0f9eb;
  border: 1px solid #b3e19d;
}
.loc-occupied {
  background: #fef0f0;
  border: 1px solid #fab6b6;
}
.loc-locked {
  background: #fef6e7;
  border: 1px solid #f5dab1;
}
.loc-name {
  font-size: 11px; color: #303133;
  text-align: center; word-break: break-all;
}

@media (max-width: 768px) {
  .data-table { font-size: 12px; }
  .data-table th, .data-table td { padding: 8px; }
  .action-cell { flex-direction: column; gap: 4px; }
  .toolbar { flex-direction: column; align-items: flex-start; }
  .search-input { width: 120px; }
  .view-dialog-box { width: 98vw; }
  .view-dialog-body { flex-direction: column; height: auto; max-height: 70vh; }
  .zone-sidebar { width: 100%; min-width: auto; max-height: 150px; border-right: none; border-bottom: 1px solid var(--border-color-light); }
}

/* ========== 分页 ========== */
.pagination-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  color: #909399;
  padding: 10px 0 4px;
  flex-wrap: wrap;
  gap: 8px;
}
.total-info { white-space: nowrap; }
.pagination-controls {
  display: flex;
  align-items: center;
  gap: 4px;
}
.pagination-controls button {
  min-width: 32px;
  height: 30px;
  padding: 0 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #fff;
  color: #606266;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}
.pagination-controls button:hover:not(:disabled) {
  border-color: var(--primary-color, #409EFF);
  color: var(--primary-color, #409EFF);
}
.pagination-controls button.active {
  background: var(--primary-color, #409EFF);
  border-color: var(--primary-color, #409EFF);
  color: #fff;
}
.pagination-controls button:disabled {
  cursor: not-allowed;
  opacity: 0.4;
}
.page-ellipsis {
  width: 32px;
  text-align: center;
  color: #909399;
}
</style>
