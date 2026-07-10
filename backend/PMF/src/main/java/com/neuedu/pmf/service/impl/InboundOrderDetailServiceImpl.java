package com.neuedu.pmf.service.impl;

import com.neuedu.pmf.entity.InboundOrderDetail;
import com.neuedu.pmf.mapper.InboundOrderDetailMapper;
import com.neuedu.pmf.service.InboundOrderDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class InboundOrderDetailServiceImpl implements InboundOrderDetailService {

    @Autowired
    private InboundOrderDetailMapper inboundOrderDetailMapper;

    @Override
    public ArrayList<InboundOrderDetail> list() {
        return inboundOrderDetailMapper.list();
    }

    @Override
    public InboundOrderDetail getById(Integer id) {
        return inboundOrderDetailMapper.findById(id);
    }

    @Override
    public boolean delete(Integer id) {
        return inboundOrderDetailMapper.deleteById(id) > 0;
    }

    @Override
    public boolean save(InboundOrderDetail inboundOrderDetail) {
        return inboundOrderDetailMapper.save(inboundOrderDetail) > 0;
    }

    @Override
    public boolean update(InboundOrderDetail inboundOrderDetail) {
        return inboundOrderDetailMapper.update(inboundOrderDetail) > 0;
    }
}
