-- 货物表迁移：删除旧字段，新增数量字段
ALTER TABLE `goods`
  DROP COLUMN `storage_location`,
  DROP COLUMN `expiry_date`,
  DROP COLUMN `batch_no`,
  DROP COLUMN `description`,
  ADD COLUMN `quantity` INT NOT NULL DEFAULT 0 COMMENT '数量' AFTER `storage_condition`;
