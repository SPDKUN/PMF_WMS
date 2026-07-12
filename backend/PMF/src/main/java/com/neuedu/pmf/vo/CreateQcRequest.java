package com.neuedu.pmf.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateQcRequest {
    private String checkType;
    private String batchId;
    private Integer inspectorId;
    private String priority;
    private String deadline;
    private String remark;
    private Integer operatorId;
}
