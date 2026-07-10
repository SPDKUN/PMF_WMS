package com.neuedu.pmf.service;

import com.neuedu.pmf.entity.InboundOrderHead;
import java.util.ArrayList;

public interface InboundOrderHeadService {
    ArrayList<InboundOrderHead> list();

    InboundOrderHead getById(String id);

    boolean delete(String id);

    boolean save(InboundOrderHead inboundOrderHead);

    boolean update(InboundOrderHead inboundOrderHead);
}
