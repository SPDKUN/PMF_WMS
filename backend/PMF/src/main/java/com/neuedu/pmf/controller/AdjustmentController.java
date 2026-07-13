package com.neuedu.pmf.controller;

import com.neuedu.pmf.common.ResultCode;
import com.neuedu.pmf.common.ResultData;
import com.neuedu.pmf.service.AdjustmentService;
import com.neuedu.pmf.vo.CreateAdjustmentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/adjustment")
public class AdjustmentController {

    @Autowired
    private AdjustmentService adjustmentService;

    @PostMapping("/create")
    public ResultData create(@RequestBody CreateAdjustmentRequest request) {
        try {
            return ResultData.success(adjustmentService.createAdjustmentTask(request));
        } catch (RuntimeException e) {
            return ResultData.fail(ResultCode.FAILED.getCode(), e.getMessage());
        }
    }

    @PutMapping("/reject/{taskId}")
    public ResultData reject(@PathVariable Integer taskId, @RequestBody java.util.Map<String, Object> body) {
        try {
            Integer operatorId = body.get("operatorId") != null ? ((Number) body.get("operatorId")).intValue() : null;
            adjustmentService.rejectAdjustment(taskId, operatorId);
            return ResultData.success();
        } catch (RuntimeException e) {
            return ResultData.fail(ResultCode.FAILED.getCode(), e.getMessage());
        }
    }

    @PutMapping("/complete/{taskId}")
    public ResultData complete(@PathVariable Integer taskId, @RequestBody java.util.Map<String, Object> body) {
        try {
            Integer operatorId = body.get("operatorId") != null ? ((Number) body.get("operatorId")).intValue() : null;
            adjustmentService.completeAdjustment(taskId, operatorId);
            return ResultData.success();
        } catch (RuntimeException e) {
            return ResultData.fail(ResultCode.FAILED.getCode(), e.getMessage());
        }
    }

    @GetMapping("/detail/{taskId}")
    public ResultData detail(@PathVariable Integer taskId) {
        try {
            return ResultData.success(adjustmentService.getAdjustmentDetail(taskId));
        } catch (RuntimeException e) {
            return ResultData.fail(ResultCode.FAILED.getCode(), e.getMessage());
        }
    }
}
