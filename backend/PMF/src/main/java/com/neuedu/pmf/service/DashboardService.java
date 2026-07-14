package com.neuedu.pmf.service;

import java.util.Map;

public interface DashboardService {

    Map<String, Object> getWeeklyInboundOutbound();

    Map<String, Object> getGoodsQuantity();

    Map<String, Object> getWarehouseTempHumidity();

    Map<String, Object> getWarehouseCapacity();
}
