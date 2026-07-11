package com.neuedu.pmf.mapper;

import com.neuedu.pmf.entity.Warehouse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;

@Mapper
public interface WarehouseMapper {
    ArrayList<Warehouse> list();
    int save(Warehouse warehouse);
    Warehouse findById(@Param("warehouse_id") Integer warehouseId);
    int update(Warehouse warehouse);
    int deleteById(@Param("warehouse_id") Integer warehouseId);
    java.util.List<Integer> findAllIds();
}
