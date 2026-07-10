package com.neuedu.pmf.controller;

import com.neuedu.pmf.common.ResultCode;
import com.neuedu.pmf.common.ResultData;
import com.neuedu.pmf.entity.InboundOrderHead;
import com.neuedu.pmf.service.InboundOrderHeadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inboundOrderHead")
public class InboundOrderHeadController {

    @Autowired
    private InboundOrderHeadService inboundOrderHeadService;

    @GetMapping("/list")
    public ResultData list() {
        return ResultData.success(inboundOrderHeadService.list());
    }

    @GetMapping("/{orderNo}")
    public ResultData getById(@PathVariable String orderNo) {
        return ResultData.success(inboundOrderHeadService.getById(orderNo));
    }

    @PostMapping
    public ResultData save(@RequestBody InboundOrderHead inboundOrderHead) {
        boolean flag = inboundOrderHeadService.save(inboundOrderHead);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }

    @PutMapping
    public ResultData update(@RequestBody InboundOrderHead inboundOrderHead) {
        boolean flag = inboundOrderHeadService.update(inboundOrderHead);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }

    @DeleteMapping("/{orderNo}")
    public ResultData delete(@PathVariable String orderNo) {
        boolean flag = inboundOrderHeadService.delete(orderNo);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }
}
