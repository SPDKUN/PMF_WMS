package com.neuedu.pmf.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OperationLog {
    private Integer log_id;
    private Integer operator_id;
    private String operation_type;
    private String operation_content;
    private String operation_result;
    private LocalDateTime operation_time;
}
