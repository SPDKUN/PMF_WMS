package com.neuedu.pmf.service;

import com.neuedu.pmf.entity.InventoryCheckDetail;
import com.neuedu.pmf.entity.WorkTask;
import com.neuedu.pmf.vo.CreateInventoryCheckRequest;
import com.neuedu.pmf.vo.CompleteInventoryCheckRequest;

import java.util.ArrayList;

public interface InventoryCheckService {
    WorkTask createInventoryCheckTask(CreateInventoryCheckRequest request);
    void completeInventoryCheck(Integer taskId, CompleteInventoryCheckRequest request);
    ArrayList<WorkTask> getMyTasks(Integer assigneeId);
    ArrayList<InventoryCheckDetail> getDetailsByCheckNo(String checkNo);
}
