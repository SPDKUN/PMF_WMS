package com.neuedu.pmf.mapper;

import com.neuedu.pmf.entity.OutboundOrderHead;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;

@Mapper
public interface OutboundOrderHeadMapper {
    ArrayList<OutboundOrderHead> list();
    int save(OutboundOrderHead outboundOrderHead);
    OutboundOrderHead findById(@Param("outbound_no") String outboundNo);
    int update(OutboundOrderHead outboundOrderHead);
    int deleteById(@Param("outbound_no") String outboundNo);
    java.util.List<String> findTodayOutboundNos(@Param("prefix") String prefix);
}
