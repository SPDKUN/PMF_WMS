package com.neuedu.pmf.service;

import com.neuedu.pmf.entity.WorkTask;
import com.neuedu.pmf.vo.CreateOutboundRequest;
import com.neuedu.pmf.vo.InventoryVO;

import java.util.ArrayList;
import java.util.Map;

public interface OutboundService {
    WorkTask createOutboundTask(CreateOutboundRequest request);
    void rejectOutbound(Integer taskId, Integer operatorId);
    void completeOutbound(Integer taskId, Integer operatorId);
    ArrayList<Map<String, Object>> getOutboundDetail(Integer taskId);
    ArrayList<InventoryVO> getAvailableInventory(Integer goodsId);
}
