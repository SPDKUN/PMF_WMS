package com.neuedu.pmf.service.impl;

import com.neuedu.pmf.entity.InboundOrderHead;
import com.neuedu.pmf.mapper.InboundOrderHeadMapper;
import com.neuedu.pmf.service.InboundOrderHeadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class InboundOrderHeadServiceImpl implements InboundOrderHeadService {

    @Autowired
    private InboundOrderHeadMapper inboundOrderHeadMapper;

    @Override
    public ArrayList<InboundOrderHead> list() {
        return inboundOrderHeadMapper.list();
    }

    @Override
    public InboundOrderHead getById(String id) {
        return inboundOrderHeadMapper.findById(id);
    }

    @Override
    public boolean delete(String id) {
        return inboundOrderHeadMapper.deleteById(id) > 0;
    }

    @Override
    public boolean save(InboundOrderHead inboundOrderHead) {
        return inboundOrderHeadMapper.save(inboundOrderHead) > 0;
    }

    @Override
    public boolean update(InboundOrderHead inboundOrderHead) {
        return inboundOrderHeadMapper.update(inboundOrderHead) > 0;
    }
}
