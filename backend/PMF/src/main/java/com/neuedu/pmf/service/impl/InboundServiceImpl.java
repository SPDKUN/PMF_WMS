package com.neuedu.pmf.service.impl;

import com.neuedu.pmf.entity.*;
import com.neuedu.pmf.mapper.*;
import com.neuedu.pmf.service.InboundService;
import com.neuedu.pmf.vo.CreateInboundRequest;
import com.neuedu.pmf.vo.CompleteInboundRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class InboundServiceImpl implements InboundService {

    @Autowired
    private InboundOrderHeadMapper inboundOrderHeadMapper;

    @Autowired
    private InboundOrderDetailMapper inboundOrderDetailMapper;

    @Autowired
    private WorkTaskMapper workTaskMapper;

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Autowired
    private BatchMapper batchMapper;

    @Autowired
    private InventoryMapper inventoryMapper;

    @Autowired
    private LocationMapper locationMapper;

    @Autowired
    private ZoneMapper zoneMapper;

    @Autowired
    private WarehouseMapper warehouseMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Override
    public ArrayList<Batch> getAvailableBatches() {
        // 1. 所有待入库库存
        ArrayList<Inventory> allInventory = inventoryMapper.list();
        Set<String> pendingBatchIds = new LinkedHashSet<>();
        for (Inventory inv : allInventory) {
            if ("待入库".equals(inv.getInventory_status())) {
                pendingBatchIds.add(inv.getBatch_id());
            }
        }

        // 2. 正在进行中的入库任务对应的batch
        ArrayList<WorkTask> allTasks = workTaskMapper.list();
        Set<String> inProgressBatchIds = new HashSet<>();
        for (WorkTask task : allTasks) {
            if ("入库".equals(task.getTask_type()) && task.getCompleted_time() == null) {
                if (task.getRelated_batch_id() != null) {
                    inProgressBatchIds.add(task.getRelated_batch_id());
                }
            }
        }

        // 3. 过滤
        pendingBatchIds.removeAll(inProgressBatchIds);

        // 4. 获取batch记录
        ArrayList<Batch> result = new ArrayList<>();
        for (String batchId : pendingBatchIds) {
            Batch batch = batchMapper.findById(batchId);
            if (batch != null) {
                result.add(batch);
            }
        }
        return result;
    }

    @Override
    @Transactional
    public WorkTask createInboundTask(CreateInboundRequest request) {
        // 1. 生成入库单号
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "I" + today;
        java.util.List<String> todayNos = inboundOrderHeadMapper.findTodayInboundNos(prefix + "%");
        int maxSeq = 0;
        for (String no : todayNos) {
            try {
                int seq = Integer.parseInt(no.substring(9));
                if (seq > maxSeq) maxSeq = seq;
            } catch (NumberFormatException ignored) {}
        }
        String inboundNo = prefix + String.format("%03d", maxSeq + 1);

        // 2. 查批次获取goods_id
        Batch batch = batchMapper.findById(request.getBatchId());
        if (batch == null) {
            throw new RuntimeException("批次不存在: " + request.getBatchId());
        }

        // 3. INSERT inbound_order_head
        InboundOrderHead head = new InboundOrderHead();
        head.setInbound_no(inboundNo);
        head.setInbound_type(request.getInboundType());
        head.setOrder_status("草稿");
        head.setOperator_id(request.getOperatorId());
        head.setInbound_time(null);
        head.setRemark(request.getRemark());
        inboundOrderHeadMapper.save(head);

        // 4. INSERT inbound_order_detail
        InboundOrderDetail detail = new InboundOrderDetail();
        detail.setInbound_no(inboundNo);
        detail.setGoods_id(batch.getGoods_id());
        detail.setBatch_no(request.getBatchId());
        detail.setProduction_date(batch.getProduction_date());
        detail.setExpiry_date(batch.getExpiry_date());
        detail.setQuantity(batch.getInitial_quantity());
        detail.setWarehouse_id(null);
        detail.setZone_id(null);
        detail.setLocation_id(null);
        inboundOrderDetailMapper.save(detail);

        // 5. INSERT work_task
        WorkTask task = new WorkTask();
        task.setTask_type("入库");
        task.setPriority(request.getPriority());
        task.setRelated_order_no(inboundNo);
        task.setRelated_batch_id(request.getBatchId());
        task.setGoods_id(batch.getGoods_id());
        task.setSource_location_id(null);
        task.setTarget_location_id(null);
        task.setAssignee_id(request.getOperatorId());
        task.setDeadline(request.getDeadline() != null && !request.getDeadline().isEmpty()
                ? LocalDateTime.parse(request.getDeadline() + "T23:59:59") : null);
        task.setCreated_time(LocalDateTime.now());
        task.setCompleted_time(null);
        task.setRemark(request.getRemark());
        workTaskMapper.save(task);

        // 6. INSERT operation_log
        OperationLog log = new OperationLog();
        log.setOperator_id(request.getOperatorId());
        log.setOperation_type("入库");
        log.setOperation_content(request.getBatchId() + "申请入库");
        log.setOperation_result("成功");
        log.setOperation_time(LocalDateTime.now());
        operationLogMapper.save(log);

        return task;
    }

    @Override
    @Transactional
    public void rejectInbound(Integer taskId, Integer operatorId) {
        WorkTask task = workTaskMapper.findById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在: " + taskId);
        }

        String inboundNo = task.getRelated_order_no();
        String batchId = task.getRelated_batch_id();

        inboundOrderDetailMapper.deleteByInboundNo(inboundNo);
        inboundOrderHeadMapper.deleteById(inboundNo);
        workTaskMapper.deleteById(taskId);

        OperationLog log = new OperationLog();
        log.setOperator_id(operatorId);
        log.setOperation_type("入库");
        log.setOperation_content(batchId + "退回入库请求");
        log.setOperation_result("成功");
        log.setOperation_time(LocalDateTime.now());
        operationLogMapper.save(log);
    }

    @Override
    @Transactional
    public void completeInbound(Integer taskId, CompleteInboundRequest request) {
        // 1. 查工作任务
        WorkTask task = workTaskMapper.findById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在: " + taskId);
        }

        String inboundNo = task.getRelated_order_no();
        String batchId = task.getRelated_batch_id();

        // 2. 查批次
        Batch batch = batchMapper.findById(batchId);
        if (batch == null) {
            throw new RuntimeException("批次不存在: " + batchId);
        }

        Integer goodsId = batch.getGoods_id();
        Integer initialQty = batch.getInitial_quantity();

        // 3. 验证库位数量
        int totalNeeded = (int) Math.ceil(initialQty / 10.0);
        List<Integer> locationIds = request.getLocationIds();
        if (locationIds.size() != totalNeeded) {
            throw new RuntimeException("需要选择恰好" + totalNeeded + "个库位，当前选择" + locationIds.size() + "个");
        }

        // 4. 先删除旧库存（待入库状态），再创建新库存
        inventoryMapper.deleteByBatchId(batchId);

        // 5. 遍历库位分配
        Map<Integer, Integer> zoneCounts = new HashMap<>();
        Map<Integer, Integer> whCounts = new HashMap<>();

        for (int i = 0; i < locationIds.size(); i++) {
            Integer locationId = locationIds.get(i);
            Location loc = locationMapper.findById(locationId);
            if (loc == null) {
                throw new RuntimeException("库位不存在: " + locationId);
            }

            int qty = (i < totalNeeded - 1) ? 10 : (initialQty - (totalNeeded - 1) * 10);

            // a. 标记库位已占用
            loc.setIs_occupied(1);
            locationMapper.update(loc);

            // b. 查库区→仓库
            Zone zone = zoneMapper.findById(loc.getZone_id());
            Integer zoneId = zone.getZone_id();
            Integer whId = zone.getWarehouse_id();

            // c. 创建库存记录
            Inventory inv = new Inventory();
            inv.setGoods_id(goodsId);
            inv.setBatch_id(batchId);
            inv.setWarehouse_id(whId);
            inv.setZone_id(zoneId);
            inv.setLocation_id(locationId);
            inv.setQuantity(qty);
            inv.setLocked_quantity(0);
            inv.setInventory_status("正常");
            inventoryMapper.save(inv);

            // d. 计数
            zoneCounts.merge(zoneId, 1, Integer::sum);
            whCounts.merge(whId, 1, Integer::sum);
        }

        // 6. 更新库区可用库位数
        for (Map.Entry<Integer, Integer> entry : zoneCounts.entrySet()) {
            Zone zone = zoneMapper.findById(entry.getKey());
            zone.setAvailable_slots(zone.getAvailable_slots() - entry.getValue());
            zoneMapper.update(zone);
        }

        // 7. 更新仓库可用库位数
        for (Map.Entry<Integer, Integer> entry : whCounts.entrySet()) {
            Warehouse wh = warehouseMapper.findById(entry.getKey());
            wh.setAvailable_slots(wh.getAvailable_slots() - entry.getValue());
            warehouseMapper.update(wh);
        }

        // 8. 更新商品数量
        Goods goods = goodsMapper.findById(goodsId);
        if (goods != null) {
            goods.setQuantity(goods.getQuantity() + initialQty);
            goodsMapper.update(goods);
        }

        // 9. 更新批次状态
        batch.setBatch_status("正常");
        batchMapper.update(batch);

        // 10. 更新入库单头
        InboundOrderHead head = inboundOrderHeadMapper.findById(inboundNo);
        if (head != null) {
            head.setOrder_status("已完成");
            head.setInbound_time(LocalDateTime.now());
            head.setRemark(request.getRemark());
            inboundOrderHeadMapper.update(head);
        }

        // 11. 完成工作任务
        task.setCompleted_time(LocalDateTime.now());
        task.setRemark(request.getRemark());
        workTaskMapper.update(task);

        // 12. 操作日志
        OperationLog log = new OperationLog();
        log.setOperator_id(request.getOperatorId());
        log.setOperation_type("入库");
        log.setOperation_content(batchId + "完成入库");
        log.setOperation_result("成功");
        log.setOperation_time(LocalDateTime.now());
        operationLogMapper.save(log);
    }
}
