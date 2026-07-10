package com.neuedu.pmf.service.impl;

import com.neuedu.pmf.entity.AdjustmentOrderHead;
import com.neuedu.pmf.mapper.AdjustmentOrderHeadMapper;
import com.neuedu.pmf.service.AdjustmentOrderHeadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class AdjustmentOrderHeadServiceImpl implements AdjustmentOrderHeadService {

    @Autowired
    private AdjustmentOrderHeadMapper adjustmentOrderHeadMapper;

    @Override
    public ArrayList<AdjustmentOrderHead> list() {
        return adjustmentOrderHeadMapper.list();
    }

    @Override
    public AdjustmentOrderHead getById(String id) {
        return adjustmentOrderHeadMapper.findById(id);
    }

    @Override
    public boolean delete(String id) {
        return adjustmentOrderHeadMapper.deleteById(id) > 0;
    }

    @Override
    public boolean save(AdjustmentOrderHead adjustmentOrderHead) {
        return adjustmentOrderHeadMapper.save(adjustmentOrderHead) > 0;
    }

    @Override
    public boolean update(AdjustmentOrderHead adjustmentOrderHead) {
        return adjustmentOrderHeadMapper.update(adjustmentOrderHead) > 0;
    }
}
