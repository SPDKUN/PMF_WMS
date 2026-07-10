package com.neuedu.pmf.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QualityCheckHead {
    private String quality_check_no;
    private String check_type;
    private String order_status;
    private Integer inspector_id;
    private LocalDateTime inspection_time;
    private String remark;
}
