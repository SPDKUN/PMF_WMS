package com.neuedu.pmf.controller;

import com.neuedu.pmf.common.ResultCode;
import com.neuedu.pmf.common.ResultData;
import com.neuedu.pmf.entity.OutboundOrderDetail;
import com.neuedu.pmf.service.OutboundOrderDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/outboundOrderDetail")
public class OutboundOrderDetailController {

    @Autowired
    private OutboundOrderDetailService outboundOrderDetailService;

    @GetMapping("/list")
    public ResultData list() {
        return ResultData.success(outboundOrderDetailService.list());
    }

    @GetMapping("/{id}")
    public ResultData getById(@PathVariable("id") Integer id) {
        return ResultData.success(outboundOrderDetailService.getById(id));
    }

    @PostMapping
    public ResultData save(@RequestBody OutboundOrderDetail outboundOrderDetail) {
        boolean flag = outboundOrderDetailService.save(outboundOrderDetail);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }

    @PutMapping
    public ResultData update(@RequestBody OutboundOrderDetail outboundOrderDetail) {
        boolean flag = outboundOrderDetailService.update(outboundOrderDetail);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }

    @DeleteMapping("/{id}")
    public ResultData delete(@PathVariable("id") Integer id) {
        boolean flag = outboundOrderDetailService.delete(id);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }
}
