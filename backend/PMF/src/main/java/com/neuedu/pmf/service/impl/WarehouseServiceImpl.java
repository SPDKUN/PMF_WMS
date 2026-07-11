package com.neuedu.pmf.service.impl;

import com.neuedu.pmf.common.ResultCode;
import com.neuedu.pmf.entity.Location;
import com.neuedu.pmf.entity.Warehouse;
import com.neuedu.pmf.entity.Zone;
import com.neuedu.pmf.exception.BusinessException;
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
        Warehouse warehouse = warehouseMapper.findById(id);
        if (warehouse == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        // 检查是否有库位被占用
        if (!warehouse.getAvailable_slots().equals(warehouse.getTotal_slots())) {
            throw new BusinessException(ResultCode.FAILED.getCode(), "仓库非空，不能删除");
        }
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

        // 预计算库区和库位的ID
        java.util.Set<Integer> usedZoneIds = new java.util.HashSet<>(zoneMapper.findAllIds());
        java.util.Set<Integer> usedLocationIds = new java.util.HashSet<>(locationMapper.findAllIds());

        int totalLocations = zoneCount * 8;

        int[] zoneIds = new int[zoneCount];
        int nextZoneId = 1;
        for (int i = 0; i < zoneCount; i++) {
            while (usedZoneIds.contains(nextZoneId)) nextZoneId++;
            zoneIds[i] = nextZoneId;
            usedZoneIds.add(nextZoneId);
            nextZoneId++;
        }

        int[] locationIds = new int[totalLocations];
        int nextLocId = 1;
        for (int i = 0; i < totalLocations; i++) {
            while (usedLocationIds.contains(nextLocId)) nextLocId++;
            locationIds[i] = nextLocId;
            usedLocationIds.add(nextLocId);
            nextLocId++;
        }

        int locIdx = 0;
        for (int i = 0; i < zoneCount; i++) {
            Integer zoneId = zoneIds[i];
            Zone zone = new Zone();
            zone.setZone_id(zoneId);
            zone.setZone_name(warehouse.getWarehouse_name() + "-" + (i + 1));
            zone.setWarehouse_id(warehouseId);
            zone.setTotal_slots(8);
            zone.setAvailable_slots(8);
            zoneMapper.save(zone);

            // 级联创建库位（每个库区8个库位）
            for (int j = 1; j <= 8; j++) {
                Location location = new Location();
                location.setLocation_id(locationIds[locIdx++]);
                location.setLocation_name(zone.getZone_name() + "-" + j);
                location.setZone_id(zoneId);
                location.setIs_occupied(0);
                locationMapper.save(location);
            }
        }

        return true;
    }

    @Override
    @Transactional
    public boolean update(Warehouse warehouse) {
        String newName = warehouse.getWarehouse_name();
        if (newName != null && !newName.isEmpty()) {
            Warehouse old = warehouseMapper.findById(warehouse.getWarehouse_id());
            if (old != null && !newName.equals(old.getWarehouse_name())) {
                String oldName = old.getWarehouse_name();
                // 级联更新库区名称
                java.util.ArrayList<Zone> zones = zoneMapper.findByWarehouseId(warehouse.getWarehouse_id());
                for (Zone zone : zones) {
                    String oldZoneName = zone.getZone_name();
                    String newZoneName = oldZoneName.replace(oldName, newName);
                    zone.setZone_name(newZoneName);
                    zoneMapper.update(zone);

                    // 级联更新库位名称
                    java.util.ArrayList<Location> locations = locationMapper.findByZoneId(zone.getZone_id());
                    for (Location loc : locations) {
                        String oldLocName = loc.getLocation_name();
                        String newLocName = oldLocName.replace(oldName, newName);
                        loc.setLocation_name(newLocName);
                        locationMapper.update(loc);
                    }
                }
            }
        }
        return warehouseMapper.update(warehouse) > 0;
    }
}
