package com.neuedu.pmf.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryCheckDetail {
    private Integer detail_id;
    private String check_no;
    private Integer warehouse_id;
    private Integer zone_id;
    private Integer location_id;
    private Integer goods_id;
    private Integer batch_id;
    private Integer book_quantity;
    private Integer actual_quantity;
    private Integer difference;
    private String detail_status;
}
