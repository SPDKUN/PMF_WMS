<template>
  <div class="query-page">
    <!-- 选项卡 -->
    <div class="tab-row">
      <button v-for="tab in tabs" :key="tab.key" :class="{ active: activeTab === tab.key }" @click="switchTab(tab.key)">
        {{ tab.label }}
      </button>
    </div>

    <!-- 人员列表 -->
    <div v-if="activeTab === 'personnel'" class="table-wrapper">
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
          </tr>
        </thead>
        <tbody>
          <tr v-if="personnelList.length === 0">
            <td colspan="7" class="empty-cell">暂无人员数据</td>
          </tr>
          <tr v-for="p in personnelList" :key="p.user_id">
            <td>{{ p.user_id }}</td>
            <td>{{ p.username }}</td>
            <td>{{ p.real_name }}</td>
            <td>{{ p.phone }}</td>
            <td>{{ p.department }}</td>
            <td>{{ p.position }}</td>
            <td><span class="status-tag" :class="p.status === 1 ? '启用' : '禁用'">{{ p.status === 1 ? '启用' : '禁用' }}</span></td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 仓库列表 -->
    <div v-if="activeTab === 'warehouse'" class="table-wrapper">
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
          </tr>
        </thead>
        <tbody>
          <tr v-if="warehouseList.length === 0">
            <td colspan="7" class="empty-cell">暂无仓库数据</td>
          </tr>
          <tr v-for="w in warehouseList" :key="w.warehouse_id">
            <td>{{ w.warehouse_id }}</td>
            <td>{{ w.warehouse_name }}</td>
            <td>{{ w.warehouse_type }}</td>
            <td>{{ w.location }}</td>
            <td>{{ w.warehouse_size }}</td>
            <td>{{ w.available_slots }} / {{ w.total_slots }}</td>
            <td><span class="status-tag" :class="w.status === '启用' ? '启用' : '禁用'">{{ w.status === '启用' ? '启用' : '停用' }}</span></td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 其他选项卡（通用表格） -->
    <div v-if="activeTab !== 'personnel' && activeTab !== 'warehouse'" class="table-wrapper">
      <table class="data-table">
        <thead>
          <tr>
            <th v-for="col in currentColumns" :key="col">{{ col }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="currentData.length === 0">
            <td :colspan="currentColumns.length" class="empty-cell">暂无数据</td>
          </tr>
          <tr v-for="(row, i) in currentData" :key="i">
            <td v-for="col in currentColumns" :key="col">{{ row[col] || '-' }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 分页 -->
    <div class="pagination-row">
      <span v-if="activeTab === 'personnel'">共 {{ personnelList.length }} 条</span>
      <span v-else-if="activeTab === 'warehouse'">共 {{ warehouseList.length }} 条</span>
      <span v-else>共 {{ currentData.length }} 条</span>
    </div>
  </div>
</template>

<script>
import request from '@/utils/request.js'

export default {
  name: 'QueryPage',
  data() {
    return {
      activeTab: 'personnel',
      tabs: [
        { key: 'personnel', label: '人员列表' },
        { key: 'warehouse', label: '仓库列表' },
        { key: 'inventory', label: '库存明细' },
        { key: 'batch', label: '批次列表' },
        { key: 'temperature', label: '温度列表' },
        { key: 'log', label: '操作日志' },
      ],
      personnelList: [],
      warehouseList: [],
      inventoryData: [
        { '商品名称': '预制菜A', 'SKU': 'PRE-A001', '库存数量': 500, '可用数量': 480, '锁定数量': 20, '单位': '箱', '仓库': 'A区冷库' },
        { '商品名称': '预制菜B', 'SKU': 'PRE-B002', '库存数量': 300, '可用数量': 300, '锁定数量': 0, '单位': '箱', '仓库': 'B区常温库' },
        { '商品名称': '调味料C', 'SKU': 'SAU-C001', '库存数量': 1200, '可用数量': 1150, '锁定数量': 50, '单位': '袋', '仓库': 'B区常温库' },
        { '商品名称': '冷冻食材D', 'SKU': 'FRZ-D001', '库存数量': 800, '可用数量': 780, '锁定数量': 20, '单位': 'kg', '仓库': 'C区冷冻库' },
      ],
      batchData: [
        { '批次号': 'B20260710-001', '商品名称': '预制菜A', '生产日期': '2026-07-01', '保质期至': '2027-01-01', '数量': 200, '状态': '正常' },
        { '批次号': 'B20260709-002', '商品名称': '冷冻食材D', '生产日期': '2026-07-05', '保质期至': '2027-07-05', '数量': 400, '状态': '正常' },
        { '批次号': 'B20260705-003', '商品名称': '调味料C', '生产日期': '2026-06-20', '保质期至': '2027-06-20', '数量': 600, '状态': '临近保质期' },
      ],
      temperatureData: [
        { '仓库': 'A区冷库', '记录时间': '2026-07-10 08:00', '温度(°C)': -18.5, '湿度(%)': 65, '状态': '正常' },
        { '仓库': 'A区冷库', '记录时间': '2026-07-10 07:00', '温度(°C)': -18.2, '湿度(%)': 64, '状态': '正常' },
        { '仓库': 'C区冷冻库', '记录时间': '2026-07-10 08:00', '温度(°C)': -22.0, '湿度(%)': 70, '状态': '正常' },
        { '仓库': 'C区冷冻库', '记录时间': '2026-07-10 07:00', '温度(°C)': -21.5, '湿度(%)': 71, '状态': '正常' },
        { '仓库': 'B区常温库', '记录时间': '2026-07-10 08:00', '温度(°C)': 22.0, '湿度(%)': 55, '状态': '正常' },
      ],
      logData: [
        { '时间': '2026-07-10 08:30', '操作人': '张三', '操作类型': '入库', '描述': '采购入库 PRE-A001 200箱', '结果': '成功' },
        { '时间': '2026-07-10 08:15', '操作人': '李四', '操作类型': '出库', '描述': '销售出库 PRE-B002 50箱', '结果': '成功' },
        { '时间': '2026-07-10 07:50', '操作人': '王五', '操作类型': '盘点', '描述': '全盘 A区冷库', '结果': '成功' },
        { '时间': '2026-07-10 07:30', '操作人': '张三', '操作类型': '质检', '描述': '来料检 FRZ-D001', '结果': '通过' },
        { '时间': '2026-07-09 17:00', '操作人': '赵六', '操作类型': '移库', '描述': 'A区→B区 调味料C 100袋', '结果': '成功' },
      ]
    }
  },
  computed: {
    currentColumns() {
      if (this.currentData.length === 0) return []
      return Object.keys(this.currentData[0])
    },
    currentData() {
      const map = {
        inventory: this.inventoryData,
        batch: this.batchData,
        temperature: this.temperatureData,
        log: this.logData,
      }
      return map[this.activeTab] || []
    }
  },
  mounted() {
    this.fetchPersonnel()
    this.fetchWarehouses()
  },
  methods: {
    switchTab(key) {
      this.activeTab = key
      if (key === 'personnel' && this.personnelList.length === 0) {
        this.fetchPersonnel()
      } else if (key === 'warehouse' && this.warehouseList.length === 0) {
        this.fetchWarehouses()
      }
    },
    async fetchPersonnel() {
      try {
        const res = await request.get('/user/personnel')
        if (res.code === 200) {
          this.personnelList = res.data || []
        }
      } catch (e) {
        // 静默失败，查看页不弹错误
      }
    },
    async fetchWarehouses() {
      try {
        const res = await request.get('/warehouse/list')
        if (res.code === 200) {
          this.warehouseList = res.data || []
        }
      } catch (e) {
        // 静默失败
      }
    }
  }
}
</script>

<style scoped>
.query-page { display: flex; flex-direction: column; gap: 12px; }

.tab-row {
  display: flex;
  gap: 0;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 4px;
  width: fit-content;
  flex-wrap: wrap;
}
.tab-row button {
  padding: 6px 18px;
  border: none;
  border-radius: 4px;
  background: transparent;
  color: #606266;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}
.tab-row button.active {
  background: #409EFF;
  color: #fff;
}

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

.status-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
}
.status-tag.启用 { background: #f0f9eb; color: #67c23a; }
.status-tag.禁用 { background: #fef0f0; color: #f56c6c; }

.pagination-row {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  font-size: 13px;
  color: #909399;
}

@media (max-width: 768px) {
  .tab-row { flex-wrap: wrap; }
  .tab-row button { padding: 5px 12px; font-size: 12px; }
  .data-table { font-size: 12px; }
  .data-table th, .data-table td { padding: 8px; }
}
</style>
