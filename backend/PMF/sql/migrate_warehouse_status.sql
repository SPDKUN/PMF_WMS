-- 仓库表迁移：删除 temperature 字段，新增 status 字段
ALTER TABLE `warehouse`
  DROP COLUMN IF EXISTS `temperature`,
  ADD COLUMN `status` VARCHAR(20) NOT NULL DEFAULT '启用' COMMENT '状态 启用/停用' AFTER `location`;
