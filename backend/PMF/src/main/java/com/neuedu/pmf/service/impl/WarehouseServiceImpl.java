package com.neuedu.pmf.service.impl;

import com.neuedu.pmf.entity.Location;
import com.neuedu.pmf.entity.Warehouse;
import com.neuedu.pmf.entity.Zone;
import com.neuedu.pmf.mapper.LocationMapper;
import com.neuedu.pmf.mapper.WarehouseMapper;
import com.neuedu.pmf.mapper.ZoneMapper;
import com.neuedu.pmf.service.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
public class WarehouseServiceImpl implements WarehouseService {

    @Autowired
    private WarehouseMapper warehouseMapper;

    @Autowired
    private ZoneMapper zoneMapper;

    @Autowired
    private LocationMapper locationMapper;

    @Override
    public ArrayList<Warehouse> list() {
        return warehouseMapper.list();
    }

    @Override
    public Warehouse getById(Integer id) {
        return warehouseMapper.findById(id);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) {
        // 级联删除：先删库位，再删库区，最后删仓库
        java.util.ArrayList<Zone> zones = zoneMapper.findByWarehouseId(id);
        for (Zone zone : zones) {
            locationMapper.deleteByZoneId(zone.getZone_id());
        }
        zoneMapper.deleteByWarehouseId(id);
        return warehouseMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional
    public boolean save(Warehouse warehouse) {
        // 根据仓库大小设置总库位数和可用库位数
        int zoneCount, totalSlots;
        switch (warehouse.getWarehouse_size()) {
            case "大":
                zoneCount = 16;
                totalSlots = 128;
                break;
            case "中":
                zoneCount = 8;
                totalSlots = 64;
                break;
            case "小":
                zoneCount = 4;
                totalSlots = 32;
                break;
            default:
                zoneCount = 4;
                totalSlots = 32;
        }

        warehouse.setTotal_slots(totalSlots);
        warehouse.setAvailable_slots(totalSlots);
        warehouse.setStatus("启用");

        // 获取当前所有ID中最小的未用正数作为新ID
        java.util.List<Integer> existingIds = warehouseMapper.findAllIds();
        int newId = 1;
        for (int id : existingIds) {
            if (id == newId) {
                newId++;
            } else if (id > newId) {
                break;
            }
        }
        warehouse.setWarehouse_id(newId);

        // 插入仓库（显式指定ID）
        warehouseMapper.save(warehouse);
        Integer warehouseId = newId;

        // 级联创建库区
        for (int i = 1; i <= zoneCount; i++) {
            Zone zone = new Zone();
            zone.setZone_name(warehouse.getWarehouse_name() + "-" + i);
            zone.setWarehouse_id(warehouseId);
            zone.setTotal_slots(8);
            zone.setAvailable_slots(8);
            zoneMapper.save(zone);
            Integer zoneId = zone.getZone_id();

            // 级联创建库位（每个库区8个库位）
            for (int j = 1; j <= 8; j++) {
                Location location = new Location();
                location.setLocation_name(zone.getZone_name() + "-" + j);
                location.setZone_id(zoneId);
                location.setIs_occupied(0);
                locationMapper.save(location);
            }
        }

        return true;
    }

    @Override
    public boolean update(Warehouse warehouse) {
        return warehouseMapper.update(warehouse) > 0;
    }
}
