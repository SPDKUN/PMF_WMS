package com.neuedu.pmf.service.impl;

import com.neuedu.pmf.entity.DefectiveHandlingDetail;
import com.neuedu.pmf.mapper.DefectiveHandlingDetailMapper;
import com.neuedu.pmf.service.DefectiveHandlingDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class DefectiveHandlingDetailServiceImpl implements DefectiveHandlingDetailService {

    @Autowired
    private DefectiveHandlingDetailMapper defectiveHandlingDetailMapper;

    @Override
    public ArrayList<DefectiveHandlingDetail> list() {
        return defectiveHandlingDetailMapper.list();
    }

    @Override
    public DefectiveHandlingDetail getById(Integer id) {
        return defectiveHandlingDetailMapper.findById(id);
    }

    @Override
    public boolean delete(Integer id) {
        return defectiveHandlingDetailMapper.deleteById(id) > 0;
    }

    @Override
    public boolean save(DefectiveHandlingDetail defectiveHandlingDetail) {
        return defectiveHandlingDetailMapper.save(defectiveHandlingDetail) > 0;
    }

    @Override
    public boolean update(DefectiveHandlingDetail defectiveHandlingDetail) {
        return defectiveHandlingDetailMapper.update(defectiveHandlingDetail) > 0;
    }
}
