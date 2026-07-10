package com.neuedu.pmf.mapper;

import com.neuedu.pmf.entity.InventoryCheckDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;

@Mapper
public interface InventoryCheckDetailMapper {
    ArrayList<InventoryCheckDetail> list();
    int save(InventoryCheckDetail inventoryCheckDetail);
    InventoryCheckDetail findById(@Param("detail_id") Integer detailId);
    int update(InventoryCheckDetail inventoryCheckDetail);
    int deleteById(@Param("detail_id") Integer detailId);
}
