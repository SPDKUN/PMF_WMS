package com.neuedu.pmf.controller;

import com.neuedu.pmf.common.ResultCode;
import com.neuedu.pmf.common.ResultData;
import com.neuedu.pmf.entity.DefectiveHandlingHead;
import com.neuedu.pmf.service.DefectiveHandlingHeadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/defectiveHandlingHead")
public class DefectiveHandlingHeadController {

    @Autowired
    private DefectiveHandlingHeadService defectiveHandlingHeadService;

    @GetMapping("/list")
    public ResultData list() {
        return ResultData.success(defectiveHandlingHeadService.list());
    }

    @GetMapping("/{orderNo}")
    public ResultData getById(@PathVariable String orderNo) {
        return ResultData.success(defectiveHandlingHeadService.getById(orderNo));
    }

    @PostMapping
    public ResultData save(@RequestBody DefectiveHandlingHead defectiveHandlingHead) {
        boolean flag = defectiveHandlingHeadService.save(defectiveHandlingHead);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }

    @PutMapping
    public ResultData update(@RequestBody DefectiveHandlingHead defectiveHandlingHead) {
        boolean flag = defectiveHandlingHeadService.update(defectiveHandlingHead);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }

    @DeleteMapping("/{orderNo}")
    public ResultData delete(@PathVariable String orderNo) {
        boolean flag = defectiveHandlingHeadService.delete(orderNo);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }
}
