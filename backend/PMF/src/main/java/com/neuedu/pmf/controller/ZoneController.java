package com.neuedu.pmf.controller;

import com.neuedu.pmf.common.ResultCode;
import com.neuedu.pmf.common.ResultData;
import com.neuedu.pmf.entity.Zone;
import com.neuedu.pmf.service.ZoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/zone")
public class ZoneController {

    @Autowired
    private ZoneService zoneService;

    @GetMapping("/list")
    public ResultData list(@RequestParam(required = false) Integer warehouseId) {
        if (warehouseId != null) {
            return ResultData.success(zoneService.listByWarehouseId(warehouseId));
        }
        return ResultData.success(zoneService.list());
    }

    @GetMapping("/{zoneId}")
    public ResultData getById(@PathVariable Integer zoneId) {
        return ResultData.success(zoneService.getById(zoneId));
    }

    @PostMapping
    public ResultData save(@RequestBody Zone zone) {
        boolean flag = zoneService.save(zone);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }

    @PutMapping
    public ResultData update(@RequestBody Zone zone) {
        boolean flag = zoneService.update(zone);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }

    @DeleteMapping("/{id}")
    public ResultData delete(@PathVariable Integer id) {
        boolean flag = zoneService.delete(id);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }
}
