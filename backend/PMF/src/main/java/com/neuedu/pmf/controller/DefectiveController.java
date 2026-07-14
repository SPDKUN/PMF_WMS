package com.neuedu.pmf.controller;

import com.neuedu.pmf.common.ResultCode;
import com.neuedu.pmf.common.ResultData;
import com.neuedu.pmf.service.DefectiveService;
import com.neuedu.pmf.vo.CreateDefectiveRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/defective")
public class DefectiveController {

    @Autowired
    private DefectiveService defectiveService;

    @PostMapping("/create")
    public ResultData create(@RequestBody CreateDefectiveRequest request) {
        try {
            return ResultData.success(defectiveService.createDefectiveTask(request));
        } catch (RuntimeException e) {
            return ResultData.fail(ResultCode.FAILED.getCode(), e.getMessage());
        }
    }

    @PutMapping("/reject/{taskId}")
    public ResultData reject(@PathVariable Integer taskId, @RequestBody java.util.Map<String, Object> body) {
        try {
            Integer operatorId = body.get("operatorId") != null ? ((Number) body.get("operatorId")).intValue() : null;
            defectiveService.rejectDefective(taskId, operatorId);
            return ResultData.success();
        } catch (RuntimeException e) {
            return ResultData.fail(ResultCode.FAILED.getCode(), e.getMessage());
        }
    }

    @PutMapping("/complete/{taskId}")
    public ResultData complete(@PathVariable Integer taskId, @RequestBody java.util.Map<String, Object> body) {
        try {
            Integer operatorId = body.get("operatorId") != null ? ((Number) body.get("operatorId")).intValue() : null;
            defectiveService.completeDefective(taskId, operatorId);
            return ResultData.success();
        } catch (RuntimeException e) {
            return ResultData.fail(ResultCode.FAILED.getCode(), e.getMessage());
        }
    }

    @GetMapping("/detail/{taskId}")
    public ResultData detail(@PathVariable Integer taskId) {
        try {
            return ResultData.success(defectiveService.getDefectiveDetail(taskId));
        } catch (RuntimeException e) {
            return ResultData.fail(ResultCode.FAILED.getCode(), e.getMessage());
        }
    }
}
