/**
 * PMF 仓储管理系统 - API 接口汇总
 */

const { get, post, put, del } = require('./request')

// ==================== 认证模块 ====================

/** 登录 */
function login(username, password) {
  return get('/auth/login', { username, password })
}

// ==================== 用户模块 ====================

/** 获取用户列表 */
function getUserList() {
  return get('/user/personnel')
}

/** 获取用户详情 */
function getUserById(userId) {
  return get('/user/' + userId)
}

/** 创建用户 */
function createUser(data) {
  return post('/user', data)
}

/** 更新用户 */
function updateUser(data) {
  return put('/user', data)
}

/** 删除用户 */
function deleteUser(id) {
  return del('/user/' + id)
}

/** 更新个人资料 */
function updateProfile(data) {
  return put('/user/profile', data)
}

// ==================== 仓库模块 ====================

/** 获取仓库列表 */
function getWarehouseList() {
  return get('/warehouse/list')
}

/** 获取仓库详情 */
function getWarehouseById(id) {
  return get('/warehouse/' + id)
}

/** 创建仓库 */
function createWarehouse(data) {
  return post('/warehouse', data)
}

/** 更新仓库 */
function updateWarehouse(data) {
  return put('/warehouse', data)
}

/** 删除仓库 */
function deleteWarehouse(id) {
  return del('/warehouse/' + id)
}

// ==================== 库区模块 ====================

/** 获取库区列表 */
function getZoneList(warehouseId) {
  return get('/zone/list', { warehouseId: warehouseId || '' })
}

/** 获取库区详情 */
function getZoneById(id) {
  return get('/zone/' + id)
}

// ==================== 库位模块 ====================

/** 获取库位列表 */
function getLocationList(zoneId) {
  return get('/location/list', { zoneId: zoneId || '' })
}

/** 获取库位详情 */
function getLocationById(id) {
  return get('/location/' + id)
}

// ==================== 货物模块 ====================

/** 获取货物列表 */
function getGoodsList() {
  return get('/goods/list')
}

/** 获取货物详情 */
function getGoodsById(id) {
  return get('/goods/' + id)
}

/** 创建货物 */
function createGoods(data) {
  return post('/goods', data)
}

/** 更新货物 */
function updateGoods(data) {
  return put('/goods', data)
}

/** 删除货物 */
function deleteGoods(id) {
  return del('/goods/' + id)
}

// ==================== 批次模块 ====================

/** 获取批次列表 */
function getBatchList() {
  return get('/batch/list')
}

/** 创建批次 */
function createBatch(data) {
  return post('/batch', data)
}

// ==================== 库存模块 ====================

/** 获取库存明细 */
function getInventoryList(goodsName, locationId) {
  return get('/inventory/listWithDetails', {
    goodsName: goodsName || '',
    locationId: locationId || ''
  })
}

// ==================== 操作日志模块 ====================

/** 获取操作日志 */
function getOperationLogList(date, operationType) {
  return get('/operationLog/list', {
    date: date || '',
    operationType: operationType || ''
  })
}

// ==================== 温湿度记录模块 ====================

/** 获取温湿度记录 */
function getTemperatureList() {
  return get('/temperatureHumidityRecord/list')
}

// ==================== 工作任务模块 ====================

/** 获取工作任务列表 */
function getWorkTaskList() {
  return get('/workTask/list')
}

/** 创建工作任务 */
function createWorkTask(data) {
  return post('/workTask', data)
}

/** 更新工作任务 */
function updateWorkTask(data) {
  return put('/workTask', data)
}

/** 删除工作任务 */
function deleteWorkTask(id) {
  return del('/workTask/' + id)
}

// ==================== 入库单模块 ====================

/** 获取入库单头列表 */
function getInboundHeadList() {
  return get('/inboundOrderHead/list')
}

/** 创建入库单头 */
function createInboundHead(data) {
  return post('/inboundOrderHead', data)
}

/** 获取入库单明细 */
function getInboundDetailList() {
  return get('/inboundOrderDetail/list')
}

/** 创建入库单明细 */
function createInboundDetail(data) {
  return post('/inboundOrderDetail', data)
}

// ==================== 出库单模块 ====================

/** 获取出库单头列表 */
function getOutboundHeadList() {
  return get('/outboundOrderHead/list')
}

/** 创建出库单头 */
function createOutboundHead(data) {
  return post('/outboundOrderHead', data)
}

// ==================== 调整单模块 ====================

/** 获取调整单头列表 */
function getAdjustmentHeadList() {
  return get('/adjustmentOrderHead/list')
}

/** 创建调整单头 */
function createAdjustmentHead(data) {
  return post('/adjustmentOrderHead', data)
}

// ==================== 盘点单模块 ====================

/** 获取盘点单头列表 */
function getInventoryCheckHeadList() {
  return get('/inventoryCheckHead/list')
}

/** 创建盘点单头 */
function createInventoryCheckHead(data) {
  return post('/inventoryCheckHead', data)
}

// ==================== 质检单模块 ====================

/** 获取质检单头列表 */
function getQualityCheckHeadList() {
  return get('/qualityCheckHead/list')
}

/** 创建质检单头 */
function createQualityCheckHead(data) {
  return post('/qualityCheckHead', data)
}

// ==================== 残次品处理单模块 ====================

/** 获取残次品处理单头列表 */
function getDefectiveHeadList() {
  return get('/defectiveHandlingHead/list')
}

/** 创建残次品处理单头 */
function createDefectiveHead(data) {
  return post('/defectiveHandlingHead', data)
}

module.exports = {
  login,
  getUserList, getUserById, createUser, updateUser, deleteUser, updateProfile,
  getWarehouseList, getWarehouseById, createWarehouse, updateWarehouse, deleteWarehouse,
  getZoneList, getZoneById,
  getLocationList, getLocationById,
  getGoodsList, getGoodsById, createGoods, updateGoods, deleteGoods,
  getBatchList, createBatch,
  getInventoryList,
  getOperationLogList,
  getTemperatureList,
  getWorkTaskList, createWorkTask, updateWorkTask, deleteWorkTask,
  getInboundHeadList, createInboundHead, getInboundDetailList, createInboundDetail,
  getOutboundHeadList, createOutboundHead,
  getAdjustmentHeadList, createAdjustmentHead,
  getInventoryCheckHeadList, createInventoryCheckHead,
  getQualityCheckHeadList, createQualityCheckHead,
  getDefectiveHeadList, createDefectiveHead
}
