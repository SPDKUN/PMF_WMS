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
    int batchUpdateLockStatus(@Param("ids") java.util.List<Integer> ids, @Param("lock_status") String lockStatus, @Param("lock_purpose") String lockPurpose);
    int freeLocation(@Param("location_id") Integer locationId);
    int occupyLocation(@Param("location_id") Integer locationId, @Param("goods_id") Integer goodsId);
}
