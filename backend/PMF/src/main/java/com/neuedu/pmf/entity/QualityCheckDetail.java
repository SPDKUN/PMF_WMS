package com.neuedu.pmf.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QualityCheckDetail {
    private Integer detail_id;
    private String quality_check_no;
    private Integer goods_id;
    private String batch_id;
    private String inspection_result;
    private String defect_reason;
    private String handling_suggestion;
}
