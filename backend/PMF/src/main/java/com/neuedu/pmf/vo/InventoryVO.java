package com.neuedu.pmf.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryVO {
    private Integer inventory_id;
    private Integer goods_id;
    private String goods_name;
    private String unit;
    private Integer batch_id;
    private Integer warehouse_id;
    private Integer zone_id;
    private Integer location_id;
    private String location_name;
    private Integer quantity;
    private Integer locked_quantity;
    private String inventory_status;
    private LocalDateTime inbound_time;
    private LocalDateTime last_updated;
}
