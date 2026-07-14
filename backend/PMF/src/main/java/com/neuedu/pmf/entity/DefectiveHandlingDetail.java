package com.neuedu.pmf.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DefectiveHandlingDetail {
    private Integer detail_id;
    private String handling_no;
    private Integer goods_id;
    private String batch_id;
    private Integer quantity;
    private Integer source_location_id;
    private Integer target_location_id;
}
