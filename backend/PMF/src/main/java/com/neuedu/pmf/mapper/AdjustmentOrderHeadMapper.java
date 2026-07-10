package com.neuedu.pmf.mapper;

import com.neuedu.pmf.entity.AdjustmentOrderHead;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;

@Mapper
public interface AdjustmentOrderHeadMapper {
    ArrayList<AdjustmentOrderHead> list();
    int save(AdjustmentOrderHead adjustmentOrderHead);
    AdjustmentOrderHead findById(@Param("adjustment_no") String adjustmentNo);
    int update(AdjustmentOrderHead adjustmentOrderHead);
    int deleteById(@Param("adjustment_no") String adjustmentNo);
}
