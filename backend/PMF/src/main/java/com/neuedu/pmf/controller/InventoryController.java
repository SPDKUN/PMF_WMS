package com.neuedu.pmf.controller;

import com.neuedu.pmf.common.ResultCode;
import com.neuedu.pmf.common.ResultData;
import com.neuedu.pmf.entity.Inventory;
import com.neuedu.pmf.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping("/list")
    public ResultData list() {
        return ResultData.success(inventoryService.list());
    }

    @GetMapping("/{id}")
    public ResultData getById(@PathVariable("id") Integer id) {
        return ResultData.success(inventoryService.getById(id));
    }

    @PostMapping
    public ResultData save(@RequestBody Inventory inventory) {
        boolean flag = inventoryService.save(inventory);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }

    @PutMapping
    public ResultData update(@RequestBody Inventory inventory) {
        boolean flag = inventoryService.update(inventory);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }

    @DeleteMapping("/{id}")
    public ResultData delete(@PathVariable("id") Integer id) {
        boolean flag = inventoryService.delete(id);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }
}
