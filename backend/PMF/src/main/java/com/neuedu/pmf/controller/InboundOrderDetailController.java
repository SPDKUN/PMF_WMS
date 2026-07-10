package com.neuedu.pmf.controller;

import com.neuedu.pmf.common.ResultCode;
import com.neuedu.pmf.common.ResultData;
import com.neuedu.pmf.entity.InboundOrderDetail;
import com.neuedu.pmf.service.InboundOrderDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inboundOrderDetail")
public class InboundOrderDetailController {

    @Autowired
    private InboundOrderDetailService inboundOrderDetailService;

    @GetMapping("/list")
    public ResultData list() {
        return ResultData.success(inboundOrderDetailService.list());
    }

    @GetMapping("/{id}")
    public ResultData getById(@PathVariable("id") Integer id) {
        return ResultData.success(inboundOrderDetailService.getById(id));
    }

    @PostMapping
    public ResultData save(@RequestBody InboundOrderDetail inboundOrderDetail) {
        boolean flag = inboundOrderDetailService.save(inboundOrderDetail);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }

    @PutMapping
    public ResultData update(@RequestBody InboundOrderDetail inboundOrderDetail) {
        boolean flag = inboundOrderDetailService.update(inboundOrderDetail);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }

    @DeleteMapping("/{id}")
    public ResultData delete(@PathVariable("id") Integer id) {
        boolean flag = inboundOrderDetailService.delete(id);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }
}
