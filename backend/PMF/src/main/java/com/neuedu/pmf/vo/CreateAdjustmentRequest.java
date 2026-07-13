package com.neuedu.pmf.vo;

import java.util.List;

public class CreateAdjustmentRequest {
    private List<AdjustmentItem> items;
    private Integer assigneeId;
    private String priority;
    private String deadline;
    private Integer operatorId;

    public static class AdjustmentItem {
        private Integer goodsId;
        private String batchId;
        private Integer quantity;
        private Integer fromWarehouseId;
        private Integer fromZoneId;
        private Integer fromLocationId;
        private Integer toWarehouseId;
        private Integer toZoneId;
        private Integer toLocationId;

        public Integer getGoodsId() { return goodsId; }
        public void setGoodsId(Integer goodsId) { this.goodsId = goodsId; }
        public String getBatchId() { return batchId; }
        public void setBatchId(String batchId) { this.batchId = batchId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public Integer getFromWarehouseId() { return fromWarehouseId; }
        public void setFromWarehouseId(Integer fromWarehouseId) { this.fromWarehouseId = fromWarehouseId; }
        public Integer getFromZoneId() { return fromZoneId; }
        public void setFromZoneId(Integer fromZoneId) { this.fromZoneId = fromZoneId; }
        public Integer getFromLocationId() { return fromLocationId; }
        public void setFromLocationId(Integer fromLocationId) { this.fromLocationId = fromLocationId; }
        public Integer getToWarehouseId() { return toWarehouseId; }
        public void setToWarehouseId(Integer toWarehouseId) { this.toWarehouseId = toWarehouseId; }
        public Integer getToZoneId() { return toZoneId; }
        public void setToZoneId(Integer toZoneId) { this.toZoneId = toZoneId; }
        public Integer getToLocationId() { return toLocationId; }
        public void setToLocationId(Integer toLocationId) { this.toLocationId = toLocationId; }
    }

    public List<AdjustmentItem> getItems() { return items; }
    public void setItems(List<AdjustmentItem> items) { this.items = items; }
    public Integer getAssigneeId() { return assigneeId; }
    public void setAssigneeId(Integer assigneeId) { this.assigneeId = assigneeId; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    public Integer getOperatorId() { return operatorId; }
    public void setOperatorId(Integer operatorId) { this.operatorId = operatorId; }
}
