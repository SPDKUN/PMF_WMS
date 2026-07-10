package com.neuedu.pmf.service;

import com.neuedu.pmf.entity.OperationLog;
import java.util.ArrayList;

public interface OperationLogService {
    ArrayList<OperationLog> list();

    OperationLog getById(Integer id);

    boolean delete(Integer id);

    boolean save(OperationLog operationLog);

    boolean update(OperationLog operationLog);
}
