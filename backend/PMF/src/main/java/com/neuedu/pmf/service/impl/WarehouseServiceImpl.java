package com.neuedu.pmf.service.impl;

import com.neuedu.pmf.entity.Warehouse;
import com.neuedu.pmf.mapper.WarehouseMapper;
import com.neuedu.pmf.service.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class WarehouseServiceImpl implements WarehouseService {

    @Autowired
    private WarehouseMapper warehouseMapper;

    @Override
    public ArrayList<Warehouse> list() {
        return warehouseMapper.list();
    }

    @Override
    public Warehouse getById(Integer id) {
        return warehouseMapper.findById(id);
    }

    @Override
    public boolean delete(Integer id) {
        return warehouseMapper.deleteById(id) > 0;
    }

    @Override
    public boolean save(Warehouse warehouse) {
        return warehouseMapper.save(warehouse) > 0;
    }

    @Override
    public boolean update(Warehouse warehouse) {
        return warehouseMapper.update(warehouse) > 0;
    }
}
