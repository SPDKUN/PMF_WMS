package com.neuedu.pmf.service;

import com.neuedu.pmf.entity.Zone;
import java.util.ArrayList;

public interface ZoneService {
    ArrayList<Zone> list();

    Zone getById(Integer id);

    boolean delete(Integer id);

    boolean save(Zone zone);

    boolean update(Zone zone);

    ArrayList<Zone> listByWarehouseId(Integer warehouseId);
}
