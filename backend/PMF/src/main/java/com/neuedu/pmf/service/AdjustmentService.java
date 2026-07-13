package com.neuedu.pmf.service;

import com.neuedu.pmf.entity.WorkTask;
import com.neuedu.pmf.vo.CreateAdjustmentRequest;

import java.util.ArrayList;
import java.util.Map;

public interface AdjustmentService {
    WorkTask createAdjustmentTask(CreateAdjustmentRequest request);
    void rejectAdjustment(Integer taskId, Integer operatorId);
    void completeAdjustment(Integer taskId, Integer operatorId);
    ArrayList<Map<String, Object>> getAdjustmentDetail(Integer taskId);
}
