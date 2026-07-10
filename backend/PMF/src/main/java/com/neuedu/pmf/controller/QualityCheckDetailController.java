package com.neuedu.pmf.controller;

import com.neuedu.pmf.common.ResultCode;
import com.neuedu.pmf.common.ResultData;
import com.neuedu.pmf.entity.QualityCheckDetail;
import com.neuedu.pmf.service.QualityCheckDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/qualityCheckDetail")
public class QualityCheckDetailController {

    @Autowired
    private QualityCheckDetailService qualityCheckDetailService;

    @GetMapping("/list")
    public ResultData list() {
        return ResultData.success(qualityCheckDetailService.list());
    }

    @GetMapping("/{id}")
    public ResultData getById(@PathVariable("id") Integer id) {
        return ResultData.success(qualityCheckDetailService.getById(id));
    }

    @PostMapping
    public ResultData save(@RequestBody QualityCheckDetail qualityCheckDetail) {
        boolean flag = qualityCheckDetailService.save(qualityCheckDetail);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }

    @PutMapping
    public ResultData update(@RequestBody QualityCheckDetail qualityCheckDetail) {
        boolean flag = qualityCheckDetailService.update(qualityCheckDetail);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }

    @DeleteMapping("/{id}")
    public ResultData delete(@PathVariable("id") Integer id) {
        boolean flag = qualityCheckDetailService.delete(id);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }
}
