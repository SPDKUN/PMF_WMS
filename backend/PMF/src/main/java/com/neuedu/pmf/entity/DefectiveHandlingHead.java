package com.neuedu.pmf.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DefectiveHandlingHead {
    private String handling_no;
    private String handling_type;
    private String order_status;
    private Integer operator_id;
    private LocalDateTime handling_time;
    private String remark;
}
