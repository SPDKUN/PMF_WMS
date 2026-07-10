package com.neuedu.pmf.service.impl;

import com.neuedu.pmf.entity.AdjustmentOrderDetail;
import com.neuedu.pmf.mapper.AdjustmentOrderDetailMapper;
import com.neuedu.pmf.service.AdjustmentOrderDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class AdjustmentOrderDetailServiceImpl implements AdjustmentOrderDetailService {

    @Autowired
    private AdjustmentOrderDetailMapper adjustmentOrderDetailMapper;

    @Override
    public ArrayList<AdjustmentOrderDetail> list() {
        return adjustmentOrderDetailMapper.list();
    }

    @Override
    public AdjustmentOrderDetail getById(Integer id) {
        return adjustmentOrderDetailMapper.findById(id);
    }

    @Override
    public boolean delete(Integer id) {
        return adjustmentOrderDetailMapper.deleteById(id) > 0;
    }

    @Override
    public boolean save(AdjustmentOrderDetail adjustmentOrderDetail) {
        return adjustmentOrderDetailMapper.save(adjustmentOrderDetail) > 0;
    }

    @Override
    public boolean update(AdjustmentOrderDetail adjustmentOrderDetail) {
        return adjustmentOrderDetailMapper.update(adjustmentOrderDetail) > 0;
    }
}
