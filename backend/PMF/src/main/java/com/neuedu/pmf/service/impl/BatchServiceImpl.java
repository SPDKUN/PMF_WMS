package com.neuedu.pmf.service.impl;

import com.neuedu.pmf.entity.Batch;
import com.neuedu.pmf.mapper.BatchMapper;
import com.neuedu.pmf.service.BatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class BatchServiceImpl implements BatchService {

    @Autowired
    private BatchMapper batchMapper;

    @Override
    public ArrayList<Batch> list() {
        return batchMapper.list();
    }

    @Override
    public Batch getById(Integer id) {
        return batchMapper.findById(id);
    }

    @Override
    public boolean delete(Integer id) {
        return batchMapper.deleteById(id) > 0;
    }

    @Override
    public boolean save(Batch batch) {
        return batchMapper.save(batch) > 0;
    }

    @Override
    public boolean update(Batch batch) {
        return batchMapper.update(batch) > 0;
    }
}
