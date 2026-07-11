package com.neuedu.pmf.service.impl;

import com.neuedu.pmf.entity.Zone;
import com.neuedu.pmf.mapper.ZoneMapper;
import com.neuedu.pmf.service.ZoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class ZoneServiceImpl implements ZoneService {

    @Autowired
    private ZoneMapper zoneMapper;

    @Override
    public ArrayList<Zone> list() {
        return zoneMapper.list();
    }

    @Override
    public Zone getById(Integer id) {
        return zoneMapper.findById(id);
    }

    @Override
    public boolean delete(Integer id) {
        return zoneMapper.deleteById(id) > 0;
    }

    @Override
    public boolean save(Zone zone) {
        return zoneMapper.save(zone) > 0;
    }

    @Override
    public boolean update(Zone zone) {
        return zoneMapper.update(zone) > 0;
    }

    @Override
    public ArrayList<Zone> listByWarehouseId(Integer warehouseId) {
        return zoneMapper.findByWarehouseId(warehouseId);
    }
}
