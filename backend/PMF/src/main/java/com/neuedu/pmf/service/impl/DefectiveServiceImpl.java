package com.neuedu.pmf.service.impl;

import com.neuedu.pmf.entity.*;
import com.neuedu.pmf.mapper.*;
import com.neuedu.pmf.service.DefectiveService;
import com.neuedu.pmf.vo.CreateDefectiveRequest;
import com.neuedu.pmf.vo.InventoryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class DefectiveServiceImpl implements DefectiveService {

    @Autowired
    private DefectiveHandlingHeadMapper defectiveHandlingHeadMapper;

    @Autowired
    private DefectiveHandlingDetailMapper defectiveHandlingDetailMapper;

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

    @Autowired
    private OutboundOrderDetailMapper outboundOrderDetailMapper;

    @Override
    @Transactional
    public WorkTask createDefectiveTask(CreateDefectiveRequest request) {
        // 1. 生成处理单号 D + yyyyMMdd + 3位序号
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "D" + today;
        List<String> todayNos = defectiveHandlingHeadMapper.findTodayHandlingNos(prefix + "%");
        int maxSeq = 0;
        for (String no : todayNos) {
            try {
                int seq = Integer.parseInt(no.substring(9));
                if (seq > maxSeq) maxSeq = seq;
            } catch (NumberFormatException ignored) {}
        }
        String handlingNo = prefix + String.format("%03d", maxSeq + 1);

        // 2. INSERT defective_handling_head
        DefectiveHandlingHead head = new DefectiveHandlingHead();
        head.setHandling_no(handlingNo);
        head.setHandling_type("报废");
        head.setOrder_status("待处理");
        head.setOperator_id(request.getAssigneeId());
        head.setHandling_time(null);
        head.setRemark(null);
        defectiveHandlingHeadMapper.save(head);

        // 3. 收集需要锁定的库位
        Set<Integer> lockLocationIds = new LinkedHashSet<>();
        Map<Integer, Integer> inventoryLockDelta = new LinkedHashMap<>();

        // 4. 遍历 items INSERT defective_handling_detail
        for (CreateDefectiveRequest.DefectiveItem item : request.getItems()) {
            DefectiveHandlingDetail detail = new DefectiveHandlingDetail();
            detail.setHandling_no(handlingNo);
            detail.setGoods_id(item.getGoodsId());
            detail.setBatch_id(item.getBatchId());
            detail.setQuantity(item.getQuantity());
            detail.setSource_location_id(item.getSourceLocationId());
            detail.setTarget_location_id(null);
            defectiveHandlingDetailMapper.save(detail);

            if (item.getSourceLocationId() != null) {
                lockLocationIds.add(item.getSourceLocationId());

                InventoryVO invVO = inventoryMapper.findByLocationIdAllStatus(item.getSourceLocationId());
                if (invVO != null) {
                    inventoryLockDelta.merge(invVO.getInventory_id(), item.getQuantity(), Integer::sum);
                }
            }
        }

        // 5. INSERT work_task
        WorkTask task = new WorkTask();
        task.setTask_type("处理不合格货物");
        task.setPriority(request.getPriority());
        task.setRelated_order_no(handlingNo);
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

        // 6. 锁定库位
        if (!lockLocationIds.isEmpty()) {
            locationMapper.batchUpdateLockStatus(new ArrayList<>(lockLocationIds), "锁定", "调整");
        }

        // 7. 更新库存锁定数量
        for (Map.Entry<Integer, Integer> entry : inventoryLockDelta.entrySet()) {
            Inventory inv = inventoryMapper.findById(entry.getKey());
            if (inv != null) {
                inv.setLocked_quantity(inv.getLocked_quantity() + entry.getValue());
                inventoryMapper.update(inv);
            }
        }

        // 8. 操作日志
        OperationLog log = new OperationLog();
        log.setOperator_id(request.getOperatorId());
        log.setOperation_type("处理次品");
        log.setOperation_content("次品处理请求");
        log.setOperation_result("成功");
        log.setOperation_time(LocalDateTime.now());
        operationLogMapper.save(log);

        return task;
    }

    @Override
    @Transactional
    public void rejectDefective(Integer taskId, Integer operatorId) {
        WorkTask task = workTaskMapper.findById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在: " + taskId);
        }

        String handlingNo = task.getRelated_order_no();
        ArrayList<DefectiveHandlingDetail> details = defectiveHandlingDetailMapper.findByHandlingNo(handlingNo);

        Set<Integer> lockLocationIds = new LinkedHashSet<>();
        Map<Integer, Integer> inventoryUnlockDelta = new LinkedHashMap<>();

        for (DefectiveHandlingDetail detail : details) {
            if (detail.getSource_location_id() != null) {
                lockLocationIds.add(detail.getSource_location_id());

                InventoryVO invVO = inventoryMapper.findByLocationIdAllStatus(detail.getSource_location_id());
                if (invVO != null) {
                    inventoryUnlockDelta.merge(invVO.getInventory_id(), detail.getQuantity(), Integer::sum);
                }
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
        defectiveHandlingDetailMapper.deleteByHandlingNo(handlingNo);
        defectiveHandlingHeadMapper.deleteById(handlingNo);
        workTaskMapper.deleteById(taskId);

        // 操作日志
        OperationLog log = new OperationLog();
        log.setOperator_id(operatorId);
        log.setOperation_type("处理次品");
        log.setOperation_content("退回次品处理请求");
        log.setOperation_result("成功");
        log.setOperation_time(LocalDateTime.now());
        operationLogMapper.save(log);
    }

    @Override
    @Transactional
    public void completeDefective(Integer taskId, Integer operatorId) {
        WorkTask task = workTaskMapper.findById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在: " + taskId);
        }

        String handlingNo = task.getRelated_order_no();
        ArrayList<DefectiveHandlingDetail> details = defectiveHandlingDetailMapper.findByHandlingNo(handlingNo);

        Set<Integer> lockLocationIds = new LinkedHashSet<>();
        Map<Integer, Integer> zoneDeltaMap = new HashMap<>();
        Map<Integer, Integer> whDeltaMap = new HashMap<>();
        Map<Integer, Integer> goodsDeltaMap = new HashMap<>();
        Set<String> batchIdsToDelete = new LinkedHashSet<>();

        for (DefectiveHandlingDetail detail : details) {
            if (detail.getSource_location_id() != null) {
                lockLocationIds.add(detail.getSource_location_id());

                // 删除库存记录
                InventoryVO invVO = inventoryMapper.findByLocationIdAllStatus(detail.getSource_location_id());
                if (invVO != null) {
                    inventoryMapper.deleteById(invVO.getInventory_id());
                }

                // 释放库位
                Location loc = locationMapper.findById(detail.getSource_location_id());
                if (loc != null) {
                    locationMapper.freeLocation(loc.getLocation_id());
                    Zone zone = zoneMapper.findById(loc.getZone_id());
                    if (zone != null) {
                        zoneDeltaMap.merge(zone.getZone_id(), 1, Integer::sum);
                        whDeltaMap.merge(zone.getWarehouse_id(), 1, Integer::sum);
                    }
                }
            }

            // 汇总货物变更（仅已入库货物才扣减goods.quantity，未入库货物从未增加过goods数量）
            if (detail.getGoods_id() != null && detail.getSource_location_id() != null) {
                goodsDeltaMap.merge(detail.getGoods_id(), detail.getQuantity(), Integer::sum);
            }

            // 记录需要删除的批次
            if (detail.getBatch_id() != null) {
                batchIdsToDelete.add(detail.getBatch_id());
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

        // 解锁库位
        if (!lockLocationIds.isEmpty()) {
            locationMapper.batchUpdateLockStatus(new ArrayList<>(lockLocationIds), "未锁定", null);
        }

        // 删除明细、头、任务
        defectiveHandlingDetailMapper.deleteByHandlingNo(handlingNo);
        defectiveHandlingHeadMapper.deleteById(handlingNo);
        workTaskMapper.deleteById(taskId);

        // 删除其他引用批次的记录（避免外键约束冲突）
        for (String batchId : batchIdsToDelete) {
            qualityCheckDetailMapper.deleteByBatchId(batchId);
            inventoryCheckDetailMapper.deleteByBatchId(batchId);
            outboundOrderDetailMapper.deleteByBatchId(batchId);
            inventoryMapper.deleteByBatchId(batchId);
        }

        // 删除批次
        for (String batchId : batchIdsToDelete) {
            batchMapper.deleteById(batchId);
        }

        // 操作日志
        OperationLog log = new OperationLog();
        log.setOperator_id(operatorId);
        log.setOperation_type("处理次品");
        log.setOperation_content("完成次品处理");
        log.setOperation_result("成功");
        log.setOperation_time(LocalDateTime.now());
        operationLogMapper.save(log);
    }

    @Override
    public ArrayList<Map<String, Object>> getDefectiveDetail(Integer taskId) {
        WorkTask task = workTaskMapper.findById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在: " + taskId);
        }

        String handlingNo = task.getRelated_order_no();
        ArrayList<DefectiveHandlingDetail> details = defectiveHandlingDetailMapper.findByHandlingNo(handlingNo);
        ArrayList<Map<String, Object>> result = new ArrayList<>();

        for (DefectiveHandlingDetail detail : details) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("detailId", detail.getDetail_id());
            map.put("batchId", detail.getBatch_id());
            map.put("goodsId", detail.getGoods_id());
            map.put("quantity", detail.getQuantity());

            Goods goods = goodsMapper.findById(detail.getGoods_id());
            map.put("goodsName", goods != null ? goods.getGoods_name() : "-");

            if (detail.getSource_location_id() != null) {
                Location loc = locationMapper.findById(detail.getSource_location_id());
                map.put("locationName", loc != null ? loc.getLocation_name() : "-");
            } else {
                map.put("locationName", null);
            }

            result.add(map);
        }

        return result;
    }
}
