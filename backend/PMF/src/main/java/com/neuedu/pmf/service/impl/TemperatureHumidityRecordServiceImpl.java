package com.neuedu.pmf.service.impl;

import com.neuedu.pmf.entity.TemperatureHumidityRecord;
import com.neuedu.pmf.mapper.TemperatureHumidityRecordMapper;
import com.neuedu.pmf.service.TemperatureHumidityRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class TemperatureHumidityRecordServiceImpl implements TemperatureHumidityRecordService {

    @Autowired
    private TemperatureHumidityRecordMapper temperatureHumidityRecordMapper;

    @Override
    public ArrayList<TemperatureHumidityRecord> list() {
        return temperatureHumidityRecordMapper.list();
    }

    @Override
    public TemperatureHumidityRecord getById(Integer id) {
        return temperatureHumidityRecordMapper.findById(id);
    }

    @Override
    public boolean delete(Integer id) {
        return temperatureHumidityRecordMapper.deleteById(id) > 0;
    }

    @Override
    public boolean save(TemperatureHumidityRecord temperatureHumidityRecord) {
        return temperatureHumidityRecordMapper.save(temperatureHumidityRecord) > 0;
    }

    @Override
    public boolean update(TemperatureHumidityRecord temperatureHumidityRecord) {
        return temperatureHumidityRecordMapper.update(temperatureHumidityRecord) > 0;
    }

    @Override
    public boolean saveOrUpdateByWarehouse(Integer warehouseId, java.math.BigDecimal temperature, java.math.BigDecimal humidity) {
        TemperatureHumidityRecord existing = temperatureHumidityRecordMapper.findByWarehouseId(warehouseId);
        if (existing != null) {
            existing.setTemperature(temperature);
            existing.setHumidity(humidity);
            existing.setRecorded_time(java.time.LocalDateTime.now());
            return temperatureHumidityRecordMapper.update(existing) > 0;
        } else {
            TemperatureHumidityRecord record = new TemperatureHumidityRecord();
            record.setWarehouse_id(warehouseId);
            record.setTemperature(temperature);
            record.setHumidity(humidity);
            record.setRecorded_time(java.time.LocalDateTime.now());
            return temperatureHumidityRecordMapper.save(record) > 0;
        }
    }
}
