package com.neuedu.pmf.controller;

import com.neuedu.pmf.common.ResultCode;
import com.neuedu.pmf.common.ResultData;
import com.neuedu.pmf.entity.OperationLog;
import com.neuedu.pmf.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/operationLog")
public class OperationLogController {

    @Autowired
    private OperationLogService operationLogService;

    @GetMapping("/list")
    public ResultData list() {
        return ResultData.success(operationLogService.list());
    }

    @GetMapping("/{id}")
    public ResultData getById(@PathVariable("id") Integer id) {
        return ResultData.success(operationLogService.getById(id));
    }

    @PostMapping
    public ResultData save(@RequestBody OperationLog operationLog) {
        boolean flag = operationLogService.save(operationLog);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }

    @PutMapping
    public ResultData update(@RequestBody OperationLog operationLog) {
        boolean flag = operationLogService.update(operationLog);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }

    @DeleteMapping("/{id}")
    public ResultData delete(@PathVariable("id") Integer id) {
        boolean flag = operationLogService.delete(id);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }
}
