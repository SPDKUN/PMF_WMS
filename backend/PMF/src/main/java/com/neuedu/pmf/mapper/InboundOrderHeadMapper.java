package com.neuedu.pmf.mapper;

import com.neuedu.pmf.entity.InboundOrderHead;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;

@Mapper
public interface InboundOrderHeadMapper {
    ArrayList<InboundOrderHead> list();
    int save(InboundOrderHead inboundOrderHead);
    InboundOrderHead findById(@Param("inbound_no") String inboundNo);
    int update(InboundOrderHead inboundOrderHead);
    int deleteById(@Param("inbound_no") String inboundNo);
}
