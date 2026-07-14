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
    ArrayList<com.neuedu.pmf.vo.InventoryVO> findByGoodsId(@Param("goods_id") Integer goodsId);
    com.neuedu.pmf.vo.InventoryVO findByLocationIdAllStatus(@Param("location_id") Integer locationId);
    ArrayList<Inventory> findInventoryByLocationId(@Param("location_id") Integer locationId);
    ArrayList<Inventory> findInventoryByWarehouseId(@Param("warehouse_id") Integer warehouseId);
    ArrayList<Inventory> findInventoryByGoodsId(@Param("goods_id") Integer goodsId);
    ArrayList<Inventory> findInventoryByBatchId(@Param("batch_id") String batchId);
    Inventory findInventoryByKey(@Param("goods_id") Integer goodsId, @Param("batch_id") String batchId,
                                  @Param("warehouse_id") Integer warehouseId, @Param("zone_id") Integer zoneId,
                                  @Param("location_id") Integer locationId);
}
