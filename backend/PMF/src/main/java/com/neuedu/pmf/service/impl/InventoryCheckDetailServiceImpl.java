package com.neuedu.pmf.service.impl;

import com.neuedu.pmf.entity.InventoryCheckDetail;
import com.neuedu.pmf.mapper.InventoryCheckDetailMapper;
import com.neuedu.pmf.service.InventoryCheckDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class InventoryCheckDetailServiceImpl implements InventoryCheckDetailService {

    @Autowired
    private InventoryCheckDetailMapper inventoryCheckDetailMapper;

    @Override
    public ArrayList<InventoryCheckDetail> list() {
        return inventoryCheckDetailMapper.list();
    }

    @Override
    public InventoryCheckDetail getById(Integer id) {
        return inventoryCheckDetailMapper.findById(id);
    }

    @Override
    public boolean delete(Integer id) {
        return inventoryCheckDetailMapper.deleteById(id) > 0;
    }

    @Override
    public boolean save(InventoryCheckDetail inventoryCheckDetail) {
        return inventoryCheckDetailMapper.save(inventoryCheckDetail) > 0;
    }

    @Override
    public boolean update(InventoryCheckDetail inventoryCheckDetail) {
        return inventoryCheckDetailMapper.update(inventoryCheckDetail) > 0;
    }
}
