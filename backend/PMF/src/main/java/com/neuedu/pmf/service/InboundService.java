package com.neuedu.pmf.service;

import com.neuedu.pmf.entity.Batch;
import com.neuedu.pmf.entity.WorkTask;
import com.neuedu.pmf.vo.CreateInboundRequest;
import com.neuedu.pmf.vo.CompleteInboundRequest;

import java.util.ArrayList;

public interface InboundService {
    ArrayList<Batch> getAvailableBatches();
    WorkTask createInboundTask(CreateInboundRequest request);
    void rejectInbound(Integer taskId, Integer operatorId);
    void completeInbound(Integer taskId, CompleteInboundRequest request);
}
