package com.neuedu.pmf.mapper;

import com.neuedu.pmf.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;

@Mapper
public interface OperationLogMapper {
    ArrayList<OperationLog> list();
    ArrayList<OperationLog> listWithFilter(@Param("date") String date,
                                           @Param("operationType") String operationType);
    int save(OperationLog operationLog);
    OperationLog findById(@Param("log_id") Integer logId);
    int update(OperationLog operationLog);
    int deleteById(@Param("log_id") Integer logId);
}
