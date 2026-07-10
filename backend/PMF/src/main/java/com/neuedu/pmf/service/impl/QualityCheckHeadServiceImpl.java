package com.neuedu.pmf.service.impl;

import com.neuedu.pmf.entity.QualityCheckHead;
import com.neuedu.pmf.mapper.QualityCheckHeadMapper;
import com.neuedu.pmf.service.QualityCheckHeadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class QualityCheckHeadServiceImpl implements QualityCheckHeadService {

    @Autowired
    private QualityCheckHeadMapper qualityCheckHeadMapper;

    @Override
    public ArrayList<QualityCheckHead> list() {
        return qualityCheckHeadMapper.list();
    }

    @Override
    public QualityCheckHead getById(String id) {
        return qualityCheckHeadMapper.findById(id);
    }

    @Override
    public boolean delete(String id) {
        return qualityCheckHeadMapper.deleteById(id) > 0;
    }

    @Override
    public boolean save(QualityCheckHead qualityCheckHead) {
        return qualityCheckHeadMapper.save(qualityCheckHead) > 0;
    }

    @Override
    public boolean update(QualityCheckHead qualityCheckHead) {
        return qualityCheckHeadMapper.update(qualityCheckHead) > 0;
    }
}
