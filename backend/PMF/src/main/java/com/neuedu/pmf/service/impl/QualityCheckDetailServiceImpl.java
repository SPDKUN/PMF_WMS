package com.neuedu.pmf.service.impl;

import com.neuedu.pmf.entity.QualityCheckDetail;
import com.neuedu.pmf.mapper.QualityCheckDetailMapper;
import com.neuedu.pmf.service.QualityCheckDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class QualityCheckDetailServiceImpl implements QualityCheckDetailService {

    @Autowired
    private QualityCheckDetailMapper qualityCheckDetailMapper;

    @Override
    public ArrayList<QualityCheckDetail> list() {
        return qualityCheckDetailMapper.list();
    }

    @Override
    public QualityCheckDetail getById(Integer id) {
        return qualityCheckDetailMapper.findById(id);
    }

    @Override
    public boolean delete(Integer id) {
        return qualityCheckDetailMapper.deleteById(id) > 0;
    }

    @Override
    public boolean save(QualityCheckDetail qualityCheckDetail) {
        return qualityCheckDetailMapper.save(qualityCheckDetail) > 0;
    }

    @Override
    public boolean update(QualityCheckDetail qualityCheckDetail) {
        return qualityCheckDetailMapper.update(qualityCheckDetail) > 0;
    }
}
