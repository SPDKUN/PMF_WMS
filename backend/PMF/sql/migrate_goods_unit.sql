-- 货物表迁移：新增单位字段
ALTER TABLE `goods`
  ADD COLUMN `unit` VARCHAR(20) DEFAULT NULL COMMENT '单位' AFTER `quantity`;
