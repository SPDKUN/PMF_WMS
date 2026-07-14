package com.neuedu.pmf.service.impl;

import com.neuedu.pmf.entity.*;
import com.neuedu.pmf.mapper.*;
import com.neuedu.pmf.service.InventoryCheckService;
import com.neuedu.pmf.vo.CreateInventoryCheckRequest;
import com.neuedu.pmf.vo.CompleteInventoryCheckRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class InventoryCheckServiceImpl implements InventoryCheckService {

    @Autowired
    private InventoryCheckHeadMapper inventoryCheckHeadMapper;

    @Autowired
    private InventoryCheckDetailMapper inventoryCheckDetailMapper;

    @Autowired
    private WorkTaskMapper workTaskMapper;

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Autowired
    private InventoryMapper inventoryMapper;

    @Autowired
    private BatchMapper batchMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private LocationMapper locationMapper;

    @Autowired
    private ZoneMapper zoneMapper;

    @Autowired
    private WarehouseMapper warehouseMapper;

    @Override
    @Transactional
    public WorkTask createInventoryCheckTask(CreateInventoryCheckRequest request) {
        // 1. 生成盘点单号 P + yyyyMMdd + 3位序号
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "P" + today;
        List<String> todayNos = inventoryCheckHeadMapper.findTodayCheckNos(prefix + "%");
        int maxSeq = 0;
        for (String no : todayNos) {
            try {
                int seq = Integer.parseInt(no.substring(9));
                if (seq > maxSeq) maxSeq = seq;
            } catch (NumberFormatException ignored) {}
        }
        String checkNo = prefix + String.format("%03d", maxSeq + 1);

        // 2. 根据盘点范围查询库存记录
        ArrayList<Inventory> inventoryList = new ArrayList<>();
        String checkType = request.getCheckType();
        String scopeType = request.getScopeType();
        String scopeValue = request.getScopeValue();

        switch (scopeType) {
            case "location":
                inventoryList = inventoryMapper.findInventoryByLocationId(Integer.parseInt(scopeValue));
                break;
            case "warehouse":
                inventoryList = inventoryMapper.findInventoryByWarehouseId(Integer.parseInt(scopeValue));
                break;
            case "goods":
                inventoryList = inventoryMapper.findInventoryByGoodsId(Integer.parseInt(scopeValue));
                break;
            case "batch":
                inventoryList = inventoryMapper.findInventoryByBatchId(scopeValue);
                break;
            default:
                throw new RuntimeException("不支持的盘点范围类型: " + scopeType);
        }

        if (inventoryList.isEmpty()) {
            throw new RuntimeException("所选范围内没有可盘点的库存记录");
        }

        // 3. INSERT inventory_check_head
        InventoryCheckHead head = new InventoryCheckHead();
        head.setCheck_no(checkNo);
        head.setCheck_type(checkType);
        head.setOrder_status("进行中");
        head.setOperator_id(request.getOperatorId());
        head.setCreated_time(LocalDateTime.now());
        head.setCompleted_time(null);
        head.setRemark(request.getRemark());
        inventoryCheckHeadMapper.save(head);

        // 4. INSERT inventory_check_detail (每个库存记录一条明细)
        Set<String> batchIdSet = new LinkedHashSet<>();
        for (Inventory inv : inventoryList) {
            InventoryCheckDetail detail = new InventoryCheckDetail();
            detail.setCheck_no(checkNo);
            detail.setWarehouse_id(inv.getWarehouse_id());
            detail.setZone_id(inv.getZone_id());
            detail.setLocation_id(inv.getLocation_id());
            detail.setGoods_id(inv.getGoods_id());
            detail.setBatch_id(inv.getBatch_id());
            detail.setBook_quantity(inv.getQuantity());
            detail.setActual_quantity(null);
            detail.setDifference(null);
            detail.setDetail_status("正常");
            inventoryCheckDetailMapper.save(detail);

            if (inv.getBatch_id() != null && !inv.getBatch_id().isEmpty()) {
                batchIdSet.add(inv.getBatch_id());
            }
        }

        // 5. 汇总批次号
        String relatedBatchIds = String.join(",", batchIdSet);

        // 6. INSERT work_task
        WorkTask task = new WorkTask();
        task.setTask_type("库存盘点");
        task.setPriority(request.getPriority());
        task.setRelated_order_no(checkNo);
        task.setRelated_batch_id(relatedBatchIds);
        task.setGoods_id(null);
        task.setSource_location_id(null);
        task.setTarget_location_id(null);
        task.setAssignee_id(request.getOperatorId());
        task.setDeadline(request.getDeadline() != null && !request.getDeadline().isEmpty()
                ? LocalDateTime.parse(request.getDeadline() + "T23:59:59") : null);
        task.setCreated_time(LocalDateTime.now());
        task.setCompleted_time(null);
        task.setRemark(request.getRemark());
        workTaskMapper.save(task);

        // 7. INSERT operation_log
        OperationLog log = new OperationLog();
        log.setOperator_id(request.getOperatorId());
        log.setOperation_type("盘点");
        log.setOperation_content(checkNo + "申请盘点");
        log.setOperation_result("成功");
        log.setOperation_time(LocalDateTime.now());
        operationLogMapper.save(log);

        return task;
    }

    @Override
    @Transactional
    public void completeInventoryCheck(Integer taskId, CompleteInventoryCheckRequest request) {
        // 1. 查工作任务
        WorkTask task = workTaskMapper.findById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在: " + taskId);
        }

        String checkNo = task.getRelated_order_no();

        // 2. 查盘点明细列表
        ArrayList<InventoryCheckDetail> allDetails = inventoryCheckDetailMapper.findByCheckNo(checkNo);
        if (allDetails.isEmpty()) {
            throw new RuntimeException("盘点单无明细数据: " + checkNo);
        }

        Map<Integer, InventoryCheckDetail> detailMap = new HashMap<>();
        for (InventoryCheckDetail d : allDetails) {
            detailMap.put(d.getDetail_id(), d);
        }

        // 3. 遍历请求中的每条明细，更新 actual_quantity / difference / detail_status
        int normalCount = 0, surplusCount = 0, shortageCount = 0;
        List<String> surplusItems = new ArrayList<>();
        List<String> shortageItems = new ArrayList<>();

        Map<String, Integer> batchDiffMap = new HashMap<>();

        for (CompleteInventoryCheckRequest.DetailItem item : request.getDetails()) {
            InventoryCheckDetail detail = detailMap.get(item.getDetailId());
            if (detail == null) {
                throw new RuntimeException("明细不存在: " + item.getDetailId());
            }

            int actualQty = item.getActualQuantity();
            int bookQty = detail.getBook_quantity();
            int diff = actualQty - bookQty;

            detail.setActual_quantity(actualQty);
            detail.setDifference(diff);

            String status;
            if (diff == 0) {
                status = "正常";
                normalCount++;
            } else if (diff > 0) {
                status = "盘盈";
                surplusCount++;
                surplusItems.add(detail.getBatch_id() + "/LOC-" + detail.getLocation_id() + "/+" + diff);
            } else {
                status = "盘亏";
                shortageCount++;
                shortageItems.add(detail.getBatch_id() + "/LOC-" + detail.getLocation_id() + "/" + diff);
            }
            detail.setDetail_status(status);
            inventoryCheckDetailMapper.update(detail);

            if (diff != 0 && detail.getBatch_id() != null) {
                batchDiffMap.merge(detail.getBatch_id(), diff, Integer::sum);
            }
        }

        // 4. UPDATE head
        InventoryCheckHead head = inventoryCheckHeadMapper.findById(checkNo);
        if (head != null) {
            head.setRemark(request.getRemark());
            head.setCompleted_time(LocalDateTime.now());
            head.setOrder_status("已完成");
            inventoryCheckHeadMapper.update(head);
        }

        // 5. UPDATE work_task
        task.setCompleted_time(LocalDateTime.now());
        task.setRemark(request.getRemark());
        workTaskMapper.update(task);

        // 6. 构建 operation_result（摘要，≤500字符）和 operation_content（TEXT，存JSON详情）
        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append("正常").append(normalCount).append("项");
        if (surplusCount > 0) resultBuilder.append("、盘盈").append(surplusCount).append("项");
        if (shortageCount > 0) resultBuilder.append("、盘亏").append(shortageCount).append("项");
        String operationResult = resultBuilder.toString();

        // 详情JSON放到 operation_content（TEXT类型，不担心超长）
        String operationContent = checkNo + "完成盘点";
        if (surplusCount > 0 || shortageCount > 0) {
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("|||{");
            jsonBuilder.append("\"surplus\":[");
            for (int i = 0; i < surplusItems.size(); i++) {
                if (i > 0) jsonBuilder.append(",");
                String[] parts = surplusItems.get(i).split("/");
                String diffStr = parts[2].startsWith("+") ? parts[2].substring(1) : parts[2];
                jsonBuilder.append("{\"batchId\":\"").append(parts[0]).append("\",")
                           .append("\"locationId\":").append(parts[1].replace("LOC-", "")).append(",")
                           .append("\"diff\":").append(diffStr).append("}");
            }
            jsonBuilder.append("],");
            jsonBuilder.append("\"shortage\":[");
            for (int i = 0; i < shortageItems.size(); i++) {
                if (i > 0) jsonBuilder.append(",");
                String[] parts = shortageItems.get(i).split("/");
                jsonBuilder.append("{\"batchId\":\"").append(parts[0]).append("\",")
                           .append("\"locationId\":").append(parts[1].replace("LOC-", "")).append(",")
                           .append("\"diff\":").append(parts[2]).append("}");
            }
            jsonBuilder.append("]");
            jsonBuilder.append("}");
            operationContent += jsonBuilder.toString();
        }

        // 7. INSERT operation_log
        OperationLog log = new OperationLog();
        log.setOperator_id(request.getOperatorId());
        log.setOperation_type("盘点");
        log.setOperation_content(operationContent);
        log.setOperation_result(operationResult);
        log.setOperation_time(LocalDateTime.now());
        operationLogMapper.save(log);

        // 8. 更新库存明细表 inventory
        Set<Integer> affectedGoodsIds = new HashSet<>();
        Set<Integer> affectedZoneIds = new HashSet<>();

        for (CompleteInventoryCheckRequest.DetailItem item : request.getDetails()) {
            InventoryCheckDetail detail = detailMap.get(item.getDetailId());
            int diff = item.getActualQuantity() - detail.getBook_quantity();
            if (diff == 0) continue;

            Inventory inv = inventoryMapper.findInventoryByKey(
                    detail.getGoods_id(), detail.getBatch_id(),
                    detail.getWarehouse_id(), detail.getZone_id(),
                    detail.getLocation_id());
            if (inv != null) {
                if (item.getActualQuantity() == 0) {
                    inventoryMapper.deleteById(inv.getInventory_id());
                } else {
                    inv.setQuantity(item.getActualQuantity());
                    inventoryMapper.update(inv);
                }
                affectedGoodsIds.add(detail.getGoods_id());
                affectedZoneIds.add(detail.getZone_id());

                if (item.getActualQuantity() == 0 && detail.getLocation_id() != null) {
                    ArrayList<Inventory> locInvs = inventoryMapper.findInventoryByLocationId(detail.getLocation_id());
                    boolean locIsEmpty = true;
                    for (Inventory li : locInvs) {
                        if (li.getQuantity() != null && li.getQuantity() > 0) {
                            locIsEmpty = false;
                            break;
                        }
                    }
                    if (locIsEmpty) {
                        Location loc = locationMapper.findById(detail.getLocation_id());
                        if (loc != null && loc.getIs_occupied() == 1) {
                            loc.setIs_occupied(0);
                            loc.setGoods_id(null);
                            locationMapper.update(loc);
                        }
                    }
                }
            }
        }

        // 重新计算受影响库区的可用库位数
        for (Integer zoneId : affectedZoneIds) {
            if (zoneId == null) continue;
            ArrayList<Location> locations = locationMapper.findByZoneId(zoneId);
            int available = 0;
            for (Location loc : locations) {
                if (loc.getIs_occupied() == 0 && !"锁定".equals(loc.getLock_status())) {
                    available++;
                }
            }
            Zone zone = zoneMapper.findById(zoneId);
            if (zone != null) {
                zone.setAvailable_slots(available);
                zoneMapper.update(zone);
                Warehouse wh = warehouseMapper.findById(zone.getWarehouse_id());
                if (wh != null) {
                    ArrayList<Zone> allZones = zoneMapper.findByWarehouseId(wh.getWarehouse_id());
                    int whAvailable = 0;
                    for (Zone z : allZones) {
                        whAvailable += (z.getAvailable_slots() != null ? z.getAvailable_slots() : 0);
                    }
                    wh.setAvailable_slots(whAvailable);
                    warehouseMapper.update(wh);
                }
            }
        }

        // 9. 更新批次表 batch 的 remaining_quantity
        for (Map.Entry<String, Integer> entry : batchDiffMap.entrySet()) {
            String batchId = entry.getKey();
            int totalDiff = entry.getValue();
            Batch batch = batchMapper.findById(batchId);
            if (batch != null) {
                int newRemaining = batch.getRemaining_quantity() + totalDiff;
                batch.setRemaining_quantity(Math.max(0, newRemaining));
                batchMapper.update(batch);
            }
        }

        // 10. 更新货物表 goods 的 quantity（重新汇总该货物所有库存数量）
        for (Integer goodsId : affectedGoodsIds) {
            if (goodsId == null) continue;
            int totalQty = 0;
            ArrayList<Inventory> goodsInventoryList = inventoryMapper.findInventoryByGoodsId(goodsId);
            for (Inventory inv : goodsInventoryList) {
                totalQty += (inv.getQuantity() != null ? inv.getQuantity() : 0);
            }
            Goods goods = goodsMapper.findById(goodsId);
            if (goods != null) {
                goods.setQuantity(totalQty);
                goodsMapper.update(goods);
            }
        }
    }

    @Override
    public ArrayList<WorkTask> getMyTasks(Integer assigneeId) {
        return workTaskMapper.findByAssigneeId(assigneeId);
    }

    @Override
    public ArrayList<InventoryCheckDetail> getDetailsByCheckNo(String checkNo) {
        return inventoryCheckDetailMapper.findByCheckNo(checkNo);
    }
}
