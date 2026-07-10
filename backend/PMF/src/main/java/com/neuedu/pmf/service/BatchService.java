package com.neuedu.pmf.service;

import com.neuedu.pmf.entity.Batch;
import java.util.ArrayList;

public interface BatchService {
    ArrayList<Batch> list();

    Batch getById(Integer id);

    boolean delete(Integer id);

    boolean save(Batch batch);

    boolean update(Batch batch);
}
