package com.neuedu.pmf.service;

import com.neuedu.pmf.entity.Inventory;
import com.neuedu.pmf.vo.InventoryVO;

import java.util.ArrayList;

public interface InventoryService {
    ArrayList<Inventory> list();

    ArrayList<InventoryVO> listWithDetails(String batchId);

    Inventory getById(Integer id);

    boolean delete(Integer id);

    boolean save(Inventory inventory);

    boolean update(Inventory inventory);
}
