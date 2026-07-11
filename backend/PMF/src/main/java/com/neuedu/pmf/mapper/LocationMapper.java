package com.neuedu.pmf.mapper;

import com.neuedu.pmf.entity.Location;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;

@Mapper
public interface LocationMapper {
    ArrayList<Location> list();
    int save(Location location);
    Location findById(@Param("location_id") Integer locationId);
    int update(Location location);
    int deleteById(@Param("location_id") Integer locationId);
    int deleteByZoneId(@Param("zone_id") Integer zoneId);
    ArrayList<Location> findByZoneId(@Param("zone_id") Integer zoneId);
    java.util.List<Integer> findAllIds();
}
