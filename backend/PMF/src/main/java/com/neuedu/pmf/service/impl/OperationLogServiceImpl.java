package com.neuedu.pmf.service.impl;

import com.neuedu.pmf.entity.OperationLog;
import com.neuedu.pmf.mapper.OperationLogMapper;
import com.neuedu.pmf.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class OperationLogServiceImpl implements OperationLogService {

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Override
    public ArrayList<OperationLog> list() {
        return operationLogMapper.list();
    }

    @Override
    public OperationLog getById(Integer id) {
        return operationLogMapper.findById(id);
    }

    @Override
    public boolean delete(Integer id) {
        return operationLogMapper.deleteById(id) > 0;
    }

    @Override
    public boolean save(OperationLog operationLog) {
        return operationLogMapper.save(operationLog) > 0;
    }

    @Override
    public boolean update(OperationLog operationLog) {
        return operationLogMapper.update(operationLog) > 0;
    }
}
