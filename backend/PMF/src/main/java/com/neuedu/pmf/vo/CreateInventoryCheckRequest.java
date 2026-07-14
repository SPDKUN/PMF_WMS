package com.neuedu.pmf.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateInventoryCheckRequest {
    private String checkType;
    private String scopeType;
    private String scopeValue;
    private Integer operatorId;
    private String priority;
    private String deadline;
    private String remark;
}
