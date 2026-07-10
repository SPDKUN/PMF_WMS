package com.neuedu.pmf.controller;

import com.neuedu.pmf.common.ResultCode;
import com.neuedu.pmf.common.ResultData;
import com.neuedu.pmf.entity.TemperatureHumidityRecord;
import com.neuedu.pmf.service.TemperatureHumidityRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/temperatureHumidityRecord")
public class TemperatureHumidityRecordController {

    @Autowired
    private TemperatureHumidityRecordService temperatureHumidityRecordService;

    @GetMapping("/list")
    public ResultData list() {
        return ResultData.success(temperatureHumidityRecordService.list());
    }

    @GetMapping("/{id}")
    public ResultData getById(@PathVariable("id") Integer id) {
        return ResultData.success(temperatureHumidityRecordService.getById(id));
    }

    @PostMapping
    public ResultData save(@RequestBody TemperatureHumidityRecord temperatureHumidityRecord) {
        boolean flag = temperatureHumidityRecordService.save(temperatureHumidityRecord);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }

    @PutMapping
    public ResultData update(@RequestBody TemperatureHumidityRecord temperatureHumidityRecord) {
        boolean flag = temperatureHumidityRecordService.update(temperatureHumidityRecord);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }

    @DeleteMapping("/{id}")
    public ResultData delete(@PathVariable("id") Integer id) {
        boolean flag = temperatureHumidityRecordService.delete(id);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }
}
