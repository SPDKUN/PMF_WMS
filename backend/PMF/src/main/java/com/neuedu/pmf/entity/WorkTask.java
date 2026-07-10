package com.neuedu.pmf.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkTask {
    private Integer task_id;
    private String task_type;
    private String priority;
    private String related_order_no;
    private Integer goods_id;
    private Integer source_location_id;
    private Integer target_location_id;
    private Integer assignee_id;
    private LocalDateTime deadline;
    private LocalDateTime created_time;
    private LocalDateTime completed_time;
    private String remark;
}
