package com.neuedu.pmf.service.impl;

import com.neuedu.pmf.entity.InventoryCheckHead;
import com.neuedu.pmf.mapper.InventoryCheckHeadMapper;
import com.neuedu.pmf.service.InventoryCheckHeadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class InventoryCheckHeadServiceImpl implements InventoryCheckHeadService {

    @Autowired
    private InventoryCheckHeadMapper inventoryCheckHeadMapper;

    @Override
    public ArrayList<InventoryCheckHead> list() {
        return inventoryCheckHeadMapper.list();
    }

    @Override
    public InventoryCheckHead getById(String id) {
        return inventoryCheckHeadMapper.findById(id);
    }

    @Override
    public boolean delete(String id) {
        return inventoryCheckHeadMapper.deleteById(id) > 0;
    }

    @Override
    public boolean save(InventoryCheckHead inventoryCheckHead) {
        return inventoryCheckHeadMapper.save(inventoryCheckHead) > 0;
    }

    @Override
    public boolean update(InventoryCheckHead inventoryCheckHead) {
        return inventoryCheckHeadMapper.update(inventoryCheckHead) > 0;
    }
}
