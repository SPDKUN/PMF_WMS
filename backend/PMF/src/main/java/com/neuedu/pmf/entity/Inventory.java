package com.neuedu.pmf.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Inventory {
    private Integer inventory_id;
    private Integer goods_id;
    private String batch_id;
    private Integer warehouse_id;
    private Integer zone_id;
    private Integer location_id;
    private Integer quantity;
    private Integer locked_quantity;
    private String inventory_status;
}
