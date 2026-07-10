package com.neuedu.pmf.service;

import com.neuedu.pmf.entity.InventoryCheckDetail;
import java.util.ArrayList;

public interface InventoryCheckDetailService {
    ArrayList<InventoryCheckDetail> list();

    InventoryCheckDetail getById(Integer id);

    boolean delete(Integer id);

    boolean save(InventoryCheckDetail inventoryCheckDetail);

    boolean update(InventoryCheckDetail inventoryCheckDetail);
}
