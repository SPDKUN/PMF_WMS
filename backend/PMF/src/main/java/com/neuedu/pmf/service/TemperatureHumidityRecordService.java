package com.neuedu.pmf.service;

import com.neuedu.pmf.entity.TemperatureHumidityRecord;
import java.util.ArrayList;

public interface TemperatureHumidityRecordService {
    ArrayList<TemperatureHumidityRecord> list();

    TemperatureHumidityRecord getById(Integer id);

    boolean delete(Integer id);

    boolean save(TemperatureHumidityRecord temperatureHumidityRecord);

    boolean update(TemperatureHumidityRecord temperatureHumidityRecord);

    boolean saveOrUpdateByWarehouse(Integer warehouseId, java.math.BigDecimal temperature, java.math.BigDecimal humidity);
}
