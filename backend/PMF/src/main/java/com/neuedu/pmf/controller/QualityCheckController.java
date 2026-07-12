package com.neuedu.pmf.controller;

import com.neuedu.pmf.common.ResultCode;
import com.neuedu.pmf.common.ResultData;
import com.neuedu.pmf.service.QualityCheckService;
import com.neuedu.pmf.vo.CreateQcRequest;
import com.neuedu.pmf.vo.CompleteQcRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/qualityCheck")
public class QualityCheckController {

    @Autowired
    private QualityCheckService qualityCheckService;

    @PostMapping("/create")
    public ResultData create(@RequestBody CreateQcRequest request) {
        try {
            return ResultData.success(qualityCheckService.createQualityCheckTask(request));
        } catch (RuntimeException e) {
            return ResultData.fail(ResultCode.FAILED.getCode(), e.getMessage());
        }
    }

    @PutMapping("/complete/{taskId}")
    public ResultData complete(@PathVariable Integer taskId, @RequestBody CompleteQcRequest request) {
        try {
            qualityCheckService.completeQualityCheck(taskId, request);
            return ResultData.success();
        } catch (RuntimeException e) {
            return ResultData.fail(ResultCode.FAILED.getCode(), e.getMessage());
        }
    }

    @GetMapping("/myTasks")
    public ResultData myTasks(@RequestParam Integer assigneeId) {
        return ResultData.success(qualityCheckService.getMyTasks(assigneeId));
    }
}
