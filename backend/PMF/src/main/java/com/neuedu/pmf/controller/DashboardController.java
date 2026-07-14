package com.neuedu.pmf.controller;

import com.neuedu.pmf.common.ResultData;
import com.neuedu.pmf.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/weeklyInboundOutbound")
    public ResultData weeklyInboundOutbound() {
        return ResultData.success(dashboardService.getWeeklyInboundOutbound());
    }

    @GetMapping("/goodsQuantity")
    public ResultData goodsQuantity() {
        return ResultData.success(dashboardService.getGoodsQuantity());
    }

    @GetMapping("/warehouseTempHumidity")
    public ResultData warehouseTempHumidity() {
        return ResultData.success(dashboardService.getWarehouseTempHumidity());
    }

    @GetMapping("/warehouseCapacity")
    public ResultData warehouseCapacity() {
        return ResultData.success(dashboardService.getWarehouseCapacity());
    }
}
