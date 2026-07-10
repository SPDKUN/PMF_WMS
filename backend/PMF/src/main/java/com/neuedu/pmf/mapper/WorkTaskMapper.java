package com.neuedu.pmf.mapper;

import com.neuedu.pmf.entity.WorkTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;

@Mapper
public interface WorkTaskMapper {
    ArrayList<WorkTask> list();
    int save(WorkTask workTask);
    WorkTask findById(@Param("task_id") Integer taskId);
    int update(WorkTask workTask);
    int deleteById(@Param("task_id") Integer taskId);
}
