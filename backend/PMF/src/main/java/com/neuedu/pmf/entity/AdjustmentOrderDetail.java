package com.neuedu.pmf.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdjustmentOrderDetail {
    private Integer detail_id;
    private String adjustment_no;
    private Integer goods_id;
    private Integer batch_id;
    private Integer quantity;
    private Integer from_warehouse_id;
    private Integer from_zone_id;
    private Integer from_location_id;
    private Integer to_warehouse_id;
    private Integer to_zone_id;
    private Integer to_location_id;
}
