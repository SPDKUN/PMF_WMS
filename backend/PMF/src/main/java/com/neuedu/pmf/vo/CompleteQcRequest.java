package com.neuedu.pmf.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompleteQcRequest {
    private String inspectionResult;
    private String defectReason;
    private String handlingSuggestion;
    private String remark;
    private Integer operatorId;
}
