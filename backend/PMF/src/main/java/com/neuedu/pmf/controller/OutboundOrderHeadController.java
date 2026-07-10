package com.neuedu.pmf.controller;

import com.neuedu.pmf.common.ResultCode;
import com.neuedu.pmf.common.ResultData;
import com.neuedu.pmf.entity.OutboundOrderHead;
import com.neuedu.pmf.service.OutboundOrderHeadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/outboundOrderHead")
public class OutboundOrderHeadController {

    @Autowired
    private OutboundOrderHeadService outboundOrderHeadService;

    @GetMapping("/list")
    public ResultData list() {
        return ResultData.success(outboundOrderHeadService.list());
    }

    @GetMapping("/{orderNo}")
    public ResultData getById(@PathVariable String orderNo) {
        return ResultData.success(outboundOrderHeadService.getById(orderNo));
    }

    @PostMapping
    public ResultData save(@RequestBody OutboundOrderHead outboundOrderHead) {
        boolean flag = outboundOrderHeadService.save(outboundOrderHead);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }

    @PutMapping
    public ResultData update(@RequestBody OutboundOrderHead outboundOrderHead) {
        boolean flag = outboundOrderHeadService.update(outboundOrderHead);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }

    @DeleteMapping("/{orderNo}")
    public ResultData delete(@PathVariable String orderNo) {
        boolean flag = outboundOrderHeadService.delete(orderNo);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }
}
