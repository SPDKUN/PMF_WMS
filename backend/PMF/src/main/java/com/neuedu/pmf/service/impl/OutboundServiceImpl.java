package com.neuedu.pmf.service.impl;

import com.neuedu.pmf.entity.*;
import com.neuedu.pmf.mapper.*;
import com.neuedu.pmf.service.OutboundService;
import com.neuedu.pmf.vo.CreateOutboundRequest;
import com.neuedu.pmf.vo.InventoryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class OutboundServiceImpl implements OutboundService {

    @Autowired
    private OutboundOrderHeadMapper outboundOrderHeadMapper;

    @Autowired
    private OutboundOrderDetailMapper outboundOrderDetailMapper;

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

    @Autowired
    private QualityCheckDetailMapper qualityCheckDetailMapper;

    @Autowired
    private InventoryCheckDetailMapper inventoryCheckDetailMapper;

    @Override
    @Transactional
    public WorkTask createOutboundTask(CreateOutboundRequest request) {
        // 1. 生成出库单号 O + yyyyMMdd + 3位序号
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "O" + today;
        List<String> todayNos = outboundOrderHeadMapper.findTodayOutboundNos(prefix + "%");
        int maxSeq = 0;
        for (String no : todayNos) {
            try {
                int seq = Integer.parseInt(no.substring(9));
                if (seq > maxSeq) maxSeq = seq;
            } catch (NumberFormatException ignored) {}
        }
        String outboundNo = prefix + String.format("%03d", maxSeq + 1);

        // 2. 获取第一个货物的名称用于日志
        String goodsName = "";
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            Goods firstGoods = goodsMapper.findById(request.getItems().get(0).getGoodsId());
            if (firstGoods != null) {
                goodsName = firstGoods.getGoods_name();
            }
        }

        // 3. INSERT outbound_order_head
        OutboundOrderHead head = new OutboundOrderHead();
        head.setOutbound_no(outboundNo);
        head.setOutbound_type("普通出库");
        head.setOrder_status("草稿");
        head.setOperator_id(request.getAssigneeId());
        head.setPriority(null);
        head.setDeadline(request.getDeadline() != null && !request.getDeadline().isEmpty()
                ? LocalDateTime.parse(request.getDeadline() + "T23:59:59") : null);
        head.setOutbound_time(null);
        head.setRemark(null);
        head.setCreate_time(LocalDateTime.now());
        outboundOrderHeadMapper.save(head);

        // 4. 收集需要锁定的库位ID 和 需要更新锁定数量的库存
        Set<Integer> lockLocationIds = new LinkedHashSet<>();
        Map<Integer, Integer> inventoryLockDelta = new LinkedHashMap<>(); // inventory_id -> lock quantity delta

        // 5. 遍历 items INSERT outbound_order_detail
        for (CreateOutboundRequest.OutboundItem item : request.getItems()) {
            OutboundOrderDetail detail = new OutboundOrderDetail();
            detail.setOutbound_no(outboundNo);
            detail.setGoods_id(item.getGoodsId());
            detail.setBatch_id(item.getBatchId());
            detail.setQuantity(item.getQuantity());
            detail.setWarehouse_id(item.getWarehouseId());
            detail.setZone_id(item.getZoneId());
            detail.setLocation_id(item.getLocationId());
            outboundOrderDetailMapper.save(detail);

            lockLocationIds.add(item.getLocationId());

            // 查找该库位对应的库存记录
            InventoryVO invVO = inventoryMapper.findByLocationId(item.getLocationId());
            if (invVO != null) {
                inventoryLockDelta.merge(invVO.getInventory_id(), item.getQuantity(), Integer::sum);
            }
        }

        // 6. INSERT work_task
        WorkTask task = new WorkTask();
        task.setTask_type("出库");
        task.setPriority(request.getPriority());
        task.setRelated_order_no(outboundNo);
        task.setRelated_batch_id(null);
        task.setGoods_id(request.getItems().get(0).getGoodsId());
        task.setSource_location_id(null);
        task.setTarget_location_id(null);
        task.setAssignee_id(request.getAssigneeId());
        task.setDeadline(request.getDeadline() != null && !request.getDeadline().isEmpty()
                ? LocalDateTime.parse(request.getDeadline() + "T23:59:59") : null);
        task.setCreated_time(LocalDateTime.now());
        task.setCompleted_time(null);
        task.setRemark(null);
        workTaskMapper.save(task);

        // 7. 锁定所有涉及的库位
        if (!lockLocationIds.isEmpty()) {
            locationMapper.batchUpdateLockStatus(new ArrayList<>(lockLocationIds), "锁定", "出库");
        }

        // 8. 更新库存锁定数量
        for (Map.Entry<Integer, Integer> entry : inventoryLockDelta.entrySet()) {
            Inventory inv = inventoryMapper.findById(entry.getKey());
            if (inv != null) {
                inv.setLocked_quantity(inv.getLocked_quantity() + entry.getValue());
                inventoryMapper.update(inv);
            }
        }

        // 9. INSERT operation_log
        OperationLog log = new OperationLog();
        log.setOperator_id(request.getOperatorId());
        log.setOperation_type("出库");
        log.setOperation_content(goodsName + "申请出库");
        log.setOperation_result("成功");
        log.setOperation_time(LocalDateTime.now());
        operationLogMapper.save(log);

        return task;
    }

    @Override
    @Transactional
    public void rejectOutbound(Integer taskId, Integer operatorId) {
        WorkTask task = workTaskMapper.findById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在: " + taskId);
        }

        String outboundNo = task.getRelated_order_no();
        ArrayList<OutboundOrderDetail> details = outboundOrderDetailMapper.findByOutboundNo(outboundNo);

        // 收集库位ID用于解锁
        Set<Integer> lockLocationIds = new LinkedHashSet<>();
        Map<Integer, Integer> inventoryUnlockDelta = new LinkedHashMap<>();

        String goodsName = "";
        for (OutboundOrderDetail detail : details) {
            if (detail.getLocation_id() != null) {
                lockLocationIds.add(detail.getLocation_id());
            }
            if (goodsName.isEmpty() && detail.getGoods_id() != null) {
                Goods goods = goodsMapper.findById(detail.getGoods_id());
                if (goods != null) goodsName = goods.getGoods_name();
            }

            // 计算需要解锁的库存数量
            InventoryVO invVO = inventoryMapper.findByLocationId(detail.getLocation_id());
            if (invVO != null) {
                inventoryUnlockDelta.merge(invVO.getInventory_id(), detail.getQuantity(), Integer::sum);
            }
        }

        // 解锁库位
        if (!lockLocationIds.isEmpty()) {
            locationMapper.batchUpdateLockStatus(new ArrayList<>(lockLocationIds), "未锁定", null);
        }

        // 恢复库存锁定数量
        for (Map.Entry<Integer, Integer> entry : inventoryUnlockDelta.entrySet()) {
            Inventory inv = inventoryMapper.findById(entry.getKey());
            if (inv != null) {
                int newLocked = Math.max(0, inv.getLocked_quantity() - entry.getValue());
                inv.setLocked_quantity(newLocked);
                inventoryMapper.update(inv);
            }
        }

        // 删除明细、头、任务
        outboundOrderDetailMapper.deleteByOutboundNo(outboundNo);
        outboundOrderHeadMapper.deleteById(outboundNo);
        workTaskMapper.deleteById(taskId);

        // 操作日志
        OperationLog log = new OperationLog();
        log.setOperator_id(operatorId);
        log.setOperation_type("出库");
        log.setOperation_content(goodsName + "退回出库请求");
        log.setOperation_result("成功");
        log.setOperation_time(LocalDateTime.now());
        operationLogMapper.save(log);
    }

    @Override
    @Transactional
    public void completeOutbound(Integer taskId, Integer operatorId) {
        WorkTask task = workTaskMapper.findById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在: " + taskId);
        }

        String outboundNo = task.getRelated_order_no();
        ArrayList<OutboundOrderDetail> details = outboundOrderDetailMapper.findByOutboundNo(outboundNo);

        Set<Integer> lockLocationIds = new LinkedHashSet<>();
        Map<Integer, Integer> zoneDeltaMap = new HashMap<>();
        Map<Integer, Integer> whDeltaMap = new HashMap<>();
        Map<Integer, Integer> goodsDeltaMap = new HashMap<>(); // goods_id -> total outbound qty
        Map<String, Integer> batchDeltaMap = new LinkedHashMap<>(); // batch_id -> total outbound qty

        String goodsName = "";

        for (OutboundOrderDetail detail : details) {
            if (detail.getLocation_id() != null) {
                lockLocationIds.add(detail.getLocation_id());
            }

            if (goodsName.isEmpty() && detail.getGoods_id() != null) {
                Goods g = goodsMapper.findById(detail.getGoods_id());
                if (g != null) goodsName = g.getGoods_name();
            }

            // 更新库存
            InventoryVO invVO = inventoryMapper.findByLocationId(detail.getLocation_id());
            if (invVO != null) {
                int newQty = invVO.getQuantity() - detail.getQuantity();
                int newLocked = Math.max(0, invVO.getLocked_quantity() - detail.getQuantity());

                if (newQty <= 0) {
                    // 库存数量归零，删除库存记录，释放库位
                    inventoryMapper.deleteById(invVO.getInventory_id());

                    Location loc = locationMapper.findById(detail.getLocation_id());
                    if (loc != null) {
                        locationMapper.freeLocation(loc.getLocation_id());
                        Zone zone = zoneMapper.findById(loc.getZone_id());
                        if (zone != null) {
                            zoneDeltaMap.merge(zone.getZone_id(), 1, Integer::sum);
                            whDeltaMap.merge(zone.getWarehouse_id(), 1, Integer::sum);
                        }
                    }
                } else {
                    // 更新库存数量和锁定数量
                    Inventory inv = new Inventory();
                    inv.setInventory_id(invVO.getInventory_id());
                    inv.setQuantity(newQty);
                    inv.setLocked_quantity(newLocked);
                    inventoryMapper.update(inv);
                }
            }

            // 汇总货物和批次变更
            if (detail.getGoods_id() != null) {
                goodsDeltaMap.merge(detail.getGoods_id(), detail.getQuantity(), Integer::sum);
            }
            if (detail.getBatch_id() != null) {
                batchDeltaMap.merge(detail.getBatch_id(), detail.getQuantity(), Integer::sum);
            }
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

        // 更新货物数量
        for (Map.Entry<Integer, Integer> entry : goodsDeltaMap.entrySet()) {
            Goods goods = goodsMapper.findById(entry.getKey());
            if (goods != null) {
                goods.setQuantity(Math.max(0, goods.getQuantity() - entry.getValue()));
                goodsMapper.update(goods);
            }
        }

        // 更新批次剩余数量，数量归零则删除
        for (Map.Entry<String, Integer> entry : batchDeltaMap.entrySet()) {
            Batch batch = batchMapper.findById(entry.getKey());
            if (batch != null) {
                batch.setRemaining_quantity(Math.max(0, batch.getRemaining_quantity() - entry.getValue()));
                batchMapper.update(batch);
                if (batch.getRemaining_quantity() <= 0) {
                    qualityCheckDetailMapper.deleteByBatchId(batch.getBatch_id());
                    inventoryCheckDetailMapper.deleteByBatchId(batch.getBatch_id());
                    inventoryMapper.deleteByBatchId(batch.getBatch_id());
                    batchMapper.deleteById(batch.getBatch_id());
                }
            }
        }

        // 解锁库位
        if (!lockLocationIds.isEmpty()) {
            locationMapper.batchUpdateLockStatus(new ArrayList<>(lockLocationIds), "未锁定", null);
        }

        // 更新出库单头
        OutboundOrderHead head = outboundOrderHeadMapper.findById(outboundNo);
        if (head != null) {
            head.setOrder_status("已完成");
            head.setOutbound_time(LocalDateTime.now());
            outboundOrderHeadMapper.update(head);
        }

        // 完成任务
        task.setCompleted_time(LocalDateTime.now());
        workTaskMapper.update(task);

        // 操作日志
        OperationLog log = new OperationLog();
        log.setOperator_id(operatorId);
        log.setOperation_type("出库");
        log.setOperation_content(goodsName + "出库成功");
        log.setOperation_result("成功");
        log.setOperation_time(LocalDateTime.now());
        operationLogMapper.save(log);
    }

    @Override
    public ArrayList<Map<String, Object>> getOutboundDetail(Integer taskId) {
        WorkTask task = workTaskMapper.findById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在: " + taskId);
        }

        String outboundNo = task.getRelated_order_no();
        ArrayList<OutboundOrderDetail> details = outboundOrderDetailMapper.findByOutboundNo(outboundNo);
        ArrayList<Map<String, Object>> result = new ArrayList<>();

        for (OutboundOrderDetail detail : details) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("detailId", detail.getDetail_id());
            map.put("goodsId", detail.getGoods_id());
            map.put("batchId", detail.getBatch_id());
            map.put("quantity", detail.getQuantity());
            map.put("locationId", detail.getLocation_id());

            Goods goods = goodsMapper.findById(detail.getGoods_id());
            map.put("goodsName", goods != null ? goods.getGoods_name() : "-");

            Batch batch = batchMapper.findById(detail.getBatch_id());
            map.put("batchNo", batch != null ? batch.getBatch_id() : detail.getBatch_id());

            Location loc = locationMapper.findById(detail.getLocation_id());
            map.put("locationName", loc != null ? loc.getLocation_name() : "-");

            result.add(map);
        }

        return result;
    }

    @Override
    public ArrayList<InventoryVO> getAvailableInventory(Integer goodsId) {
        return inventoryMapper.findByGoodsId(goodsId);
    }
}
