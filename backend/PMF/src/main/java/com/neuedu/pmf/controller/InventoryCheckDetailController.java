package com.neuedu.pmf.controller;

import com.neuedu.pmf.common.ResultCode;
import com.neuedu.pmf.common.ResultData;
import com.neuedu.pmf.entity.InventoryCheckDetail;
import com.neuedu.pmf.service.InventoryCheckDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventoryCheckDetail")
public class InventoryCheckDetailController {

    @Autowired
    private InventoryCheckDetailService inventoryCheckDetailService;

    @GetMapping("/list")
    public ResultData list() {
        return ResultData.success(inventoryCheckDetailService.list());
    }

    @GetMapping("/{id}")
    public ResultData getById(@PathVariable("id") Integer id) {
        return ResultData.success(inventoryCheckDetailService.getById(id));
    }

    @PostMapping
    public ResultData save(@RequestBody InventoryCheckDetail inventoryCheckDetail) {
        boolean flag = inventoryCheckDetailService.save(inventoryCheckDetail);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }

    @PutMapping
    public ResultData update(@RequestBody InventoryCheckDetail inventoryCheckDetail) {
        boolean flag = inventoryCheckDetailService.update(inventoryCheckDetail);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }

    @DeleteMapping("/{id}")
    public ResultData delete(@PathVariable("id") Integer id) {
        boolean flag = inventoryCheckDetailService.delete(id);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }
}
