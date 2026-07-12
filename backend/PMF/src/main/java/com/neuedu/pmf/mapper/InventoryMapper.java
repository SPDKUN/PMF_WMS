package com.neuedu.pmf.mapper;

import com.neuedu.pmf.entity.Inventory;
import com.neuedu.pmf.vo.InventoryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;

@Mapper
public interface InventoryMapper {
    ArrayList<Inventory> list();
    ArrayList<InventoryVO> listWithDetails(@Param("goodsName") String goodsName, @Param("locationId") Integer locationId);
    int save(Inventory inventory);
    Inventory findById(@Param("inventory_id") Integer inventoryId);
    int update(Inventory inventory);
    int deleteById(@Param("inventory_id") Integer inventoryId);
}
