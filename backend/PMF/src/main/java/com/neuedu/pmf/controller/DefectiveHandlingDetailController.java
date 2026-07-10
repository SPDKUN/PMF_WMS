package com.neuedu.pmf.controller;

import com.neuedu.pmf.common.ResultCode;
import com.neuedu.pmf.common.ResultData;
import com.neuedu.pmf.entity.DefectiveHandlingDetail;
import com.neuedu.pmf.service.DefectiveHandlingDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/defectiveHandlingDetail")
public class DefectiveHandlingDetailController {

    @Autowired
    private DefectiveHandlingDetailService defectiveHandlingDetailService;

    @GetMapping("/list")
    public ResultData list() {
        return ResultData.success(defectiveHandlingDetailService.list());
    }

    @GetMapping("/{id}")
    public ResultData getById(@PathVariable("id") Integer id) {
        return ResultData.success(defectiveHandlingDetailService.getById(id));
    }

    @PostMapping
    public ResultData save(@RequestBody DefectiveHandlingDetail defectiveHandlingDetail) {
        boolean flag = defectiveHandlingDetailService.save(defectiveHandlingDetail);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }

    @PutMapping
    public ResultData update(@RequestBody DefectiveHandlingDetail defectiveHandlingDetail) {
        boolean flag = defectiveHandlingDetailService.update(defectiveHandlingDetail);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }

    @DeleteMapping("/{id}")
    public ResultData delete(@PathVariable("id") Integer id) {
        boolean flag = defectiveHandlingDetailService.delete(id);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }
}
