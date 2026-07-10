package com.neuedu.pmf.controller;

import com.neuedu.pmf.common.ResultCode;
import com.neuedu.pmf.common.ResultData;
import com.neuedu.pmf.entity.QualityCheckHead;
import com.neuedu.pmf.service.QualityCheckHeadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/qualityCheckHead")
public class QualityCheckHeadController {

    @Autowired
    private QualityCheckHeadService qualityCheckHeadService;

    @GetMapping("/list")
    public ResultData list() {
        return ResultData.success(qualityCheckHeadService.list());
    }

    @GetMapping("/{orderNo}")
    public ResultData getById(@PathVariable String orderNo) {
        return ResultData.success(qualityCheckHeadService.getById(orderNo));
    }

    @PostMapping
    public ResultData save(@RequestBody QualityCheckHead qualityCheckHead) {
        boolean flag = qualityCheckHeadService.save(qualityCheckHead);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }

    @PutMapping
    public ResultData update(@RequestBody QualityCheckHead qualityCheckHead) {
        boolean flag = qualityCheckHeadService.update(qualityCheckHead);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }

    @DeleteMapping("/{orderNo}")
    public ResultData delete(@PathVariable String orderNo) {
        boolean flag = qualityCheckHeadService.delete(orderNo);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }
}
