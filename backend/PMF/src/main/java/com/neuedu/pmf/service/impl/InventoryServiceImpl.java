package com.neuedu.pmf.service.impl;

import com.neuedu.pmf.entity.Inventory;
import com.neuedu.pmf.mapper.InventoryMapper;
import com.neuedu.pmf.service.InventoryService;
import com.neuedu.pmf.vo.InventoryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class InventoryServiceImpl implements InventoryService {

    @Autowired
    private InventoryMapper inventoryMapper;

    @Override
    public ArrayList<Inventory> list() {
        return inventoryMapper.list();
    }

    @Override
    public ArrayList<InventoryVO> listWithDetails(String batchId) {
        return inventoryMapper.listWithDetails(batchId);
    }

    @Override
    public Inventory getById(Integer id) {
        return inventoryMapper.findById(id);
    }

    @Override
    public boolean delete(Integer id) {
        return inventoryMapper.deleteById(id) > 0;
    }

    @Override
    public boolean save(Inventory inventory) {
        return inventoryMapper.save(inventory) > 0;
    }

    @Override
    public boolean update(Inventory inventory) {
        return inventoryMapper.update(inventory) > 0;
    }
}
