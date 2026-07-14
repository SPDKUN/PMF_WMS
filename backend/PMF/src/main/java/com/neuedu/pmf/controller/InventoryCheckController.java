package com.neuedu.pmf.controller;

import com.neuedu.pmf.common.ResultCode;
import com.neuedu.pmf.common.ResultData;
import com.neuedu.pmf.service.InventoryCheckService;
import com.neuedu.pmf.vo.CreateInventoryCheckRequest;
import com.neuedu.pmf.vo.CompleteInventoryCheckRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventoryCheck")
public class InventoryCheckController {

    @Autowired
    private InventoryCheckService inventoryCheckService;

    @PostMapping("/create")
    public ResultData create(@RequestBody CreateInventoryCheckRequest request) {
        try {
            return ResultData.success(inventoryCheckService.createInventoryCheckTask(request));
        } catch (RuntimeException e) {
            return ResultData.fail(ResultCode.FAILED.getCode(), e.getMessage());
        }
    }

    @PutMapping("/complete/{taskId}")
    public ResultData complete(@PathVariable Integer taskId, @RequestBody CompleteInventoryCheckRequest request) {
        try {
            inventoryCheckService.completeInventoryCheck(taskId, request);
            return ResultData.success();
        } catch (RuntimeException e) {
            return ResultData.fail(ResultCode.FAILED.getCode(), e.getMessage());
        }
    }

    @GetMapping("/myTasks")
    public ResultData myTasks(@RequestParam Integer assigneeId) {
        try {
            return ResultData.success(inventoryCheckService.getMyTasks(assigneeId));
        } catch (RuntimeException e) {
            return ResultData.fail(ResultCode.FAILED.getCode(), e.getMessage());
        }
    }

    @GetMapping("/details")
    public ResultData getDetails(@RequestParam String checkNo) {
        try {
            return ResultData.success(inventoryCheckService.getDetailsByCheckNo(checkNo));
        } catch (RuntimeException e) {
            return ResultData.fail(ResultCode.FAILED.getCode(), e.getMessage());
        }
    }
}
