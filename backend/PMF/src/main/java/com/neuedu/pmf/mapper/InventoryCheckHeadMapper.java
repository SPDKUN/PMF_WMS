package com.neuedu.pmf.mapper;

import com.neuedu.pmf.entity.InventoryCheckHead;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;

@Mapper
public interface InventoryCheckHeadMapper {
    ArrayList<InventoryCheckHead> list();
    int save(InventoryCheckHead inventoryCheckHead);
    InventoryCheckHead findById(@Param("check_no") String checkNo);
    int update(InventoryCheckHead inventoryCheckHead);
    int deleteById(@Param("check_no") String checkNo);
}
