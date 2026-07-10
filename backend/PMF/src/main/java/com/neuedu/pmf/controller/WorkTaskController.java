package com.neuedu.pmf.controller;

import com.neuedu.pmf.common.ResultCode;
import com.neuedu.pmf.common.ResultData;
import com.neuedu.pmf.entity.WorkTask;
import com.neuedu.pmf.service.WorkTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/workTask")
public class WorkTaskController {

    @Autowired
    private WorkTaskService workTaskService;

    @GetMapping("/list")
    public ResultData list() {
        return ResultData.success(workTaskService.list());
    }

    @GetMapping("/{id}")
    public ResultData getById(@PathVariable("id") Integer id) {
        return ResultData.success(workTaskService.getById(id));
    }

    @PostMapping
    public ResultData save(@RequestBody WorkTask workTask) {
        boolean flag = workTaskService.save(workTask);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }

    @PutMapping
    public ResultData update(@RequestBody WorkTask workTask) {
        boolean flag = workTaskService.update(workTask);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }

    @DeleteMapping("/{id}")
    public ResultData delete(@PathVariable("id") Integer id) {
        boolean flag = workTaskService.delete(id);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }
}
