<template>
  <div class="tasks-page">
    <!-- 选项卡 -->
    <div class="tab-row">
      <button :class="{ active: activeTab === 'todo' }" @click="switchTab('todo')">我的待办</button>
      <button :class="{ active: activeTab === 'assign' }" @click="switchTab('assign')">操作中心</button>
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
          <p style="text-align:center;color:var(--foreground-muted);padding:20px;">该类型任务完成功能开发中...</p>
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
          <p style="text-align:center;font-size:14px;color:var(--foreground);">{{ confirmDialog.message }}</p>
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
              <span v-if="inboundCompleteDialog.selectedLocationIds.length === inboundCompleteDialog.totalNeeded" style="color:var(--success);"> ✓ 已满足</span>
              <span v-else-if="inboundCompleteDialog.selectedLocationIds.length > inboundCompleteDialog.totalNeeded" style="color:var(--warning);">（多了 {{ inboundCompleteDialog.selectedLocationIds.length - inboundCompleteDialog.totalNeeded }} 个）</span>
              <span v-else style="color:var(--danger);">（还需 {{ inboundCompleteDialog.totalNeeded - inboundCompleteDialog.selectedLocationIds.length }} 个）</span>
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
            <div v-if="inboundCompleteDialog.zones.length === 0" style="padding:16px;color:var(--foreground-muted);font-size:12px;">
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
              <div v-if="inboundCompleteDialog.locations.length === 0" style="width:100%;text-align:center;padding:30px;color:var(--foreground-muted);">
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
          <div v-else style="text-align:center;color:var(--foreground-placeholder);font-size:13px;padding:8px;">
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
          <p style="font-size:13px;color:var(--foreground-regular);">截至日期：<strong>{{ formatDeadline(adjustmentCompleteDialog.task.deadline) }}</strong></p>
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

    <!-- 出库弹窗 -->
    <div class="dialog-overlay" v-if="outboundDialog.visible" @click.self="outboundDialog.visible = false">
      <div class="dialog-box" style="width:750px;max-width:98vw;max-height:90vh;overflow-y:auto;">
        <div class="dialog-header">
          <h3>新建出库任务</h3>
          <button class="dialog-close" @click="outboundDialog.visible = false">&times;</button>
        </div>
        <div class="dialog-body" style="flex-direction:column;">
          <div class="form-item">
            <label>选择货物 <span class="required">*</span></label>
            <select v-model="outboundDialog.goodsId" @change="onOutboundGoodsChange">
              <option :value="null">请选择货物</option>
              <option v-for="g in goodsList" :key="g.goods_id" :value="g.goods_id">
                {{ g.goods_name }} (剩余 {{ g.quantity || 0 }} {{ g.unit || '' }})
              </option>
            </select>
          </div>

          <div v-if="outboundDialog.inventoryRows.length > 0" style="margin-top:8px;">
            <table class="data-table">
              <thead>
                <tr>
                  <th>批次</th>
                  <th>存储位置</th>
                  <th>到期日期</th>
                  <th>剩余数量</th>
                  <th>选择数量</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(row, idx) in outboundDialog.inventoryRows" :key="idx">
                  <td>{{ row.batch_id }}</td>
                  <td>{{ row.location_name }}</td>
                  <td>{{ row.expiry_date || '-' }}</td>
                  <td>{{ row.available }}</td>
                  <td>
                    <input type="number" :min="0" :max="row.available" :value="row.selectQty"
                           @input="onOutboundQtyInput(row, $event)"
                           style="width:80px;padding:4px 8px;border:1px solid var(--border-color-light);border-radius:4px;" />
                  </td>
                </tr>
              </tbody>
            </table>
            <div style="text-align:right;margin-top:8px;font-size:14px;font-weight:600;color:var(--foreground);">
              已选总数量：<span style="color:var(--primary-color);">{{ outboundTotalSelected }}</span>
            </div>
          </div>
          <div v-else-if="outboundDialog.goodsId" style="text-align:center;color:var(--foreground-placeholder);font-size:13px;padding:12px;">
            该货物暂无可用库存
          </div>

          <div style="display:flex;gap:16px;flex-wrap:wrap;margin-top:8px;">
            <div class="form-item" style="flex:1;min-width:150px;">
              <label>负责人 <span class="required">*</span></label>
              <select v-model="outboundDialog.assigneeId">
                <option :value="null">请选择负责人</option>
                <option v-for="u in warehouseStaff" :key="u.user_id" :value="u.user_id">{{ u.real_name || u.username }}</option>
              </select>
            </div>
            <div class="form-item" style="flex:1;min-width:120px;">
              <label>优先级 <span class="required">*</span></label>
              <select v-model="outboundDialog.priority">
                <option value="">请选择</option>
                <option value="高">高</option>
                <option value="中">中</option>
                <option value="低">低</option>
              </select>
            </div>
            <div class="form-item" style="flex:1;min-width:150px;">
              <label>截至日期 <span class="required">*</span></label>
              <input type="date" v-model="outboundDialog.deadline" />
            </div>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-cancel" @click="outboundDialog.visible = false">取消</button>
          <button class="btn btn-primary"
                  :disabled="outboundTotalSelected <= 0"
                  @click="submitOutbound">
            确认创建
          </button>
        </div>
      </div>
    </div>

    <!-- 出库完成弹窗 -->
    <div class="dialog-overlay" v-if="outboundCompleteDialog.visible" @click.self="outboundCompleteDialog.visible = false">
      <div class="dialog-box" style="width:650px;">
        <div class="dialog-header">
          <h3>完成出库</h3>
          <button class="dialog-close" @click="outboundCompleteDialog.visible = false">&times;</button>
        </div>
        <div class="dialog-body" style="flex-direction:column;">
          <p style="font-size:13px;color:var(--foreground-regular);">截至日期：<strong>{{ formatDeadline(outboundCompleteDialog.task.deadline) }}</strong></p>
          <p style="font-size:13px;color:var(--foreground-regular);margin-top:4px;" v-if="outboundCompleteDialog.details.length > 0">
            货物名称：<strong>{{ outboundCompleteDialog.details[0].goodsName }}</strong>
          </p>
          <table class="data-table" style="margin-top:8px;">
            <thead>
              <tr><th>批次</th><th>位置</th><th>数量</th></tr>
            </thead>
            <tbody>
              <tr v-if="outboundCompleteDialog.details.length === 0">
                <td colspan="3" class="empty-cell">加载中...</td>
              </tr>
              <tr v-for="(d, idx) in outboundCompleteDialog.details" :key="idx">
                <td>{{ d.batchId }}</td>
                <td>{{ d.locationName }}</td>
                <td>{{ d.quantity }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-danger" @click="rejectOutbound">退回出库请求</button>
          <button class="btn btn-primary" @click="confirmCompleteOutbound">确认出库</button>
        </div>
      </div>
    </div>

    <!-- 处理次品弹窗 -->
    <div class="dialog-overlay" v-if="defectiveDialog.visible" @click.self="defectiveDialog.visible = false">
      <div class="dialog-box" style="width:700px;max-width:98vw;max-height:90vh;overflow-y:auto;">
        <div class="dialog-header">
          <h3>新建处理次品任务</h3>
          <button class="dialog-close" @click="defectiveDialog.visible = false">&times;</button>
        </div>
        <div class="dialog-body" style="flex-direction:column;">
          <div v-if="defectiveDialog.batches.length > 0">
            <table class="data-table">
              <thead>
                <tr>
                  <th>批次号</th>
                  <th>货物名称</th>
                  <th>是否选中</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(b, idx) in defectiveDialog.batches" :key="idx">
                  <td>{{ b.batch_id }}</td>
                  <td>{{ b.goods_name }}</td>
                  <td>
                    <input type="checkbox" v-model="b.checked" />
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <div v-else style="text-align:center;color:var(--foreground-placeholder);font-size:13px;padding:12px;">
            暂无待报废的批次
          </div>

          <div style="display:flex;gap:16px;flex-wrap:wrap;margin-top:8px;">
            <div class="form-item" style="flex:1;min-width:150px;">
              <label>负责人 <span class="required">*</span></label>
              <select v-model="defectiveDialog.assigneeId">
                <option :value="null">请选择负责人</option>
                <option v-for="u in warehouseStaff" :key="u.user_id" :value="u.user_id">{{ u.real_name || u.username }}</option>
              </select>
            </div>
            <div class="form-item" style="flex:1;min-width:120px;">
              <label>优先级 <span class="required">*</span></label>
              <select v-model="defectiveDialog.priority">
                <option value="">请选择</option>
                <option value="高">高</option>
                <option value="中">中</option>
                <option value="低">低</option>
              </select>
            </div>
            <div class="form-item" style="flex:1;min-width:150px;">
              <label>截至日期 <span class="required">*</span></label>
              <input type="date" v-model="defectiveDialog.deadline" />
            </div>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-cancel" @click="defectiveDialog.visible = false">取消</button>
          <button class="btn btn-primary"
                  :disabled="!defectiveDialog.batches.some(b => b.checked)"
                  @click="submitDefective">
            确认创建
          </button>
        </div>
      </div>
    </div>

    <!-- 库存盘点弹窗 -->
    <div class="dialog-overlay" v-if="invCheckDialog.visible" @click.self="invCheckDialog.visible = false">
      <div class="dialog-box">
        <div class="dialog-header">
          <h3>新建盘点任务</h3>
          <button class="dialog-close" @click="invCheckDialog.visible = false">&times;</button>
        </div>
        <div class="dialog-body">
          <div class="form-item">
            <label>盘点类型 <span class="required">*</span></label>
            <select v-model="invCheckDialog.form.checkType" @change="onInvCheckTypeChange">
              <option value="">请选择盘点类型</option>
              <option value="按仓库盘点">按仓库盘点</option>
              <option value="按商品盘点">按商品盘点</option>
              <option value="按批次盘点">按批次盘点</option>
            </select>
          </div>
          <div class="form-item">
            <label>盘点范围 <span class="required">*</span></label>
            <select v-if="invCheckDialog.form.checkType === '按仓库盘点'" v-model="invCheckDialog.form.scopeValue">
              <option value="">请选择仓库</option>
              <option v-for="wh in warehouseListForCheck" :key="wh.warehouse_id" :value="wh.warehouse_id">
                {{ wh.warehouse_name }} (可用:{{ wh.available_slots }}/{{ wh.total_slots }})
              </option>
            </select>
            <select v-else-if="invCheckDialog.form.checkType === '按商品盘点'" v-model="invCheckDialog.form.scopeValue">
              <option value="">请选择货物</option>
              <option v-for="g in goodsInInventory" :key="g.goods_id" :value="g.goods_id">
                {{ g.goods_name }} ({{ g.goods_code }})
              </option>
            </select>
            <select v-else-if="invCheckDialog.form.checkType === '按批次盘点'" v-model="invCheckDialog.form.scopeValue">
              <option value="">请选择批次</option>
              <option v-for="b in normalBatchesInInventory" :key="b.batch_id" :value="b.batch_id">
                {{ b.batch_id }} ({{ getGoodsName(b.goods_id) }})
              </option>
            </select>
            <span v-else style="color:var(--foreground-muted);font-size:12px;">请先选择盘点类型</span>
          </div>
          <div class="form-item">
            <label>盘点员 <span class="required">*</span></label>
            <select v-model="invCheckDialog.form.operatorId">
              <option :value="null">请选择盘点员</option>
              <option v-for="u in checkerList" :key="u.user_id" :value="u.user_id">
                {{ u.real_name || u.username }}
              </option>
            </select>
          </div>
          <div class="form-item">
            <label>优先级 <span class="required">*</span></label>
            <select v-model="invCheckDialog.form.priority">
              <option value="">请选择优先级</option>
              <option value="高">高</option>
              <option value="中">中</option>
              <option value="低">低</option>
            </select>
          </div>
          <div class="form-item">
            <label>截至时间 <span class="required">*</span></label>
            <input type="date" v-model="invCheckDialog.form.deadline" />
          </div>
          <div class="form-item">
            <label>备注</label>
            <textarea v-model="invCheckDialog.form.remark" placeholder="可选备注" rows="3"></textarea>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-cancel" @click="invCheckDialog.visible = false">取消</button>
          <button class="btn btn-primary" @click="submitInvCheck">确认创建</button>
        </div>
      </div>
    </div>

    <!-- 处理次品完成弹窗 -->
    <div class="dialog-overlay" v-if="defectiveCompleteDialog.visible" @click.self="defectiveCompleteDialog.visible = false">
      <div class="dialog-box" style="width:650px;">
        <div class="dialog-header">
          <h3>完成次品处理</h3>
          <button class="dialog-close" @click="defectiveCompleteDialog.visible = false">&times;</button>
        </div>
        <div class="dialog-body" style="flex-direction:column;">
          <p style="font-size:13px;color:var(--foreground-regular);">截至日期：<strong>{{ formatDeadline(defectiveCompleteDialog.task.deadline) }}</strong></p>
          <table class="data-table" style="margin-top:8px;">
            <thead>
              <tr><th>批次</th><th>货物名称</th><th>位置</th><th>数量</th></tr>
            </thead>
            <tbody>
              <tr v-if="defectiveCompleteDialog.details.length === 0">
                <td colspan="4" class="empty-cell">加载中...</td>
              </tr>
              <tr v-for="(d, idx) in defectiveCompleteDialog.details" :key="idx">
                <td>{{ d.batchId }}</td>
                <td>{{ d.goodsName }}</td>
                <td>{{ d.locationName || '未入库' }}</td>
                <td>{{ d.quantity }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-danger" @click="rejectDefective">退回处理请求</button>
          <button class="btn btn-primary" @click="confirmCompleteDefective">完成处理</button>
        </div>
      </div>
    </div>

    <!-- 上传温湿度弹窗 -->
    <div class="dialog-overlay" v-if="tempHumidityDialog.visible" @click.self="tempHumidityDialog.visible = false">
      <div class="dialog-box">
        <div class="dialog-header">
          <h3>上传温湿度</h3>
          <button class="dialog-close" @click="tempHumidityDialog.visible = false">&times;</button>
        </div>
        <div class="dialog-body">
          <div class="form-item">
            <label>选择库房 <span class="required">*</span></label>
            <select v-model="tempHumidityDialog.form.warehouseId">
              <option :value="null">请选择库房</option>
              <option v-for="w in warehouseList" :key="w.warehouse_id" :value="w.warehouse_id">
                {{ w.warehouse_name }}
              </option>
            </select>
          </div>
          <div class="form-item">
            <label>温度 (°C) <span class="required">*</span></label>
            <input type="number" v-model="tempHumidityDialog.form.temperature" step="0.1" placeholder="请输入温度，如 25.0" />
          </div>
          <div class="form-item">
            <label>湿度 (%) <span class="required">*</span></label>
            <input type="number" v-model="tempHumidityDialog.form.humidity" step="0.1" placeholder="请输入湿度，如 60.0" />
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-cancel" @click="tempHumidityDialog.visible = false">取消</button>
          <button class="btn btn-primary" @click="submitTempHumidity">确认上传</button>
        </div>
      </div>
    </div>

    <!-- 库存盘点完成弹窗 -->
    <div class="dialog-overlay" v-if="invCheckCompleteDialog.visible" @click.self="invCheckCompleteDialog.visible = false">
      <div class="dialog-box" style="width:700px;">
        <div class="dialog-header">
          <h3>完成盘点</h3>
          <button class="dialog-close" @click="invCheckCompleteDialog.visible = false">&times;</button>
        </div>
        <div class="dialog-body" style="flex-direction:column;">
          <p style="font-size:13px;color:var(--foreground-regular);margin-bottom:8px;">
            盘点单号：<strong>{{ invCheckCompleteDialog.task ? invCheckCompleteDialog.task.related_order_no : '' }}</strong>
          </p>
          <div class="table-wrapper" style="max-height:300px;overflow-y:auto;">
            <table class="data-table">
              <thead>
                <tr>
                  <th>货物</th>
                  <th>库位</th>
                  <th>批次</th>
                  <th>账面数量</th>
                  <th>实盘数量</th>
                  <th>差异</th>
                  <th>状态</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(d, i) in invCheckCompleteDialog.details" :key="d.detail_id">
                  <td>{{ getGoodsName(d.goods_id) }}</td>
                  <td>{{ getLocationName(d.location_id) }}</td>
                  <td>{{ d.batch_id || '-' }}</td>
                  <td>{{ d.book_quantity }}</td>
                  <td>
                    <input type="number" v-model.number="invCheckCompleteDialog.details[i].actual_quantity"
                      min="0" style="width:80px;padding:4px 6px;border:1px solid var(--border-color-light);border-radius:4px;font-size:13px;" />
                  </td>
                  <td :class="invCheckDiffClass(d)">
                    {{ d.actual_quantity != null ? (d.actual_quantity - d.book_quantity > 0 ? '+' : '') + (d.actual_quantity - d.book_quantity) : '-' }}
                  </td>
                  <td>
                    <span v-if="d.actual_quantity != null" class="priority-tag" :class="invCheckStatusClass(d)">
                      {{ invCheckStatusLabel(d) }}
                    </span>
                    <span v-else>-</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="form-item" style="margin-top:12px;">
            <label>备注</label>
            <textarea v-model="invCheckCompleteDialog.remark" placeholder="备注" rows="2"></textarea>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-cancel" @click="invCheckCompleteDialog.visible = false">取消</button>
          <button class="btn btn-primary" @click="confirmInvCheckComplete">提交</button>
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
        { key: 'newBatch',  label: '新增批次', color: 'var(--primary-color)', icon: '&#128230;', desc: '创建新的货物批次并关联货物信息' },
        { key: 'inbound',   label: '入库',     color: 'var(--success)', icon: '&#128229;', desc: '将货物录入仓库并分配库位' },
        { key: 'outbound',  label: '出库',     color: 'var(--warning)', icon: '&#128228;', desc: '按出库单拣货并完成出库' },
        { key: 'adjust',    label: '库存调整', color: '#9065db', icon: '&#9881;',  desc: '修正库存数量差异或库位调整' },
        { key: 'check',     label: '库存盘点', color: '#20a0ff', icon: '&#128203;', desc: '对库位货物进行盘点核对' },
        { key: 'quality',   label: '质检',     color: 'var(--danger)', icon: '&#128270;', desc: '对入库货物进行质量检验' },
        { key: 'defective', label: '处理次品', color: '#f39c12', icon: '&#128465;', desc: '登记并处理不合格或报废货物' },
        { key: 'uploadTempHumidity', label: '上传温湿度', color: '#00b4d8', icon: '&#127777;', desc: '上传库房温度和湿度记录' },
      ],
      taskList: [],
      userId: null,
      userPosition: '',
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
      },
      outboundDialog: {
        visible: false,
        goodsId: null,
        inventoryRows: [],
        assigneeId: null,
        priority: '',
        deadline: ''
      },
      outboundCompleteDialog: {
        visible: false,
        task: null,
        details: []
      },
      defectiveDialog: {
        visible: false,
        batches: [],
        assigneeId: null,
        priority: '',
        deadline: ''
      },
      defectiveCompleteDialog: {
        visible: false,
        task: null,
        details: []
      },
      invCheckDialog: {
        visible: false,
        form: { checkType: '', scopeValue: '', operatorId: null, priority: '', deadline: '', remark: '' }
      },
      invCheckCompleteDialog: {
        visible: false,
        task: null,
        details: [],
        remark: ''
      },
      tempHumidityDialog: {
        visible: false,
        form: { warehouseId: null, temperature: '', humidity: '' }
      },
      warehouseList: [],
      checkerList: [],
      warehouseListForCheck: [],
      goodsInInventory: [],
      normalBatchesInInventory: [],
      locationMap: {}
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
    },
    outboundTotalSelected() {
      let total = 0
      for (const row of this.outboundDialog.inventoryRows) {
        if (row.selectQty > 0) total += row.selectQty
      }
      return total
    }
  },
  watch: {
    '$route.query.tab'(val) {
      if (val && ['todo', 'assign'].includes(val)) {
        this.activeTab = val
      }
    },
    '$route.query.op'(val) {
      if (val) {
        this.$nextTick(() => { this.handleOp(val) })
      }
    }
  },
  mounted() {
    const tab = this.$route.query.tab
    if (tab && ['todo', 'assign'].includes(tab)) {
      this.activeTab = tab
    }
    const op = this.$route.query.op
    if (op) {
      this.$nextTick(() => { this.handleOp(op) })
    }
    const stored = localStorage.getItem('userInfo')
    if (stored) {
      try {
        const data = JSON.parse(stored)
        this.userId = data.user_id
        this.userPosition = data.position || ''
      } catch (e) { /* ignore */ }
    }
    this.fetchMyTasks()
    this.fetchGoods()
    this.fetchAllBatches()
  },
  methods: {
    switchTab(key) {
      this.activeTab = key
      this.$router.replace({ query: { tab: key } })
    },
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
      const warehouseOps = ['newBatch', 'inbound', 'outbound', 'adjust', 'check', 'quality', 'defective']
      if (warehouseOps.includes(key)) {
        if (this.userPosition !== '主管' && this.userPosition !== '仓库管理员') {
          alert('仅主管和仓库管理员可执行此操作')
          return
        }
      }
      if (key === 'uploadTempHumidity') {
        if (this.userPosition !== '质检员' && this.userPosition !== '温度检测员') {
          alert('仅质检员和温度检测员可上传温湿度')
          return
        }
      }

      if (key === 'newBatch') {
        this.openNewBatchDialog()
      } else if (key === 'quality') {
        this.openQcDialog()
      } else if (key === 'inbound') {
        this.openInboundDialog()
      } else if (key === 'adjust') {
        this.openAdjustmentDialog()
      } else if (key === 'outbound') {
        this.openOutboundDialog()
      } else if (key === 'defective') {
        this.openDefectiveDialog()
      } else if (key === 'uploadTempHumidity') {
        this.openTempHumidityDialog()
      } else if (key === 'check') {
        this.openInventoryCheckDialog()
      } else {
        alert('功能开发中...')
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
      const tasks = []
      try {
        const wtRes = await request.get('/workTask/myTasks', { assigneeId: uid })
        if (wtRes.code === 200) {
          tasks.push(...(wtRes.data || []))
        }
      } catch (e) { /* ignore */ }
      tasks.sort((a, b) => {
        const pa = a.priority === '高' ? 0 : a.priority === '中' ? 1 : 2
        const pb = b.priority === '高' ? 0 : b.priority === '中' ? 1 : 2
        return pa - pb
      })
      this.taskList = tasks
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
      } else if (task.task_type === '出库') {
        this.openOutboundCompleteDialog(task)
      } else if (task.task_type === '处理不合格货物') {
        this.openDefectiveCompleteDialog(task)
      } else if (task.task_type === '库存盘点') {
        this.openInvCheckCompleteDialog(task)
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
    },

    // --- 出库 ---
    openOutboundDialog() {
      this.fetchWarehouseStaff()
      this.fetchGoods()
      this.fetchAllBatches()
      this.outboundDialog = {
        visible: true,
        goodsId: null,
        inventoryRows: [],
        assigneeId: null,
        priority: '',
        deadline: ''
      }
    },

    async onOutboundGoodsChange() {
      this.outboundDialog.inventoryRows = []
      if (!this.outboundDialog.goodsId) return
      try {
        const res = await request.get('/outbound/availableInventory', { goodsId: this.outboundDialog.goodsId })
        if (res.code === 200) {
          const list = res.data || []
          const rows = list.map(inv => {
            const batch = this.allBatches.find(b => b.batch_id === inv.batch_id)
            const available = Math.max(0, inv.quantity - inv.locked_quantity)
            return {
              inventory_id: inv.inventory_id,
              batch_id: inv.batch_id,
              expiry_date: batch ? batch.expiry_date : null,
              location_id: inv.location_id,
              location_name: inv.location_name || '-',
              warehouse_id: inv.warehouse_id,
              zone_id: inv.zone_id,
              goods_id: inv.goods_id,
              available: available,
              selectQty: 0
            }
          })
          // FIFO: 按到期日期排序，日期靠前的排前面
          rows.sort((a, b) => {
            if (!a.expiry_date) return 1
            if (!b.expiry_date) return -1
            return a.expiry_date.localeCompare(b.expiry_date)
          })
          this.outboundDialog.inventoryRows = rows
        }
      } catch (e) { /* ignore */ }
    },

    onOutboundQtyInput(row, event) {
      const val = parseInt(event.target.value) || 0
      row.selectQty = Math.max(0, Math.min(val, row.available))
    },

    submitOutbound() {
      if (this.outboundTotalSelected <= 0) return
      const f = this.outboundDialog
      if (!f.assigneeId) { alert('请选择负责人'); return }
      if (!f.priority) { alert('请选择优先级'); return }
      if (!f.deadline) { alert('请选择截至日期'); return }
      this.confirmDialog.message = '确定要创建出库任务吗？将锁定所有涉及的库位。'
      this.confirmDialog.confirmText = '确认创建'
      this.confirmDialog.callback = this.submitCreateOutbound
      this.confirmDialog.visible = true
    },

    async submitCreateOutbound() {
      this.confirmDialog.visible = false
      const rows = this.outboundDialog.inventoryRows.filter(r => r.selectQty > 0)
      const items = rows.map(r => ({
        goodsId: r.goods_id,
        batchId: r.batch_id,
        quantity: r.selectQty,
        warehouseId: r.warehouse_id,
        zoneId: r.zone_id,
        locationId: r.location_id
      }))
      const uid = this.getUserId()
      try {
        const res = await request.post('/outbound/create', {
          items: items,
          assigneeId: this.outboundDialog.assigneeId,
          priority: this.outboundDialog.priority,
          deadline: this.outboundDialog.deadline,
          operatorId: uid
        })
        if (res.code === 200) {
          alert('出库任务创建成功')
          this.outboundDialog.visible = false
          this.fetchMyTasks()
        } else {
          alert(res.msg || '创建失败')
        }
      } catch (e) {
        alert('创建失败')
      }
    },

    // --- 出库完成 ---
    async openOutboundCompleteDialog(task) {
      this.outboundCompleteDialog.task = task
      this.outboundCompleteDialog.details = []
      this.outboundCompleteDialog.visible = true
      try {
        const res = await request.get('/outbound/detail/' + task.task_id)
        if (res.code === 200) {
          this.outboundCompleteDialog.details = res.data || []
        }
      } catch (e) { /* ignore */ }
    },

    rejectOutbound() {
      if (!confirm('确定要退回该出库请求吗？退回后将解锁所有库位并删除相关单据。')) return
      const uid = this.getUserId()
      request.put('/outbound/reject/' + this.outboundCompleteDialog.task.task_id, { operatorId: uid })
        .then(res => {
          if (res.code === 200) {
            alert('已退回出库请求')
            this.outboundCompleteDialog.visible = false
            this.fetchMyTasks()
          } else {
            alert(res.msg || '操作失败')
          }
        })
        .catch(() => alert('操作失败'))
    },

    confirmCompleteOutbound() {
      this.confirmDialog.message = '确定要完成出库吗？出库后库存数量将相应减少。'
      this.confirmDialog.confirmText = '确认出库'
      this.confirmDialog.callback = this.submitCompleteOutbound
      this.confirmDialog.visible = true
    },

    async submitCompleteOutbound() {
      this.confirmDialog.visible = false
      const uid = this.getUserId()
      try {
        const res = await request.put('/outbound/complete/' + this.outboundCompleteDialog.task.task_id, { operatorId: uid })
        if (res.code === 200) {
          alert('出库完成')
          this.outboundCompleteDialog.visible = false
          this.fetchMyTasks()
        } else {
          alert(res.msg || '操作失败')
        }
      } catch (e) {
        alert('操作失败')
      }
    },

    // --- 处理次品 ---
    async openDefectiveDialog() {
      this.fetchWarehouseStaff()
      this.fetchGoods()
      this.fetchAllBatches()
      this.defectiveDialog = {
        visible: true,
        batches: [],
        assigneeId: null,
        priority: '',
        deadline: ''
      }
      // 加载报废状态的批次
      try {
        const res = await request.get('/batch/list')
        if (res.code === 200) {
          const allBatches = res.data || []
          const scrapBatches = allBatches.filter(b => b.batch_status === '报废')
          this.defectiveDialog.batches = scrapBatches.map(b => {
            const goods = this.goodsList.find(g => g.goods_id === b.goods_id)
            return {
              batch_id: b.batch_id,
              goods_id: b.goods_id,
              goods_name: goods ? goods.goods_name : '-',
              remaining_quantity: b.remaining_quantity,
              checked: false
            }
          })
        }
      } catch (e) { /* ignore */ }
    },

    submitDefective() {
      const selected = this.defectiveDialog.batches.filter(b => b.checked)
      if (selected.length === 0) return
      if (!this.defectiveDialog.assigneeId) { alert('请选择负责人'); return }
      if (!this.defectiveDialog.priority) { alert('请选择优先级'); return }
      if (!this.defectiveDialog.deadline) { alert('请选择截至日期'); return }
      this.confirmDialog.message = '确定要创建次品处理任务吗？将锁定相关库位。'
      this.confirmDialog.confirmText = '确认创建'
      this.confirmDialog.callback = this.submitCreateDefective
      this.confirmDialog.visible = true
    },

    async submitCreateDefective() {
      this.confirmDialog.visible = false
      const selected = this.defectiveDialog.batches.filter(b => b.checked)
      const items = []
      for (const b of selected) {
        try {
          const res = await request.get('/inventory/listWithDetails', { batchId: b.batch_id })
          // 过滤出已入库的库存记录（location_id不为空的才是已上架货物）
          const storedInvs = (res.data || []).filter(inv => inv.location_id != null)
          if (res.code === 200 && storedInvs.length > 0) {
            // 已入库：使用库存记录（不限制 inventory_status，报废批次的库存都要处理）
            for (const inv of storedInvs) {
              items.push({
                batchId: b.batch_id,
                goodsId: b.goods_id,
                quantity: inv.quantity,
                sourceLocationId: inv.location_id,
                warehouseId: inv.warehouse_id,
                zoneId: inv.zone_id
              })
            }
          } else {
            // 未入库：使用批次剩余数量
            items.push({
              batchId: b.batch_id,
              goodsId: b.goods_id,
              quantity: b.remaining_quantity,
              sourceLocationId: null,
              warehouseId: null,
              zoneId: null
            })
          }
        } catch (e) {
          items.push({
            batchId: b.batch_id,
            goodsId: b.goods_id,
            quantity: b.remaining_quantity,
            sourceLocationId: null,
            warehouseId: null,
            zoneId: null
          })
        }
      }
      if (items.length === 0) {
        alert('选中的批次无可处理的库存')
        return
      }
      const uid = this.getUserId()
      try {
        const res = await request.post('/defective/create', {
          items: items,
          assigneeId: this.defectiveDialog.assigneeId,
          priority: this.defectiveDialog.priority,
          deadline: this.defectiveDialog.deadline,
          operatorId: uid
        })
        if (res.code === 200) {
          alert('次品处理任务创建成功')
          this.defectiveDialog.visible = false
          this.fetchMyTasks()
        } else {
          alert(res.msg || '创建失败')
        }
      } catch (e) {
        alert('创建失败')
      }
    },

    // --- 处理次品完成 ---
    async openDefectiveCompleteDialog(task) {
      this.defectiveCompleteDialog.task = task
      this.defectiveCompleteDialog.details = []
      this.defectiveCompleteDialog.visible = true
      try {
        const res = await request.get('/defective/detail/' + task.task_id)
        if (res.code === 200) {
          this.defectiveCompleteDialog.details = res.data || []
        }
      } catch (e) { /* ignore */ }
    },

    rejectDefective() {
      if (!confirm('确定要退回该次品处理请求吗？退回后将解锁所有库位并删除相关单据。')) return
      const uid = this.getUserId()
      request.put('/defective/reject/' + this.defectiveCompleteDialog.task.task_id, { operatorId: uid })
        .then(res => {
          if (res.code === 200) {
            alert('已退回次品处理请求')
            this.defectiveCompleteDialog.visible = false
            this.fetchMyTasks()
          } else {
            alert(res.msg || '操作失败')
          }
        })
        .catch(() => alert('操作失败'))
    },

    confirmCompleteDefective() {
      this.confirmDialog.message = '确定要完成次品处理吗？相关库存和批次将被删除。'
      this.confirmDialog.confirmText = '完成处理'
      this.confirmDialog.callback = this.submitCompleteDefective
      this.confirmDialog.visible = true
    },

    async submitCompleteDefective() {
      this.confirmDialog.visible = false
      const uid = this.getUserId()
      try {
        const res = await request.put('/defective/complete/' + this.defectiveCompleteDialog.task.task_id, { operatorId: uid })
        if (res.code === 200) {
          alert('次品处理完成')
          this.defectiveCompleteDialog.visible = false
          this.fetchMyTasks()
        } else {
          alert(res.msg || '操作失败')
        }
      } catch (e) {
        alert('操作失败')
      }
    },

    // --- 上传温湿度 ---
    async fetchWarehouseList() {
      try {
        const res = await request.get('/warehouse/list')
        if (res.code === 200) { this.warehouseList = res.data || [] }
      } catch (e) { /* ignore */ }
    },
    openTempHumidityDialog() {
      this.tempHumidityDialog.form = { warehouseId: null, temperature: '', humidity: '' }
      this.fetchWarehouseList()
      this.tempHumidityDialog.visible = true
    },
    async submitTempHumidity() {
      const f = this.tempHumidityDialog.form
      if (!f.warehouseId) { alert('请选择库房'); return }
      if (f.temperature === '' || f.temperature == null) { alert('请输入温度'); return }
      if (f.humidity === '' || f.humidity == null) { alert('请输入湿度'); return }
      try {
        const res = await request.post('/temperatureHumidityRecord/upload', {
          warehouseId: f.warehouseId,
          temperature: parseFloat(f.temperature),
          humidity: parseFloat(f.humidity)
        })
        if (res.code === 200) {
          alert('温湿度上传成功')
          this.tempHumidityDialog.visible = false
        } else {
          alert(res.msg || '上传失败')
        }
      } catch (e) {
        alert('上传失败')
      }
    },

    // --- 库存盘点 ---
    async fetchCheckers() {
      try {
        const res = await request.get('/user/checkers')
        if (res.code === 200) { this.checkerList = res.data || [] }
      } catch (e) { /* ignore */ }
    },
    async fetchWarehousesForCheck() {
      try {
        const res = await request.get('/warehouse/list')
        if (res.code === 200) {
          this.warehouseListForCheck = (res.data || []).filter(w => w.status === '启用')
        }
      } catch (e) { /* ignore */ }
    },
    async fetchGoodsInInventory() {
      try {
        const res = await request.get('/inventory/listWithDetails')
        if (res.code === 200) {
          const invList = res.data || []
          const seen = new Set()
          this.goodsInInventory = []
          invList.forEach(item => {
            if (!seen.has(item.goods_id)) {
              seen.add(item.goods_id)
              this.goodsInInventory.push({ goods_id: item.goods_id, goods_name: item.goods_name, goods_code: item.goods_name })
            }
          })
        }
      } catch (e) { /* ignore */ }
    },
    async fetchNormalBatchesInInventory() {
      try {
        const invRes = await request.get('/inventory/listWithDetails')
        const batchRes = await request.get('/batch/list')
        if (invRes.code === 200 && batchRes.code === 200) {
          const invList = invRes.data || []
          const batches = batchRes.data || []
          const invBatchIds = new Set(invList.map(i => i.batch_id))
          this.normalBatchesInInventory = batches.filter(
            b => b.batch_status === '正常' && invBatchIds.has(b.batch_id)
          )
        }
      } catch (e) { /* ignore */ }
    },
    openInventoryCheckDialog() {
      this.invCheckDialog.form = { checkType: '', scopeValue: '', operatorId: null, priority: '', deadline: '', remark: '' }
      this.fetchCheckers()
      this.fetchWarehousesForCheck()
      this.fetchGoodsInInventory()
      this.fetchNormalBatchesInInventory()
      this.fetchGoods()
      this.invCheckDialog.visible = true
    },
    onInvCheckTypeChange() {
      this.invCheckDialog.form.scopeValue = ''
    },
    async submitInvCheck() {
      const f = this.invCheckDialog.form
      if (!f.checkType) { alert('请选择盘点类型'); return }
      if (!f.scopeValue) { alert('请选择盘点范围'); return }
      if (!f.operatorId) { alert('请选择盘点员'); return }
      if (!f.priority) { alert('请选择优先级'); return }
      if (!f.deadline) { alert('请选择截至时间'); return }

      let scopeType = ''
      if (f.checkType === '按仓库盘点') scopeType = 'warehouse'
      else if (f.checkType === '按商品盘点') scopeType = 'goods'
      else if (f.checkType === '按批次盘点') scopeType = 'batch'

      try {
        const res = await request.post('/inventoryCheck/create', {
          checkType: f.checkType,
          scopeType: scopeType,
          scopeValue: String(f.scopeValue),
          operatorId: f.operatorId,
          priority: f.priority,
          deadline: f.deadline,
          remark: f.remark
        })
        if (res.code === 200) {
          alert('盘点任务创建成功')
          this.invCheckDialog.visible = false
          this.fetchMyTasks()
        } else {
          alert(res.msg || '创建失败')
        }
      } catch (e) {
        alert('创建失败')
      }
    },
    async fetchAllLocations() {
      try {
        const res = await request.get('/location/list')
        if (res.code === 200) {
          const list = res.data || []
          list.forEach(loc => {
            this.locationMap[loc.location_id] = loc.location_name
          })
        }
      } catch (e) { /* ignore */ }
    },
    getLocationName(locationId) {
      if (!locationId) return '-'
      return this.locationMap[locationId] || '库位' + locationId
    },
    async openInvCheckCompleteDialog(task) {
      this.invCheckCompleteDialog.task = task
      this.invCheckCompleteDialog.details = []
      this.invCheckCompleteDialog.remark = task.remark || ''
      this.fetchGoods()
      this.fetchAllLocations()
      try {
        const res = await request.get('/inventoryCheck/details', { checkNo: task.related_order_no })
        if (res.code === 200) {
          this.invCheckCompleteDialog.details = (res.data || []).map(d => ({
            ...d,
            actual_quantity: null
          }))
        }
      } catch (e) {
        alert('获取盘点明细失败')
      }
      this.invCheckCompleteDialog.visible = true
    },
    invCheckDiffClass(d) {
      if (d.actual_quantity == null) return ''
      const diff = d.actual_quantity - d.book_quantity
      if (diff > 0) return 'diff-surplus'
      if (diff < 0) return 'diff-shortage'
      return 'diff-normal'
    },
    invCheckStatusClass(d) {
      if (d.actual_quantity == null) return ''
      const diff = d.actual_quantity - d.book_quantity
      if (diff > 0) return '高'
      if (diff < 0) return '中'
      return '低'
    },
    invCheckStatusLabel(d) {
      if (d.actual_quantity == null) return '-'
      const diff = d.actual_quantity - d.book_quantity
      if (diff > 0) return '盘盈'
      if (diff < 0) return '盘亏'
      return '正常'
    },
    confirmInvCheckComplete() {
      const details = this.invCheckCompleteDialog.details
      for (let i = 0; i < details.length; i++) {
        const qty = details[i].actual_quantity
        if (qty == null || qty < 0) {
          alert('请为所有明细填写有效的实盘数量（第' + (i + 1) + '行）')
          return
        }
        if (qty > 10) {
          alert('您所填写的货物数量不合法，每个库位货物上限不能超过10')
          return
        }
      }
      this.confirmDialog.message = '确定要提交盘点结果吗？提交后将更新库存数据。'
      this.confirmDialog.confirmText = '确认提交'
      this.confirmDialog.callback = this.submitInvCheckComplete
      this.confirmDialog.visible = true
    },
    async submitInvCheckComplete() {
      this.confirmDialog.visible = false
      const uid = this.getUserId()
      const details = this.invCheckCompleteDialog.details.map(d => ({
        detailId: d.detail_id,
        actualQuantity: d.actual_quantity
      }))
      try {
        const res = await request.put('/inventoryCheck/complete/' + this.invCheckCompleteDialog.task.task_id, {
          details: details,
          remark: this.invCheckCompleteDialog.remark,
          operatorId: uid
        })
        if (res.code === 200) {
          alert('盘点任务已完成')
          this.invCheckCompleteDialog.visible = false
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
  gap: 4px;
  background: var(--card);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: 4px;
  width: fit-content;
  box-shadow: var(--shadow-sm);
}
.tab-row button {
  padding: 7px 22px;
  border: none;
  border-radius: var(--radius-md);
  background: transparent;
  color: var(--foreground-regular);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}
.tab-row button:hover { color: var(--foreground); background: var(--border-light); }
.tab-row button.active {
  background: var(--primary);
  color: var(--text-on-primary);
  box-shadow: var(--shadow-sm);
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
  color: var(--foreground);
  margin: 0 0 6px 0;
}
.assign-header p {
  font-size: 13px;
  color: var(--foreground-muted);
  margin: 0;
}

.op-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  max-width: 900px;
  margin: 0 auto;
}
.op-card {
  --card-color: var(--primary);
  background: var(--card);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-xl);
  padding: 24px 16px 20px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.25, 0.8, 0.5, 1);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  position: relative;
  overflow: hidden;
  aspect-ratio: 1;
  box-shadow: var(--shadow-sm);
}
.op-card::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 3px;
  background: var(--card-color);
  border-radius: 0;
}
.op-card:hover {
  border-color: var(--card-color);
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
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
  color: var(--foreground);
  margin-bottom: 6px;
}
.op-card-desc {
  font-size: 12px;
  color: var(--foreground-muted);
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
  background: var(--primary);
  color: var(--text-on-primary);
  align-self: flex-start;
}
.btn-primary:hover { background: var(--primary-hover); }
.btn-cancel {
  background: hsl(0, 0%, 96%);
  color: var(--foreground-regular);
  border: 1px solid var(--border);
}
.btn-cancel:hover { background: hsl(0, 0%, 90%); }

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

.priority-tag {
  font-size: 11px; padding: 2px 8px; border-radius: 4px;
}
.priority-tag.高 { background: var(--danger-bg); color: var(--danger); }
.priority-tag.中 { background: var(--warning-bg); color: var(--warning); }
.priority-tag.低 { background: var(--success-bg); color: var(--success); }

.action-cell { display: flex; gap: 6px; }
.btn-action {
  padding: 4px 12px; border: none; border-radius: 4px; font-size: 12px; cursor: pointer; transition: all 0.2s;
}
.btn-action.done { background: var(--success-bg); color: var(--success); }
.btn-action.done:hover { background: var(--success-bg); }

/* 表单 */
.form-item { display: flex; flex-direction: column; gap: 4px; margin-bottom: 12px; }
.form-item label { font-size: 13px; color: var(--foreground-regular); }
.form-item .required { color: var(--danger); }
.form-item input, .form-item select, .form-item textarea {
  padding: 8px 12px;
  border: 1px solid var(--border-color-light);
  border-radius: 4px;
  font-size: 13px;
  color: var(--foreground);
  outline: none;
  transition: border-color 0.2s;
  font-family: inherit;
}
.form-item input:focus, .form-item select:focus, .form-item textarea:focus {
  border-color: var(--primary-color);
}
.form-item select { background: var(--card); cursor: pointer; }
.form-item textarea { resize: vertical; min-height: 60px; }

/* 弹窗 */
.dialog-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: var(--overlay-bg); z-index: 9999;
  display: flex; align-items: center; justify-content: center;
}
.confirm-dialog-overlay { z-index: 99999; }
.dialog-box {
  background: var(--card);
  border: 1px solid var(--border-color-light);
  border-radius: 4px;
  width: 480px; max-width: 92vw;
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
.dialog-body {
  padding: 16px 20px; display: flex; flex-direction: column;
}
.dialog-footer {
  display: flex; justify-content: flex-end; gap: 8px;
  padding: 10px 20px 16px; border-top: 1px solid var(--border-color-light);
}

/* 入库完成弹窗 - 仓库可视化 */
.view-dialog-box { width: 900px; }
.view-dialog-body {
  display: flex; height: 400px; border-top: 1px solid var(--border-color-light); border-bottom: 1px solid var(--border-color-light);
}
.progress-bar-wrap {
  background: var(--page-bg); padding: 8px 12px; border-radius: 4px; margin-bottom: 4px;
}
.progress-text { font-size: 13px; color: var(--foreground-regular); }

.zone-sidebar {
  width: 240px; min-width: 240px; border-right: 1px solid var(--border-color-light);
  overflow-y: auto; background: var(--bg-secondary);
}
.zone-sidebar-title {
  padding: 12px 14px; font-size: 13px; font-weight: 600; color: var(--foreground);
  border-bottom: 1px solid var(--border-color-light); background: var(--card); position: sticky; top: 0;
}
.zone-item {
  display: flex; align-items: center; gap: 8px;
  padding: 10px 14px; cursor: pointer; border-bottom: 1px solid var(--bg-secondary);
}
.zone-item:hover { background: var(--primary-bg); }
.zone-item.active { background: var(--primary-bg-hover); }
.zone-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.dot-green { background: var(--success); }
.dot-red { background: var(--danger); }
.zone-name { flex: 1; font-size: 13px; color: var(--foreground); }
.zone-available { font-size: 11px; color: var(--foreground-muted); }
.location-main { flex: 1; display: flex; flex-direction: column; overflow-y: auto; }
.location-main-title {
  padding: 12px 16px; font-size: 13px; font-weight: 600;
  border-bottom: 1px solid var(--border-color-light); background: var(--card); position: sticky; top: 0;
}
.location-main-placeholder {
  flex: 1; display: flex; align-items: center; justify-content: center;
  color: var(--foreground-placeholder); font-size: 14px;
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
.loc-free { background: var(--success-bg); border: 1px solid var(--success-bg); }
.loc-free:hover { border-color: var(--success); }
.loc-occupied { background: var(--danger-bg); border: 1px solid var(--danger-bg); cursor: not-allowed; }
.loc-locked { background: #fef6e7; border: 1px solid var(--warning-bg); cursor: not-allowed; }
.loc-selected { background: var(--primary-bg-hover); border: 2px solid var(--primary-color); }
.loc-name { font-size: 11px; color: var(--foreground); text-align: center; word-break: break-all; }
.loc-checkbox { font-size: 12px; }
.loc-checkbox input[type="checkbox"] { cursor: pointer; }

.btn-danger {
  background: var(--danger); color: var(--text-on-primary);
  display: inline-flex; align-items: center; gap: 4px;
  padding: 6px 16px; border: none; border-radius: 4px;
  font-size: 13px; cursor: pointer; transition: all 0.2s;
}
.btn-danger:hover { background: #f78989; }
.btn-primary:disabled { background: #6ee7b7; cursor: not-allowed; }

/* 库存调整弹窗 */
.adjust-panels { display: flex; gap: 12px; }
.adjust-panel { flex: 1; border: 1px solid var(--border-color-light); border-radius: 4px; overflow: hidden; display: flex; flex-direction: column; }
.adjust-panel-title { padding: 8px 12px; font-size: 13px; font-weight: 600; color: var(--foreground); background: var(--page-bg); border-bottom: 1px solid var(--border-color-light); }
.adjust-panel .form-item { padding: 4px 8px; margin: 0; }
.adjust-panel .progress-bar-wrap { margin: 0 8px 4px; }
.adjust-panel-body { display: flex; flex: 1; height: 200px; border-top: 1px solid var(--border-color-light); }
.adjust-zone-sidebar { width: 140px; min-width: 140px; }
.adjust-location-card { width: 72px; height: 46px; font-size: 10px; position: relative; }
.adjust-location-card.loc-occupied { cursor: pointer; }
.adjust-location-card.loc-occupied:hover { border-color: var(--primary-color); }
.loc-seq-badge {
  position: absolute; top: 2px; right: 4px;
  background: var(--primary-color); color: var(--text-on-primary); font-size: 10px; font-weight: 700;
  width: 18px; height: 18px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
}
.adjust-table-wrap { max-height: 200px; overflow-y: auto; }
.adjust-table-wrap .data-table { font-size: 12px; }
.adjust-table-wrap .data-table th, .adjust-table-wrap .data-table td { padding: 6px 8px; }

.diff-surplus { color: var(--warning); font-weight: 600; }
.diff-shortage { color: var(--danger); font-weight: 600; }
.diff-normal { color: var(--success); }

@media (max-width: 768px) {
  .data-table { font-size: 12px; }
  .data-table th, .data-table td { padding: 8px; }
  .op-grid { grid-template-columns: repeat(2, 1fr); gap: 10px; max-width: 100%; }
  .op-card { padding: 16px 12px 14px; }
  .adjust-panels { flex-direction: column; }
  .adjust-panel-body { height: 180px; }
}
</style>
