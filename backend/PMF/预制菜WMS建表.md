# 预制菜WMS仓储管理系统 — 数据库建表 SQL

## 1. 用户表 `user`

```sql
CREATE TABLE `user` (
  `user_id`       INT           NOT NULL AUTO_INCREMENT,
  `username`      VARCHAR(50)   NOT NULL,
  `real_name`     VARCHAR(50)   DEFAULT NULL,
  `phone`         VARCHAR(20)   DEFAULT NULL,
  `department`    VARCHAR(50)   DEFAULT NULL,
  `position`      VARCHAR(50)   DEFAULT NULL,
  `password`      VARCHAR(255)  NOT NULL,
  `status`        TINYINT       NOT NULL DEFAULT 1,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 2. 货物表 `goods`

```sql
CREATE TABLE `goods` (
  `goods_id`           INT           NOT NULL AUTO_INCREMENT,
  `goods_code`         VARCHAR(50)   NOT NULL,
  `category`           ENUM('原料','成品') NOT NULL,
  `goods_name`         VARCHAR(100)  NOT NULL,
  `storage_condition`  ENUM('常温','冷冻','恒温') NOT NULL,
  `quantity`           INT           NOT NULL DEFAULT 0,
  `unit`               VARCHAR(20)   DEFAULT NULL,
  PRIMARY KEY (`goods_id`),
  UNIQUE KEY `uk_goods_code` (`goods_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 3. 库房表 `warehouse`

```sql
CREATE TABLE `warehouse` (
  `warehouse_id`     INT           NOT NULL AUTO_INCREMENT,
  `warehouse_name`   VARCHAR(100)  NOT NULL,
  `warehouse_type`   ENUM('一般仓库','冷冻库','恒温库') NOT NULL,
  `warehouse_size`   ENUM('大','中','小') DEFAULT NULL,
  `total_slots`      INT           NOT NULL DEFAULT 0,
  `available_slots`  INT           NOT NULL DEFAULT 0,
  `location`         VARCHAR(200)  DEFAULT NULL,
  `status`           VARCHAR(20)   NOT NULL DEFAULT '启用',
  `description`      TEXT          DEFAULT NULL,
  PRIMARY KEY (`warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 4. 库区表 `zone`

```sql
CREATE TABLE `zone` (
  `zone_id`         INT           NOT NULL AUTO_INCREMENT,
  `zone_name`       VARCHAR(100)  NOT NULL,
  `warehouse_id`    INT           NOT NULL,
  `total_slots`     INT           NOT NULL DEFAULT 0,
  `available_slots` INT           NOT NULL DEFAULT 0,
  `description`     TEXT          DEFAULT NULL,
  PRIMARY KEY (`zone_id`),
  KEY `idx_zone_warehouse` (`warehouse_id`),
  CONSTRAINT `fk_zone_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse` (`warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 5. 库位表 `location`

```sql
CREATE TABLE `location` (
  `location_id`    INT           NOT NULL AUTO_INCREMENT,
  `location_name`  VARCHAR(100)  NOT NULL,
  `zone_id`        INT           NOT NULL,
  `goods_id`       INT           DEFAULT NULL,
  `is_occupied`    TINYINT       NOT NULL DEFAULT 0,
  `description`    TEXT          DEFAULT NULL,
  PRIMARY KEY (`location_id`),
  KEY `idx_location_zone` (`zone_id`),
  KEY `idx_location_goods` (`goods_id`),
  CONSTRAINT `fk_location_zone`  FOREIGN KEY (`zone_id`)  REFERENCES `zone` (`zone_id`),
  CONSTRAINT `fk_location_goods` FOREIGN KEY (`goods_id`) REFERENCES `goods` (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 6. 批次单 `batch`

```sql
CREATE TABLE `batch` (
  `batch_id`           VARCHAR(20)   NOT NULL,
  `goods_id`           INT           NOT NULL,
  `production_date`    DATE          NOT NULL,
  `expiry_date`        DATE          NOT NULL,
  `initial_quantity`   INT           NOT NULL DEFAULT 0,
  `remaining_quantity` INT           NOT NULL DEFAULT 0,
  `batch_status`       ENUM('正常','待检','锁定','报废') NOT NULL DEFAULT '待检',
  PRIMARY KEY (`batch_id`),
  KEY `idx_batch_goods` (`goods_id`),
  CONSTRAINT `fk_batch_goods` FOREIGN KEY (`goods_id`) REFERENCES `goods` (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 7. 库存明细表 `inventory`

```sql
CREATE TABLE `inventory` (
  `inventory_id`     INT           NOT NULL AUTO_INCREMENT,
  `goods_id`         INT           NOT NULL,
  `batch_id`         VARCHAR(20)   NOT NULL,
  `warehouse_id`     INT           DEFAULT NULL,
  `zone_id`          INT           DEFAULT NULL,
  `location_id`      INT           DEFAULT NULL,
  `quantity`         INT           NOT NULL DEFAULT 0,
  `locked_quantity`  INT           NOT NULL DEFAULT 0,
  `inventory_status` ENUM('正常','待入库','待出库','待报废') NOT NULL DEFAULT '正常',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 8. 工作任务表 `work_task`

```sql
CREATE TABLE `work_task` (
  `task_id`             INT           NOT NULL AUTO_INCREMENT,
  `task_type`           ENUM('入库','出库','库存调整','库存盘点','质检','处理不合格货物') NOT NULL,
  `priority`            ENUM('高','中','低') NOT NULL DEFAULT '中',
  `related_order_no`    VARCHAR(100)  DEFAULT NULL,
  `related_batch_id`    VARCHAR(100)  DEFAULT NULL,
  `goods_id`            INT           DEFAULT NULL,
  `source_location_id`  INT           DEFAULT NULL,
  `target_location_id`  INT           DEFAULT NULL,
  `assignee_id`         INT           DEFAULT NULL,
  `deadline`            DATETIME      DEFAULT NULL,
  `created_time`        DATETIME      DEFAULT CURRENT_TIMESTAMP,
  `completed_time`      DATETIME      DEFAULT NULL,
  `remark`              TEXT          DEFAULT NULL,
  PRIMARY KEY (`task_id`),
  KEY `idx_task_goods`   (`goods_id`),
  KEY `idx_task_assignee` (`assignee_id`),
  KEY `idx_task_src_loc` (`source_location_id`),
  KEY `idx_task_tgt_loc` (`target_location_id`),
  CONSTRAINT `fk_task_goods`   FOREIGN KEY (`goods_id`)   REFERENCES `goods` (`goods_id`),
  CONSTRAINT `fk_task_assignee` FOREIGN KEY (`assignee_id`) REFERENCES `user` (`user_id`),
  CONSTRAINT `fk_task_src_loc` FOREIGN KEY (`source_location_id`) REFERENCES `location` (`location_id`),
  CONSTRAINT `fk_task_tgt_loc` FOREIGN KEY (`target_location_id`) REFERENCES `location` (`location_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 9. 入库单头表 `inbound_order_head`

```sql
CREATE TABLE `inbound_order_head` (
  `inbound_no`    VARCHAR(50)   NOT NULL,
  `inbound_type`  ENUM('采购入库','退货入库','生产入库') NOT NULL,
  `order_status`  ENUM('草稿','已审核','已完成') NOT NULL DEFAULT '草稿',
  `operator_id`   INT           NOT NULL,
  `inbound_time`  DATETIME      DEFAULT NULL,
  `remark`        TEXT          DEFAULT NULL,
  PRIMARY KEY (`inbound_no`),
  KEY `idx_inbound_head_operator` (`operator_id`),
  CONSTRAINT `fk_inbound_head_operator` FOREIGN KEY (`operator_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 10. 入库单明细表 `inbound_order_detail`

```sql
CREATE TABLE `inbound_order_detail` (
  `detail_id`       INT           NOT NULL AUTO_INCREMENT,
  `inbound_no`      VARCHAR(50)   NOT NULL,
  `goods_id`        INT           NOT NULL,
  `batch_no`        VARCHAR(50)   DEFAULT NULL,
  `production_date` DATE          DEFAULT NULL,
  `expiry_date`     DATE          DEFAULT NULL,
  `quantity`        INT           NOT NULL DEFAULT 0,
  `warehouse_id`    INT           DEFAULT NULL,
  `zone_id`         INT           DEFAULT NULL,
  `location_id`     INT           DEFAULT NULL,
  PRIMARY KEY (`detail_id`),
  KEY `idx_inbound_detail_head` (`inbound_no`),
  KEY `idx_inbound_detail_goods` (`goods_id`),
  CONSTRAINT `fk_inbound_detail_head`  FOREIGN KEY (`inbound_no`)   REFERENCES `inbound_order_head` (`inbound_no`),
  CONSTRAINT `fk_inbound_detail_goods` FOREIGN KEY (`goods_id`)     REFERENCES `goods` (`goods_id`),
  CONSTRAINT `fk_inbound_detail_wh`    FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse` (`warehouse_id`),
  CONSTRAINT `fk_inbound_detail_zone`  FOREIGN KEY (`zone_id`)      REFERENCES `zone` (`zone_id`),
  CONSTRAINT `fk_inbound_detail_loc`   FOREIGN KEY (`location_id`)  REFERENCES `location` (`location_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 11. 出库单头表 `outbound_order_head`

```sql
CREATE TABLE `outbound_order_head` (
  `outbound_no`    VARCHAR(50)   NOT NULL,
  `outbound_type`  ENUM('销售出库','领料出库','报废出库') NOT NULL,
  `order_status`   ENUM('草稿','已审核','已完成') NOT NULL DEFAULT '草稿',
  `operator_id`    INT           NOT NULL,
  `outbound_time`  DATETIME      DEFAULT NULL,
  `remark`         TEXT          DEFAULT NULL,
  PRIMARY KEY (`outbound_no`),
  KEY `idx_outbound_head_operator` (`operator_id`),
  CONSTRAINT `fk_outbound_head_operator` FOREIGN KEY (`operator_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 12. 出库单明细表 `outbound_order_detail`

```sql
CREATE TABLE `outbound_order_detail` (
  `detail_id`     INT           NOT NULL AUTO_INCREMENT,
  `outbound_no`   VARCHAR(50)   NOT NULL,
  `goods_id`      INT           NOT NULL,
  `batch_id`      VARCHAR(20)   NOT NULL,
  `quantity`      INT           NOT NULL DEFAULT 0,
  `warehouse_id`  INT           DEFAULT NULL,
  `zone_id`       INT           DEFAULT NULL,
  `location_id`   INT           DEFAULT NULL,
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 13. 库存调整单头表 `adjustment_order_head`

```sql
CREATE TABLE `adjustment_order_head` (
  `adjustment_no`   VARCHAR(50)   NOT NULL,
  `adjustment_type` ENUM('库位移库','批次调整','数量修正') NOT NULL,
  `order_status`    ENUM('草稿','已审核','已完成') NOT NULL DEFAULT '草稿',
  `operator_id`     INT           NOT NULL,
  `adjustment_time` DATETIME      DEFAULT NULL,
  PRIMARY KEY (`adjustment_no`),
  KEY `idx_adjustment_head_operator` (`operator_id`),
  CONSTRAINT `fk_adjustment_head_operator` FOREIGN KEY (`operator_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 14. 库存调整单明细表 `adjustment_order_detail`

```sql
CREATE TABLE `adjustment_order_detail` (
  `detail_id`         INT           NOT NULL AUTO_INCREMENT,
  `adjustment_no`     VARCHAR(50)   NOT NULL,
  `goods_id`          INT           NOT NULL,
  `batch_id`          VARCHAR(20)   NOT NULL,
  `quantity`          INT           NOT NULL DEFAULT 0,
  `from_warehouse_id` INT           DEFAULT NULL,
  `from_zone_id`      INT           DEFAULT NULL,
  `from_location_id`  INT           DEFAULT NULL,
  `to_warehouse_id`   INT           DEFAULT NULL,
  `to_zone_id`        INT           DEFAULT NULL,
  `to_location_id`    INT           DEFAULT NULL,
  PRIMARY KEY (`detail_id`),
  KEY `idx_adjustment_detail_head` (`adjustment_no`),
  KEY `idx_adjustment_detail_goods` (`goods_id`),
  CONSTRAINT `fk_adjustment_detail_head`  FOREIGN KEY (`adjustment_no`) REFERENCES `adjustment_order_head` (`adjustment_no`),
  CONSTRAINT `fk_adjustment_detail_goods` FOREIGN KEY (`goods_id`)     REFERENCES `goods` (`goods_id`),
  CONSTRAINT `fk_adjustment_detail_batch` FOREIGN KEY (`batch_id`)     REFERENCES `batch` (`batch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 15. 盘点单头表 `inventory_check_head`

```sql
CREATE TABLE `inventory_check_head` (
  `check_no`       VARCHAR(50)   NOT NULL,
  `check_type`     ENUM('全盘','抽盘','动碰盘点') NOT NULL,
  `order_status`   ENUM('草稿','进行中','已完成') NOT NULL DEFAULT '草稿',
  `operator_id`    INT           NOT NULL,
  `created_time`   DATETIME      DEFAULT CURRENT_TIMESTAMP,
  `completed_time` DATETIME      DEFAULT NULL,
  `remark`         TEXT          DEFAULT NULL,
  PRIMARY KEY (`check_no`),
  KEY `idx_check_head_operator` (`operator_id`),
  CONSTRAINT `fk_check_head_operator` FOREIGN KEY (`operator_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 16. 盘点单明细表 `inventory_check_detail`

```sql
CREATE TABLE `inventory_check_detail` (
  `detail_id`       INT           NOT NULL AUTO_INCREMENT,
  `check_no`        VARCHAR(50)   NOT NULL,
  `warehouse_id`    INT           NOT NULL,
  `zone_id`         INT           DEFAULT NULL,
  `location_id`     INT           DEFAULT NULL,
  `goods_id`        INT           NOT NULL,
  `batch_id`        VARCHAR(20)   NOT NULL,
  `book_quantity`   INT           NOT NULL DEFAULT 0,
  `actual_quantity` INT           DEFAULT NULL,
  `difference`      INT           DEFAULT NULL,
  `detail_status`   ENUM('正常','盘盈','盘亏') DEFAULT '正常',
  PRIMARY KEY (`detail_id`),
  KEY `idx_check_detail_head` (`check_no`),
  KEY `idx_check_detail_wh`   (`warehouse_id`),
  KEY `idx_check_detail_goods` (`goods_id`),
  CONSTRAINT `fk_check_detail_head`  FOREIGN KEY (`check_no`)     REFERENCES `inventory_check_head` (`check_no`),
  CONSTRAINT `fk_check_detail_wh`    FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse` (`warehouse_id`),
  CONSTRAINT `fk_check_detail_goods` FOREIGN KEY (`goods_id`)    REFERENCES `goods` (`goods_id`),
  CONSTRAINT `fk_check_detail_batch` FOREIGN KEY (`batch_id`)    REFERENCES `batch` (`batch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 17. 质检单头表 `quality_check_head`

```sql
CREATE TABLE `quality_check_head` (
  `quality_check_no` VARCHAR(50)   NOT NULL,
  `check_type`       ENUM('来料检','成品检','日常抽检') NOT NULL,
  `order_status`     ENUM('待检','进行中','已完成') NOT NULL DEFAULT '待检',
  `inspector_id`     INT           NOT NULL,
  `inspection_time`  DATETIME      DEFAULT NULL,
  `remark`           TEXT          DEFAULT NULL,
  PRIMARY KEY (`quality_check_no`),
  KEY `idx_quality_head_inspector` (`inspector_id`),
  CONSTRAINT `fk_quality_head_inspector` FOREIGN KEY (`inspector_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 18. 质检单明细表 `quality_check_detail`

```sql
CREATE TABLE `quality_check_detail` (
  `detail_id`          INT           NOT NULL AUTO_INCREMENT,
  `quality_check_no`   VARCHAR(50)   NOT NULL,
  `goods_id`           INT           NOT NULL,
  `batch_id`           VARCHAR(20)   NOT NULL,
  `inspection_result`  VARCHAR(50)   DEFAULT NULL,
  `defect_reason`      TEXT          DEFAULT NULL,
  `handling_suggestion` VARCHAR(100) DEFAULT NULL,
  PRIMARY KEY (`detail_id`),
  KEY `idx_quality_detail_head` (`quality_check_no`),
  KEY `idx_quality_detail_goods` (`goods_id`),
  CONSTRAINT `fk_quality_detail_head`  FOREIGN KEY (`quality_check_no`) REFERENCES `quality_check_head` (`quality_check_no`),
  CONSTRAINT `fk_quality_detail_goods` FOREIGN KEY (`goods_id`)       REFERENCES `goods` (`goods_id`),
  CONSTRAINT `fk_quality_detail_batch` FOREIGN KEY (`batch_id`)       REFERENCES `batch` (`batch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 19. 不合格处理单头表 `defective_handling_head`

```sql
CREATE TABLE `defective_handling_head` (
  `handling_no`   VARCHAR(50)   NOT NULL,
  `handling_type` ENUM('报废','退货','销毁','降级使用') NOT NULL,
  `order_status`  ENUM('待处理','已完成') NOT NULL DEFAULT '待处理',
  `operator_id`   INT           NOT NULL,
  `handling_time` DATETIME      DEFAULT NULL,
  `remark`        TEXT          DEFAULT NULL,
  PRIMARY KEY (`handling_no`),
  KEY `idx_defective_head_operator` (`operator_id`),
  CONSTRAINT `fk_defective_head_operator` FOREIGN KEY (`operator_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 20. 不合格处理单明细表 `defective_handling_detail`

```sql
CREATE TABLE `defective_handling_detail` (
  `detail_id`           INT           NOT NULL AUTO_INCREMENT,
  `handling_no`         VARCHAR(50)   NOT NULL,
  `goods_id`            INT           NOT NULL,
  `batch_id`            VARCHAR(20)   NOT NULL,
  `quantity`            INT           NOT NULL DEFAULT 0,
  `source_location_id`  INT           DEFAULT NULL,
  `target_location_id`  INT           DEFAULT NULL,
  PRIMARY KEY (`detail_id`),
  KEY `idx_defective_detail_head` (`handling_no`),
  KEY `idx_defective_detail_goods` (`goods_id`),
  CONSTRAINT `fk_defective_detail_head`  FOREIGN KEY (`handling_no`)        REFERENCES `defective_handling_head` (`handling_no`),
  CONSTRAINT `fk_defective_detail_goods` FOREIGN KEY (`goods_id`)           REFERENCES `goods` (`goods_id`),
  CONSTRAINT `fk_defective_detail_batch` FOREIGN KEY (`batch_id`)           REFERENCES `batch` (`batch_id`),
  CONSTRAINT `fk_defective_detail_sloc`  FOREIGN KEY (`source_location_id`) REFERENCES `location` (`location_id`),
  CONSTRAINT `fk_defective_detail_tloc`  FOREIGN KEY (`target_location_id`) REFERENCES `location` (`location_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 21. 温湿度记录表 `temperature_humidity_record`

```sql
CREATE TABLE `temperature_humidity_record` (
  `record_id`    INT           NOT NULL AUTO_INCREMENT,
  `warehouse_id` INT           NOT NULL,
  `temperature`  DECIMAL(5,2)  DEFAULT NULL,
  `humidity`     DECIMAL(5,2)  DEFAULT NULL,
  `recorded_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`record_id`),
  KEY `idx_thr_warehouse` (`warehouse_id`),
  KEY `idx_thr_time` (`recorded_time`),
  CONSTRAINT `fk_thr_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouse` (`warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 22. 操作日志表 `operation_log`

```sql
CREATE TABLE `operation_log` (
  `log_id`          INT           NOT NULL AUTO_INCREMENT,
  `operator_id`     INT           NOT NULL,
  `operation_type`  VARCHAR(50)   NOT NULL,
  `operation_content` TEXT        DEFAULT NULL,
  `operation_result` VARCHAR(100) DEFAULT NULL,
  `operation_time`  DATETIME      DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`log_id`),
  KEY `idx_log_operator` (`operator_id`),
  KEY `idx_log_time` (`operation_time`),
  CONSTRAINT `fk_log_operator` FOREIGN KEY (`operator_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 表结构总览

| # | 表名 | 说明 | 主键 | 关联表 |
|---|------|------|------|--------|
| 1 | `user` | 用户 | user_id (自增) | - |
| 2 | `goods` | 货物 | goods_id (自增) | - |
| 3 | `warehouse` | 库房 | warehouse_id (自增) | - |
| 4 | `zone` | 库区 | zone_id (自增) | warehouse |
| 5 | `location` | 库位 | location_id (自增) | zone, goods |
| 6 | `batch` | 批次 | batch_id (单号) | goods |
| 7 | `inventory` | 库存明细 | inventory_id (自增) | goods, batch, warehouse, zone, location |
| 8 | `work_task` | 工作任务 | task_id (自增) | goods, user, location |
| 9 | `inbound_order_head` | 入库单头 | inbound_no (单号) | user |
| 10 | `inbound_order_detail` | 入库单明细 | detail_id (自增) | inbound_order_head, goods, warehouse, zone, location |
| 11 | `outbound_order_head` | 出库单头 | outbound_no (单号) | user |
| 12 | `outbound_order_detail` | 出库单明细 | detail_id (自增) | outbound_order_head, goods, batch, warehouse, zone, location |
| 13 | `adjustment_order_head` | 调整单头 | adjustment_no (单号) | user |
| 14 | `adjustment_order_detail` | 调整单明细 | detail_id (自增) | adjustment_order_head, goods, batch |
| 15 | `inventory_check_head` | 盘点单头 | check_no (单号) | user |
| 16 | `inventory_check_detail` | 盘点单明细 | detail_id (自增) | inventory_check_head, warehouse, goods, batch |
| 17 | `quality_check_head` | 质检单头 | quality_check_no (单号) | user |
| 18 | `quality_check_detail` | 质检单明细 | detail_id (自增) | quality_check_head, goods, batch |
| 19 | `defective_handling_head` | 不合格处理单头 | handling_no (单号) | user |
| 20 | `defective_handling_detail` | 不合格处理单明细 | detail_id (自增) | defective_handling_head, goods, batch, location |
| 21 | `temperature_humidity_record` | 温湿度记录 | record_id (自增) | warehouse |
| 22 | `operation_log` | 操作日志 | log_id (自增) | user |
