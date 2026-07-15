## 库存盘点 — 实际实现设计文档

本文档根据后端代码 `InventoryCheckServiceImpl.java`、前端 `tasks/index.vue` 以及相关接口整理，描述盘点功能实际落地方案。

---

### 涉及到的表

仓库表 `warehouse`、库区表 `zone`、库位表 `location`、库存明细表 `inventory`、批次表 `batch`、货物表 `goods`、操作日志表 `operation_log`、工作任务表 `work_task`、盘点单头表 `inventory_check_head`、盘点单明细表 `inventory_check_detail`

---

### 一、创建盘点任务 — 弹窗输入（用户操作）

**① 盘点类型**
- 前端下拉框选项：按仓库盘点、按商品盘点、按批次盘点
- 写入 `inventory_check_head.check_type`

**② 盘点范围（与盘点类型联动）**
- 按仓库盘点 → 下拉选择仓库 ID（显示仓库名称及可用/总库位数）
- 按商品盘点 → 下拉选择货物 ID（仅库存明细表中存在的货物）
- 按批次盘点 → 下拉选择批次 ID（仅 `batch_status = '正常'` 的批次）
- 写入盘点明细表的具体字段（货物ID/库位ID等），见第二部分

**③ 盘点员**
- 下拉框调用 `/user/checkers` 接口（对应职位"仓库员工"）
- 写入 `inventory_check_head.operator_id` 和 `work_task.assignee_id`

**④ 优先级**
- 选项：高、中、低
- 写入 `work_task.priority`

**⑤ 截至时间**
- 日期选择器
- 写入 `work_task.deadline`，格式 `yyyy-MM-ddT23:59:59`

**⑥ 备注**
- 输入框，可选
- 写入 `inventory_check_head.remark` 和 `work_task.remark`

---

### 二、程序生成数据

**① 盘点单号 (check_no)**
- 格式：`P` + `yyyyMMdd` + 3 位流水号
- 例：`P20260715002`（2026年7月15日第2个盘点任务）
- 实现：从 `inventory_check_head` 表中查当天最大流水号 + 1

**② 盘点明细记录**
- 后端根据 `scopeType`（`warehouse` / `goods` / `batch`）调用不同 mapper 方法查询库存记录：
  - `warehouse` → `inventoryMapper.findInventoryByWarehouseId()`
  - `goods` → `inventoryMapper.findInventoryByGoodsId()`
  - `batch` → `inventoryMapper.findInventoryByBatchId()`
- 每条库存记录生成一条盘点明细，包含：
  - `check_no`：盘点单号
  - `warehouse_id` / `zone_id` / `location_id`：从库存记录取
  - `goods_id` / `batch_id`：从库存记录取
  - `book_quantity`：库存记录当前 quantity
  - `actual_quantity` / `difference`：先置空，待完成时填写
  - `detail_status`：初始值 `"正常"`

**③ 工作任务 (work_task)**
- `task_type`：固定 `"库存盘点"`
- `priority` / `deadline` / `remark`：取自用户输入
- `related_order_no`：盘点单号
- `related_batch_id`：涉及的批次号逗号拼接
- `assignee_id`：盘点员 ID
- `source_location_id` / `target_location_id`：置空（盘点不涉及移库）
- `created_time`：当前时间
- `completed_time`：先置空

**④ 操作日志（创建时）**
- `operation_type`：`"盘点"`
- `operation_content`：`"{盘点单号}申请盘点"`，如 `P20260715002申请盘点`
- `operation_result`：`"成功"`
- `operator_id`：当前用户 ID
- `operation_time`：当前时间

---

### 三、我的待办页面

**数据来源：** `GET /workTask/myTasks?assigneeId={userId}`，查 `work_task` 表中 `assignee_id` 匹配且 `completed_time IS NULL` 的记录，按优先级高→低排序。

**表格列：** 单号（related_order_no）、批次号（related_batch_id）、类型（task_type）、优先级（priority）、截至日期（deadline）、操作（"去完成"按钮）

**"去完成"按钮：** 根据 `task_type` 分发到不同完成弹窗：
- `"库存盘点"` → 打开盘点完成弹窗
- `"质检"` → 打开质检完成弹窗
- `"入库"` → 打开入库完成弹窗
- `"库存调整"` → 打开调整完成弹窗
- `"出库"` → 打开出库完成弹窗
- `"处理不合格货物"` → 打开次品处理完成弹窗

---

### 四、完成盘点 — 弹窗

弹窗标题 "完成盘点"，显示盘点单号。

**① 盘点明细表格**
- 通过 `GET /inventoryCheck/details?checkNo={checkNo}` 获取明细
- 每行显示：
  - 货物（goods_id → 货物名称）
  - 库位（location_id）
  - 批次（batch_id）
  - 账面数量（book_quantity，只读）
  - 实盘数量（actual_quantity，输入框）
  - 差异（自动计算 = actual_quantity - book_quantity，绿色正数/红色负数）
  - 状态（自动判定：正常/盘盈/盘亏）

**② 差异自动计算与判定**
- 当盘点员填入实盘数量后，前端实时计算：
  - `差异 = actual_quantity - book_quantity`
  - 差异 > 0 → "盘盈"，差异 < 0 → "盘亏"，差异 = 0 → "正常"

**③ 备注**
- 输入框，可选修改

**④ 提交**
- 点击"提交"按钮后弹出二次确认框
- 调用 `PUT /inventoryCheck/complete/{taskId}`，请求体：
  ```json
  {
    "details": [{ "detailId": 1, "actualQuantity": 100 }, ...],
    "remark": "...",
    "operatorId": 1
  }
  ```

---

### 五、程序自动填充（提交时）

在 `InventoryCheckServiceImpl.completeInventoryCheck()` 中：

- `inventory_check_head.completed_time` → 当前时间
- `inventory_check_head.order_status` → `"已完成"`
- `work_task.completed_time` → 当前时间

---

### 六、完成盘点后的数据更新

**6.1 操作日志（完成时）**
- `operation_type`：`"盘点"`
- `operation_content`：`"{盘点单号}完成盘点"`，盘盈/盘亏时附加 JSON 详情
- `operation_result`：摘要，如 `"正常3项、盘盈1项、盘亏2项"`

**6.2 库存明细表 (inventory)**
- 对每条盘点明细中 `actual_quantity ≠ book_quantity` 的记录：
  - 若 `actual_quantity == 0`：**删除**该库存记录
  - 若 `actual_quantity > 0`：更新 `quantity = actual_quantity`

**6.3 库位表 (location)**
- 当库存记录被删除（quantity 降为 0）时，检查该库位是否还有其他库存
- 若库位空了：`is_occupied` 设为 0，`goods_id` 置空

**6.4 库区表 (zone)**
- 遍历受影响的库区，重新统计空余库位数（`is_occupied = 0` 且 `lock_status ≠ '锁定'` 的库位数量）
- 更新 `zone.available_slots`

**6.5 仓库表 (warehouse)**
- 根据库区汇总，重新计算 `warehouse.available_slots`

**6.6 批次表 (batch)**
- 按批次汇总差异数量，更新 `batch.remaining_quantity`（≥ 0）

**6.7 货物表 (goods)**
- 对受影响的货物，重新汇总所有库存数量
- 更新 `goods.quantity`

---

### 七、后端 API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/inventoryCheck/create` | 创建盘点任务，返回 WorkTask |
| PUT | `/inventoryCheck/complete/{taskId}` | 完成盘点 |
| GET | `/inventoryCheck/myTasks?assigneeId=` | 我的盘点任务 |
| GET | `/inventoryCheck/details?checkNo=` | 盘点明细查询 |

**创建请求体 `CreateInventoryCheckRequest`：**
```json
{
  "checkType": "按仓库盘点",
  "scopeType": "warehouse",
  "scopeValue": "1",
  "operatorId": 1,
  "priority": "高",
  "deadline": "2026-07-20",
  "remark": "备注"
}
```

其中 `scopeType` 支持：`warehouse`、`goods`、`batch`

**完成请求体 `CompleteInventoryCheckRequest`：**
```json
{
  "details": [
    { "detailId": 1, "actualQuantity": 100 }
  ],
  "remark": "备注",
  "operatorId": 1
}
```
