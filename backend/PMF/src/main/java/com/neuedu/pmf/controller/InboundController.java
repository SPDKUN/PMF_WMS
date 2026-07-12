package com.neuedu.pmf.controller;

import com.neuedu.pmf.common.ResultCode;
import com.neuedu.pmf.common.ResultData;
import com.neuedu.pmf.service.InboundService;
import com.neuedu.pmf.vo.CreateInboundRequest;
import com.neuedu.pmf.vo.CompleteInboundRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inbound")
public class InboundController {

    @Autowired
    private InboundService inboundService;

    @GetMapping("/availableBatches")
    public ResultData availableBatches() {
        return ResultData.success(inboundService.getAvailableBatches());
    }

    @PostMapping("/create")
    public ResultData create(@RequestBody CreateInboundRequest request) {
        try {
            return ResultData.success(inboundService.createInboundTask(request));
        } catch (RuntimeException e) {
            return ResultData.fail(ResultCode.FAILED.getCode(), e.getMessage());
        }
    }

    @PutMapping("/reject/{taskId}")
    public ResultData reject(@PathVariable Integer taskId, @RequestBody java.util.Map<String, Object> body) {
        try {
            Integer operatorId = body.get("operatorId") != null ? ((Number) body.get("operatorId")).intValue() : null;
            inboundService.rejectInbound(taskId, operatorId);
            return ResultData.success();
        } catch (RuntimeException e) {
            return ResultData.fail(ResultCode.FAILED.getCode(), e.getMessage());
        }
    }

    @PutMapping("/complete/{taskId}")
    public ResultData complete(@PathVariable Integer taskId, @RequestBody CompleteInboundRequest request) {
        try {
            inboundService.completeInbound(taskId, request);
            return ResultData.success();
        } catch (RuntimeException e) {
            return ResultData.fail(ResultCode.FAILED.getCode(), e.getMessage());
        }
    }
}
