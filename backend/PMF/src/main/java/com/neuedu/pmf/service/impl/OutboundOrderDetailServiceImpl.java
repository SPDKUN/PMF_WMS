package com.neuedu.pmf.service.impl;

import com.neuedu.pmf.entity.OutboundOrderDetail;
import com.neuedu.pmf.mapper.OutboundOrderDetailMapper;
import com.neuedu.pmf.service.OutboundOrderDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class OutboundOrderDetailServiceImpl implements OutboundOrderDetailService {

    @Autowired
    private OutboundOrderDetailMapper outboundOrderDetailMapper;

    @Override
    public ArrayList<OutboundOrderDetail> list() {
        return outboundOrderDetailMapper.list();
    }

    @Override
    public OutboundOrderDetail getById(Integer id) {
        return outboundOrderDetailMapper.findById(id);
    }

    @Override
    public boolean delete(Integer id) {
        return outboundOrderDetailMapper.deleteById(id) > 0;
    }

    @Override
    public boolean save(OutboundOrderDetail outboundOrderDetail) {
        return outboundOrderDetailMapper.save(outboundOrderDetail) > 0;
    }

    @Override
    public boolean update(OutboundOrderDetail outboundOrderDetail) {
        return outboundOrderDetailMapper.update(outboundOrderDetail) > 0;
    }
}
