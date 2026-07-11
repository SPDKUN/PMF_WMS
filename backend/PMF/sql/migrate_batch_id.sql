-- 批次表迁移：将 batch_id 从 INT AUTO_INCREMENT 改为 VARCHAR 格式（如 B20260711003）
-- 注意：如果其他表（inventory, outbound_order_detail 等）已存在并引用 batch_id，
-- 需要先删除这些表的外键约束，执行此迁移，再重新创建外键。

ALTER TABLE `batch`
  MODIFY COLUMN `batch_id` VARCHAR(20) NOT NULL COMMENT '批次号',
  DROP PRIMARY KEY,
  ADD PRIMARY KEY (`batch_id`);
