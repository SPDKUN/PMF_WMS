package com.neuedu.pmf.mapper;

import com.neuedu.pmf.entity.AdjustmentOrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;

@Mapper
public interface AdjustmentOrderDetailMapper {
    ArrayList<AdjustmentOrderDetail> list();
    int save(AdjustmentOrderDetail adjustmentOrderDetail);
    AdjustmentOrderDetail findById(@Param("detail_id") Integer detailId);
    int update(AdjustmentOrderDetail adjustmentOrderDetail);
    int deleteById(@Param("detail_id") Integer detailId);
}
