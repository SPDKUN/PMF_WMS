package com.neuedu.pmf.service;

import com.neuedu.pmf.entity.Warehouse;
import java.util.ArrayList;

public interface WarehouseService {
    ArrayList<Warehouse> list();

    Warehouse getById(Integer id);

    boolean delete(Integer id);

    boolean save(Warehouse warehouse);

    boolean update(Warehouse warehouse);
}
