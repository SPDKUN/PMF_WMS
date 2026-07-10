package com.neuedu.pmf.controller;

import com.neuedu.pmf.common.ResultCode;
import com.neuedu.pmf.common.ResultData;
import com.neuedu.pmf.entity.AdjustmentOrderDetail;
import com.neuedu.pmf.service.AdjustmentOrderDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/adjustmentOrderDetail")
public class AdjustmentOrderDetailController {

    @Autowired
    private AdjustmentOrderDetailService adjustmentOrderDetailService;

    @GetMapping("/list")
    public ResultData list() {
        return ResultData.success(adjustmentOrderDetailService.list());
    }

    @GetMapping("/{id}")
    public ResultData getById(@PathVariable("id") Integer id) {
        return ResultData.success(adjustmentOrderDetailService.getById(id));
    }

    @PostMapping
    public ResultData save(@RequestBody AdjustmentOrderDetail adjustmentOrderDetail) {
        boolean flag = adjustmentOrderDetailService.save(adjustmentOrderDetail);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }

    @PutMapping
    public ResultData update(@RequestBody AdjustmentOrderDetail adjustmentOrderDetail) {
        boolean flag = adjustmentOrderDetailService.update(adjustmentOrderDetail);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }

    @DeleteMapping("/{id}")
    public ResultData delete(@PathVariable("id") Integer id) {
        boolean flag = adjustmentOrderDetailService.delete(id);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }
}
