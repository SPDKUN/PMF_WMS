package com.neuedu.pmf.controller;

import com.neuedu.pmf.common.ResultCode;
import com.neuedu.pmf.common.ResultData;
import com.neuedu.pmf.entity.Location;
import com.neuedu.pmf.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/location")
public class LocationController {

    @Autowired
    private LocationService locationService;

    @GetMapping("/list")
    public ResultData list() {
        return ResultData.success(locationService.list());
    }

    @GetMapping("/{locationId}")
    public ResultData getById(@PathVariable Integer locationId) {
        return ResultData.success(locationService.getById(locationId));
    }

    @PostMapping
    public ResultData save(@RequestBody Location location) {
        boolean flag = locationService.save(location);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }

    @PutMapping
    public ResultData update(@RequestBody Location location) {
        boolean flag = locationService.update(location);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }

    @DeleteMapping("/{id}")
    public ResultData delete(@PathVariable Integer id) {
        boolean flag = locationService.delete(id);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }
}
