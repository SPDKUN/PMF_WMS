package com.neuedu.pmf.mapper;

import com.neuedu.pmf.entity.Inventory;
import com.neuedu.pmf.vo.InventoryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;

@Mapper
public interface InventoryMapper {
    ArrayList<Inventory> list();
    ArrayList<InventoryVO> listWithDetails(@Param("batchId") String batchId);
    int save(Inventory inventory);
    Inventory findById(@Param("inventory_id") Integer inventoryId);
    int update(Inventory inventory);
    int deleteById(@Param("inventory_id") Integer inventoryId);
    int deleteByBatchId(@Param("batch_id") String batchId);
    int updateStatusByBatchId(@Param("batch_id") String batchId, @Param("inventory_status") String inventoryStatus);
    com.neuedu.pmf.vo.InventoryVO findByLocationId(@Param("location_id") Integer locationId);
}
