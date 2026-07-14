package com.neuedu.pmf.controller;

import com.neuedu.pmf.common.ResultCode;
import com.neuedu.pmf.common.ResultData;
import com.neuedu.pmf.entity.TemperatureHumidityRecord;
import com.neuedu.pmf.service.TemperatureHumidityRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

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

    @PostMapping("/upload")
    public ResultData upload(@RequestBody Map<String, Object> params) {
        Integer warehouseId = params.get("warehouseId") != null ? ((Number) params.get("warehouseId")).intValue() : null;
        BigDecimal temperature = params.get("temperature") != null ? new BigDecimal(params.get("temperature").toString()) : null;
        BigDecimal humidity = params.get("humidity") != null ? new BigDecimal(params.get("humidity").toString()) : null;

        if (warehouseId == null || temperature == null || humidity == null) {
            return ResultData.fail(400, "仓库、温度和湿度不能为空");
        }

        boolean flag = temperatureHumidityRecordService.saveOrUpdateByWarehouse(warehouseId, temperature, humidity);
        return flag ? ResultData.success() : ResultData.fail(ResultCode.FAILED);
    }
}
