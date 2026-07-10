package com.neuedu.pmf.service.impl;

import com.neuedu.pmf.entity.DefectiveHandlingHead;
import com.neuedu.pmf.mapper.DefectiveHandlingHeadMapper;
import com.neuedu.pmf.service.DefectiveHandlingHeadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class DefectiveHandlingHeadServiceImpl implements DefectiveHandlingHeadService {

    @Autowired
    private DefectiveHandlingHeadMapper defectiveHandlingHeadMapper;

    @Override
    public ArrayList<DefectiveHandlingHead> list() {
        return defectiveHandlingHeadMapper.list();
    }

    @Override
    public DefectiveHandlingHead getById(String id) {
        return defectiveHandlingHeadMapper.findById(id);
    }

    @Override
    public boolean delete(String id) {
        return defectiveHandlingHeadMapper.deleteById(id) > 0;
    }

    @Override
    public boolean save(DefectiveHandlingHead defectiveHandlingHead) {
        return defectiveHandlingHeadMapper.save(defectiveHandlingHead) > 0;
    }

    @Override
    public boolean update(DefectiveHandlingHead defectiveHandlingHead) {
        return defectiveHandlingHeadMapper.update(defectiveHandlingHead) > 0;
    }
}
