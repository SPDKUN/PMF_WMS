package com.neuedu.pmf.vo;

import java.util.List;

public class CreateOutboundRequest {
    private List<OutboundItem> items;
    private Integer assigneeId;
    private String priority;
    private String deadline;
    private Integer operatorId;

    public static class OutboundItem {
        private Integer goodsId;
        private String batchId;
        private Integer quantity;
        private Integer warehouseId;
        private Integer zoneId;
        private Integer locationId;

        public Integer getGoodsId() { return goodsId; }
        public void setGoodsId(Integer goodsId) { this.goodsId = goodsId; }
        public String getBatchId() { return batchId; }
        public void setBatchId(String batchId) { this.batchId = batchId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public Integer getWarehouseId() { return warehouseId; }
        public void setWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; }
        public Integer getZoneId() { return zoneId; }
        public void setZoneId(Integer zoneId) { this.zoneId = zoneId; }
        public Integer getLocationId() { return locationId; }
        public void setLocationId(Integer locationId) { this.locationId = locationId; }
    }

    public List<OutboundItem> getItems() { return items; }
    public void setItems(List<OutboundItem> items) { this.items = items; }
    public Integer getAssigneeId() { return assigneeId; }
    public void setAssigneeId(Integer assigneeId) { this.assigneeId = assigneeId; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    public Integer getOperatorId() { return operatorId; }
    public void setOperatorId(Integer operatorId) { this.operatorId = operatorId; }
}
