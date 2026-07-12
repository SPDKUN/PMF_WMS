package com.neuedu.pmf.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateInboundRequest {
    private String inboundType;
    private String batchId;
    private Integer operatorId;
    private String priority;
    private String deadline;
    private String remark;
}
