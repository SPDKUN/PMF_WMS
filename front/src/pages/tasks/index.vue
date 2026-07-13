<template>
  <div class="tasks-page">
    <!-- 选项卡 -->
    <div class="tab-row">
      <button :class="{ active: activeTab === 'todo' }" @click="activeTab = 'todo'">我的待办</button>
      <button :class="{ active: activeTab === 'assign' }" @click="activeTab = 'assign'">布置任务</button>
    </div>

    <!-- 我的待办 -->
    <div v-if="activeTab === 'todo'" class="tab-content">
      <div class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>单号</th>
              <th>批次号</th>
              <th>类型</th>
              <th>优先级</th>
              <th>截至日期</th>
              <th width="120">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="taskList.length === 0">
              <td colspan="6" class="empty-cell">所有待办已完成！</td>
            </tr>
            <tr v-for="item in taskList" :key="item.task_id">
              <td>{{ item.related_order_no || '-' }}</td>
              <td>{{ item.related_batch_id || '-' }}</td>
              <td>{{ item.task_type }}</td>
              <td><span class="priority-tag" :class="priorityClass(item.priority)">{{ item.priority }}</span></td>
              <td>{{ formatDeadline(item.deadline) }}</td>
              <td class="action-cell">
                <button class="btn-action done" @click="openCompleteDialog(item)">去完成</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 布置任务 -->
    <div v-if="activeTab === 'assign'" class="assign-content">
      <div class="assign-header">
        <h3>仓库操作中心</h3>
        <p>选择一个操作类型开始处理仓库任务</p>
      </div>
      <div class="op-grid">
        <div
          v-for="op in operations"
          :key="op.key"
          class="op-card"
          :style="{ '--card-color': op.color }"
          @click="handleOp(op.key)"
        >
          <div class="op-card-icon">
            <span v-html="op.icon"></span>
          </div>
          <div class="op-card-title">{{ op.label }}</div>
          <div class="op-card-desc">{{ op.desc }}</div>
        </div>
      </div>
    </div>

    <!-- 新增批次弹窗 -->
    <div class="dialog-overlay" v-if="newBatchDialog.visible" @click.self="newBatchDialog.visible = false">
      <div class="dialog-box">
        <div class="dialog-header">
          <h3>新增批次</h3>
          <button class="dialog-close" @click="newBatchDialog.visible = false">&times;</button>
        </div>
        <div class="dialog-body">
          <div class="form-item">
            <label>选择货物 <span class="required">*</span></label>
            <select v-model="newBatchDialog.form.goods_id">
              <option :value="null">请选择货物</option>
              <option v-for="g in goodsList" :key="g.goods_id" :value="g.goods_id">
                {{ g.goods_name }} ({{ g.goods_code }})
              </option>
            </select>
          </div>
          <div class="form-item">
            <label>生产日期 <span class="required">*</span></label>
            <input type="date" v-model="newBatchDialog.form.production_date" />
          </div>
          <div class="form-item">
            <label>保质期至 <span class="required">*</span></label>
            <input type="date" v-model="newBatchDialog.form.expiry_date" />
          </div>
          <div class="form-item">
            <label>入库初始数量 <span class="required">*</span></label>
            <input type="number" v-model.number="newBatchDialog.form.initial_quantity" min="1" placeholder="请输入入库初始数量" />
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-cancel" @click="newBatchDialog.visible = false">取消</button>
          <button class="btn btn-primary" @click="submitNewBatch">确认创建</button>
        </div>
      </div>
    </div>

    <!-- 质检弹窗 -->
    <div class="dialog-overlay" v-if="qcDialog.visible" @click.self="qcDialog.visible = false">
      <div class="dialog-box">
        <div class="dialog-header">
          <h3>新建质检任务</h3>
          <button class="dialog-close" @click="qcDialog.visible = false">&times;</button>
        </div>
        <div class="dialog-body">
          <div class="form-item">
            <label>质检类型 <span class="required">*</span></label>
            <select v-model="qcDialog.form.checkType" @change="onQcTypeChange">
              <option value="">请选择质检类型</option>
              <option value="来料检">来料检</option>
              <option value="成品检">成品检</option>
              <option value="日常抽检">日常抽检</option>
            </select>
          </div>
          <div class="form-item">
            <label>批次 <span class="required">*</span></label>
            <select v-model="qcDialog.form.batchId" :disabled="!qcDialog.form.checkType">
              <option value="">请选择批次</option>
              <option v-for="b in filteredBatches" :key="b.batch_id" :value="b.batch_id">
                {{ b.batch_id }} ({{ getGoodsName(b.goods_id) }})
              </option>
            </select>
          </div>
          <div class="form-item">
            <label>质检员 <span class="required">*</span></label>
            <select v-model="qcDialog.form.inspectorId">
              <option :value="null">请选择质检员</option>
              <option v-for="u in inspectorList" :key="u.user_id" :value="u.user_id">
                {{ u.real_name || u.username }}
              </option>
            </select>
          </div>
          <div class="form-item">
            <label>优先级 <span class="required">*</span></label>
            <select v-model="qcDialog.form.priority">
              <option value="">请选择优先级</option>
              <option value="高">高</option>
              <option value="中">中</option>
              <option value="低">低</option>
            </select>
          </div>
          <div class="form-item">
            <label>截至时间 <span class="required">*</span></label>
            <input type="date" v-model="qcDialog.form.deadline" />
          </div>
          <div class="form-item">
            <label>备注</label>
            <textarea v-model="qcDialog.form.remark" placeholder="可选备注" rows="3"></textarea>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-cancel" @click="qcDialog.visible = false">取消</button>
          <button class="btn btn-primary" @click="submitQc">确认创建</button>
        </div>
      </div>
    </div>

    <!-- 入库弹窗 -->
    <div class="dialog-overlay" v-if="inboundDialog.visible" @click.self="inboundDialog.visible = false">
      <div class="dialog-box">
        <div class="dialog-header">
          <h3>新建入库任务</h3>
          <button class="dialog-close" @click="inboundDialog.visible = false">&times;</button>
        </div>
        <div class="dialog-body">
          <div class="form-item">
            <label>入库类型 <span class="required">*</span></label>
            <select v-model="inboundDialog.form.inboundType">
              <option value="">请选择入库类型</option>
              <option value="采购入库">采购入库</option>
              <option value="退货入库">退货入库</option>
              <option value="生产入库">生产入库</option>
            </select>
          </div>
          <div class="form-item">
            <label>批次 <span class="required">*</span></label>
            <select v-model="inboundDialog.form.batchId">
              <option value="">请选择批次</option>
              <option v-for="b in availableBatches" :key="b.batch_id" :value="b.batch_id">
                {{ b.batch_id }} ({{ getGoodsName(b.goods_id) }})
              </option>
            </select>
          </div>
          <div class="form-item">
            <label>操作员 <span class="required">*</span></label>
            <select v-model="inboundDialog.form.operatorId">
              <option :value="null">请选择操作员</option>
              <option v-for="u in warehouseStaff" :key="u.user_id" :value="u.user_id">
                {{ u.real_name || u.username }}
              </option>
            </select>
          </div>
          <div class="form-item">
            <label>优先级 <span class="required">*</span></label>
            <select v-model="inboundDialog.form.priority">
              <option value="">请选择优先级</option>
              <option value="高">高</option>
              <option value="中">中</option>
              <option value="低">低</option>
            </select>
          </div>
          <div class="form-item">
            <label>截至时间 <span class="required">*</span></label>
            <input type="date" v-model="inboundDialog.form.deadline" />
          </div>
          <div class="form-item">
            <label>备注</label>
            <textarea v-model="inboundDialog.form.remark" placeholder="可选备注" rows="3"></textarea>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-cancel" @click="inboundDialog.visible = false">取消</button>
          <button class="btn btn-primary" @click="submitInboundConfirm">确认创建</button>
        </div>
      </div>
    </div>

    <!-- 完成任务弹窗 -->
    <div class="dialog-overlay" v-if="completeDialog.visible" @click.self="completeDialog.visible = false">
      <div class="dialog-box">
        <div class="dialog-header">
          <h3>{{ completeDialog.isQc ? '完成质检' : '完成任务' }}</h3>
          <button class="dialog-close" @click="completeDialog.visible = false">&times;</button>
        </div>
        <div class="dialog-body" v-if="completeDialog.isQc">
          <div class="form-item">
            <label>质检结果 <span class="required">*</span></label>
            <select v-model="completeDialog.form.inspectionResult">
              <option value="">请选择质检结果</option>
              <option value="合格">合格</option>
              <option value="不合格">不合格</option>
            </select>
          </div>
          <div class="form-item">
            <label>不合格原因描述</label>
            <textarea v-model="completeDialog.form.defectReason" placeholder="如不合格请填写原因" rows="2"></textarea>
          </div>
          <div class="form-item">
            <label>处理建议</label>
            <input type="text" v-model="completeDialog.form.handlingSuggestion" placeholder="如报废、退货、降级" />
          </div>
          <div class="form-item">
            <label>备注</label>
            <textarea v-model="completeDialog.form.remark" placeholder="备注" rows="2"></textarea>
          </div>
        </div>
        <div class="dialog-body" v-else>
          <p style="text-align:center;color:#909399;padding:20px;">该类型任务完成功能开发中...</p>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-cancel" @click="completeDialog.visible = false">取消</button>
          <button class="btn btn-primary" v-if="completeDialog.isQc" @click="confirmComplete">提交</button>
        </div>
      </div>
    </div>

    <!-- 二次确认弹窗 -->
    <div class="dialog-overlay confirm-dialog-overlay" v-if="confirmDialog.visible" @click.self="confirmDialog.visible = false">
      <div class="dialog-box" style="width:360px;">
        <div class="dialog-header">
          <h3>确认提交</h3>
          <button class="dialog-close" @click="confirmDialog.visible = false">&times;</button>
        </div>
        <div class="dialog-body">
          <p style="text-align:center;font-size:14px;color:#303133;">{{ confirmDialog.message }}</p>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-cancel" @click="confirmDialog.visible = false">取消</button>
          <button class="btn btn-primary" @click="confirmDialog.callback">{{ confirmDialog.confirmText }}</button>
        </div>
      </div>
    </div>

    <!-- 入库完成弹窗（仓库可视化选库位） -->
    <div class="dialog-overlay" v-if="inboundCompleteDialog.visible" @click.self="inboundCompleteDialog.visible = false">
      <div class="dialog-box view-dialog-box">
        <div class="dialog-header">
          <h3>完成入库 - {{ inboundCompleteDialog.task ? inboundCompleteDialog.task.related_order_no : '' }}</h3>
          <button class="dialog-close" @click="inboundCompleteDialog.visible = false">&times;</button>
        </div>
        <div class="dialog-body" style="padding: 12px 20px; flex-direction: column;">
          <!-- 仓库选择 -->
          <div class="form-item">
            <label>选择仓库 <span class="required">*</span></label>
            <select v-model="inboundCompleteDialog.warehouseId" @change="onInboundWarehouseChange">
              <option :value="null">请选择仓库</option>
              <option v-for="w in inboundCompleteDialog.warehouses" :key="w.warehouse_id" :value="w.warehouse_id">
                {{ w.warehouse_name }}
              </option>
            </select>
          </div>
          <!-- 进度 -->
          <div class="progress-bar-wrap" v-if="inboundCompleteDialog.totalNeeded > 0">
            <div class="progress-text">
              已选库位：<strong>{{ inboundCompleteDialog.selectedLocationIds.length }}</strong> / {{ inboundCompleteDialog.totalNeeded }}
              <span v-if="inboundCompleteDialog.selectedLocationIds.length === inboundCompleteDialog.totalNeeded" style="color:#67c23a;"> ✓ 已满足</span>
              <span v-else-if="inboundCompleteDialog.selectedLocationIds.length > inboundCompleteDialog.totalNeeded" style="color:#e6a23c;">（多了 {{ inboundCompleteDialog.selectedLocationIds.length - inboundCompleteDialog.totalNeeded }} 个）</span>
              <span v-else style="color:#f56c6c;">（还需 {{ inboundCompleteDialog.totalNeeded - inboundCompleteDialog.selectedLocationIds.length }} 个）</span>
            </div>
          </div>
        </div>
        <div class="view-dialog-body" v-if="inboundCompleteDialog.warehouseId">
          <!-- 库区侧栏 -->
          <div class="zone-sidebar">
            <div class="zone-sidebar-title">库区列表</div>
            <div v-for="zone in inboundCompleteDialog.zones" :key="zone.zone_id"
                 class="zone-item" :class="{ active: inboundCompleteDialog.selectedZoneId === zone.zone_id }"
                 @click="selectInboundZone(zone)">
              <span class="zone-dot" :class="zone.available_slots > 0 ? 'dot-green' : 'dot-red'"></span>
              <span class="zone-name">{{ zone.zone_name }}</span>
              <span class="zone-available">可用 {{ zone.available_slots }}/{{ zone.total_slots }}</span>
            </div>
            <div v-if="inboundCompleteDialog.zones.length === 0" style="padding:16px;color:#909399;font-size:12px;">
              该仓库暂无库区
            </div>
          </div>
          <!-- 库位网格 -->
          <div class="location-main">
            <div class="location-main-title" v-if="inboundCompleteDialog.selectedZoneId">
              {{ inboundCompleteDialog.selectedZoneName }} - 选择库位
            </div>
            <div class="location-grid" v-if="inboundCompleteDialog.selectedZoneId">
              <div v-for="loc in inboundCompleteDialog.locations" :key="loc.location_id"
                   class="location-card"
                   :class="loc.is_occupied === 1 ? 'loc-occupied' : (loc.lock_status === '锁定' ? 'loc-locked' : (isLocationSelected(loc.location_id) ? 'loc-selected' : 'loc-free'))"
                   @click="toggleLocation(loc)">
                <span class="loc-checkbox" v-if="loc.is_occupied !== 1">
                  <input type="checkbox" :checked="isLocationSelected(loc.location_id)" />
                </span>
                <span class="loc-name">{{ loc.location_name }}</span>
              </div>
              <div v-if="inboundCompleteDialog.locations.length === 0" style="width:100%;text-align:center;padding:30px;color:#909399;">
                该库区暂无库位
              </div>
            </div>
            <div class="location-main-placeholder" v-else>
              请选择左侧库区查看库位
            </div>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-danger" @click="rejectInbound">退回入库请求</button>
          <button class="btn btn-primary"
                  :disabled="inboundCompleteDialog.selectedLocationIds.length !== inboundCompleteDialog.totalNeeded"
                  @click="confirmCompleteInbound">
            确认入库
          </button>
        </div>
      </div>
    </div>

    <!-- 库存调整弹窗 -->
    <div class="dialog-overlay" v-if="adjustmentDialog.visible" @click.self="adjustmentDialog.visible = false">
      <div class="dialog-box" style="width:1100px;max-width:98vw;max-height:90vh;overflow-y:auto;">
        <div class="dialog-header">
          <h3>新建库存调整任务 — 库位移库</h3>
          <button class="dialog-close" @click="adjustmentDialog.visible = false">&times;</button>
        </div>
        <div class="dialog-body" style="flex-direction:column;padding:8px 16px;gap:8px;">
          <!-- 上半区域：左右两个仓库可视化面板 -->
          <div class="adjust-panels">
            <!-- 左面板：源库位 -->
            <div class="adjust-panel">
              <div class="adjust-panel-title">源库位（选择要转移的货物）</div>
              <div class="form-item" style="margin-bottom:4px;">
                <select v-model="adjustmentDialog.left.warehouseId" @change="onAdjustLeftWarehouseChange">
                  <option :value="null">请选择仓库</option>
                  <option v-for="w in adjustmentDialog.left.warehouses" :key="w.warehouse_id" :value="w.warehouse_id">{{ w.warehouse_name }}</option>
                </select>
              </div>
              <div class="progress-bar-wrap" v-if="adjustmentDialog.left.warehouseId">
                <div class="progress-text">已选：<strong>{{ adjustmentDialog.left.selectedIds.length }}</strong> 个库位</div>
              </div>
              <div class="adjust-panel-body" v-if="adjustmentDialog.left.warehouseId">
                <div class="zone-sidebar adjust-zone-sidebar">
                  <div v-for="zone in adjustmentDialog.left.zones" :key="zone.zone_id"
                       class="zone-item" :class="{ active: adjustmentDialog.left.selectedZoneId === zone.zone_id }"
                       @click="selectAdjustLeftZone(zone)">
                    <span class="zone-dot" :class="zone.available_slots > 0 ? 'dot-green' : 'dot-red'"></span>
                    <span class="zone-name">{{ zone.zone_name }}</span>
                  </div>
                </div>
                <div class="location-main">
                  <div class="location-grid" v-if="adjustmentDialog.left.selectedZoneId">
                    <div v-for="loc in adjustmentDialog.left.locations" :key="loc.location_id"
                         class="location-card adjust-location-card"
                         :class="loc.lock_status === '锁定' ? 'loc-locked' : (loc.is_occupied === 1 ? (isAdjustLeftSelected(loc.location_id) ? 'loc-selected' : 'loc-occupied') : 'loc-free')"
                         @click="toggleAdjustLeftLocation(loc)">
                      <span class="loc-seq-badge" v-if="isAdjustLeftSelected(loc.location_id)">{{ getAdjustLeftSeq(loc.location_id) }}</span>
                      <span class="loc-name">{{ loc.location_name }}</span>
                    </div>
                  </div>
                  <div class="location-main-placeholder" v-else>请选择左侧库区</div>
                </div>
              </div>
            </div>
            <!-- 右面板：目标库位 -->
            <div class="adjust-panel">
              <div class="adjust-panel-title">目标库位（选择新存放位置）</div>
              <div class="form-item" style="margin-bottom:4px;">
                <select v-model="adjustmentDialog.right.warehouseId" @change="onAdjustRightWarehouseChange">
                  <option :value="null">请选择仓库</option>
                  <option v-for="w in adjustmentDialog.right.warehouses" :key="w.warehouse_id" :value="w.warehouse_id">{{ w.warehouse_name }}</option>
                </select>
              </div>
              <div class="progress-bar-wrap" v-if="adjustmentDialog.right.warehouseId">
                <div class="progress-text">已选：<strong>{{ adjustmentDialog.right.selectedIds.length }}</strong> 个库位</div>
              </div>
              <div class="adjust-panel-body" v-if="adjustmentDialog.right.warehouseId">
                <div class="zone-sidebar adjust-zone-sidebar">
                  <div v-for="zone in adjustmentDialog.right.zones" :key="zone.zone_id"
                       class="zone-item" :class="{ active: adjustmentDialog.right.selectedZoneId === zone.zone_id }"
                       @click="selectAdjustRightZone(zone)">
                    <span class="zone-dot" :class="zone.available_slots > 0 ? 'dot-green' : 'dot-red'"></span>
                    <span class="zone-name">{{ zone.zone_name }}</span>
                  </div>
                </div>
                <div class="location-main">
                  <div class="location-grid" v-if="adjustmentDialog.right.selectedZoneId">
                    <div v-for="loc in adjustmentDialog.right.locations" :key="loc.location_id"
                         class="location-card adjust-location-card"
                         :class="loc.is_occupied === 1 || loc.lock_status === '锁定' ? (loc.lock_status === '锁定' ? 'loc-locked' : 'loc-occupied') : (isAdjustRightSelected(loc.location_id) ? 'loc-selected' : 'loc-free')"
                         @click="toggleAdjustRightLocation(loc)">
                      <span class="loc-seq-badge" v-if="isAdjustRightSelected(loc.location_id)">{{ getAdjustRightSeq(loc.location_id) }}</span>
                      <span class="loc-name">{{ loc.location_name }}</span>
                    </div>
                  </div>
                  <div class="location-main-placeholder" v-else>请选择右侧库区</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 下半区域：选中库位明细表 -->
          <div class="adjust-table-wrap" v-if="adjustmentDialog.matchedItems.length > 0">
            <table class="data-table">
              <thead>
                <tr>
                  <th>编号</th>
                  <th>批次号</th>
                  <th>货物名称</th>
                  <th>数量</th>
                  <th>原位置</th>
                  <th>新位置</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(item, idx) in adjustmentDialog.matchedItems" :key="idx">
                  <td>{{ idx + 1 }}</td>
                  <td>{{ item.batchId || '-' }}</td>
                  <td>{{ item.goodsName || '-' }}</td>
                  <td>{{ item.quantity || '-' }}</td>
                  <td>{{ item.fromLocationName || '-' }}</td>
                  <td>{{ item.toLocationName || '-' }}</td>
                </tr>
              </tbody>
            </table>
          </div>
          <div v-else style="text-align:center;color:#c0c4cc;font-size:13px;padding:8px;">
            请在左右两侧选择库位，系统将按顺序自动匹配
          </div>

          <!-- 表单 -->
          <div style="display:flex;gap:16px;flex-wrap:wrap;">
            <div class="form-item" style="flex:1;min-width:150px;">
              <label>负责人 <span class="required">*</span></label>
              <select v-model="adjustmentDialog.assigneeId">
                <option :value="null">请选择负责人</option>
                <option v-for="u in warehouseStaff" :key="u.user_id" :value="u.user_id">{{ u.real_name || u.username }}</option>
              </select>
            </div>
            <div class="form-item" style="flex:1;min-width:120px;">
              <label>优先级 <span class="required">*</span></label>
              <select v-model="adjustmentDialog.priority">
                <option value="">请选择</option>
                <option value="高">高</option>
                <option value="中">中</option>
                <option value="低">低</option>
              </select>
            </div>
            <div class="form-item" style="flex:1;min-width:150px;">
              <label>截至日期 <span class="required">*</span></label>
              <input type="date" v-model="adjustmentDialog.deadline" />
            </div>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-cancel" @click="adjustmentDialog.visible = false">取消</button>
          <button class="btn btn-primary"
                  :disabled="!canSubmitAdjustment"
                  @click="confirmCreateAdjustment">
            确认创建
          </button>
        </div>
      </div>
    </div>

    <!-- 库存调整完成弹窗 -->
    <div class="dialog-overlay" v-if="adjustmentCompleteDialog.visible" @click.self="adjustmentCompleteDialog.visible = false">
      <div class="dialog-box" style="width:700px;">
        <div class="dialog-header">
          <h3>完成库存调整</h3>
          <button class="dialog-close" @click="adjustmentCompleteDialog.visible = false">&times;</button>
        </div>
        <div class="dialog-body" style="flex-direction:column;">
          <p style="font-size:13px;color:#606266;">截至日期：<strong>{{ formatDeadline(adjustmentCompleteDialog.task.deadline) }}</strong></p>
          <table class="data-table" style="margin-top:8px;">
            <thead>
              <tr><th>批次</th><th>货物名称</th><th>数量</th><th>原库位</th><th>新库位</th></tr>
            </thead>
            <tbody>
              <tr v-if="adjustmentCompleteDialog.details.length === 0">
                <td colspan="5" class="empty-cell">加载中...</td>
              </tr>
              <tr v-for="(d, idx) in adjustmentCompleteDialog.details" :key="idx">
                <td>{{ d.batchId }}</td>
                <td>{{ d.goodsName }}</td>
                <td>{{ d.quantity }}</td>
                <td>{{ d.fromLocationName }}</td>
                <td>{{ d.toLocationName }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-danger" @click="rejectAdjustment">退回调整申请</button>
          <button class="btn btn-primary" @click="confirmCompleteAdjustment">完成调整</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import request from '@/utils/request.js'

export default {
  name: 'TasksPage',
  data() {
    return {
      activeTab: 'todo',
      operations: [
        { key: 'newBatch',  label: '新增批次', color: '#409EFF', icon: '&#128230;', desc: '创建新的货物批次并关联货物信息' },
        { key: 'inbound',   label: '入库',     color: '#67c23a', icon: '&#128229;', desc: '将货物录入仓库并分配库位' },
        { key: 'outbound',  label: '出库',     color: '#e6a23c', icon: '&#128228;', desc: '按出库单拣货并完成出库' },
        { key: 'adjust',    label: '库存调整', color: '#9065db', icon: '&#9881;',  desc: '修正库存数量差异或库位调整' },
        { key: 'check',     label: '库存盘点', color: '#20a0ff', icon: '&#128203;', desc: '对库位货物进行盘点核对' },
        { key: 'quality',   label: '质检',     color: '#f56c6c', icon: '&#128270;', desc: '对入库货物进行质量检验' },
        { key: 'defective', label: '处理次品', color: '#f39c12', icon: '&#128465;', desc: '登记并处理不合格或报废货物' },
      ],
      taskList: [],
      userId: null,
      goodsList: [],
      batchList: [],
      inspectorList: [],
      allBatches: [],
      newBatchDialog: {
        visible: false,
        form: { goods_id: null, production_date: '', expiry_date: '', initial_quantity: 0 }
      },
      qcDialog: {
        visible: false,
        form: { checkType: '', batchId: '', inspectorId: null, priority: '', deadline: '', remark: '' }
      },
      completeDialog: {
        visible: false,
        isQc: false,
        task: null,
        form: { inspectionResult: '', defectReason: '', handlingSuggestion: '', remark: '' }
      },
      confirmDialog: {
        visible: false,
        message: '确定要提交质检结果吗？提交后将无法撤回。',
        confirmText: '确认提交',
        callback: null
      },
      inboundDialog: {
        visible: false,
        form: { inboundType: '', batchId: '', operatorId: null, priority: '', deadline: '', remark: '' }
      },
      availableBatches: [],
      warehouseStaff: [],
      inboundCompleteDialog: {
        visible: false,
        task: null,
        totalNeeded: 0,
        warehouseId: null,
        warehouses: [],
        zones: [],
        selectedZoneId: null,
        selectedZoneName: '',
        locations: [],
        selectedLocationIds: [],
        remark: ''
      },
      adjustmentDialog: {
        visible: false,
        left: { warehouseId: null, warehouses: [], zones: [], selectedZoneId: null, locations: [], selectedIds: [], selectedDetails: {} },
        right: { warehouseId: null, warehouses: [], zones: [], selectedZoneId: null, locations: [], selectedIds: [], selectedDetails: {} },
        matchedItems: [],
        assigneeId: null,
        priority: '',
        deadline: ''
      },
      adjustmentCompleteDialog: {
        visible: false,
        task: null,
        details: []
      }
    }
  },
  computed: {
    filteredBatches() {
      if (!this.qcDialog.form.checkType) return []
      const statusMap = { '来料检': '待检', '成品检': '锁定', '日常抽检': '正常' }
      const status = statusMap[this.qcDialog.form.checkType]
      return this.allBatches.filter(b => b.batch_status === status)
    },
    canSubmitAdjustment() {
      const left = this.adjustmentDialog.left.selectedIds.length
      const right = this.adjustmentDialog.right.selectedIds.length
      return left > 0 && left === right
        && this.adjustmentDialog.assigneeId
        && this.adjustmentDialog.priority
        && this.adjustmentDialog.deadline
    }
  },
  mounted() {
    const stored = localStorage.getItem('userInfo')
    if (stored) {
      try {
        const data = JSON.parse(stored)
        this.userId = data.user_id
      } catch (e) { /* ignore */ }
    }
    this.fetchMyTasks()
    this.fetchGoods()
    this.fetchAllBatches()
  },
  methods: {
    getUserId() {
      if (this.userId) return this.userId
      const stored = localStorage.getItem('userInfo')
      if (stored) {
        try { this.userId = JSON.parse(stored).user_id } catch (e) {}
      }
      return this.userId
    },
    priorityClass(p) {
      if (p === '高') return '高'
      if (p === '中') return '中'
      return '低'
    },
    formatDeadline(d) {
      if (!d) return '-'
      return d.substring(0, 10)
    },
    handleOp(key) {
      if (key === 'newBatch') {
        this.openNewBatchDialog()
      } else if (key === 'quality') {
        this.openQcDialog()
      } else if (key === 'inbound') {
        this.openInboundDialog()
      } else if (key === 'adjust') {
        this.openAdjustmentDialog()
      } else {
        const labels = { outbound: '出库', check: '库存盘点', defective: '处理次品' }
        alert(labels[key] + '功能开发中...')
      }
    },
    getGoodsName(goodsId) {
      const g = this.goodsList.find(item => item.goods_id === goodsId)
      return g ? g.goods_name : '-'
    },

    // --- 我的待办 ---
    async fetchMyTasks() {
      const uid = this.getUserId()
      if (!uid) return
      try {
        const res = await request.get('/qualityCheck/myTasks', { assigneeId: uid })
        if (res.code === 200) {
          this.taskList = res.data || []
        }
      } catch (e) { /* ignore */ }
    },

    // --- 新增批次 ---
    async fetchGoods() {
      try {
        const res = await request.get('/goods/list')
        if (res.code === 200) { this.goodsList = res.data || [] }
      } catch (e) { /* ignore */ }
    },
    async fetchAllBatches() {
      try {
        const res = await request.get('/batch/list')
        if (res.code === 200) { this.allBatches = res.data || [] }
      } catch (e) { /* ignore */ }
    },
    openNewBatchDialog() {
      const today = new Date().toISOString().slice(0, 10)
      this.newBatchDialog.form = { goods_id: null, production_date: today, expiry_date: '', initial_quantity: 0 }
      this.newBatchDialog.visible = true
    },
    async submitNewBatch() {
      const f = this.newBatchDialog.form
      if (!f.goods_id) { alert('请选择货物'); return }
      if (!f.production_date) { alert('请选择生产日期'); return }
      if (!f.expiry_date) { alert('请选择保质期至'); return }
      if (!f.initial_quantity || f.initial_quantity <= 0) { alert('请输入有效的入库初始数量'); return }
      try {
        const res = await request.post('/batch', {
          goods_id: parseInt(f.goods_id),
          production_date: f.production_date,
          expiry_date: f.expiry_date,
          initial_quantity: f.initial_quantity
        })
        if (res.code === 200) {
          alert('批次创建成功')
          this.newBatchDialog.visible = false
          this.fetchAllBatches()
        } else {
          alert(res.msg || '创建失败')
        }
      } catch (e) {
        alert('创建失败')
      }
    },

    // --- 质检 ---
    async fetchInspectors() {
      try {
        const res = await request.get('/user/inspectors')
        if (res.code === 200) { this.inspectorList = res.data || [] }
      } catch (e) { /* ignore */ }
    },
    openQcDialog() {
      this.qcDialog.form = { checkType: '', batchId: '', inspectorId: null, priority: '', deadline: '', remark: '' }
      this.fetchInspectors()
      this.fetchAllBatches()
      this.qcDialog.visible = true
    },
    onQcTypeChange() {
      this.qcDialog.form.batchId = ''
    },
    async submitQc() {
      const f = this.qcDialog.form
      if (!f.checkType) { alert('请选择质检类型'); return }
      if (!f.batchId) { alert('请选择批次'); return }
      if (!f.inspectorId) { alert('请选择质检员'); return }
      if (!f.priority) { alert('请选择优先级'); return }
      if (!f.deadline) { alert('请选择截至时间'); return }

      const uid = this.getUserId()
      try {
        const res = await request.post('/qualityCheck/create', {
          checkType: f.checkType,
          batchId: f.batchId,
          inspectorId: f.inspectorId,
          priority: f.priority,
          deadline: f.deadline,
          remark: f.remark,
          operatorId: uid
        })
        if (res.code === 200) {
          alert('质检任务创建成功')
          this.qcDialog.visible = false
          this.fetchMyTasks()
        } else {
          alert(res.msg || '创建失败')
        }
      } catch (e) {
        alert('创建失败')
      }
    },

    // --- 完成任务 ---
    openCompleteDialog(task) {
      if (task.task_type === '质检') {
        this.completeDialog.task = task
        this.completeDialog.isQc = true
        this.completeDialog.form = {
          inspectionResult: '',
          defectReason: '',
          handlingSuggestion: '',
          remark: task.remark || ''
        }
        this.completeDialog.visible = true
      } else if (task.task_type === '入库') {
        this.openInboundCompleteDialog(task)
      } else if (task.task_type === '库存调整') {
        this.openAdjustmentCompleteDialog(task)
      } else {
        alert('该类型任务完成功能开发中...')
      }
    },
    confirmComplete() {
      const f = this.completeDialog.form
      if (!f.inspectionResult) { alert('请选择质检结果'); return }
      this.confirmDialog.message = '确定要提交质检结果吗？提交后将无法撤回。'
      this.confirmDialog.confirmText = '确认提交'
      this.confirmDialog.callback = this.submitComplete
      this.confirmDialog.visible = true
    },
    async submitComplete() {
      this.confirmDialog.visible = false
      const uid = this.getUserId()
      const f = this.completeDialog.form
      try {
        const res = await request.put('/qualityCheck/complete/' + this.completeDialog.task.task_id, {
          inspectionResult: f.inspectionResult,
          defectReason: f.defectReason,
          handlingSuggestion: f.handlingSuggestion,
          remark: f.remark,
          operatorId: uid
        })
        if (res.code === 200) {
          alert('质检任务已完成')
          this.completeDialog.visible = false
          this.fetchMyTasks()
        } else {
          alert(res.msg || '操作失败')
        }
      } catch (e) {
        alert('操作失败')
      }
    },

    // --- 入库 ---
    async fetchAvailableBatches() {
      try {
        const res = await request.get('/inbound/availableBatches')
        if (res.code === 200) { this.availableBatches = res.data || [] }
      } catch (e) { /* ignore */ }
    },
    async fetchWarehouseStaff() {
      try {
        const res = await request.get('/user/warehouseStaff')
        if (res.code === 200) { this.warehouseStaff = res.data || [] }
      } catch (e) { /* ignore */ }
    },
    openInboundDialog() {
      this.inboundDialog.form = { inboundType: '', batchId: '', operatorId: null, priority: '', deadline: '', remark: '' }
      this.fetchAvailableBatches()
      this.fetchWarehouseStaff()
      this.fetchGoods()
      this.inboundDialog.visible = true
    },
    submitInboundConfirm() {
      const f = this.inboundDialog.form
      if (!f.inboundType) { alert('请选择入库类型'); return }
      if (!f.batchId) { alert('请选择批次'); return }
      if (!f.operatorId) { alert('请选择操作员'); return }
      if (!f.priority) { alert('请选择优先级'); return }
      if (!f.deadline) { alert('请选择截至时间'); return }
      this.confirmDialog.message = '确定要创建入库任务吗？'
      this.confirmDialog.confirmText = '确认创建'
      this.confirmDialog.callback = this.submitInbound
      this.confirmDialog.visible = true
    },
    async submitInbound() {
      this.confirmDialog.visible = false
      const f = this.inboundDialog.form
      try {
        const res = await request.post('/inbound/create', {
          inboundType: f.inboundType,
          batchId: f.batchId,
          operatorId: f.operatorId,
          priority: f.priority,
          deadline: f.deadline,
          remark: f.remark
        })
        if (res.code === 200) {
          alert('入库任务创建成功')
          this.inboundDialog.visible = false
          this.fetchMyTasks()
        } else {
          alert(res.msg || '创建失败')
        }
      } catch (e) {
        alert('创建失败')
      }
    },

    // --- 入库完成（仓库可视化） ---
    async openInboundCompleteDialog(task) {
      this.inboundCompleteDialog.task = task
      this.inboundCompleteDialog.totalNeeded = 0
      this.inboundCompleteDialog.warehouseId = null
      this.inboundCompleteDialog.warehouses = []
      this.inboundCompleteDialog.zones = []
      this.inboundCompleteDialog.selectedZoneId = null
      this.inboundCompleteDialog.selectedZoneName = ''
      this.inboundCompleteDialog.locations = []
      this.inboundCompleteDialog.selectedLocationIds = []
      this.inboundCompleteDialog.remark = task.remark || ''

      // 从task获取batch信息计算totalNeeded
      try {
        const res = await request.get('/batch/list')
        if (res.code === 200) {
          const batches = res.data || []
          const batch = batches.find(b => b.batch_id === task.related_batch_id)
          if (batch) {
            this.inboundCompleteDialog.totalNeeded = Math.ceil(batch.initial_quantity / 10)
          }
        }
      } catch (e) { /* ignore */ }

      // 获取仓库列表
      try {
        const res = await request.get('/warehouse/list')
        if (res.code === 200) {
          this.inboundCompleteDialog.warehouses = res.data || []
        }
      } catch (e) { /* ignore */ }

      this.inboundCompleteDialog.visible = true
    },
    async onInboundWarehouseChange() {
      this.inboundCompleteDialog.zones = []
      this.inboundCompleteDialog.selectedZoneId = null
      this.inboundCompleteDialog.locations = []
      if (!this.inboundCompleteDialog.warehouseId) return
      try {
        const res = await request.get('/zone/list', { warehouseId: this.inboundCompleteDialog.warehouseId })
        if (res.code === 200) {
          this.inboundCompleteDialog.zones = res.data || []
        }
      } catch (e) { /* ignore */ }
    },
    async selectInboundZone(zone) {
      this.inboundCompleteDialog.selectedZoneId = zone.zone_id
      this.inboundCompleteDialog.selectedZoneName = zone.zone_name
      this.inboundCompleteDialog.locations = []
      try {
        const res = await request.get('/location/list', { zoneId: zone.zone_id })
        if (res.code === 200) {
          this.inboundCompleteDialog.locations = res.data || []
        }
      } catch (e) { /* ignore */ }
    },
    isLocationSelected(locationId) {
      return this.inboundCompleteDialog.selectedLocationIds.includes(locationId)
    },
    toggleLocation(loc) {
      if (loc.is_occupied === 1) return
      if (loc.lock_status === '锁定') return
      const idx = this.inboundCompleteDialog.selectedLocationIds.indexOf(loc.location_id)
      if (idx >= 0) {
        this.inboundCompleteDialog.selectedLocationIds.splice(idx, 1)
      } else {
        this.inboundCompleteDialog.selectedLocationIds.push(loc.location_id)
      }
    },
    rejectInbound() {
      if (!confirm('确定要退回该入库请求吗？退回后将删除相关入库单。')) return
      const uid = this.getUserId()
      request.put('/inbound/reject/' + this.inboundCompleteDialog.task.task_id, { operatorId: uid })
        .then(res => {
          if (res.code === 200) {
            alert('已退回入库请求')
            this.inboundCompleteDialog.visible = false
            this.fetchMyTasks()
          } else {
            alert(res.msg || '操作失败')
          }
        })
        .catch(() => alert('操作失败'))
    },
    confirmCompleteInbound() {
      if (this.inboundCompleteDialog.selectedLocationIds.length !== this.inboundCompleteDialog.totalNeeded) {
        alert('请选择恰好 ' + this.inboundCompleteDialog.totalNeeded + ' 个库位，当前已选 ' + this.inboundCompleteDialog.selectedLocationIds.length + ' 个')
        return
      }
      this.confirmDialog.message = '确定要完成入库吗？将按所选库位分配货物。'
      this.confirmDialog.confirmText = '确认入库'
      this.confirmDialog.callback = this.submitCompleteInbound
      this.confirmDialog.visible = true
    },
    async submitCompleteInbound() {
      this.confirmDialog.visible = false
      const uid = this.getUserId()
      try {
        const res = await request.put('/inbound/complete/' + this.inboundCompleteDialog.task.task_id, {
          warehouseId: this.inboundCompleteDialog.warehouseId,
          locationIds: this.inboundCompleteDialog.selectedLocationIds,
          remark: this.inboundCompleteDialog.remark,
          operatorId: uid
        })
        if (res.code === 200) {
          alert('入库完成')
          this.inboundCompleteDialog.visible = false
          this.fetchMyTasks()
        } else {
          alert(res.msg || '操作失败')
        }
      } catch (e) {
        alert('操作失败')
      }
    },

    // --- 库存调整 ---
    async openAdjustmentDialog() {
      this.fetchWarehouseStaff()
      this.fetchGoods()
      // 初始化左右面板
      const initPanel = () => ({ warehouseId: null, warehouses: [], zones: [], selectedZoneId: null, locations: [], selectedIds: [], selectedDetails: {} })
      this.adjustmentDialog = {
        visible: true,
        left: initPanel(),
        right: initPanel(),
        matchedItems: [],
        assigneeId: null,
        priority: '',
        deadline: ''
      }
      // 获取仓库列表
      try {
        const res = await request.get('/warehouse/list')
        if (res.code === 200) {
          const warehouses = res.data || []
          this.adjustmentDialog.left.warehouses = warehouses
          this.adjustmentDialog.right.warehouses = warehouses
        }
      } catch (e) { /* ignore */ }
    },

    async onAdjustLeftWarehouseChange() {
      const panel = this.adjustmentDialog.left
      panel.zones = []; panel.selectedZoneId = null; panel.locations = []
      if (!panel.warehouseId) return
      try {
        const res = await request.get('/zone/list', { warehouseId: panel.warehouseId })
        if (res.code === 200) { panel.zones = res.data || [] }
      } catch (e) { /* ignore */ }
    },
    async selectAdjustLeftZone(zone) {
      const panel = this.adjustmentDialog.left
      panel.selectedZoneId = zone.zone_id
      panel.locations = []
      try {
        const res = await request.get('/location/list', { zoneId: zone.zone_id })
        if (res.code === 200) { panel.locations = res.data || [] }
      } catch (e) { /* ignore */ }
    },
    isAdjustLeftSelected(locId) { return this.adjustmentDialog.left.selectedIds.includes(locId) },
    getAdjustLeftSeq(locId) { return this.adjustmentDialog.left.selectedIds.indexOf(locId) + 1 },
    async toggleAdjustLeftLocation(loc) {
      // 只能选已占用且未锁定的库位
      if (loc.is_occupied !== 1) return
      if (loc.lock_status === '锁定') return
      const panel = this.adjustmentDialog.left
      const idx = panel.selectedIds.indexOf(loc.location_id)
      if (idx >= 0) {
        panel.selectedIds.splice(idx, 1)
        delete panel.selectedDetails[loc.location_id]
      } else {
        // 查询库位上的库存信息
        await this.fetchLocationInventory(loc, 'left')
      }
      this.updateAdjustmentMatchedItems()
    },

    async onAdjustRightWarehouseChange() {
      const panel = this.adjustmentDialog.right
      panel.zones = []; panel.selectedZoneId = null; panel.locations = []
      if (!panel.warehouseId) return
      try {
        const res = await request.get('/zone/list', { warehouseId: panel.warehouseId })
        if (res.code === 200) { panel.zones = res.data || [] }
      } catch (e) { /* ignore */ }
    },
    async selectAdjustRightZone(zone) {
      const panel = this.adjustmentDialog.right
      panel.selectedZoneId = zone.zone_id
      panel.locations = []
      try {
        const res = await request.get('/location/list', { zoneId: zone.zone_id })
        if (res.code === 200) { panel.locations = res.data || [] }
      } catch (e) { /* ignore */ }
    },
    isAdjustRightSelected(locId) { return this.adjustmentDialog.right.selectedIds.includes(locId) },
    getAdjustRightSeq(locId) { return this.adjustmentDialog.right.selectedIds.indexOf(locId) + 1 },
    toggleAdjustRightLocation(loc) {
      // 只能选空闲且未锁定的库位
      if (loc.is_occupied === 1) return
      if (loc.lock_status === '锁定') return
      const panel = this.adjustmentDialog.right
      const idx = panel.selectedIds.indexOf(loc.location_id)
      if (idx >= 0) {
        panel.selectedIds.splice(idx, 1)
      } else {
        panel.selectedIds.push(loc.location_id)
      }
      this.updateAdjustmentMatchedItems()
    },

    async fetchLocationInventory(loc, side) {
      try {
        const res = await request.get('/inventory/listWithDetails')
        if (res.code === 200) {
          const list = res.data || []
          const inv = list.find(i => i.location_id === loc.location_id && i.inventory_status === '正常')
          const panel = this.adjustmentDialog.left
          panel.selectedIds.push(loc.location_id)
          if (inv) {
            panel.selectedDetails[loc.location_id] = {
              goodsId: inv.goods_id,
              goodsName: inv.goods_name || '-',
              batchId: inv.batch_id,
              quantity: inv.quantity,
              fromLocationName: loc.location_name,
              fromWarehouseId: inv.warehouse_id,
              fromZoneId: inv.zone_id,
              fromLocationId: loc.location_id
            }
          } else {
            panel.selectedDetails[loc.location_id] = {
              goodsId: null, goodsName: '?', batchId: '?', quantity: 0,
              fromLocationName: loc.location_name,
              fromWarehouseId: null, fromZoneId: null, fromLocationId: loc.location_id
            }
          }
        }
      } catch (e) {
        const panel = this.adjustmentDialog.left
        panel.selectedIds.push(loc.location_id)
        panel.selectedDetails[loc.location_id] = {
          goodsId: null, goodsName: '?', batchId: '?', quantity: 0,
          fromLocationName: loc.location_name,
          fromWarehouseId: null, fromZoneId: null, fromLocationId: loc.location_id
        }
      }
    },

    updateAdjustmentMatchedItems() {
      const left = this.adjustmentDialog.left
      const right = this.adjustmentDialog.right
      const items = []
      const maxLen = Math.max(left.selectedIds.length, right.selectedIds.length)
      for (let i = 0; i < maxLen; i++) {
        const leftId = left.selectedIds[i]
        const rightId = right.selectedIds[i]
        const leftDetail = leftId ? left.selectedDetails[leftId] : null
        const rightLoc = rightId ? right.locations.find(l => l.location_id === rightId) : null
        // 查询右库位的 warehouse/zone
        let toWhId = null, toZoneId = null
        if (rightLoc) {
          toZoneId = rightLoc.zone_id
          // 从 zones 中查找 warehouse_id
          const zone = right.zones.find(z => z.zone_id === rightLoc.zone_id)
          if (zone) toWhId = zone.warehouse_id
        }
        items.push({
          goodsId: leftDetail ? leftDetail.goodsId : null,
          goodsName: leftDetail ? leftDetail.goodsName : '-',
          batchId: leftDetail ? leftDetail.batchId : '-',
          quantity: leftDetail ? leftDetail.quantity : 0,
          fromLocationName: leftDetail ? leftDetail.fromLocationName : '-',
          fromWarehouseId: leftDetail ? leftDetail.fromWarehouseId : null,
          fromZoneId: leftDetail ? leftDetail.fromZoneId : null,
          fromLocationId: leftDetail ? leftDetail.fromLocationId : null,
          toLocationName: rightLoc ? rightLoc.location_name : '-',
          toWarehouseId: toWhId,
          toZoneId: toZoneId,
          toLocationId: rightId || null
        })
      }
      this.adjustmentDialog.matchedItems = items
    },

    confirmCreateAdjustment() {
      this.confirmDialog.message = '确定要创建库存调整任务吗？将锁定所有涉及的库位。'
      this.confirmDialog.confirmText = '确认创建'
      this.confirmDialog.callback = this.submitCreateAdjustment
      this.confirmDialog.visible = true
    },
    async submitCreateAdjustment() {
      this.confirmDialog.visible = false
      const items = this.adjustmentDialog.matchedItems
        .filter(item => item.fromLocationId && item.toLocationId)
        .map(item => ({
          goodsId: item.goodsId,
          batchId: item.batchId,
          quantity: item.quantity,
          fromWarehouseId: item.fromWarehouseId,
          fromZoneId: item.fromZoneId,
          fromLocationId: item.fromLocationId,
          toWarehouseId: item.toWarehouseId,
          toZoneId: item.toZoneId,
          toLocationId: item.toLocationId
        }))
      const uid = this.getUserId()
      try {
        const res = await request.post('/adjustment/create', {
          items: items,
          assigneeId: this.adjustmentDialog.assigneeId,
          priority: this.adjustmentDialog.priority,
          deadline: this.adjustmentDialog.deadline,
          operatorId: uid
        })
        if (res.code === 200) {
          alert('库存调整任务创建成功')
          this.adjustmentDialog.visible = false
          this.fetchMyTasks()
        } else {
          alert(res.msg || '创建失败')
        }
      } catch (e) {
        alert('创建失败')
      }
    },

    // --- 库存调整完成 ---
    async openAdjustmentCompleteDialog(task) {
      this.adjustmentCompleteDialog.task = task
      this.adjustmentCompleteDialog.details = []
      this.adjustmentCompleteDialog.visible = true
      try {
        const res = await request.get('/adjustment/detail/' + task.task_id)
        if (res.code === 200) {
          this.adjustmentCompleteDialog.details = res.data || []
        }
      } catch (e) { /* ignore */ }
    },
    rejectAdjustment() {
      if (!confirm('确定要退回该调整申请吗？退回后将解锁所有库位并删除相关单据。')) return
      const uid = this.getUserId()
      request.put('/adjustment/reject/' + this.adjustmentCompleteDialog.task.task_id, { operatorId: uid })
        .then(res => {
          if (res.code === 200) {
            alert('已退回调整申请')
            this.adjustmentCompleteDialog.visible = false
            this.fetchMyTasks()
          } else {
            alert(res.msg || '操作失败')
          }
        })
        .catch(() => alert('操作失败'))
    },
    confirmCompleteAdjustment() {
      this.confirmDialog.message = '确定要完成调整吗？货物将转移到新库位，原库位将被释放。'
      this.confirmDialog.confirmText = '确认完成'
      this.confirmDialog.callback = this.submitCompleteAdjustment
      this.confirmDialog.visible = true
    },
    async submitCompleteAdjustment() {
      this.confirmDialog.visible = false
      const uid = this.getUserId()
      try {
        const res = await request.put('/adjustment/complete/' + this.adjustmentCompleteDialog.task.task_id, { operatorId: uid })
        if (res.code === 200) {
          alert('调整完成')
          this.adjustmentCompleteDialog.visible = false
          this.fetchMyTasks()
        } else {
          alert(res.msg || '操作失败')
        }
      } catch (e) {
        alert('操作失败')
      }
    }
  }
}
</script>

<style scoped>
.tasks-page { display: flex; flex-direction: column; gap: 12px; }

.tab-row {
  display: flex;
  gap: 0;
  background: #fff;
  border: 1px solid #ebeef5;
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
  background: #409EFF;
  color: #fff;
}

.tab-content { display: flex; flex-direction: column; gap: 10px; }

/* 布置任务 - 操作中心 */
.assign-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}
.assign-header {
  text-align: center;
}
.assign-header h3 {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 6px 0;
}
.assign-header p {
  font-size: 13px;
  color: #909399;
  margin: 0;
}

.op-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
}
.op-card {
  --card-color: #409EFF;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  padding: 24px 16px 20px;
  cursor: pointer;
  transition: all 0.25s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  position: relative;
  overflow: hidden;
}
.op-card::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 3px;
  background: var(--card-color);
  border-radius: 0 0 2px 2px;
}
.op-card:hover {
  border-color: var(--card-color);
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.1);
}
.op-card:active {
  transform: translateY(-1px);
}

.op-card-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: color-mix(in srgb, var(--card-color) 12%, transparent);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-bottom: 12px;
}
.op-card-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
}
.op-card-desc {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
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
.btn-primary {
  background: #409EFF;
  color: #fff;
  align-self: flex-start;
}
.btn-primary:hover { background: #66b1ff; }
.btn-cancel {
  background: #f4f4f5;
  color: #606266;
}
.btn-cancel:hover { background: #e6e6e8; }

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

.priority-tag {
  font-size: 11px; padding: 2px 8px; border-radius: 4px;
}
.priority-tag.高 { background: #fef0f0; color: #f56c6c; }
.priority-tag.中 { background: #fdf6ec; color: #e6a23c; }
.priority-tag.低 { background: #f0f9eb; color: #67c23a; }

.action-cell { display: flex; gap: 6px; }
.btn-action {
  padding: 4px 12px; border: none; border-radius: 4px; font-size: 12px; cursor: pointer; transition: all 0.2s;
}
.btn-action.done { background: #f0f9eb; color: #67c23a; }
.btn-action.done:hover { background: #e1f3d8; }

/* 表单 */
.form-item { display: flex; flex-direction: column; gap: 4px; margin-bottom: 12px; }
.form-item label { font-size: 13px; color: #606266; }
.form-item .required { color: #f56c6c; }
.form-item input, .form-item select, .form-item textarea {
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 13px;
  color: #303133;
  outline: none;
  transition: border-color 0.2s;
  font-family: inherit;
}
.form-item input:focus, .form-item select:focus, .form-item textarea:focus {
  border-color: #409EFF;
}
.form-item select { background: #fff; cursor: pointer; }
.form-item textarea { resize: vertical; min-height: 60px; }

/* 弹窗 */
.dialog-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.35); z-index: 9999;
  display: flex; align-items: center; justify-content: center;
}
.confirm-dialog-overlay { z-index: 99999; }
.dialog-box {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  width: 480px; max-width: 92vw;
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
.dialog-body {
  padding: 16px 20px; display: flex; flex-direction: column;
}
.dialog-footer {
  display: flex; justify-content: flex-end; gap: 8px;
  padding: 10px 20px 16px; border-top: 1px solid #ebeef5;
}

/* 入库完成弹窗 - 仓库可视化 */
.view-dialog-box { width: 900px; }
.view-dialog-body {
  display: flex; height: 400px; border-top: 1px solid #ebeef5; border-bottom: 1px solid #ebeef5;
}
.progress-bar-wrap {
  background: #f5f7fa; padding: 8px 12px; border-radius: 4px; margin-bottom: 4px;
}
.progress-text { font-size: 13px; color: #606266; }

.zone-sidebar {
  width: 240px; min-width: 240px; border-right: 1px solid #ebeef5;
  overflow-y: auto; background: #fafafa;
}
.zone-sidebar-title {
  padding: 12px 14px; font-size: 13px; font-weight: 600; color: #303133;
  border-bottom: 1px solid #ebeef5; background: #fff; position: sticky; top: 0;
}
.zone-item {
  display: flex; align-items: center; gap: 8px;
  padding: 10px 14px; cursor: pointer; border-bottom: 1px solid #f0f0f0;
}
.zone-item:hover { background: #ecf5ff; }
.zone-item.active { background: #d9ecff; }
.zone-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.dot-green { background: #67c23a; }
.dot-red { background: #f56c6c; }
.zone-name { flex: 1; font-size: 13px; color: #303133; }
.zone-available { font-size: 11px; color: #909399; }
.location-main { flex: 1; display: flex; flex-direction: column; overflow-y: auto; }
.location-main-title {
  padding: 12px 16px; font-size: 13px; font-weight: 600;
  border-bottom: 1px solid #ebeef5; background: #fff; position: sticky; top: 0;
}
.location-main-placeholder {
  flex: 1; display: flex; align-items: center; justify-content: center;
  color: #c0c4cc; font-size: 14px;
}
.location-grid {
  display: flex; flex-wrap: wrap; gap: 10px;
  padding: 16px; align-content: flex-start;
}
.location-card {
  width: 90px; height: 56px; border-radius: 4px;
  display: flex; align-items: center; justify-content: center; gap: 4px;
  cursor: pointer; transition: all 0.15s; position: relative;
}
.location-card:hover { transform: scale(1.05); }
.loc-free { background: #f0f9eb; border: 1px solid #b3e19d; }
.loc-free:hover { border-color: #67c23a; }
.loc-occupied { background: #fef0f0; border: 1px solid #fab6b6; cursor: not-allowed; }
.loc-locked { background: #fef6e7; border: 1px solid #f5dab1; cursor: not-allowed; }
.loc-selected { background: #d9ecff; border: 2px solid #409EFF; }
.loc-name { font-size: 11px; color: #303133; text-align: center; word-break: break-all; }
.loc-checkbox { font-size: 12px; }
.loc-checkbox input[type="checkbox"] { cursor: pointer; }

.btn-danger {
  background: #f56c6c; color: #fff;
  display: inline-flex; align-items: center; gap: 4px;
  padding: 6px 16px; border: none; border-radius: 4px;
  font-size: 13px; cursor: pointer; transition: all 0.2s;
}
.btn-danger:hover { background: #f78989; }
.btn-primary:disabled { background: #a0cfff; cursor: not-allowed; }

/* 库存调整弹窗 */
.adjust-panels { display: flex; gap: 12px; }
.adjust-panel { flex: 1; border: 1px solid #ebeef5; border-radius: 4px; overflow: hidden; display: flex; flex-direction: column; }
.adjust-panel-title { padding: 8px 12px; font-size: 13px; font-weight: 600; color: #303133; background: #f5f7fa; border-bottom: 1px solid #ebeef5; }
.adjust-panel .form-item { padding: 4px 8px; margin: 0; }
.adjust-panel .progress-bar-wrap { margin: 0 8px 4px; }
.adjust-panel-body { display: flex; flex: 1; height: 200px; border-top: 1px solid #ebeef5; }
.adjust-zone-sidebar { width: 140px; min-width: 140px; }
.adjust-location-card { width: 72px; height: 46px; font-size: 10px; position: relative; }
.adjust-location-card.loc-occupied { cursor: pointer; }
.adjust-location-card.loc-occupied:hover { border-color: #409EFF; }
.loc-seq-badge {
  position: absolute; top: 2px; right: 4px;
  background: #409EFF; color: #fff; font-size: 10px; font-weight: 700;
  width: 18px; height: 18px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
}
.adjust-table-wrap { max-height: 200px; overflow-y: auto; }
.adjust-table-wrap .data-table { font-size: 12px; }
.adjust-table-wrap .data-table th, .adjust-table-wrap .data-table td { padding: 6px 8px; }

@media (max-width: 768px) {
  .data-table { font-size: 12px; }
  .data-table th, .data-table td { padding: 8px; }
  .op-grid { grid-template-columns: repeat(2, 1fr); gap: 10px; max-width: 100%; }
  .op-card { padding: 16px 12px 14px; }
  .adjust-panels { flex-direction: column; }
  .adjust-panel-body { height: 180px; }
}
</style>
