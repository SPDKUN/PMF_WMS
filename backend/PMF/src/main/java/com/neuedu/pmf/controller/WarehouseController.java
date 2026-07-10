package com.neuedu.pmf.controller;

import com.neuedu.pmf.common.ResultCode;
import com.neuedu.pmf.common.ResultData;
import com.neuedu.pmf.entity.Warehouse;
import com.neuedu.pmf.service.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/warehouse")
public class WarehouseController {

    @Autowired
    private WarehouseService warehouseService;

    @GetMapping("/list")
    public ResultData list() {
        return ResultData.success(warehouseService.list());
    }

    @GetMapping("/{warehouseId}")
    public ResultData getById(@PathVariable Integer warehouseId) {
        return ResultData.success(warehouseService.getById(warehouseId));
    }

    @PostMapping
    public ResultData save(@RequestBody Warehouse warehouse) {
        boolean flag = warehouseService.save(warehouse);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }

    @PutMapping
    public ResultData update(@RequestBody Warehouse warehouse) {
        boolean flag = warehouseService.update(warehouse);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }

    @DeleteMapping("/{id}")
    public ResultData delete(@PathVariable Integer id) {
        boolean flag = warehouseService.delete(id);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }
}
