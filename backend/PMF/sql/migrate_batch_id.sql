-- 批次表迁移：将 batch_id 从 INT AUTO_INCREMENT 改为 VARCHAR 格式（如 B20260711003）

-- 先查一下所有引用 batch 表的外键名称（如报错可据此调整）
-- SELECT TABLE_NAME, CONSTRAINT_NAME FROM INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS WHERE REFERENCED_TABLE_NAME = 'batch';

-- 1. 删除子表外键约束
ALTER TABLE `adjustment_order_detail`  DROP FOREIGN KEY `fk_adjustment_detail_batch`;
ALTER TABLE `defective_handling_detail` DROP FOREIGN KEY `fk_defective_detail_batch`;
ALTER TABLE `inventory`                DROP FOREIGN KEY `fk_inventory_batch`;
ALTER TABLE `inventory_check_detail`   DROP FOREIGN KEY `fk_check_detail_batch`;
ALTER TABLE `outbound_order_detail`    DROP FOREIGN KEY `fk_outbound_detail_batch`;
ALTER TABLE `quality_check_detail`     DROP FOREIGN KEY `fk_quality_detail_batch`;
ALTER TABLE `inspectionreport`         DROP FOREIGN KEY IF EXISTS `fk_inspectionreport_batch`;
ALTER TABLE `inventoryrecord`          DROP FOREIGN KEY IF EXISTS `fk_inventoryrecord_batch`;

-- 2. 修改子表 batch_id 列类型
ALTER TABLE `adjustment_order_detail`  MODIFY COLUMN `batch_id` VARCHAR(20) NOT NULL COMMENT '批次ID';
ALTER TABLE `defective_handling_detail` MODIFY COLUMN `batch_id` VARCHAR(20) NOT NULL COMMENT '批次ID';
ALTER TABLE `inventory`                MODIFY COLUMN `batch_id` VARCHAR(20) NOT NULL COMMENT '批次ID';
ALTER TABLE `inventory_check_detail`   MODIFY COLUMN `batch_id` VARCHAR(20) NOT NULL COMMENT '批次ID';
ALTER TABLE `outbound_order_detail`    MODIFY COLUMN `batch_id` VARCHAR(20) NOT NULL COMMENT '批次ID';
ALTER TABLE `quality_check_detail`     MODIFY COLUMN `batch_id` VARCHAR(20) NOT NULL COMMENT '批次ID';
ALTER TABLE `inspectionreport`         MODIFY COLUMN `batch_id` VARCHAR(20) NOT NULL COMMENT '批次ID';
ALTER TABLE `inventoryrecord`          MODIFY COLUMN `batch_id` VARCHAR(20) NOT NULL COMMENT '批次ID';

-- 3. 修改主表 batch_id 列类型
ALTER TABLE `batch`
  MODIFY COLUMN `batch_id` VARCHAR(20) NOT NULL COMMENT '批次号',
  DROP PRIMARY KEY,
  ADD PRIMARY KEY (`batch_id`);

-- 4. 重建子表外键约束
ALTER TABLE `adjustment_order_detail`  ADD CONSTRAINT `fk_adjustment_detail_batch`  FOREIGN KEY (`batch_id`) REFERENCES `batch` (`batch_id`);
ALTER TABLE `defective_handling_detail` ADD CONSTRAINT `fk_defective_detail_batch` FOREIGN KEY (`batch_id`) REFERENCES `batch` (`batch_id`);
ALTER TABLE `inventory`                ADD CONSTRAINT `fk_inventory_batch`         FOREIGN KEY (`batch_id`) REFERENCES `batch` (`batch_id`);
ALTER TABLE `inventory_check_detail`   ADD CONSTRAINT `fk_check_detail_batch`     FOREIGN KEY (`batch_id`) REFERENCES `batch` (`batch_id`);
ALTER TABLE `outbound_order_detail`    ADD CONSTRAINT `fk_outbound_detail_batch`  FOREIGN KEY (`batch_id`) REFERENCES `batch` (`batch_id`);
ALTER TABLE `quality_check_detail`     ADD CONSTRAINT `fk_quality_detail_batch`   FOREIGN KEY (`batch_id`) REFERENCES `batch` (`batch_id`);
ALTER TABLE `inspectionreport`         ADD CONSTRAINT `fk_inspectionreport_batch` FOREIGN KEY (`batch_id`) REFERENCES `batch` (`batch_id`);
ALTER TABLE `inventoryrecord`          ADD CONSTRAINT `fk_inventoryrecord_batch`  FOREIGN KEY (`batch_id`) REFERENCES `batch` (`batch_id`);
