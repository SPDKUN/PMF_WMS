<template>
  <div class="query-page">
    <!-- 选项卡 -->
    <div class="tab-row">
      <button v-for="tab in tabs" :key="tab.key" :class="{ active: activeTab === tab.key }" @click="activeTab = tab.key">
        {{ tab.label }}
      </button>
    </div>

    <!-- 表格内容 -->
    <div class="table-wrapper">
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
      <span>共 {{ currentData.length }} 条</span>
    </div>
  </div>
</template>

<script>
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
      personnelData: [
        { '姓名': '张三', '手机号': '13800001111', '邮箱': 'zhangsan@wms.com', '角色': '管理员', '状态': '启用' },
        { '姓名': '李四', '手机号': '13800002222', '邮箱': 'lisi@wms.com', '角色': '仓管员', '状态': '启用' },
        { '姓名': '王五', '手机号': '13800003333', '邮箱': 'wangwu@wms.com', '角色': '操作员', '状态': '启用' },
        { '姓名': '赵六', '手机号': '13800004444', '邮箱': 'zhaoliu@wms.com', '角色': '质检员', '状态': '禁用' },
      ],
      warehouseData: [
        { '仓库名称': 'A区冷库', '地址': '园区北路88号', '面积(m²)': 1200, '库位数': 80, '状态': '启用' },
        { '仓库名称': 'B区常温库', '地址': '园区南路66号', '面积(m²)': 2000, '库位数': 150, '状态': '启用' },
        { '仓库名称': 'C区冷冻库', '地址': '园区东路33号', '面积(m²)': 800, '库位数': 50, '状态': '停用' },
      ],
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
        personnel: this.personnelData,
        warehouse: this.warehouseData,
        inventory: this.inventoryData,
        batch: this.batchData,
        temperature: this.temperatureData,
        log: this.logData,
      }
      return map[this.activeTab] || []
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
