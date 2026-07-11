package com.neuedu.pmf.controller;

import com.neuedu.pmf.common.ResultCode;
import com.neuedu.pmf.common.ResultData;
import com.neuedu.pmf.entity.Batch;
import com.neuedu.pmf.service.BatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/batch")
public class BatchController {

    @Autowired
    private BatchService batchService;

    @GetMapping("/list")
    public ResultData list() {
        return ResultData.success(batchService.list());
    }

    @GetMapping("/{batchId}")
    public ResultData getById(@PathVariable String batchId) {
        return ResultData.success(batchService.getById(batchId));
    }

    @PostMapping
    public ResultData save(@RequestBody Batch batch) {
        boolean flag = batchService.save(batch);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }

    @PutMapping
    public ResultData update(@RequestBody Batch batch) {
        boolean flag = batchService.update(batch);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }

    @DeleteMapping("/{id}")
    public ResultData delete(@PathVariable String id) {
        boolean flag = batchService.delete(id);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }
}
