package com.neuedu.pmf.service;

import com.neuedu.pmf.entity.Inventory;
import java.util.ArrayList;

public interface InventoryService {
    ArrayList<Inventory> list();

    Inventory getById(Integer id);

    boolean delete(Integer id);

    boolean save(Inventory inventory);

    boolean update(Inventory inventory);
}
