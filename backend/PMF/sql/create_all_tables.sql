-- ============================================
-- WMS 仓储管理系统 — 全部建表 SQL
-- Schema: pmf
-- 按外键依赖顺序创建
-- ============================================

-- 1. 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `user_id`       INT           NOT NULL AUTO_INCREMENT  COMMENT '用户ID',
  `username`      VARCHAR(50)   NOT NULL                 COMMENT '登录账号',
  `real_name`     VARCHAR(50)   DEFAULT NULL             COMMENT '姓名',
  `phone`         VARCHAR(20)   DEFAULT NULL             COMMENT '电话',
  `department`    VARCHAR(50)   DEFAULT NULL             COMMENT '所属部门',
  `position`      VARCHAR(50)   DEFAULT NULL             COMMENT '职位',
  `password`      VARCHAR(255)  NOT NULL                 COMMENT '密码',
  `status`        TINYINT       NOT NULL DEFAULT 1       COMMENT '状态 0禁用 1启用',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 货物表
CREATE TABLE IF NOT EXISTS `goods` (
  `goods_id`           INT           NOT NULL AUTO_INCREMENT  COMMENT '货物ID',
  `goods_code`         VARCHAR(50)   NOT NULL                 COMMENT '货物编码',
  `category`           ENUM('原料','成品') NOT NULL           COMMENT '种类',
  `goods_name`         VARCHAR(100)  NOT NULL                 COMMENT '名称',
  `storage_condition`  ENUM('常温','冷冻','恒温') NOT NULL   COMMENT '存储条件',
  `quantity`           INT           NOT NULL DEFAULT 0       COMMENT '数量',
  `unit`               VARCHAR(20)   DEFAULT NULL             COMMENT '单位',
  PRIMARY KEY (`goods_id`),
  UNIQUE KEY `uk_goods_code` (`goods_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='货物表';

-- 3. 库房表
CREATE TABLE IF NOT EXISTS `warehouse` (
  `warehouse_id`     INT           NOT NULL AUTO_INCREMENT  COMMENT '库房ID',
  `warehouse_name`   VARCHAR(100)  NOT NULL                 COMMENT '库房名称',
  `warehouse_type`   ENUM('一般仓库','冷冻库','恒温库') NOT NULL COMMENT '库房种类',
  `warehouse_size`   ENUM('大','中','小') DEFAULT NULL     COMMENT '库房大小',
  `total_slots`      INT           NOT NULL DEFAULT 0       COMMENT '总库位数',
  `available_slots`  INT           NOT NULL DEFAULT 0       COMMENT '空位余量',
  `location`         VARCHAR(200)  DEFAULT NULL             COMMENT '库房位置',
  `status`           VARCHAR(20)   NOT NULL DEFAULT '启用'   COMMENT '状态 启用/停用',
  `description`      TEXT          DEFAULT NULL             COMMENT '库房描述',
  PRIMARY KEY (`warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库房表';

-- 4. 库区表
CREATE TABLE IF NOT EXISTS `zone` (
  `zone_id`         INT           NOT NULL AUTO_INCREMENT  COMMENT '库区ID',
  `zone_name`       VARCHAR(100)  NOT NULL                 COMMENT '库区名称',
  `warehouse_id`    INT           NOT NULL                 COMMENT '所属库房ID',
  `total_slots`     INT           NOT NULL DEFAULT 0       COMMENT '总库位数',
  `available_slots` INT           NOT NULL DEFAULT 0       COMMENT '空位余量',
  `description`     TEXT          DEFAULT NULL             COMMENT '库区描述',
  PRIMARY KEY (`zone_id`),
  KEY `idx_zone_warehouse` (`warehouse_id`),
  CONSTRAINT `fk_zone_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse` (`warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库区表';

-- 5. 库位表
CREATE TABLE IF NOT EXISTS `location` (
  `location_id`    INT           NOT NULL AUTO_INCREMENT  COMMENT '库位ID',
  `location_name`  VARCHAR(100)  NOT NULL                 COMMENT '库位名称',
  `zone_id`        INT           NOT NULL                 COMMENT '所属库区ID',
  `goods_id`       INT           DEFAULT NULL             COMMENT '存储货物ID',
  `is_occupied`    TINYINT       NOT NULL DEFAULT 0       COMMENT '是否被占用 0否 1是',
  `lock_status`    ENUM('未锁定','锁定') NOT NULL DEFAULT '未锁定' COMMENT '锁定状态',
  `lock_purpose`   ENUM('出库','调整')   DEFAULT NULL             COMMENT '锁定用途（仅lock_status=锁定时有效）',
  `description`    TEXT          DEFAULT NULL             COMMENT '库位描述',
  PRIMARY KEY (`location_id`),
  KEY `idx_location_zone` (`zone_id`),
  KEY `idx_location_goods` (`goods_id`),
  CONSTRAINT `fk_location_zone`  FOREIGN KEY (`zone_id`)  REFERENCES `zone` (`zone_id`),
  CONSTRAINT `fk_location_goods` FOREIGN KEY (`goods_id`) REFERENCES `goods` (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库位表';

-- 6. 批次单
CREATE TABLE IF NOT EXISTS `batch` (
  `batch_id`           VARCHAR(20)   NOT NULL                 COMMENT '批次号（如 B20260711003）',
  `goods_id`           INT           NOT NULL                 COMMENT '货物ID',
  `production_date`    DATE          NOT NULL                 COMMENT '生成日期',
  `expiry_date`        DATE          NOT NULL                 COMMENT '到期日期',
  `initial_quantity`   INT           NOT NULL DEFAULT 0       COMMENT '入库初始数量',
  `remaining_quantity` INT           NOT NULL DEFAULT 0       COMMENT '当前剩余总数量',
  `batch_status`       ENUM('正常','待检','锁定','报废') NOT NULL DEFAULT '待检' COMMENT '批次状态',
  PRIMARY KEY (`batch_id`),
  KEY `idx_batch_goods` (`goods_id`),
  CONSTRAINT `fk_batch_goods` FOREIGN KEY (`goods_id`) REFERENCES `goods` (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='批次单';

-- 7. 库存明细表
CREATE TABLE IF NOT EXISTS `inventory` (
  `inventory_id`     INT           NOT NULL AUTO_INCREMENT  COMMENT '库存明细ID',
  `goods_id`         INT           NOT NULL                 COMMENT '货物ID',
  `batch_id`         VARCHAR(20)   NOT NULL                 COMMENT '批次ID',
  `warehouse_id`     INT           DEFAULT NULL             COMMENT '实际所在库房ID',
  `zone_id`          INT           DEFAULT NULL             COMMENT '实际所在库区ID',
  `location_id`      INT           DEFAULT NULL             COMMENT '实际所在库位ID',
  `quantity`         INT           NOT NULL DEFAULT 0       COMMENT '当前数量（此批次在此库位的数量）',
  `locked_quantity`  INT           NOT NULL DEFAULT 0       COMMENT '锁定数量（已分配任务但未出库）',
  `inventory_status` ENUM('正常','待入库','待出库','待报废') NOT NULL DEFAULT '正常' COMMENT '库存状态',
  PRIMARY KEY (`inventory_id`),
  KEY `idx_inventory_goods`   (`goods_id`),
  KEY `idx_inventory_batch`   (`batch_id`),
  KEY `idx_inventory_wh`      (`warehouse_id`),
  KEY `idx_inventory_zone`    (`zone_id`),
  KEY `idx_inventory_loc`     (`location_id`),
  CONSTRAINT `fk_inventory_goods`     FOREIGN KEY (`goods_id`)     REFERENCES `goods` (`goods_id`),
  CONSTRAINT `fk_inventory_batch`     FOREIGN KEY (`batch_id`)     REFERENCES `batch` (`batch_id`),
  CONSTRAINT `fk_inventory_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse` (`warehouse_id`),
  CONSTRAINT `fk_inventory_zone`      FOREIGN KEY (`zone_id`)      REFERENCES `zone` (`zone_id`),
  CONSTRAINT `fk_inventory_location`  FOREIGN KEY (`location_id`)  REFERENCES `location` (`location_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存明细表';

-- 8. 工作任务表
CREATE TABLE IF NOT EXISTS `work_task` (
  `task_id`             INT           NOT NULL AUTO_INCREMENT  COMMENT '任务ID',
  `task_type`           ENUM('入库','出库','库存调整','库存盘点','质检','处理不合格货物') NOT NULL COMMENT '任务种类',
  `priority`            ENUM('高','中','低') NOT NULL DEFAULT '中' COMMENT '优先级',
  `related_order_no`    VARCHAR(100)  DEFAULT NULL             COMMENT '关联单号',
  `related_batch_id`    VARCHAR(100)  DEFAULT NULL             COMMENT '相关批次号',
  `goods_id`            INT           DEFAULT NULL             COMMENT '相关货物ID',
  `source_location_id`  INT           DEFAULT NULL             COMMENT '源库位ID（出库/调整用）',
  `target_location_id`  INT           DEFAULT NULL             COMMENT '目标库位ID（入库/调整用）',
  `assignee_id`         INT           DEFAULT NULL             COMMENT '负责人ID',
  `deadline`            DATETIME      DEFAULT NULL             COMMENT '截至时间',
  `created_time`        DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `completed_time`      DATETIME      DEFAULT NULL             COMMENT '实际完成时间',
  `remark`              TEXT          DEFAULT NULL             COMMENT '备注',
  PRIMARY KEY (`task_id`),
  KEY `idx_task_goods`   (`goods_id`),
  KEY `idx_task_assignee` (`assignee_id`),
  KEY `idx_task_src_loc` (`source_location_id`),
  KEY `idx_task_tgt_loc` (`target_location_id`),
  CONSTRAINT `fk_task_goods`   FOREIGN KEY (`goods_id`)   REFERENCES `goods` (`goods_id`),
  CONSTRAINT `fk_task_assignee` FOREIGN KEY (`assignee_id`) REFERENCES `user` (`user_id`),
  CONSTRAINT `fk_task_src_loc` FOREIGN KEY (`source_location_id`) REFERENCES `location` (`location_id`),
  CONSTRAINT `fk_task_tgt_loc` FOREIGN KEY (`target_location_id`) REFERENCES `location` (`location_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作任务表';

-- 9. 入库单头表
CREATE TABLE IF NOT EXISTS `inbound_order_head` (
  `inbound_no`    VARCHAR(50)   NOT NULL                 COMMENT '入库单号',
  `inbound_type`  ENUM('采购入库','退货入库','生产入库') NOT NULL COMMENT '入库类型',
  `order_status`  ENUM('草稿','已审核','已完成') NOT NULL DEFAULT '草稿' COMMENT '状态',
  `operator_id`   INT           NOT NULL                 COMMENT '操作人ID',
  `inbound_time`  DATETIME      DEFAULT NULL             COMMENT '入库时间',
  `remark`        TEXT          DEFAULT NULL             COMMENT '备注',
  PRIMARY KEY (`inbound_no`),
  KEY `idx_inbound_head_operator` (`operator_id`),
  CONSTRAINT `fk_inbound_head_operator` FOREIGN KEY (`operator_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库单头表';

-- 10. 入库单明细表
CREATE TABLE IF NOT EXISTS `inbound_order_detail` (
  `detail_id`       INT           NOT NULL AUTO_INCREMENT  COMMENT '明细ID',
  `inbound_no`      VARCHAR(50)   NOT NULL                 COMMENT '入库单号',
  `goods_id`        INT           NOT NULL                 COMMENT '货物ID',
  `batch_no`        VARCHAR(50)   DEFAULT NULL             COMMENT '批次号',
  `production_date` DATE          DEFAULT NULL             COMMENT '生产日期',
  `expiry_date`     DATE          DEFAULT NULL             COMMENT '到期日期',
  `quantity`        INT           NOT NULL DEFAULT 0       COMMENT '数量',
  `warehouse_id`    INT           DEFAULT NULL             COMMENT '实际入库库房ID',
  `zone_id`         INT           DEFAULT NULL             COMMENT '实际入库库区ID',
  `location_id`     INT           DEFAULT NULL             COMMENT '实际入库库位ID',
  PRIMARY KEY (`detail_id`),
  KEY `idx_inbound_detail_head` (`inbound_no`),
  KEY `idx_inbound_detail_goods` (`goods_id`),
  CONSTRAINT `fk_inbound_detail_head`  FOREIGN KEY (`inbound_no`)   REFERENCES `inbound_order_head` (`inbound_no`),
  CONSTRAINT `fk_inbound_detail_goods` FOREIGN KEY (`goods_id`)     REFERENCES `goods` (`goods_id`),
  CONSTRAINT `fk_inbound_detail_wh`    FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse` (`warehouse_id`),
  CONSTRAINT `fk_inbound_detail_zone`  FOREIGN KEY (`zone_id`)      REFERENCES `zone` (`zone_id`),
  CONSTRAINT `fk_inbound_detail_loc`   FOREIGN KEY (`location_id`)  REFERENCES `location` (`location_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库单明细表';

-- 11. 出库单头表
CREATE TABLE IF NOT EXISTS `outbound_order_head` (
  `outbound_no`    VARCHAR(50)   NOT NULL                 COMMENT '出库单号',
  `outbound_type`  ENUM('销售出库','领料出库','报废出库') NOT NULL COMMENT '出库类型',
  `order_status`   ENUM('草稿','已审核','已完成') NOT NULL DEFAULT '草稿' COMMENT '单据状态',
  `operator_id`    INT           NOT NULL                 COMMENT '操作人ID',
  `outbound_time`  DATETIME      DEFAULT NULL             COMMENT '出库时间',
  `remark`         TEXT          DEFAULT NULL             COMMENT '备注',
  PRIMARY KEY (`outbound_no`),
  KEY `idx_outbound_head_operator` (`operator_id`),
  CONSTRAINT `fk_outbound_head_operator` FOREIGN KEY (`operator_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库单头表';

-- 12. 出库单明细表
CREATE TABLE IF NOT EXISTS `outbound_order_detail` (
  `detail_id`     INT           NOT NULL AUTO_INCREMENT  COMMENT '明细ID',
  `outbound_no`   VARCHAR(50)   NOT NULL                 COMMENT '出库单号',
  `goods_id`      INT           NOT NULL                 COMMENT '货物ID',
  `batch_id`      VARCHAR(20)   NOT NULL                 COMMENT '批次ID',
  `quantity`      INT           NOT NULL DEFAULT 0       COMMENT '数量',
  `warehouse_id`  INT           DEFAULT NULL             COMMENT '实际出库库房ID',
  `zone_id`       INT           DEFAULT NULL             COMMENT '实际出库库区ID',
  `location_id`   INT           DEFAULT NULL             COMMENT '实际出库库位ID',
  PRIMARY KEY (`detail_id`),
  KEY `idx_outbound_detail_head` (`outbound_no`),
  KEY `idx_outbound_detail_goods` (`goods_id`),
  KEY `idx_outbound_detail_batch` (`batch_id`),
  CONSTRAINT `fk_outbound_detail_head`  FOREIGN KEY (`outbound_no`)   REFERENCES `outbound_order_head` (`outbound_no`),
  CONSTRAINT `fk_outbound_detail_goods` FOREIGN KEY (`goods_id`)     REFERENCES `goods` (`goods_id`),
  CONSTRAINT `fk_outbound_detail_batch` FOREIGN KEY (`batch_id`)     REFERENCES `batch` (`batch_id`),
  CONSTRAINT `fk_outbound_detail_wh`    FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse` (`warehouse_id`),
  CONSTRAINT `fk_outbound_detail_zone`  FOREIGN KEY (`zone_id`)      REFERENCES `zone` (`zone_id`),
  CONSTRAINT `fk_outbound_detail_loc`   FOREIGN KEY (`location_id`)  REFERENCES `location` (`location_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库单明细表';

-- 13. 库存调整单头表
CREATE TABLE IF NOT EXISTS `adjustment_order_head` (
  `adjustment_no`   VARCHAR(50)   NOT NULL                 COMMENT '调整单号',
  `adjustment_type` ENUM('库位移库','批次调整','数量修正') NOT NULL COMMENT '调整类型',
  `order_status`    ENUM('草稿','已审核','已完成') NOT NULL DEFAULT '草稿' COMMENT '单据状态',
  `operator_id`     INT           NOT NULL                 COMMENT '操作人ID',
  `adjustment_time` DATETIME      DEFAULT NULL             COMMENT '调整时间',
  PRIMARY KEY (`adjustment_no`),
  KEY `idx_adjustment_head_operator` (`operator_id`),
  CONSTRAINT `fk_adjustment_head_operator` FOREIGN KEY (`operator_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存调整单头表';

-- 14. 库存调整单明细表
CREATE TABLE IF NOT EXISTS `adjustment_order_detail` (
  `detail_id`         INT           NOT NULL AUTO_INCREMENT  COMMENT '明细ID',
  `adjustment_no`     VARCHAR(50)   NOT NULL                 COMMENT '调整单号',
  `goods_id`          INT           NOT NULL                 COMMENT '货物ID',
  `batch_id`          VARCHAR(20)   NOT NULL                 COMMENT '批次ID',
  `quantity`          INT           NOT NULL DEFAULT 0       COMMENT '调整数量',
  `from_warehouse_id` INT           DEFAULT NULL             COMMENT '原库房ID',
  `from_zone_id`      INT           DEFAULT NULL             COMMENT '原库区ID',
  `from_location_id`  INT           DEFAULT NULL             COMMENT '原库位ID',
  `to_warehouse_id`   INT           DEFAULT NULL             COMMENT '新库房ID',
  `to_zone_id`        INT           DEFAULT NULL             COMMENT '新库区ID',
  `to_location_id`    INT           DEFAULT NULL             COMMENT '新库位ID',
  PRIMARY KEY (`detail_id`),
  KEY `idx_adjustment_detail_head` (`adjustment_no`),
  KEY `idx_adjustment_detail_goods` (`goods_id`),
  CONSTRAINT `fk_adjustment_detail_head`  FOREIGN KEY (`adjustment_no`) REFERENCES `adjustment_order_head` (`adjustment_no`),
  CONSTRAINT `fk_adjustment_detail_goods` FOREIGN KEY (`goods_id`)     REFERENCES `goods` (`goods_id`),
  CONSTRAINT `fk_adjustment_detail_batch` FOREIGN KEY (`batch_id`)     REFERENCES `batch` (`batch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存调整单明细表';

-- 15. 盘点单头表
CREATE TABLE IF NOT EXISTS `inventory_check_head` (
  `check_no`       VARCHAR(50)   NOT NULL                 COMMENT '盘点单号',
  `check_type`     ENUM('全盘','抽盘','动碰盘点') NOT NULL COMMENT '盘点类型',
  `order_status`   ENUM('草稿','进行中','已完成') NOT NULL DEFAULT '草稿' COMMENT '单据状态',
  `operator_id`    INT           NOT NULL                 COMMENT '操作人ID',
  `created_time`   DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `completed_time` DATETIME      DEFAULT NULL             COMMENT '盘点完成时间',
  `remark`         TEXT          DEFAULT NULL             COMMENT '备注',
  PRIMARY KEY (`check_no`),
  KEY `idx_check_head_operator` (`operator_id`),
  CONSTRAINT `fk_check_head_operator` FOREIGN KEY (`operator_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盘点单头表';

-- 16. 盘点单明细表
CREATE TABLE IF NOT EXISTS `inventory_check_detail` (
  `detail_id`       INT           NOT NULL AUTO_INCREMENT  COMMENT '明细ID',
  `check_no`        VARCHAR(50)   NOT NULL                 COMMENT '盘点单号',
  `warehouse_id`    INT           NOT NULL                 COMMENT '盘点库房ID',
  `zone_id`         INT           DEFAULT NULL             COMMENT '盘点库区ID（可为空）',
  `location_id`     INT           DEFAULT NULL             COMMENT '盘点库位ID（可为空）',
  `goods_id`        INT           NOT NULL                 COMMENT '货物ID',
  `batch_id`        VARCHAR(20)   NOT NULL                 COMMENT '批次ID',
  `book_quantity`   INT           NOT NULL DEFAULT 0       COMMENT '账面数量',
  `actual_quantity` INT           DEFAULT NULL             COMMENT '实盘数量',
  `difference`      INT           DEFAULT NULL             COMMENT '差异（实盘 - 账面）',
  `detail_status`   ENUM('正常','盘盈','盘亏') DEFAULT '正常' COMMENT '明细状态',
  PRIMARY KEY (`detail_id`),
  KEY `idx_check_detail_head` (`check_no`),
  KEY `idx_check_detail_wh`   (`warehouse_id`),
  KEY `idx_check_detail_goods` (`goods_id`),
  CONSTRAINT `fk_check_detail_head`  FOREIGN KEY (`check_no`)     REFERENCES `inventory_check_head` (`check_no`),
  CONSTRAINT `fk_check_detail_wh`    FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse` (`warehouse_id`),
  CONSTRAINT `fk_check_detail_goods` FOREIGN KEY (`goods_id`)    REFERENCES `goods` (`goods_id`),
  CONSTRAINT `fk_check_detail_batch` FOREIGN KEY (`batch_id`)    REFERENCES `batch` (`batch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盘点单明细表';

-- 17. 质检单头表
CREATE TABLE IF NOT EXISTS `quality_check_head` (
  `quality_check_no` VARCHAR(50)   NOT NULL                 COMMENT '质检单号',
  `check_type`       ENUM('来料检','成品检','日常抽检') NOT NULL COMMENT '质检类型',
  `order_status`     ENUM('待检','进行中','已完成') NOT NULL DEFAULT '待检' COMMENT '单据状态',
  `inspector_id`     INT           NOT NULL                 COMMENT '质检员ID',
  `inspection_time`  DATETIME      DEFAULT NULL             COMMENT '质检时间',
  `remark`           TEXT          DEFAULT NULL             COMMENT '备注',
  PRIMARY KEY (`quality_check_no`),
  KEY `idx_quality_head_inspector` (`inspector_id`),
  CONSTRAINT `fk_quality_head_inspector` FOREIGN KEY (`inspector_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='质检单头表';

-- 18. 质检单明细表
CREATE TABLE IF NOT EXISTS `quality_check_detail` (
  `detail_id`          INT           NOT NULL AUTO_INCREMENT  COMMENT '明细ID',
  `quality_check_no`   VARCHAR(50)   NOT NULL                 COMMENT '质检单号',
  `goods_id`           INT           NOT NULL                 COMMENT '货物ID',
  `batch_id`           VARCHAR(100)  NOT NULL                 COMMENT '批次ID',
  `inspection_result`  VARCHAR(50)   DEFAULT NULL             COMMENT '质检结果',
  `defect_reason`      TEXT          DEFAULT NULL             COMMENT '不合格原因描述',
  `handling_suggestion` VARCHAR(100) DEFAULT NULL             COMMENT '处理建议（如报废、退货、降级）',
  PRIMARY KEY (`detail_id`),
  KEY `idx_quality_detail_head` (`quality_check_no`),
  KEY `idx_quality_detail_goods` (`goods_id`),
  CONSTRAINT `fk_quality_detail_head`  FOREIGN KEY (`quality_check_no`) REFERENCES `quality_check_head` (`quality_check_no`),
  CONSTRAINT `fk_quality_detail_goods` FOREIGN KEY (`goods_id`)       REFERENCES `goods` (`goods_id`),
  CONSTRAINT `fk_quality_detail_batch` FOREIGN KEY (`batch_id`)       REFERENCES `batch` (`batch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='质检单明细表';

-- 19. 不合格处理单头表
CREATE TABLE IF NOT EXISTS `defective_handling_head` (
  `handling_no`   VARCHAR(50)   NOT NULL                 COMMENT '处理单号',
  `handling_type` ENUM('报废','退货','销毁','降级使用') NOT NULL COMMENT '处理方式',
  `order_status`  ENUM('待处理','已完成') NOT NULL DEFAULT '待处理' COMMENT '单据状态',
  `operator_id`   INT           NOT NULL                 COMMENT '操作人ID',
  `handling_time` DATETIME      DEFAULT NULL             COMMENT '处理时间',
  `remark`        TEXT          DEFAULT NULL             COMMENT '备注',
  PRIMARY KEY (`handling_no`),
  KEY `idx_defective_head_operator` (`operator_id`),
  CONSTRAINT `fk_defective_head_operator` FOREIGN KEY (`operator_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='不合格处理单头表';

-- 20. 不合格处理单明细表
CREATE TABLE IF NOT EXISTS `defective_handling_detail` (
  `detail_id`           INT           NOT NULL AUTO_INCREMENT  COMMENT '明细ID',
  `handling_no`         VARCHAR(50)   NOT NULL                 COMMENT '处理单号',
  `goods_id`            INT           NOT NULL                 COMMENT '货物ID',
  `batch_id`            VARCHAR(20)   NOT NULL                 COMMENT '批次ID',
  `quantity`            INT           NOT NULL DEFAULT 0       COMMENT '处理数量',
  `source_location_id`  INT           DEFAULT NULL             COMMENT '原库位ID',
  `target_location_id`  INT           DEFAULT NULL             COMMENT '目标库位ID（若报废移至报废区）',
  PRIMARY KEY (`detail_id`),
  KEY `idx_defective_detail_head` (`handling_no`),
  KEY `idx_defective_detail_goods` (`goods_id`),
  CONSTRAINT `fk_defective_detail_head`  FOREIGN KEY (`handling_no`)        REFERENCES `defective_handling_head` (`handling_no`),
  CONSTRAINT `fk_defective_detail_goods` FOREIGN KEY (`goods_id`)           REFERENCES `goods` (`goods_id`),
  CONSTRAINT `fk_defective_detail_batch` FOREIGN KEY (`batch_id`)           REFERENCES `batch` (`batch_id`),
  CONSTRAINT `fk_defective_detail_sloc`  FOREIGN KEY (`source_location_id`) REFERENCES `location` (`location_id`),
  CONSTRAINT `fk_defective_detail_tloc`  FOREIGN KEY (`target_location_id`) REFERENCES `location` (`location_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='不合格处理单明细表';

-- 21. 温湿度记录表
CREATE TABLE IF NOT EXISTS `temperature_humidity_record` (
  `record_id`    INT           NOT NULL AUTO_INCREMENT  COMMENT '记录ID',
  `warehouse_id` INT           NOT NULL                 COMMENT '库房ID',
  `temperature`  DECIMAL(5,2)  DEFAULT NULL             COMMENT '温度（℃）',
  `humidity`     DECIMAL(5,2)  DEFAULT NULL             COMMENT '湿度（%）',
  `recorded_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
  PRIMARY KEY (`record_id`),
  KEY `idx_thr_warehouse` (`warehouse_id`),
  KEY `idx_thr_time` (`recorded_time`),
  CONSTRAINT `fk_thr_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse` (`warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='温湿度记录表';

-- 22. 操作日志表
CREATE TABLE IF NOT EXISTS `operation_log` (
  `log_id`          INT           NOT NULL AUTO_INCREMENT  COMMENT '日志ID',
  `operator_id`     INT           NOT NULL                 COMMENT '操作人ID',
  `operation_type`  VARCHAR(50)   NOT NULL                 COMMENT '操作类型（增、删、改、审、盘点等）',
  `operation_content` TEXT        DEFAULT NULL             COMMENT '操作内容',
  `operation_result` VARCHAR(100) DEFAULT NULL             COMMENT '操作结果',
  `operation_time`  DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_log_operator` (`operator_id`),
  KEY `idx_log_time` (`operation_time`),
  CONSTRAINT `fk_log_operator` FOREIGN KEY (`operator_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';
