package com.neuedu.pmf.service.impl;

import com.neuedu.pmf.entity.*;
import com.neuedu.pmf.mapper.*;
import com.neuedu.pmf.service.QualityCheckService;
import com.neuedu.pmf.vo.CreateQcRequest;
import com.neuedu.pmf.vo.CompleteQcRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@Service
public class QualityCheckServiceImpl implements QualityCheckService {

    @Autowired
    private QualityCheckHeadMapper qualityCheckHeadMapper;

    @Autowired
    private QualityCheckDetailMapper qualityCheckDetailMapper;

    @Autowired
    private WorkTaskMapper workTaskMapper;

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Autowired
    private BatchMapper batchMapper;

    @Autowired
    private InventoryMapper inventoryMapper;

    @Override
    @Transactional
    public WorkTask createQualityCheckTask(CreateQcRequest request) {
        // 1. 生成质检单号
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "C" + today;
        java.util.List<String> todayNos = qualityCheckHeadMapper.findTodayCheckNos(prefix + "%");
        int maxSeq = 0;
        for (String no : todayNos) {
            try {
                int seq = Integer.parseInt(no.substring(9));
                if (seq > maxSeq) maxSeq = seq;
            } catch (NumberFormatException ignored) {}
        }
        String qualityCheckNo = prefix + String.format("%03d", maxSeq + 1);

        // 2. 查批次获取goods_id
        Batch batch = batchMapper.findById(request.getBatchId());
        if (batch == null) {
            throw new RuntimeException("批次不存在: " + request.getBatchId());
        }

        // 3. INSERT quality_check_head
        QualityCheckHead head = new QualityCheckHead();
        head.setQuality_check_no(qualityCheckNo);
        head.setCheck_type(request.getCheckType());
        head.setOrder_status("待检");
        head.setInspector_id(request.getInspectorId());
        head.setInspection_time(null);
        head.setRemark(request.getRemark());
        qualityCheckHeadMapper.save(head);

        // 4. INSERT quality_check_detail
        QualityCheckDetail detail = new QualityCheckDetail();
        detail.setQuality_check_no(qualityCheckNo);
        detail.setGoods_id(batch.getGoods_id());
        detail.setBatch_id(request.getBatchId());
        detail.setInspection_result(null);
        detail.setDefect_reason(null);
        detail.setHandling_suggestion(null);
        qualityCheckDetailMapper.save(detail);

        // 5. INSERT work_task
        WorkTask task = new WorkTask();
        task.setTask_type("质检");
        task.setPriority(request.getPriority());
        task.setRelated_order_no(qualityCheckNo);
        task.setRelated_batch_id(request.getBatchId());
        task.setGoods_id(null);
        task.setSource_location_id(null);
        task.setTarget_location_id(null);
        task.setAssignee_id(request.getInspectorId());
        task.setDeadline(request.getDeadline() != null && !request.getDeadline().isEmpty()
                ? LocalDateTime.parse(request.getDeadline() + "T23:59:59") : null);
        task.setCreated_time(LocalDateTime.now());
        task.setCompleted_time(null);
        task.setRemark(request.getRemark());
        workTaskMapper.save(task);

        // 6. INSERT operation_log
        OperationLog log = new OperationLog();
        log.setOperator_id(request.getOperatorId());
        log.setOperation_type("质检");
        log.setOperation_content(request.getBatchId() + "申请质检");
        log.setOperation_result("成功");
        log.setOperation_time(LocalDateTime.now());
        operationLogMapper.save(log);

        return task;
    }

    @Override
    @Transactional
    public void completeQualityCheck(Integer taskId, CompleteQcRequest request) {
        // 1. 查工作任务
        WorkTask task = workTaskMapper.findById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在: " + taskId);
        }

        String qualityCheckNo = task.getRelated_order_no();
        String batchId = task.getRelated_batch_id();

        // 2. 查质检明细和头
        QualityCheckDetail detail = qualityCheckDetailMapper.findByCheckNo(qualityCheckNo);
        QualityCheckHead head = qualityCheckHeadMapper.findById(qualityCheckNo);

        // 3. UPDATE detail
        detail.setInspection_result(request.getInspectionResult());
        detail.setDefect_reason(request.getDefectReason());
        detail.setHandling_suggestion(request.getHandlingSuggestion());
        qualityCheckDetailMapper.update(detail);

        // 4. UPDATE head
        head.setRemark(request.getRemark());
        head.setInspection_time(LocalDateTime.now());
        head.setOrder_status("已完成");
        qualityCheckHeadMapper.update(head);

        // 5. UPDATE work_task
        task.setCompleted_time(LocalDateTime.now());
        task.setRemark(request.getRemark());
        workTaskMapper.update(task);

        // 6. INSERT operation_log
        OperationLog log = new OperationLog();
        log.setOperator_id(request.getOperatorId());
        log.setOperation_type("质检");
        log.setOperation_content(batchId + "完成质检");
        log.setOperation_result(request.getInspectionResult());
        log.setOperation_time(LocalDateTime.now());
        operationLogMapper.save(log);

        // 7. 根据结果更新批次和库存
        String result = request.getInspectionResult();
        if ("合格".equals(result)) {
            Batch batch = batchMapper.findById(batchId);
            if (batch != null && "待检".equals(batch.getBatch_status())) {
                batch.setBatch_status("正常");
                batchMapper.update(batch);
            }
        } else if ("不合格".equals(result)) {
            Batch batch = batchMapper.findById(batchId);
            if (batch != null) {
                batch.setBatch_status("报废");
                batchMapper.update(batch);
            }
            inventoryMapper.updateStatusByBatchId(batchId, "待报废");
        }
    }

    @Override
    public ArrayList<WorkTask> getMyTasks(Integer assigneeId) {
        return workTaskMapper.findByAssigneeId(assigneeId);
    }
}
