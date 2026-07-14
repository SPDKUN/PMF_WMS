package com.neuedu.pmf.controller;

import com.neuedu.pmf.common.ResultCode;
import com.neuedu.pmf.common.ResultData;
import com.neuedu.pmf.service.OutboundService;
import com.neuedu.pmf.vo.CreateOutboundRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/outbound")
public class OutboundController {

    @Autowired
    private OutboundService outboundService;

    @PostMapping("/create")
    public ResultData create(@RequestBody CreateOutboundRequest request) {
        try {
            return ResultData.success(outboundService.createOutboundTask(request));
        } catch (RuntimeException e) {
            return ResultData.fail(ResultCode.FAILED.getCode(), e.getMessage());
        }
    }

    @PutMapping("/reject/{taskId}")
    public ResultData reject(@PathVariable Integer taskId, @RequestBody java.util.Map<String, Object> body) {
        try {
            Integer operatorId = body.get("operatorId") != null ? ((Number) body.get("operatorId")).intValue() : null;
            outboundService.rejectOutbound(taskId, operatorId);
            return ResultData.success();
        } catch (RuntimeException e) {
            return ResultData.fail(ResultCode.FAILED.getCode(), e.getMessage());
        }
    }

    @PutMapping("/complete/{taskId}")
    public ResultData complete(@PathVariable Integer taskId, @RequestBody java.util.Map<String, Object> body) {
        try {
            Integer operatorId = body.get("operatorId") != null ? ((Number) body.get("operatorId")).intValue() : null;
            outboundService.completeOutbound(taskId, operatorId);
            return ResultData.success();
        } catch (RuntimeException e) {
            return ResultData.fail(ResultCode.FAILED.getCode(), e.getMessage());
        }
    }

    @GetMapping("/detail/{taskId}")
    public ResultData detail(@PathVariable Integer taskId) {
        try {
            return ResultData.success(outboundService.getOutboundDetail(taskId));
        } catch (RuntimeException e) {
            return ResultData.fail(ResultCode.FAILED.getCode(), e.getMessage());
        }
    }

    @GetMapping("/availableInventory")
    public ResultData availableInventory(@RequestParam Integer goodsId) {
        try {
            return ResultData.success(outboundService.getAvailableInventory(goodsId));
        } catch (RuntimeException e) {
            return ResultData.fail(ResultCode.FAILED.getCode(), e.getMessage());
        }
    }
}
