package com.neuedu.pmf.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OutboundOrderDetail {
    private Integer detail_id;
    private String outbound_no;
    private Integer goods_id;
    private Integer batch_id;
    private Integer quantity;
    private Integer warehouse_id;
    private Integer zone_id;
    private Integer location_id;
}
