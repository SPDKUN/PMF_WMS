package com.neuedu.pmf.service.impl;

import com.neuedu.pmf.entity.*;
import com.neuedu.pmf.mapper.*;
import com.neuedu.pmf.service.AdjustmentService;
import com.neuedu.pmf.vo.CreateAdjustmentRequest;
import com.neuedu.pmf.vo.InventoryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AdjustmentServiceImpl implements AdjustmentService {

    @Autowired
    private AdjustmentOrderHeadMapper adjustmentOrderHeadMapper;

    @Autowired
    private AdjustmentOrderDetailMapper adjustmentOrderDetailMapper;

    @Autowired
    private WorkTaskMapper workTaskMapper;

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Autowired
    private LocationMapper locationMapper;

    @Autowired
    private InventoryMapper inventoryMapper;

    @Autowired
    private ZoneMapper zoneMapper;

    @Autowired
    private WarehouseMapper warehouseMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private BatchMapper batchMapper;

    @Override
    @Transactional
    public WorkTask createAdjustmentTask(CreateAdjustmentRequest request) {
        // 1. 生成调整单号 A + yyyyMMdd + 3位序号
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "A" + today;
        List<String> todayNos = adjustmentOrderHeadMapper.findTodayAdjustmentNos(prefix + "%");
        int maxSeq = 0;
        for (String no : todayNos) {
            try {
                int seq = Integer.parseInt(no.substring(9));
                if (seq > maxSeq) maxSeq = seq;
            } catch (NumberFormatException ignored) {}
        }
        String adjustmentNo = prefix + String.format("%03d", maxSeq + 1);

        // 2. INSERT adjustment_order_head
        AdjustmentOrderHead head = new AdjustmentOrderHead();
        head.setAdjustment_no(adjustmentNo);
        head.setAdjustment_type("库位移库");
        head.setOrder_status("草稿");
        head.setOperator_id(request.getOperatorId());
        head.setAdjustment_time(null);
        adjustmentOrderHeadMapper.save(head);

        // 3. 收集所有需要锁定的库位ID
        Set<Integer> lockLocationIds = new LinkedHashSet<>();

        // 4. 遍历 items INSERT adjustment_order_detail
        for (CreateAdjustmentRequest.AdjustmentItem item : request.getItems()) {
            AdjustmentOrderDetail detail = new AdjustmentOrderDetail();
            detail.setAdjustment_no(adjustmentNo);
            detail.setGoods_id(item.getGoodsId());
            detail.setBatch_id(item.getBatchId());
            detail.setQuantity(item.getQuantity());
            detail.setFrom_warehouse_id(item.getFromWarehouseId());
            detail.setFrom_zone_id(item.getFromZoneId());
            detail.setFrom_location_id(item.getFromLocationId());
            detail.setTo_warehouse_id(item.getToWarehouseId());
            detail.setTo_zone_id(item.getToZoneId());
            detail.setTo_location_id(item.getToLocationId());
            adjustmentOrderDetailMapper.save(detail);

            lockLocationIds.add(item.getFromLocationId());
            lockLocationIds.add(item.getToLocationId());
        }

        // 5. INSERT work_task
        WorkTask task = new WorkTask();
        task.setTask_type("库存调整");
        task.setPriority(request.getPriority());
        task.setRelated_order_no(adjustmentNo);
        task.setRelated_batch_id(null);
        task.setGoods_id(null);
        task.setSource_location_id(null);
        task.setTarget_location_id(null);
        task.setAssignee_id(request.getAssigneeId());
        task.setDeadline(request.getDeadline() != null && !request.getDeadline().isEmpty()
                ? LocalDateTime.parse(request.getDeadline() + "T23:59:59") : null);
        task.setCreated_time(LocalDateTime.now());
        task.setCompleted_time(null);
        task.setRemark(null);
        workTaskMapper.save(task);

        // 6. 锁定所有涉及的库位
        if (!lockLocationIds.isEmpty()) {
            locationMapper.batchUpdateLockStatus(new ArrayList<>(lockLocationIds), "锁定", "调整");
        }

        // 7. INSERT operation_log
        OperationLog log = new OperationLog();
        log.setOperator_id(request.getOperatorId());
        log.setOperation_type("库存调整");
        log.setOperation_content("申请调整");
        log.setOperation_result("成功");
        log.setOperation_time(LocalDateTime.now());
        operationLogMapper.save(log);

        return task;
    }

    @Override
    @Transactional
    public void rejectAdjustment(Integer taskId, Integer operatorId) {
        WorkTask task = workTaskMapper.findById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在: " + taskId);
        }

        String adjustmentNo = task.getRelated_order_no();

        // 查所有明细，收集涉及的库位
        ArrayList<AdjustmentOrderDetail> details = adjustmentOrderDetailMapper.findByAdjustmentNo(adjustmentNo);
        Set<Integer> lockLocationIds = new LinkedHashSet<>();
        for (AdjustmentOrderDetail detail : details) {
            if (detail.getFrom_location_id() != null) lockLocationIds.add(detail.getFrom_location_id());
            if (detail.getTo_location_id() != null) lockLocationIds.add(detail.getTo_location_id());
        }

        // 解锁库位
        if (!lockLocationIds.isEmpty()) {
            locationMapper.batchUpdateLockStatus(new ArrayList<>(lockLocationIds), "未锁定", null);
        }

        // 删除明细、头、任务
        adjustmentOrderDetailMapper.deleteByAdjustmentNo(adjustmentNo);
        adjustmentOrderHeadMapper.deleteById(adjustmentNo);
        workTaskMapper.deleteById(taskId);

        // 操作日志
        OperationLog log = new OperationLog();
        log.setOperator_id(operatorId);
        log.setOperation_type("库存调整");
        log.setOperation_content("退回调整请求");
        log.setOperation_result("成功");
        log.setOperation_time(LocalDateTime.now());
        operationLogMapper.save(log);
    }

    @Override
    @Transactional
    public void completeAdjustment(Integer taskId, Integer operatorId) {
        WorkTask task = workTaskMapper.findById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在: " + taskId);
        }

        String adjustmentNo = task.getRelated_order_no();
        ArrayList<AdjustmentOrderDetail> details = adjustmentOrderDetailMapper.findByAdjustmentNo(adjustmentNo);
        Set<Integer> lockLocationIds = new LinkedHashSet<>();
        Map<Integer, Integer> zoneDeltaMap = new HashMap<>();
        Map<Integer, Integer> whDeltaMap = new HashMap<>();

        for (AdjustmentOrderDetail detail : details) {
            // 更新库存：将货物从原库位移到新库位
            InventoryVO invVO = inventoryMapper.findByLocationId(detail.getFrom_location_id());
            if (invVO != null) {
                Inventory inv = new Inventory();
                inv.setInventory_id(invVO.getInventory_id());
                inv.setWarehouse_id(detail.getTo_warehouse_id());
                inv.setZone_id(detail.getTo_zone_id());
                inv.setLocation_id(detail.getTo_location_id());
                inventoryMapper.update(inv);
            }

            // 标记原库位空闲
            Location fromLoc = locationMapper.findById(detail.getFrom_location_id());
            if (fromLoc != null) {
                locationMapper.freeLocation(fromLoc.getLocation_id());
                // 原库位释放，对应库区+仓库空位+1
                Zone fromZone = zoneMapper.findById(fromLoc.getZone_id());
                if (fromZone != null) {
                    zoneDeltaMap.merge(fromZone.getZone_id(), 1, Integer::sum);
                    whDeltaMap.merge(fromZone.getWarehouse_id(), 1, Integer::sum);
                }
            }

            // 标记新库位占用
            Location toLoc = locationMapper.findById(detail.getTo_location_id());
            if (toLoc != null) {
                locationMapper.occupyLocation(toLoc.getLocation_id(), detail.getGoods_id());
                // 新库位占用，对应库区+仓库空位-1
                Zone toZone = zoneMapper.findById(toLoc.getZone_id());
                if (toZone != null) {
                    zoneDeltaMap.merge(toZone.getZone_id(), -1, Integer::sum);
                    whDeltaMap.merge(toZone.getWarehouse_id(), -1, Integer::sum);
                }
            }

            if (detail.getFrom_location_id() != null) lockLocationIds.add(detail.getFrom_location_id());
            if (detail.getTo_location_id() != null) lockLocationIds.add(detail.getTo_location_id());
        }

        // 批量更新库区空位余量
        for (Map.Entry<Integer, Integer> entry : zoneDeltaMap.entrySet()) {
            if (entry.getValue() != 0) {
                zoneMapper.updateAvailableSlots(entry.getKey(), entry.getValue());
            }
        }

        // 批量更新仓库空位余量
        for (Map.Entry<Integer, Integer> entry : whDeltaMap.entrySet()) {
            if (entry.getValue() != 0) {
                warehouseMapper.updateAvailableSlots(entry.getKey(), entry.getValue());
            }
        }

        // 解锁库位
        if (!lockLocationIds.isEmpty()) {
            locationMapper.batchUpdateLockStatus(new ArrayList<>(lockLocationIds), "未锁定", null);
        }

        // 更新调整单头
        AdjustmentOrderHead head = adjustmentOrderHeadMapper.findById(adjustmentNo);
        if (head != null) {
            head.setOrder_status("已完成");
            head.setAdjustment_time(LocalDateTime.now());
            adjustmentOrderHeadMapper.update(head);
        }

        // 完成任务
        task.setCompleted_time(LocalDateTime.now());
        workTaskMapper.update(task);

        // 操作日志
        OperationLog log = new OperationLog();
        log.setOperator_id(operatorId);
        log.setOperation_type("库存调整");
        log.setOperation_content("完成调整");
        log.setOperation_result("成功");
        log.setOperation_time(LocalDateTime.now());
        operationLogMapper.save(log);
    }

    @Override
    public ArrayList<Map<String, Object>> getAdjustmentDetail(Integer taskId) {
        WorkTask task = workTaskMapper.findById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在: " + taskId);
        }

        String adjustmentNo = task.getRelated_order_no();
        ArrayList<AdjustmentOrderDetail> details = adjustmentOrderDetailMapper.findByAdjustmentNo(adjustmentNo);
        ArrayList<Map<String, Object>> result = new ArrayList<>();

        for (AdjustmentOrderDetail detail : details) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("detailId", detail.getDetail_id());
            map.put("goodsId", detail.getGoods_id());
            map.put("batchId", detail.getBatch_id());
            map.put("quantity", detail.getQuantity());

            Goods goods = goodsMapper.findById(detail.getGoods_id());
            map.put("goodsName", goods != null ? goods.getGoods_name() : "-");

            Batch batch = batchMapper.findById(detail.getBatch_id());
            map.put("batchNo", batch != null ? batch.getBatch_id() : detail.getBatch_id());

            Location fromLoc = locationMapper.findById(detail.getFrom_location_id());
            map.put("fromLocationName", fromLoc != null ? fromLoc.getLocation_name() : "-");

            Location toLoc = locationMapper.findById(detail.getTo_location_id());
            map.put("toLocationName", toLoc != null ? toLoc.getLocation_name() : "-");

            result.add(map);
        }

        return result;
    }
}
