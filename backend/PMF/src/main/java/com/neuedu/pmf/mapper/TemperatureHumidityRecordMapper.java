package com.neuedu.pmf.mapper;

import com.neuedu.pmf.entity.TemperatureHumidityRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;

@Mapper
public interface TemperatureHumidityRecordMapper {
    ArrayList<TemperatureHumidityRecord> list();
    int save(TemperatureHumidityRecord temperatureHumidityRecord);
    TemperatureHumidityRecord findById(@Param("record_id") Integer recordId);
    TemperatureHumidityRecord findByWarehouseId(@Param("warehouse_id") Integer warehouseId);
    int update(TemperatureHumidityRecord temperatureHumidityRecord);
    int deleteById(@Param("record_id") Integer recordId);
}
