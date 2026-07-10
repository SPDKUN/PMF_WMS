package com.neuedu.pmf.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InboundOrderDetail {
    private Integer detail_id;
    private String inbound_no;
    private Integer goods_id;
    private String batch_no;
    private LocalDate production_date;
    private LocalDate expiry_date;
    private Integer quantity;
    private Integer warehouse_id;
    private Integer zone_id;
    private Integer location_id;
}
