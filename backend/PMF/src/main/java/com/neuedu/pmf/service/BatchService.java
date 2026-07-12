package com.neuedu.pmf.service;

import com.neuedu.pmf.entity.Batch;
import java.util.ArrayList;

public interface BatchService {
    ArrayList<Batch> list();

    Batch getById(String id);

    boolean delete(String id);

    boolean save(Batch batch);

    boolean update(Batch batch);

    ArrayList<Batch> listByStatus(String batchStatus);
}
