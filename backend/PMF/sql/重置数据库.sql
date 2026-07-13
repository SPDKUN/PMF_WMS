-- ============================================
-- WMS 仓储管理系统 — 重置数据库脚本
-- 保留用户信息，重置仓库/库区/库位到初始状态
-- 货物数量清零，清除所有业务数据
-- ============================================

SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 1. 清除所有业务单据数据（按 FK 依赖顺序，子表优先）
-- ============================================

DELETE FROM temperature_humidity_record;
DELETE FROM operation_log;
DELETE FROM defective_handling_detail;
DELETE FROM defective_handling_head;
DELETE FROM quality_check_detail;
DELETE FROM quality_check_head;
DELETE FROM inventory_check_detail;
DELETE FROM inventory_check_head;
DELETE FROM adjustment_order_detail;
DELETE FROM adjustment_order_head;
DELETE FROM outbound_order_detail;
DELETE FROM outbound_order_head;
DELETE FROM inbound_order_detail;
DELETE FROM inbound_order_head;
DELETE FROM work_task;
DELETE FROM inventory;
DELETE FROM batch;

-- ============================================
-- 2. 重置库位：全部标记为空闲、未锁定
-- ============================================
UPDATE location
   SET is_occupied  = 0,
       goods_id     = NULL,
       lock_status  = '未锁定',
       lock_purpose = NULL;

-- ============================================
-- 3. 重置库区空位余量 = 总库位数
-- ============================================
UPDATE zone SET available_slots = total_slots;

-- ============================================
-- 4. 重置库房空位余量 = 总库位数
-- ============================================
UPDATE warehouse SET available_slots = total_slots;

-- ============================================
-- 5. 重置货物数量为 0
-- ============================================
UPDATE goods SET quantity = 0;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 完成
-- user 表数据完整保留
-- warehouse / zone / location 结构保留，状态恢复初始
-- goods 保留，数量归零
-- 其余业务表全部清空
-- ============================================
