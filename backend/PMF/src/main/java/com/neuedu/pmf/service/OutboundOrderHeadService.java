package com.neuedu.pmf.service;

import com.neuedu.pmf.entity.OutboundOrderHead;
import java.util.ArrayList;

public interface OutboundOrderHeadService {
    ArrayList<OutboundOrderHead> list();

    OutboundOrderHead getById(String id);

    boolean delete(String id);

    boolean save(OutboundOrderHead outboundOrderHead);

    boolean update(OutboundOrderHead outboundOrderHead);
}
