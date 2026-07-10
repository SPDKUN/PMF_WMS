package com.neuedu.pmf.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryCheckHead {
    private String check_no;
    private String check_type;
    private String order_status;
    private Integer operator_id;
    private LocalDateTime created_time;
    private LocalDateTime completed_time;
    private String remark;
}
