package com.neuedu.pmf.service.impl;

import com.neuedu.pmf.entity.WorkTask;
import com.neuedu.pmf.mapper.WorkTaskMapper;
import com.neuedu.pmf.service.WorkTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class WorkTaskServiceImpl implements WorkTaskService {

    @Autowired
    private WorkTaskMapper workTaskMapper;

    @Override
    public ArrayList<WorkTask> list() {
        return workTaskMapper.list();
    }

    @Override
    public WorkTask getById(Integer id) {
        return workTaskMapper.findById(id);
    }

    @Override
    public boolean delete(Integer id) {
        return workTaskMapper.deleteById(id) > 0;
    }

    @Override
    public boolean save(WorkTask workTask) {
        return workTaskMapper.save(workTask) > 0;
    }

    @Override
    public boolean update(WorkTask workTask) {
        return workTaskMapper.update(workTask) > 0;
    }

    @Override
    public ArrayList<WorkTask> listByAssigneeId(Integer assigneeId) {
        return workTaskMapper.findByAssigneeId(assigneeId);
    }
}
