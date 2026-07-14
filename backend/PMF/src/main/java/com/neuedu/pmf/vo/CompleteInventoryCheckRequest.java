package com.neuedu.pmf.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompleteInventoryCheckRequest {
    private List<DetailItem> details;
    private String remark;
    private Integer operatorId;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DetailItem {
        private Integer detailId;
        private Integer actualQuantity;
    }
}
