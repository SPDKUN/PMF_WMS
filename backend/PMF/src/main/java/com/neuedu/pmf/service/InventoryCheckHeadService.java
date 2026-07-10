package com.neuedu.pmf.service;

import com.neuedu.pmf.entity.InventoryCheckHead;
import java.util.ArrayList;

public interface InventoryCheckHeadService {
    ArrayList<InventoryCheckHead> list();

    InventoryCheckHead getById(String id);

    boolean delete(String id);

    boolean save(InventoryCheckHead inventoryCheckHead);

    boolean update(InventoryCheckHead inventoryCheckHead);
}
