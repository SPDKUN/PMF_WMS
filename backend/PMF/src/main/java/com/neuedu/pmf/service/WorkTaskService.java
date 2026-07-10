package com.neuedu.pmf.service;

import com.neuedu.pmf.entity.WorkTask;
import java.util.ArrayList;

public interface WorkTaskService {
    ArrayList<WorkTask> list();

    WorkTask getById(Integer id);

    boolean delete(Integer id);

    boolean save(WorkTask workTask);

    boolean update(WorkTask workTask);
}
