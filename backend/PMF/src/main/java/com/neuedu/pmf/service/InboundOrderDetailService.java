package com.neuedu.pmf.service;

import com.neuedu.pmf.entity.InboundOrderDetail;
import java.util.ArrayList;

public interface InboundOrderDetailService {
    ArrayList<InboundOrderDetail> list();

    InboundOrderDetail getById(Integer id);

    boolean delete(Integer id);

    boolean save(InboundOrderDetail inboundOrderDetail);

    boolean update(InboundOrderDetail inboundOrderDetail);
}
