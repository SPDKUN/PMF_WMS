package com.neuedu.pmf.service;

import com.neuedu.pmf.entity.WorkTask;
import com.neuedu.pmf.vo.CreateQcRequest;
import com.neuedu.pmf.vo.CompleteQcRequest;

import java.util.ArrayList;

public interface QualityCheckService {
    WorkTask createQualityCheckTask(CreateQcRequest request);
    void completeQualityCheck(Integer taskId, CompleteQcRequest request);
    ArrayList<WorkTask> getMyTasks(Integer assigneeId);
}
