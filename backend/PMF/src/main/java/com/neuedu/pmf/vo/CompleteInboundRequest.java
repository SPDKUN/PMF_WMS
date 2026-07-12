package com.neuedu.pmf.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompleteInboundRequest {
    private Integer warehouseId;
    private List<Integer> locationIds;
    private String remark;
    private Integer operatorId;
}
