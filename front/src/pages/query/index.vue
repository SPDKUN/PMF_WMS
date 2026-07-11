<template>
  <div class="query-page">
    <!-- 选项卡 -->
    <div class="tab-row">
      <button v-for="tab in tabs" :key="tab.key" :class="{ active: activeTab === tab.key }" @click="switchTab(tab.key)">
        {{ tab.label }}
      </button>
    </div>

    <!-- 人员列表 -->
    <div v-if="activeTab === 'personnel'" class="tab-content">
      <div class="toolbar">
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
          <button class="btn btn-search" @click="searchPersonnel">搜索</button>
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
    </div>

    <!-- 仓库列表 -->
    <div v-if="activeTab === 'warehouse'" class="tab-content">
      <div class="toolbar">
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
          <button class="btn btn-search" @click="searchWarehouse">搜索</button>
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
            <th width="100">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="warehouseList.length === 0">
            <td colspan="8" class="empty-cell">暂无仓库数据</td>
          </tr>
          <tr v-for="w in warehouseList" :key="w.warehouse_id">
            <td>{{ w.warehouse_id }}</td>
            <td>{{ w.warehouse_name }}</td>
            <td>{{ w.warehouse_type }}</td>
            <td>{{ w.location }}</td>
            <td>{{ w.warehouse_size }}</td>
            <td>{{ w.available_slots }} / {{ w.total_slots }}</td>
            <td><span class="status-tag" :class="w.status === '启用' ? '启用' : '禁用'">{{ w.status === '启用' ? '启用' : '停用' }}</span></td>
            <td><button class="btn-action view" @click="openViewDialog(w)">查看</button></td>
          </tr>
        </tbody>
      </table>
    </div>
    </div>

    <!-- 货物列表 -->
    <div v-if="activeTab === 'goods'" class="tab-content">
      <div class="toolbar">
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
          <button class="btn btn-search" @click="searchGoods">搜索</button>
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
            </tr>
          </thead>
          <tbody>
            <tr v-if="goodsList.length === 0">
              <td colspan="7" class="empty-cell">暂无货物数据</td>
            </tr>
            <tr v-for="g in goodsList" :key="g.goods_id">
              <td>{{ g.goods_id }}</td>
              <td>{{ g.goods_code }}</td>
              <td>{{ g.category }}</td>
              <td>{{ g.goods_name }}</td>
              <td>{{ g.storage_condition }}</td>
              <td>{{ g.quantity }}</td>
              <td>{{ g.unit }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 批次列表 -->
    <div v-if="activeTab === 'batch'" class="tab-content">
      <div class="toolbar">
        <div class="search-bar">
          <input
            type="text"
            v-model="batchSearchKeyword"
            placeholder="请输入批次号"
            @keyup.enter="searchBatch"
            class="search-input"
          />
          <button class="btn btn-search" @click="searchBatch">搜索</button>
          <button class="btn btn-cancel" @click="resetBatchSearch">重置</button>
        </div>
      </div>
      <div class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>批次号</th>
              <th>货物名称</th>
              <th>保质期至</th>
              <th>数量</th>
              <th>状态</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="batchList.length === 0">
              <td colspan="5" class="empty-cell">暂无批次数据</td>
            </tr>
            <tr v-for="b in batchList" :key="b.batch_id">
              <td>{{ b.batch_id }}</td>
              <td>{{ getBatchGoodsName(b.goods_id) }}</td>
              <td>{{ b.expiry_date }}</td>
              <td>{{ b.remaining_quantity }}</td>
              <td><span class="status-tag" :class="getBatchStatusClass(b.batch_status)">{{ b.batch_status }}</span></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 其他选项卡（通用表格） -->
    <div v-if="activeTab !== 'personnel' && activeTab !== 'warehouse' && activeTab !== 'goods' && activeTab !== 'batch'" class="table-wrapper">
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
      <span v-else-if="activeTab === 'goods'">共 {{ goodsList.length }} 条</span>
      <span v-else-if="activeTab === 'batch'">共 {{ batchList.length }} 条</span>
      <span v-else>共 {{ currentData.length }} 条</span>
    </div>

    <!-- 仓库查看弹窗（可视化） -->
    <div class="dialog-overlay" v-if="viewDialog.visible" @click.self="viewDialog.visible = false">
      <div class="dialog-box view-dialog-box">
        <div class="dialog-header">
          <h3>{{ viewDialog.warehouse.warehouse_name }} - 库区库位可视化</h3>
          <button class="dialog-close" @click="viewDialog.visible = false">&times;</button>
        </div>
        <div class="view-dialog-body">
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
          <div class="location-main">
            <div class="location-main-title" v-if="viewDialog.selectedZone">
              {{ viewDialog.selectedZone.zone_name }} - 库位一览
            </div>
            <div class="location-grid" v-if="viewDialog.selectedZone">
              <div
                v-for="loc in viewDialog.locations"
                :key="loc.location_id"
                class="location-card"
                :class="loc.is_occupied === 1 ? 'loc-occupied' : 'loc-free'"
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

export default {
  name: 'QueryPage',
  data() {
    return {
      activeTab: 'personnel',
      tabs: [
        { key: 'personnel', label: '人员列表' },
        { key: 'warehouse', label: '仓库列表' },
        { key: 'goods', label: '货物列表' },
        { key: 'inventory', label: '库存明细' },
        { key: 'batch', label: '批次列表' },
        { key: 'temperature', label: '温度列表' },
        { key: 'log', label: '操作日志' },
      ],
      allPersonnel: [],
      personnelList: [],
      searchType: 'user_id',
      searchKeyword: '',
      allWarehouses: [],
      warehouseList: [],
      warehouseSearchType: 'warehouse_id',
      warehouseSearchKeyword: '',
      allGoods: [],
      goodsList: [],
      goodsSearchType: 'goods_code',
      goodsSearchKeyword: '',
      allBatches: [],
      batchList: [],
      batchSearchKeyword: '',
      batchGoodsMap: {},
      viewDialog: {
        visible: false,
        warehouse: {},
        zones: [],
        selectedZoneId: null,
        selectedZone: null,
        locations: []
      },
      inventoryData: [
        { '商品名称': '预制菜A', 'SKU': 'PRE-A001', '库存数量': 500, '可用数量': 480, '锁定数量': 20, '单位': '箱', '仓库': 'A区冷库' },
        { '商品名称': '预制菜B', 'SKU': 'PRE-B002', '库存数量': 300, '可用数量': 300, '锁定数量': 0, '单位': '箱', '仓库': 'B区常温库' },
        { '商品名称': '调味料C', 'SKU': 'SAU-C001', '库存数量': 1200, '可用数量': 1150, '锁定数量': 50, '单位': '袋', '仓库': 'B区常温库' },
        { '商品名称': '冷冻食材D', 'SKU': 'FRZ-D001', '库存数量': 800, '可用数量': 780, '锁定数量': 20, '单位': 'kg', '仓库': 'C区冷冻库' },
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
        temperature: this.temperatureData,
        log: this.logData,
      }
      return map[this.activeTab] || []
    }
  },
  mounted() {
    this.fetchPersonnel()
    this.fetchWarehouses()
    this.fetchGoods()
    this.fetchBatches()
  },
  methods: {
    switchTab(key) {
      this.activeTab = key
      if (key === 'personnel' && this.personnelList.length === 0) {
        this.fetchPersonnel()
      } else if (key === 'warehouse' && this.warehouseList.length === 0) {
        this.fetchWarehouses()
      } else if (key === 'goods' && this.goodsList.length === 0) {
        this.fetchGoods()
      } else if (key === 'batch' && this.batchList.length === 0) {
        this.fetchBatches()
      }
    },
    async fetchPersonnel() {
      try {
        const res = await request.get('/user/personnel')
        if (res.code === 200) {
          this.allPersonnel = res.data || []
          this.personnelList = this.allPersonnel
        }
      } catch (e) {
        // 静默失败，查看页不弹错误
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
        if (isNaN(id)) return
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
    async fetchWarehouses() {
      try {
        const res = await request.get('/warehouse/list')
        if (res.code === 200) {
          this.allWarehouses = res.data || []
          this.warehouseList = this.allWarehouses
        }
      } catch (e) {
        // 静默失败
      }
    },
    searchWarehouse() {
      const keyword = this.warehouseSearchKeyword.trim()
      if (!keyword) {
        this.warehouseList = this.allWarehouses
        return
      }
      if (this.warehouseSearchType === 'warehouse_id') {
        const id = parseInt(keyword)
        if (isNaN(id)) return
        this.warehouseList = this.allWarehouses.filter(w => w.warehouse_id === id)
      } else {
        this.warehouseList = this.allWarehouses.filter(w =>
          w.warehouse_name && w.warehouse_name.includes(keyword)
        )
      }
    },
    resetWarehouseSearch() {
      this.warehouseSearchKeyword = ''
      this.warehouseList = this.allWarehouses
    },
    async fetchGoods() {
      try {
        const res = await request.get('/goods/list')
        if (res.code === 200) {
          this.allGoods = res.data || []
          this.goodsList = this.allGoods
        }
      } catch (e) {
        // 静默失败
      }
    },
    searchGoods() {
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
      this.goodsSearchKeyword = ''
      this.goodsList = this.allGoods
    },
    async fetchBatches() {
      try {
        const res = await request.get('/batch/list')
        if (res.code === 200) {
          this.allBatches = res.data || []
          this.batchList = this.allBatches
          this.buildBatchGoodsMap()
        }
      } catch (e) {
        // 静默失败
      }
    },
    async buildBatchGoodsMap() {
      try {
        const res = await request.get('/goods/list')
        if (res.code === 200) {
          const goodsList = res.data || []
          this.batchGoodsMap = {}
          goodsList.forEach(g => {
            this.batchGoodsMap[g.goods_id] = g.goods_name
          })
        }
      } catch (e) {
        // 静默失败
      }
    },
    getBatchGoodsName(goodsId) {
      return this.batchGoodsMap[goodsId] || '-'
    },
    getBatchStatusClass(status) {
      if (status === '正常') return 'status-normal'
      if (status === '待检') return 'status-pending'
      if (status === '锁定') return 'status-locked'
      if (status === '报废') return 'status-scrap'
      return ''
    },
    searchBatch() {
      const keyword = this.batchSearchKeyword.trim()
      if (!keyword) {
        this.batchList = this.allBatches
        return
      }
      this.batchList = this.allBatches.filter(b =>
        b.batch_id && b.batch_id.includes(keyword)
      )
    },
    resetBatchSearch() {
      this.batchSearchKeyword = ''
      this.batchList = this.allBatches
    },
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
        // 静默失败
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
        // 静默失败
      }
    }
  }
}
</script>

<style scoped>
.query-page { display: flex; flex-direction: column; gap: 12px; }

.tab-content {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
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
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 13px;
  color: #303133;
  background: #fff;
}
.search-input {
  height: 32px;
  width: 180px;
  padding: 0 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 13px;
  color: #303133;
  outline: none;
  transition: border-color 0.2s;
}
.search-input:focus { border-color: #409EFF; }
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
.btn-search {
  background: #ecf5ff;
  color: #409EFF;
}
.btn-search:hover { background: #d9ecff; }
.btn-cancel {
  background: #f4f4f5;
  color: #606266;
}
.btn-cancel:hover { background: #e6e6e8; }

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
.status-tag.启用, .status-tag.status-normal { background: #f0f9eb; color: #67c23a; }
.status-tag.禁用, .status-tag.status-scrap { background: #fef0f0; color: #f56c6c; }
.status-tag.status-pending { background: #fdf6ec; color: #e6a23c; }
.status-tag.status-locked { background: #ecf5ff; color: #409EFF; }

.pagination-row {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  font-size: 13px;
  color: #909399;
}

.btn-action {
  padding: 4px 10px;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-action.view { background: #f0f9eb; color: #67c23a; }
.btn-action.view:hover { background: #e1f3d8; }

/* 仓库查看弹窗 */
.dialog-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.35); z-index: 9999;
  display: flex; align-items: center; justify-content: center;
}
.dialog-box {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  width: 460px; max-width: 92vw;
}
.dialog-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 20px; border-bottom: 1px solid #ebeef5;
}
.dialog-header h3 { font-size: 15px; font-weight: 600; color: #303133; margin: 0; }
.dialog-close {
  background: none; border: none; color: #c0c4cc;
  font-size: 20px; cursor: pointer; padding: 0; line-height: 1;
}
.dialog-close:hover { color: #303133; }

.view-dialog-box { width: 900px; }
.view-dialog-body { display: flex; height: 460px; }
.zone-sidebar {
  width: 240px; min-width: 240px;
  border-right: 1px solid #ebeef5;
  overflow-y: auto;
  background: #fafafa;
}
.zone-sidebar-title {
  padding: 12px 14px;
  font-size: 13px; font-weight: 600; color: #303133;
  border-bottom: 1px solid #ebeef5;
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
.zone-item:hover { background: #ecf5ff; }
.zone-item.active { background: #d9ecff; }
.zone-dot {
  width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0;
}
.dot-green { background: #67c23a; }
.dot-red { background: #f56c6c; }
.zone-name {
  flex: 1; font-size: 13px; color: #303133;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.zone-available { font-size: 11px; color: #909399; flex-shrink: 0; }
.zone-empty { padding: 20px; text-align: center; color: #c0c4cc; font-size: 13px; }

.location-main { flex: 1; display: flex; flex-direction: column; overflow-y: auto; }
.location-main-title {
  padding: 12px 16px;
  font-size: 13px; font-weight: 600; color: #303133;
  border-bottom: 1px solid #ebeef5;
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
.loc-free { background: #f0f9eb; border: 1px solid #b3e19d; }
.loc-occupied { background: #fef0f0; border: 1px solid #fab6b6; }
.loc-name { font-size: 11px; color: #303133; text-align: center; word-break: break-all; }

@media (max-width: 768px) {
  .tab-row { flex-wrap: wrap; }
  .tab-row button { padding: 5px 12px; font-size: 12px; }
  .data-table { font-size: 12px; }
  .data-table th, .data-table td { padding: 8px; }
  .view-dialog-box { width: 98vw; }
  .view-dialog-body { flex-direction: column; height: auto; max-height: 70vh; }
  .zone-sidebar { width: 100%; min-width: auto; max-height: 150px; border-right: none; border-bottom: 1px solid #ebeef5; }
}
</style>
