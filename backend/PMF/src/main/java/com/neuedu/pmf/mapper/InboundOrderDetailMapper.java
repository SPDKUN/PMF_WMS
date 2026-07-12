package com.neuedu.pmf.mapper;

import com.neuedu.pmf.entity.InboundOrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;

@Mapper
public interface InboundOrderDetailMapper {
    ArrayList<InboundOrderDetail> list();
    int save(InboundOrderDetail inboundOrderDetail);
    InboundOrderDetail findById(@Param("detail_id") Integer detailId);
    ArrayList<InboundOrderDetail> findByInboundNo(@Param("inbound_no") String inboundNo);
    int update(InboundOrderDetail inboundOrderDetail);
    int deleteById(@Param("detail_id") Integer detailId);
    int deleteByInboundNo(@Param("inbound_no") String inboundNo);
}
