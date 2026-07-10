package com.neuedu.pmf.service;

import com.neuedu.pmf.entity.OutboundOrderDetail;
import java.util.ArrayList;

public interface OutboundOrderDetailService {
    ArrayList<OutboundOrderDetail> list();

    OutboundOrderDetail getById(Integer id);

    boolean delete(Integer id);

    boolean save(OutboundOrderDetail outboundOrderDetail);

    boolean update(OutboundOrderDetail outboundOrderDetail);
}
