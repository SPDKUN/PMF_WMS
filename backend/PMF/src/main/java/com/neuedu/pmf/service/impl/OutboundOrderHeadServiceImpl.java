package com.neuedu.pmf.service.impl;

import com.neuedu.pmf.entity.OutboundOrderHead;
import com.neuedu.pmf.mapper.OutboundOrderHeadMapper;
import com.neuedu.pmf.service.OutboundOrderHeadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class OutboundOrderHeadServiceImpl implements OutboundOrderHeadService {

    @Autowired
    private OutboundOrderHeadMapper outboundOrderHeadMapper;

    @Override
    public ArrayList<OutboundOrderHead> list() {
        return outboundOrderHeadMapper.list();
    }

    @Override
    public OutboundOrderHead getById(String id) {
        return outboundOrderHeadMapper.findById(id);
    }

    @Override
    public boolean delete(String id) {
        return outboundOrderHeadMapper.deleteById(id) > 0;
    }

    @Override
    public boolean save(OutboundOrderHead outboundOrderHead) {
        return outboundOrderHeadMapper.save(outboundOrderHead) > 0;
    }

    @Override
    public boolean update(OutboundOrderHead outboundOrderHead) {
        return outboundOrderHeadMapper.update(outboundOrderHead) > 0;
    }
}
