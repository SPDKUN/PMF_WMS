package com.neuedu.pmf.mapper;

import com.neuedu.pmf.entity.OutboundOrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;

@Mapper
public interface OutboundOrderDetailMapper {
    ArrayList<OutboundOrderDetail> list();
    int save(OutboundOrderDetail outboundOrderDetail);
    OutboundOrderDetail findById(@Param("detail_id") Integer detailId);
    int update(OutboundOrderDetail outboundOrderDetail);
    int deleteById(@Param("detail_id") Integer detailId);
}
