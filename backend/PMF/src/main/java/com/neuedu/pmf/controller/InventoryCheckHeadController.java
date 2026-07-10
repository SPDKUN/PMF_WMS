package com.neuedu.pmf.controller;

import com.neuedu.pmf.common.ResultCode;
import com.neuedu.pmf.common.ResultData;
import com.neuedu.pmf.entity.InventoryCheckHead;
import com.neuedu.pmf.service.InventoryCheckHeadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventoryCheckHead")
public class InventoryCheckHeadController {

    @Autowired
    private InventoryCheckHeadService inventoryCheckHeadService;

    @GetMapping("/list")
    public ResultData list() {
        return ResultData.success(inventoryCheckHeadService.list());
    }

    @GetMapping("/{orderNo}")
    public ResultData getById(@PathVariable String orderNo) {
        return ResultData.success(inventoryCheckHeadService.getById(orderNo));
    }

    @PostMapping
    public ResultData save(@RequestBody InventoryCheckHead inventoryCheckHead) {
        boolean flag = inventoryCheckHeadService.save(inventoryCheckHead);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }

    @PutMapping
    public ResultData update(@RequestBody InventoryCheckHead inventoryCheckHead) {
        boolean flag = inventoryCheckHeadService.update(inventoryCheckHead);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }

    @DeleteMapping("/{orderNo}")
    public ResultData delete(@PathVariable String orderNo) {
        boolean flag = inventoryCheckHeadService.delete(orderNo);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }
}
