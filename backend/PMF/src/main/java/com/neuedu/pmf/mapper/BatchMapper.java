package com.neuedu.pmf.mapper;

import com.neuedu.pmf.entity.Batch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;

@Mapper
public interface BatchMapper {
    ArrayList<Batch> list();
    int save(Batch batch);
    Batch findById(@Param("batch_id") String batchId);
    int update(Batch batch);
    int deleteById(@Param("batch_id") String batchId);
    java.util.List<String> findTodayBatchIds(@Param("prefix") String prefix);
}
