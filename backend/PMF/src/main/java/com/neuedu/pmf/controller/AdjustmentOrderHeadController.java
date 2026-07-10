package com.neuedu.pmf.controller;

import com.neuedu.pmf.common.ResultCode;
import com.neuedu.pmf.common.ResultData;
import com.neuedu.pmf.entity.AdjustmentOrderHead;
import com.neuedu.pmf.service.AdjustmentOrderHeadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/adjustmentOrderHead")
public class AdjustmentOrderHeadController {

    @Autowired
    private AdjustmentOrderHeadService adjustmentOrderHeadService;

    @GetMapping("/list")
    public ResultData list() {
        return ResultData.success(adjustmentOrderHeadService.list());
    }

    @GetMapping("/{orderNo}")
    public ResultData getById(@PathVariable String orderNo) {
        return ResultData.success(adjustmentOrderHeadService.getById(orderNo));
    }

    @PostMapping
    public ResultData save(@RequestBody AdjustmentOrderHead adjustmentOrderHead) {
        boolean flag = adjustmentOrderHeadService.save(adjustmentOrderHead);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }

    @PutMapping
    public ResultData update(@RequestBody AdjustmentOrderHead adjustmentOrderHead) {
        boolean flag = adjustmentOrderHeadService.update(adjustmentOrderHead);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }

    @DeleteMapping("/{orderNo}")
    public ResultData delete(@PathVariable String orderNo) {
        boolean flag = adjustmentOrderHeadService.delete(orderNo);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }
}
