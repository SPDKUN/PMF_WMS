package com.neuedu.pmf.service;

import com.neuedu.pmf.entity.WorkTask;
import com.neuedu.pmf.vo.CreateDefectiveRequest;

import java.util.ArrayList;
import java.util.Map;

public interface DefectiveService {
    WorkTask createDefectiveTask(CreateDefectiveRequest request);
    void rejectDefective(Integer taskId, Integer operatorId);
    void completeDefective(Integer taskId, Integer operatorId);
    ArrayList<Map<String, Object>> getDefectiveDetail(Integer taskId);
}
