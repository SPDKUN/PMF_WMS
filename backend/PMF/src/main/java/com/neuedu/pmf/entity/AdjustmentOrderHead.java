package com.neuedu.pmf.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdjustmentOrderHead {
    private String adjustment_no;
    private String adjustment_type;
    private String order_status;
    private Integer operator_id;
    private LocalDateTime adjustment_time;
}
