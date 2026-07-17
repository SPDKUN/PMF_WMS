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
          <button class="btn btn-export" @click="exportPersonnel">导出Excel</button>
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
          <tr v-for="p in pagedPersonnel" :key="p.user_id">
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
          <button class="btn btn-export" @click="exportWarehouse">导出Excel</button>
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
          <tr v-for="w in pagedWarehouses" :key="w.warehouse_id">
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
          <button class="btn btn-export" @click="exportGoods">导出Excel</button>
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
            <tr v-for="g in pagedGoods" :key="g.goods_id">
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
          <button class="btn btn-export" @click="exportBatch">导出Excel</button>
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
            <tr v-for="b in pagedBatches" :key="b.batch_id">
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

    <!-- 库存明细 -->
    <div v-if="activeTab === 'inventory'" class="tab-content">
      <div class="toolbar">
        <div class="search-bar">
          <div class="search-item">
            <label>批次号</label>
            <input
              type="text"
              v-model="inventorySearch.batchId"
              placeholder="请输入批次号"
              @keyup.enter="searchInventory"
              class="search-input"
            />
          </div>
          <button class="btn btn-search" @click="searchInventory">搜索</button>
          <button class="btn btn-cancel" @click="resetInventorySearch">重置</button>
          <button class="btn btn-export" @click="exportInventory">导出Excel</button>
        </div>
      </div>
      <div class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>货物名称</th>
              <th>批次号</th>
              <th>存储位置</th>
              <th>当前数量</th>
              <th>锁定数量</th>
              <th>库存状态</th>
              <th>单位</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="inventoryTableData.length === 0">
              <td colspan="8" class="empty-cell">暂无库存数据</td>
            </tr>
            <tr v-for="item in pagedInventory" :key="item.inventory_id">
              <td>{{ item.inventory_id }}</td>
              <td>{{ item.goods_name || '-' }}</td>
              <td>{{ item.batch_id }}</td>
              <td>{{ item.location_name || '-' }}</td>
              <td>{{ item.quantity }}</td>
              <td>{{ item.locked_quantity }}</td>
              <td><span class="status-tag" :class="invStatusClass(item.inventory_status)">{{ item.inventory_status }}</span></td>
              <td>{{ item.unit || '-' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 操作日志 -->
    <div v-if="activeTab === 'log'" class="tab-content">
      <div class="toolbar">
        <div class="search-bar">
          <div class="search-item">
            <label>操作日期</label>
            <input type="date" v-model="logSearch.date" class="search-input" placeholder="选择日期" />
          </div>
          <div class="search-item">
            <label>操作类型</label>
            <select v-model="logSearch.operationType">
              <option value="">全部</option>
              <option v-for="t in logOperationTypes" :key="t" :value="t">{{ t }}</option>
            </select>
          </div>
          <button class="btn btn-search" @click="searchLogs">搜索</button>
          <button class="btn btn-cancel" @click="resetLogSearch">重置</button>
          <button class="btn btn-export" @click="exportLogs">导出Excel</button>
        </div>
      </div>
      <div class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>时间（操作时间）</th>
              <th>操作人名称</th>
              <th>操作类型</th>
              <th>操作内容</th>
              <th>操作结果</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="logData.length === 0">
              <td colspan="5" class="empty-cell">暂无操作日志</td>
            </tr>
            <tr v-for="log in pagedLogs" :key="log.log_id">
              <td>{{ log.operation_time }}</td>
              <td>{{ getOperatorName(log.operator_id) }}</td>
              <td>{{ log.operation_type }}</td>
              <td>{{ trimOperationContent(log.operation_content) }}</td>
              <td>
                <span>{{ trimOperationContent(log.operation_result) }}</span>
                <a v-if="hasDetailInLog(log)" class="detail-link" @click="openDetailFromLog(log)">详情</a>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 其他选项卡（通用表格） -->
    <div v-if="activeTab !== 'personnel' && activeTab !== 'warehouse' && activeTab !== 'goods' && activeTab !== 'batch' && activeTab !== 'inventory' && activeTab !== 'log'" class="table-wrapper" v-show="false">
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
    <div class="pagination-row" v-if="activeTab === 'personnel'">
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

    <div class="pagination-row" v-if="activeTab === 'warehouse'">
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

    <div class="pagination-row" v-if="activeTab === 'goods'">
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

    <div class="pagination-row" v-if="activeTab === 'batch'">
      <span class="total-info">共 {{ batchList.length }} 条</span>
      <div class="pagination-controls">
        <button :disabled="page.batch <= 1" @click="page.batch = 1">首页</button>
        <button :disabled="page.batch <= 1" @click="page.batch--">上一页</button>
        <template v-for="p in getPageNumbers(batchTotalPages, page.batch)" :key="p">
          <span v-if="p === '...'" class="page-ellipsis">...</span>
          <button v-else :class="{ active: page.batch === p }" @click="page.batch = p">{{ p }}</button>
        </template>
        <button :disabled="page.batch >= batchTotalPages" @click="page.batch++">下一页</button>
        <button :disabled="page.batch >= batchTotalPages" @click="page.batch = batchTotalPages">末页</button>
      </div>
    </div>

    <div class="pagination-row" v-if="activeTab === 'inventory'">
      <span class="total-info">共 {{ inventoryTableData.length }} 条</span>
      <div class="pagination-controls">
        <button :disabled="page.inventory <= 1" @click="page.inventory = 1">首页</button>
        <button :disabled="page.inventory <= 1" @click="page.inventory--">上一页</button>
        <template v-for="p in getPageNumbers(inventoryTotalPages, page.inventory)" :key="p">
          <span v-if="p === '...'" class="page-ellipsis">...</span>
          <button v-else :class="{ active: page.inventory === p }" @click="page.inventory = p">{{ p }}</button>
        </template>
        <button :disabled="page.inventory >= inventoryTotalPages" @click="page.inventory++">下一页</button>
        <button :disabled="page.inventory >= inventoryTotalPages" @click="page.inventory = inventoryTotalPages">末页</button>
      </div>
    </div>

    <div class="pagination-row" v-if="activeTab === 'log'">
      <span class="total-info">共 {{ logData.length }} 条</span>
      <div class="pagination-controls">
        <button :disabled="page.log <= 1" @click="page.log = 1">首页</button>
        <button :disabled="page.log <= 1" @click="page.log--">上一页</button>
        <template v-for="p in getPageNumbers(logTotalPages, page.log)" :key="p">
          <span v-if="p === '...'" class="page-ellipsis">...</span>
          <button v-else :class="{ active: page.log === p }" @click="page.log = p">{{ p }}</button>
        </template>
        <button :disabled="page.log >= logTotalPages" @click="page.log++">下一页</button>
        <button :disabled="page.log >= logTotalPages" @click="page.log = logTotalPages">末页</button>
      </div>
    </div>

    <!-- 盘点详情弹窗 -->
    <div class="dialog-overlay" v-if="detailDialog.visible" @click.self="detailDialog.visible = false">
      <div class="dialog-box" style="width:700px;">
        <div class="dialog-header">
          <h3>盘点详情</h3>
          <button class="dialog-close" @click="detailDialog.visible = false">&times;</button>
        </div>
        <div class="dialog-body" style="max-height:500px;overflow-y:auto;">
          <div v-if="detailDialog.surplus.length > 0" style="margin-bottom:16px;">
            <h4 style="margin:0 0 8px 0;color:var(--warning);">盘盈明细（{{ detailDialog.surplus.length }}项）</h4>
            <table class="data-table">
              <thead>
                <tr>
                  <th>序号</th>
                  <th>批次号</th>
                  <th>库位ID</th>
                  <th>差异数量</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(item, idx) in detailDialog.surplus" :key="'s'+idx">
                  <td>{{ idx + 1 }}</td>
                  <td>{{ item.batchId }}</td>
                  <td>LOC-{{ item.locationId }}</td>
                  <td style="color:var(--warning);font-weight:600;">+{{ item.diff }}</td>
                </tr>
              </tbody>
            </table>
          </div>
          <div v-if="detailDialog.shortage.length > 0">
            <h4 style="margin:0 0 8px 0;color:var(--danger);">盘亏明细（{{ detailDialog.shortage.length }}项）</h4>
            <table class="data-table">
              <thead>
                <tr>
                  <th>序号</th>
                  <th>批次号</th>
                  <th>库位ID</th>
                  <th>差异数量</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(item, idx) in detailDialog.shortage" :key="'h'+idx">
                  <td>{{ idx + 1 }}</td>
                  <td>{{ item.batchId }}</td>
                  <td>LOC-{{ item.locationId }}</td>
                  <td style="color:var(--danger);font-weight:600;">{{ item.diff }}</td>
                </tr>
              </tbody>
            </table>
          </div>
          <div v-if="detailDialog.surplus.length === 0 && detailDialog.shortage.length === 0" style="text-align:center;color:var(--foreground-muted);padding:20px;">
            暂无详情数据
          </div>
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
                :class="loc.is_occupied === 1 ? 'loc-occupied' : (loc.lock_status === '锁定' ? 'loc-locked' : 'loc-free')"
                @mouseenter="onLocMouseEnter($event, loc)"
                @mouseleave="onLocMouseLeave"
              >
                <span class="loc-name">{{ loc.location_name }}</span>
              </div>
              <div v-if="viewDialog.locations.length === 0" class="zone-empty">暂无库位数据</div>
            </div>
            <!-- 悬浮提示框（fixed定位，不受overflow裁剪） -->
            <div
              class="loc-tooltip-fixed"
              v-if="tooltipVisible"
              :style="{ left: tooltipStyle.left + 'px', top: tooltipStyle.top + 'px' }"
            >
              <div class="tooltip-row"><span class="tooltip-label">批次：</span>{{ tooltipData.batchId || '-' }}</div>
              <div class="tooltip-row"><span class="tooltip-label">货物：</span>{{ tooltipData.goodsName || '-' }}</div>
              <div class="tooltip-row"><span class="tooltip-label">到期：</span>{{ tooltipData.expiryDate || '-' }}</div>
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
      pageSize: 10,
      page: { personnel: 1, warehouse: 1, goods: 1, batch: 1, inventory: 1, log: 1 },
      tabs: [
        { key: 'personnel', label: '人员列表' },
        { key: 'warehouse', label: '仓库列表' },
        { key: 'goods', label: '货物列表' },
        { key: 'inventory', label: '库存明细' },
        { key: 'batch', label: '批次列表' },
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
        locations: [],
        locationInfoMap: {}
      },
      tooltipVisible: false,
      tooltipStyle: { left: 0, top: 0 },
      tooltipData: { batchId: '', goodsName: '', expiryDate: '' },
      inventorySearch: {
        batchId: '',
      },
      inventoryTableData: [],
      inventoryTotal: 0,
      logData: [],
      logSearch: {
        date: '',
        operationType: ''
      },
      logOperationTypes: [],
      operatorNameMap: {},
      detailDialog: {
        visible: false,
        surplus: [],
        shortage: []
      }
    }
  },
  computed: {
    currentColumns() {
      if (this.currentData.length === 0) return []
      return Object.keys(this.currentData[0])
    },
    currentData() { return [] },

    // --- 分页计算 ---
    pagedPersonnel() {
      const start = (this.page.personnel - 1) * this.pageSize
      return this.personnelList.slice(start, start + this.pageSize)
    },
    personnelTotalPages() { return Math.ceil(this.personnelList.length / this.pageSize) || 1 },

    pagedWarehouses() {
      const start = (this.page.warehouse - 1) * this.pageSize
      return this.warehouseList.slice(start, start + this.pageSize)
    },
    warehouseTotalPages() { return Math.ceil(this.warehouseList.length / this.pageSize) || 1 },

    pagedGoods() {
      const start = (this.page.goods - 1) * this.pageSize
      return this.goodsList.slice(start, start + this.pageSize)
    },
    goodsTotalPages() { return Math.ceil(this.goodsList.length / this.pageSize) || 1 },

    pagedBatches() {
      const start = (this.page.batch - 1) * this.pageSize
      return this.batchList.slice(start, start + this.pageSize)
    },
    batchTotalPages() { return Math.ceil(this.batchList.length / this.pageSize) || 1 },

    pagedInventory() {
      const start = (this.page.inventory - 1) * this.pageSize
      return this.inventoryTableData.slice(start, start + this.pageSize)
    },
    inventoryTotalPages() { return Math.ceil(this.inventoryTableData.length / this.pageSize) || 1 },

    pagedLogs() {
      const start = (this.page.log - 1) * this.pageSize
      return this.logData.slice(start, start + this.pageSize)
    },
    logTotalPages() { return Math.ceil(this.logData.length / this.pageSize) || 1 },
  },
  watch: {
    '$route.query.tab'(val) {
      if (val && ['personnel', 'warehouse', 'goods', 'inventory', 'batch', 'log'].includes(val)) {
        this.activeTab = val
      }
    }
  },
  mounted() {
    const tab = this.$route.query.tab
    if (tab && ['personnel', 'warehouse', 'goods', 'inventory', 'batch', 'log'].includes(tab)) {
      this.activeTab = tab
    }
    this.fetchPersonnel()
    this.fetchWarehouses()
    this.fetchGoods()
    this.fetchBatches()
  },
  methods: {
    // --- 分页按钮生成 ---
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

    switchTab(key) {
      this.activeTab = key
      this.$router.replace({ query: { tab: key } })
      this.page[key] = 1  // 切换标签重置页码
      if (key === 'personnel' && this.personnelList.length === 0) {
        this.fetchPersonnel()
      } else if (key === 'warehouse' && this.warehouseList.length === 0) {
        this.fetchWarehouses()
      } else if (key === 'goods' && this.goodsList.length === 0) {
        this.fetchGoods()
      } else if (key === 'batch' && this.batchList.length === 0) {
        this.fetchBatches()
      } else if (key === 'inventory' && this.inventoryTableData.length === 0) {
        this.loadInventoryData()
      } else if (key === 'log' && this.logData.length === 0) {
        this.fetchLogs()
      }
    },
    async fetchPersonnel() {
      try {
        const res = await request.get('/user/personnel')
        if (res.code === 200) {
          this.allPersonnel = res.data || []
          this.personnelList = this.allPersonnel
          this.buildOperatorNameMap()
        }
      } catch (e) {
        // 静默失败，查看页不弹错误
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
        if (isNaN(id)) return
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
      this.page.warehouse = 1
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
      this.page.warehouse = 1
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
      this.page.batch = 1
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
      this.page.batch = 1
      this.batchSearchKeyword = ''
      this.batchList = this.allBatches
    },

    // --- 库存明细 ---
    async loadInventoryData() {
      try {
        const params = {}
        if (this.inventorySearch.batchId) { params.batchId = this.inventorySearch.batchId }
        const res = await request.get('/inventory/listWithDetails', params)
        if (res.code === 200) {
          this.inventoryTableData = res.data || []
          this.inventoryTotal = this.inventoryTableData.length
        }
      } catch (e) {
        this.inventoryTableData = []
        this.inventoryTotal = 0
      }
    },
    invStatusClass(status) {
      if (status === '正常') return 'status-normal'
      if (status === '待入库') return 'status-pending'
      if (status === '待出库') return 'status-locked'
      if (status === '待报废') return 'status-scrap'
      return ''
    },
    searchInventory() { this.page.inventory = 1; this.loadInventoryData() },
    resetInventorySearch() {
      this.page.inventory = 1
      this.inventorySearch = { batchId: '' }
      this.loadInventoryData()
    },

    // --- 操作日志 ---
    async fetchLogs(params = {}) {
      try {
        const res = await request.get('/operationLog/list', params)
        if (res.code === 200) {
          this.logData = res.data || []
          this.extractLogOperationTypes()
          this.buildOperatorNameMap()
        }
      } catch (e) {
        // 静默失败
      }
    },
    extractLogOperationTypes() {
      const types = new Set()
      this.logData.forEach(log => {
        if (log.operation_type) types.add(log.operation_type)
      })
      this.logOperationTypes = Array.from(types)
    },
    buildOperatorNameMap() {
      this.allPersonnel.forEach(p => {
        this.operatorNameMap[p.user_id] = p.real_name || p.username
      })
    },
    getOperatorName(operatorId) {
      return this.operatorNameMap[operatorId] || operatorId
    },
    searchLogs() {
      this.page.log = 1
      const params = {}
      if (this.logSearch.date) params.date = this.logSearch.date
      if (this.logSearch.operationType) params.operationType = this.logSearch.operationType
      this.fetchLogs(params)
    },
    resetLogSearch() {
      this.page.log = 1
      this.logSearch = { date: '', operationType: '' }
      this.fetchLogs()
    },
    // 去掉操作内容中 ||| 后面的JSON详情，仅显示简洁文本
    trimOperationContent(content) {
      if (!content) return '-'
      const idx = content.indexOf('|||')
      return idx >= 0 ? content.substring(0, idx) : content
    },
    // 兼容新旧数据：JSON详情可能在operation_content（新）或operation_result（旧）
    hasDetailInLog(log) {
      return (log.operation_content && log.operation_content.indexOf('|||{') >= 0)
          || (log.operation_result && log.operation_result.indexOf('|||{') >= 0)
    },
    openDetailFromLog(log) {
      // 优先从 operation_content 取（新格式），再从 operation_result 取（旧格式）
      let source = null
      if (log.operation_content && log.operation_content.indexOf('|||{') >= 0) {
        source = log.operation_content
      } else if (log.operation_result && log.operation_result.indexOf('|||{') >= 0) {
        source = log.operation_result
      }
      if (!source) return
      const idx = source.indexOf('|||{')
      try {
        let jsonStr = source.substring(idx + 3)
        jsonStr = jsonStr.replace(/"diff":\+(\d+)/g, '"diff":$1')
        const json = JSON.parse(jsonStr)
        this.detailDialog.surplus = json.surplus || []
        this.detailDialog.shortage = json.shortage || []
        this.detailDialog.visible = true
      } catch (e) {
        console.error('解析盘点详情失败:', e)
      }
    },

    exportPersonnel() { request.exportExcel('/user/excel', '人员列表.xlsx') },
    exportWarehouse() { request.exportExcel('/warehouse/excel', '仓库列表.xlsx') },
    exportGoods() { request.exportExcel('/goods/excel', '货物列表.xlsx') },
    exportInventory() { request.exportExcel('/inventory/excel', '库存明细.xlsx') },
    exportBatch() { request.exportExcel('/batch/excel', '批次列表.xlsx') },
    exportLogs() { request.exportExcel('/operationLog/excel', '操作日志.xlsx') },

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
      this.viewDialog.locationInfoMap = {}
      this.tooltipVisible = false
      try {
        const locRes = await request.get('/location/list', { zoneId: zone.zone_id })
        if (locRes.code === 200) {
          this.viewDialog.locations = locRes.data || []
        }
      } catch (e) {
        // 静默失败
      }
      try {
        const [invRes, batchRes] = await Promise.all([
          request.get('/inventory/listWithDetails'),
          request.get('/batch/list')
        ])
        const invList = (invRes.code === 200 ? invRes.data : []) || []
        const batchList = (batchRes.code === 200 ? batchRes.data : []) || []
        const batchMap = {}
        batchList.forEach(b => { batchMap[b.batch_id] = b })
        invList.forEach(inv => {
          if (inv.location_id && inv.zone_id === zone.zone_id) {
            const batch = batchMap[inv.batch_id]
            this.viewDialog.locationInfoMap[inv.location_id] = {
              batchId: inv.batch_id,
              goodsName: inv.goods_name,
              expiryDate: batch ? (batch.expiry_date || '-') : '-'
            }
          }
        })
      } catch (e) {
        // 静默失败
      }
    },
    onLocMouseEnter(event, loc) {
      if (loc.is_occupied !== 1) return
      const rect = event.currentTarget.getBoundingClientRect()
      const info = this.viewDialog.locationInfoMap[loc.location_id] || {}
      this.tooltipData = {
        batchId: info.batchId || '-',
        goodsName: info.goodsName || '-',
        expiryDate: info.expiryDate || '-'
      }
      this.tooltipStyle = {
        left: rect.left + rect.width / 2,
        top: rect.top - 8
      }
      this.tooltipVisible = true
    },
    onLocMouseLeave() {
      this.tooltipVisible = false
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
  height: 34px;
  padding: 0 10px;
  border: 1px solid var(--input-border);
  border-radius: var(--radius-md);
  font-size: 13px;
  color: var(--foreground);
  background: var(--card);
  outline: none;
}
.search-input {
  height: 34px;
  width: 180px;
  padding: 0 10px;
  border: 1px solid var(--input-border);
  border-radius: var(--radius-md);
  font-size: 13px;
  color: var(--foreground);
  outline: none;
  transition: border-color 0.2s;
}
.search-input:focus { border-color: var(--primary); }
.search-input::placeholder { color: var(--foreground-placeholder); }

/* 隐藏 date 输入框空值时的默认格式提示 */
input[type="date"]:invalid::-webkit-datetime-edit {
  color: transparent;
}
input[type="date"]:focus::-webkit-datetime-edit,
input[type="date"]:valid::-webkit-datetime-edit {
  color: var(--foreground);
}

.search-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.search-item label {
  font-size: 12px;
  color: var(--foreground-muted);
}
.search-item input,
.search-item select {
  height: 32px;
  padding: 0 8px;
  border: 1px solid var(--border-color-light);
  border-radius: 4px;
  font-size: 13px;
  color: var(--foreground);
  outline: none;
  background: var(--card);
  min-width: 120px;
}
.search-item input:focus,
.search-item select:focus { border-color: var(--primary-color); }
.search-item select:disabled {
  background: var(--page-bg);
  color: var(--foreground-placeholder);
  cursor: not-allowed;
}

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
  color: #409eff;
}
.btn-search:hover { background: #d9ecff; }
.btn-cancel {
  background: var(--bg-secondary);
  color: var(--foreground-regular);
}
.btn-cancel:hover { background: var(--bg-secondary-hover); }
.btn-export {
  background: var(--warning-bg);
  color: var(--warning);
}
.btn-export:hover { background: var(--warning-bg); }

.detail-link {
  color: var(--primary);
  cursor: pointer;
  margin-left: 6px;
  font-size: 13px;
  text-decoration: none;
}
.detail-link:hover { color: var(--primary-hover); }

.tab-row {
  display: flex;
  gap: 4px;
  background: var(--card);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: 4px;
  width: fit-content;
  flex-wrap: wrap;
  box-shadow: var(--shadow-sm);
}
.tab-row button {
  padding: 7px 20px;
  border: none;
  border-radius: var(--radius-md);
  background: transparent;
  color: var(--foreground-regular);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}
.tab-row button:hover { color: var(--foreground); background: var(--border-light); }
.tab-row button.active {
  background: var(--primary);
  color: var(--text-on-primary);
  box-shadow: var(--shadow-sm);
}

.table-wrapper {
  background: var(--card);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  overflow: hidden;
  overflow-x: auto;
  box-shadow: var(--shadow-sm);
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
  color: var(--foreground);
  border-bottom: 1px solid var(--border-color-light);
  white-space: nowrap;
}
.data-table td {
  padding: 10px 14px;
  border-bottom: 1px solid var(--border-color-light);
  color: var(--foreground-regular);
}
.data-table tbody tr:hover { background: var(--page-bg); }
.empty-cell { text-align: center; padding: 40px 0; color: var(--foreground-placeholder); }

.status-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
}
.status-tag.启用, .status-tag.status-normal { background: var(--success-bg); color: var(--success); }
.status-tag.禁用, .status-tag.status-scrap { background: var(--danger-bg); color: var(--danger); }
.status-tag.status-pending { background: var(--warning-bg); color: var(--warning); }
.status-tag.status-locked { background: var(--primary-bg); color: var(--primary-color); }

.pagination-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  color: var(--foreground-muted);
  padding: 10px 0 4px;
  flex-wrap: wrap;
  gap: 8px;
}

.total-info {
  white-space: nowrap;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 4px;
}

.pagination-controls button {
  min-width: 32px;
  height: 30px;
  padding: 0 8px;
  border: 1px solid var(--border);
  border-radius: 4px;
  background: var(--card);
  color: var(--foreground-regular);
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
  color: var(--text-on-primary);
}

.pagination-controls button:disabled {
  cursor: not-allowed;
  opacity: 0.4;
}

.page-ellipsis {
  width: 32px;
  text-align: center;
  color: var(--foreground-muted);
}

.btn-action {
  padding: 4px 10px;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-action.view { background: var(--success-bg); color: var(--success); }
.btn-action.view:hover { background: var(--success-bg); }

/* 仓库查看弹窗 */
.dialog-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: var(--overlay-bg); z-index: 9999;
  display: flex; align-items: center; justify-content: center;
}
.dialog-box {
  background: var(--card);
  border: 1px solid var(--border-color-light);
  border-radius: 4px;
  width: 460px; max-width: 92vw;
}
.dialog-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 20px; border-bottom: 1px solid var(--border-color-light);
}
.dialog-header h3 { font-size: 15px; font-weight: 600; color: var(--foreground); margin: 0; }
.dialog-close {
  background: none; border: none; color: var(--foreground-placeholder);
  font-size: 20px; cursor: pointer; padding: 0; line-height: 1;
}
.dialog-close:hover { color: var(--foreground); }

.view-dialog-box { width: 900px; }
.view-dialog-body { display: flex; height: 460px; }
.zone-sidebar {
  width: 240px; min-width: 240px;
  border-right: 1px solid var(--border-color-light);
  overflow-y: auto;
  background: var(--bg-secondary);
}
.zone-sidebar-title {
  padding: 12px 14px;
  font-size: 13px; font-weight: 600; color: var(--foreground);
  border-bottom: 1px solid var(--border-color-light);
  background: var(--card);
  position: sticky; top: 0;
}
.zone-item {
  display: flex; align-items: center; gap: 8px;
  padding: 10px 14px;
  cursor: pointer;
  border-bottom: 1px solid var(--bg-secondary);
  transition: background 0.15s;
}
.zone-item:hover { background: var(--primary-bg); }
.zone-item.active { background: var(--primary-bg-hover); }
.zone-dot {
  width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0;
}
.dot-green { background: var(--success); }
.dot-red { background: var(--danger); }
.zone-name {
  flex: 1; font-size: 13px; color: var(--foreground);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.zone-available { font-size: 11px; color: var(--foreground-muted); flex-shrink: 0; }
.zone-empty { padding: 20px; text-align: center; color: var(--foreground-placeholder); font-size: 13px; }

.location-main { flex: 1; display: flex; flex-direction: column; overflow-y: auto; }
.location-main-title {
  padding: 12px 16px;
  font-size: 13px; font-weight: 600; color: var(--foreground);
  border-bottom: 1px solid var(--border-color-light);
  background: var(--card);
  position: sticky; top: 0; z-index: 1;
}
.location-main-placeholder {
  flex: 1; display: flex; align-items: center; justify-content: center;
  color: var(--foreground-placeholder); font-size: 14px;
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
.loc-tooltip-fixed {
  position: fixed;
  transform: translate(-50%, -100%);
  background: var(--foreground);
  color: var(--text-on-primary);
  padding: 8px 10px;
  border-radius: 4px;
  font-size: 11px;
  line-height: 1.6;
  white-space: nowrap;
  z-index: 99999;
  box-shadow: 0 2px 8px rgba(0,0,0,0.2);
  pointer-events: none;
}
.loc-tooltip-fixed::after {
  content: '';
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  border: 5px solid transparent;
  border-top-color: var(--foreground);
}
.tooltip-row { white-space: nowrap; }
.tooltip-label { color: var(--foreground-placeholder); }
.loc-free { background: var(--success-bg); border: 1px solid var(--success-bg); }
.loc-occupied { background: var(--danger-bg); border: 1px solid var(--danger-bg); }
.loc-locked { background: #fef6e7; border: 1px solid var(--warning-bg); }
.loc-name { font-size: 11px; color: var(--foreground); text-align: center; word-break: break-all; }

@media (max-width: 768px) {
  .tab-row { flex-wrap: wrap; }
  .tab-row button { padding: 5px 12px; font-size: 12px; }
  .data-table { font-size: 12px; }
  .data-table th, .data-table td { padding: 8px; }
  .view-dialog-box { width: 98vw; }
  .view-dialog-body { flex-direction: column; height: auto; max-height: 70vh; }
  .zone-sidebar { width: 100%; min-width: auto; max-height: 150px; border-right: none; border-bottom: 1px solid var(--border-color-light); }
}
</style>
