package com.neuedu.pmf.mapper;

import com.neuedu.pmf.entity.Zone;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;

@Mapper
public interface ZoneMapper {
    ArrayList<Zone> list();
    int save(Zone zone);
    Zone findById(@Param("zone_id") Integer zoneId);
    int update(Zone zone);
    int deleteById(@Param("zone_id") Integer zoneId);
    int deleteByWarehouseId(@Param("warehouse_id") Integer warehouseId);
    ArrayList<Zone> findByWarehouseId(@Param("warehouse_id") Integer warehouseId);
}
