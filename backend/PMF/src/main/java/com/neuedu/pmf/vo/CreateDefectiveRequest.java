package com.neuedu.pmf.vo;

import java.util.List;

public class CreateDefectiveRequest {
    private List<DefectiveItem> items;
    private Integer assigneeId;
    private String priority;
    private String deadline;
    private Integer operatorId;

    public static class DefectiveItem {
        private String batchId;
        private Integer goodsId;
        private Integer quantity;
        private Integer sourceLocationId;
        private Integer warehouseId;
        private Integer zoneId;

        public String getBatchId() { return batchId; }
        public void setBatchId(String batchId) { this.batchId = batchId; }
        public Integer getGoodsId() { return goodsId; }
        public void setGoodsId(Integer goodsId) { this.goodsId = goodsId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public Integer getSourceLocationId() { return sourceLocationId; }
        public void setSourceLocationId(Integer sourceLocationId) { this.sourceLocationId = sourceLocationId; }
        public Integer getWarehouseId() { return warehouseId; }
        public void setWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; }
        public Integer getZoneId() { return zoneId; }
        public void setZoneId(Integer zoneId) { this.zoneId = zoneId; }
    }

    public List<DefectiveItem> getItems() { return items; }
    public void setItems(List<DefectiveItem> items) { this.items = items; }
    public Integer getAssigneeId() { return assigneeId; }
    public void setAssigneeId(Integer assigneeId) { this.assigneeId = assigneeId; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    public Integer getOperatorId() { return operatorId; }
    public void setOperatorId(Integer operatorId) { this.operatorId = operatorId; }
}
